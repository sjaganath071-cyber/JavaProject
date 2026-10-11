
-- Additive migration from the reported legacy VEHICLES shape to the
-- resource-service Vehicle entity mapping. No columns or rows are dropped.
-- Run as the owner of VEHICLES, after reviewing a backup and the table DDL.
-- This script may be run again after CAPACITY values are supplied.

SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

DECLARE
    v_count NUMBER;
    v_nulls NUMBER;
    v_duplicates NUMBER;
    v_max_id NUMBER;
    v_columns_missing VARCHAR2(2000);
    v_constraint_name VARCHAR2(30);

    FUNCTION available_object_name(p_prefix IN VARCHAR2) RETURN VARCHAR2 IS
        v_name VARCHAR2(30);
        v_name_count NUMBER;
    BEGIN
        LOOP
            v_name := SUBSTR(p_prefix, 1, 20) || '_' ||
                      SUBSTR(RAWTOHEX(SYS_GUID()), 1, 8);
            SELECT
                (SELECT COUNT(*) FROM USER_CONSTRAINTS WHERE CONSTRAINT_NAME = v_name) +
                (SELECT COUNT(*) FROM USER_INDEXES WHERE INDEX_NAME = v_name)
            INTO v_name_count
            FROM DUAL;
            EXIT WHEN v_name_count = 0;
        END LOOP;
        RETURN v_name;
    END available_object_name;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM USER_TABLES
    WHERE TABLE_NAME = 'VEHICLES';

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20010,
            'VEHICLES does not exist in the connected schema; migration stopped.'
        );
    END IF;

    -- Require the legacy columns reported by the operator before any changes.
    FOR c IN (
        SELECT 'VEHICLE_ID' column_name FROM DUAL UNION ALL
        SELECT 'REGISTRATION_NUMBER' FROM DUAL UNION ALL
        SELECT 'VEHICLE_NAME' FROM DUAL UNION ALL
        SELECT 'VEHICLE_TYPE' FROM DUAL UNION ALL
        SELECT 'UNIT_ID' FROM DUAL UNION ALL
        SELECT 'LOCATION' FROM DUAL UNION ALL
        SELECT 'STATUS' FROM DUAL
    ) LOOP
        SELECT COUNT(*) INTO v_count
        FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = 'VEHICLES'
          AND COLUMN_NAME = c.column_name;
        IF v_count = 0 THEN
            v_columns_missing := v_columns_missing || ' ' || c.column_name;
        END IF;
    END LOOP;

    IF v_columns_missing IS NOT NULL THEN
        RAISE_APPLICATION_ERROR(
            -20011,
            'Unexpected VEHICLES structure; required legacy columns missing:' ||
            v_columns_missing || '. No migration performed.'
        );
    END IF;

    -- Check compatibility before copying into the target entity field lengths.
    EXECUTE IMMEDIATE
        'SELECT COUNT(*) FROM VEHICLES ' ||
        'WHERE REGISTRATION_NUMBER IS NULL OR LENGTH(REGISTRATION_NUMBER) > 30'
        INTO v_count;
    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20012,
            'Registration numbers are NULL or exceed 30 characters. Review/fix those values before migration.'
        );
    END IF;

    EXECUTE IMMEDIATE
        'SELECT COUNT(*) FROM VEHICLES ' ||
        'WHERE LOCATION IS NOT NULL AND LENGTH(LOCATION) > 50'
        INTO v_count;
    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20013,
            'One or more LOCATION values exceed LOCATION_CODE length 50. Review before migration.'
        );
    END IF;

    EXECUTE IMMEDIATE
        'SELECT COUNT(*) FROM VEHICLES ' ||
        'WHERE VEHICLE_TYPE IS NULL OR LENGTH(VEHICLE_TYPE) > 50 ' ||
        'OR STATUS IS NULL OR LENGTH(STATUS) > 30'
        INTO v_count;
    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20018,
            'VEHICLE_TYPE or STATUS has NULL/overlength values; review before migration.'
        );
    END IF;

    -- Add entity-mapped columns as nullable so existing rows are preserved and
    -- can be backfilled before NOT NULL constraints are applied.
    FOR c IN (
        SELECT 'VEHICLE_NUMBER' column_name, 'VARCHAR2(30)' column_ddl FROM DUAL UNION ALL
        SELECT 'CAPACITY', 'NUMBER(10)' FROM DUAL UNION ALL
        SELECT 'LOCATION_CODE', 'VARCHAR2(50)' FROM DUAL UNION ALL
        SELECT 'CONDITION_STATUS', 'VARCHAR2(30)' FROM DUAL UNION ALL
        SELECT 'CREATED_AT', 'TIMESTAMP' FROM DUAL UNION ALL
        SELECT 'UPDATED_AT', 'TIMESTAMP' FROM DUAL
    ) LOOP
        SELECT COUNT(*) INTO v_count
        FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = 'VEHICLES'
          AND COLUMN_NAME = c.column_name;

        IF v_count = 0 THEN
            EXECUTE IMMEDIATE
                'ALTER TABLE VEHICLES ADD (' || c.column_name || ' ' || c.column_ddl || ')';
            DBMS_OUTPUT.PUT_LINE('Added VEHICLES.' || c.column_name);
        END IF;
    END LOOP;

    -- Preserve source columns; copy only direct semantic equivalents.
    EXECUTE IMMEDIATE
        'UPDATE VEHICLES SET VEHICLE_NUMBER = REGISTRATION_NUMBER ' ||
        'WHERE VEHICLE_NUMBER IS NULL';
    EXECUTE IMMEDIATE
        'UPDATE VEHICLES SET LOCATION_CODE = LOCATION ' ||
        'WHERE LOCATION_CODE IS NULL';
    EXECUTE IMMEDIATE
        'UPDATE VEHICLES SET CONDITION_STATUS = ''UNKNOWN'' ' ||
        'WHERE CONDITION_STATUS IS NULL';
    EXECUTE IMMEDIATE
        'UPDATE VEHICLES SET CREATED_AT = LOCALTIMESTAMP ' ||
        'WHERE CREATED_AT IS NULL';
    EXECUTE IMMEDIATE
        'UPDATE VEHICLES SET UPDATED_AT = LOCALTIMESTAMP ' ||
        'WHERE UPDATED_AT IS NULL';
    COMMIT;

    -- CAPACITY has no legacy source column. Never invent capacities.
    EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM VEHICLES WHERE CAPACITY IS NULL'
        INTO v_nulls;
    IF v_nulls > 0 THEN
        DBMS_OUTPUT.PUT_LINE(
            'Migration backfill committed without deleting data. ' || v_nulls ||
            ' existing row(s) still need authoritative CAPACITY values.'
        );
        DBMS_OUTPUT.PUT_LINE(
            'Update CAPACITY using verified vehicle data, then rerun this migration.'
        );
        RAISE_APPLICATION_ERROR(
            -20014,
            'CAPACITY is required by the resource entity; supply verified values and rerun.'
        );
    END IF;

    EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM VEHICLES WHERE VEHICLE_ID IS NULL'
        INTO v_nulls;
    IF v_nulls > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20019,
            'VEHICLE_ID contains NULL values; cannot enforce the primary key.'
        );
    END IF;

    EXECUTE IMMEDIATE
        'SELECT COUNT(*) FROM VEHICLES ' ||
        'WHERE VEHICLE_NUMBER IS NULL ' ||
        'OR VEHICLE_TYPE IS NULL ' ||
        'OR CAPACITY <= 0 ' ||
        'OR CONDITION_STATUS IS NULL ' ||
        'OR STATUS IS NULL ' ||
        'OR CREATED_AT IS NULL ' ||
        'OR UPDATED_AT IS NULL'
        INTO v_nulls;

    IF v_nulls > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20015,
            'VEHICLES contains values incompatible with required entity fields or CAPACITY > 0.'
        );
    END IF;

    EXECUTE IMMEDIATE
        'SELECT COUNT(*) FROM (' ||
        'SELECT VEHICLE_ID FROM VEHICLES GROUP BY VEHICLE_ID HAVING COUNT(*) > 1)'
        INTO v_duplicates;
    IF v_duplicates > 0 THEN
        RAISE_APPLICATION_ERROR(-20016, 'Duplicate VEHICLE_ID values; no key added.');
    END IF;

    EXECUTE IMMEDIATE
        'SELECT COUNT(*) FROM (' ||
        'SELECT VEHICLE_NUMBER FROM VEHICLES GROUP BY VEHICLE_NUMBER HAVING COUNT(*) > 1)'
        INTO v_duplicates;
    IF v_duplicates > 0 THEN
        RAISE_APPLICATION_ERROR(-20017, 'Duplicate VEHICLE_NUMBER values; resolve before adding uniqueness.');
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM USER_CONSTRAINTS c
    JOIN USER_CONS_COLUMNS cc
      ON cc.CONSTRAINT_NAME = c.CONSTRAINT_NAME
     AND cc.TABLE_NAME = c.TABLE_NAME
    WHERE c.TABLE_NAME = 'VEHICLES'
      AND c.CONSTRAINT_TYPE = 'P'
      AND c.STATUS = 'ENABLED'
      AND cc.COLUMN_NAME = 'VEHICLE_ID';
    IF v_count = 0 THEN
        v_constraint_name := available_object_name('PK_VEHICLES');
        EXECUTE IMMEDIATE
            'ALTER TABLE VEHICLES ADD CONSTRAINT ' || v_constraint_name ||
            ' PRIMARY KEY (VEHICLE_ID)';
    END IF;

    FOR c IN (
        SELECT 'VEHICLE_NUMBER' column_name FROM DUAL UNION ALL
        SELECT 'VEHICLE_TYPE' FROM DUAL UNION ALL
        SELECT 'CAPACITY' FROM DUAL UNION ALL
        SELECT 'CONDITION_STATUS' FROM DUAL UNION ALL
        SELECT 'STATUS' FROM DUAL UNION ALL
        SELECT 'CREATED_AT' FROM DUAL UNION ALL
        SELECT 'UPDATED_AT' FROM DUAL
    ) LOOP
        SELECT COUNT(*) INTO v_count
        FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = 'VEHICLES'
          AND COLUMN_NAME = c.column_name
          AND NULLABLE = 'N';
        IF v_count = 0 THEN
            EXECUTE IMMEDIATE
                'ALTER TABLE VEHICLES MODIFY (' || c.column_name || ' NOT NULL)';
        END IF;
    END LOOP;

    SELECT COUNT(*) INTO v_count
    FROM USER_CONSTRAINTS c
    JOIN USER_CONS_COLUMNS cc
      ON cc.CONSTRAINT_NAME = c.CONSTRAINT_NAME
     AND cc.TABLE_NAME = c.TABLE_NAME
    WHERE c.TABLE_NAME = 'VEHICLES'
      AND c.CONSTRAINT_TYPE = 'U'
      AND c.STATUS = 'ENABLED'
      AND cc.COLUMN_NAME = 'VEHICLE_NUMBER';
    IF v_count = 0 THEN
        v_constraint_name := available_object_name('UK_VEHICLES_NUMBER');
        EXECUTE IMMEDIATE
            'ALTER TABLE VEHICLES ADD CONSTRAINT ' || v_constraint_name ||
            ' UNIQUE (VEHICLE_NUMBER)';
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM USER_CONSTRAINTS
    WHERE TABLE_NAME = 'VEHICLES'
      AND CONSTRAINT_TYPE = 'C'
      AND REPLACE(REPLACE(REPLACE(REPLACE(UPPER(SEARCH_CONDITION_VC),
          ' ', ''), CHR(9), ''), '(', ''), ')', '') LIKE '%CAPACITY>0%';
    IF v_count = 0 THEN
        v_constraint_name := available_object_name('CK_VEHICLES_CAPACITY');
        EXECUTE IMMEDIATE
            'ALTER TABLE VEHICLES ADD CONSTRAINT ' || v_constraint_name ||
            ' CHECK (CAPACITY > 0)';
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM USER_SEQUENCES
    WHERE SEQUENCE_NAME = 'VEHICLE_SEQ';
    IF v_count = 0 THEN
        SELECT NVL(MAX(VEHICLE_ID), 0) INTO v_max_id FROM VEHICLES;
        EXECUTE IMMEDIATE
            'CREATE SEQUENCE VEHICLE_SEQ START WITH ' ||
            TO_CHAR(v_max_id + 1, 'FM9999999999999999990') ||
            ' INCREMENT BY 1 NOCACHE NOCYCLE';
        DBMS_OUTPUT.PUT_LINE('Created VEHICLE_SEQ above existing VEHICLE_ID values.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('VEHICLE_SEQ exists; left unchanged.');
    END IF;

    DBMS_OUTPUT.PUT_LINE('VEHICLES now matches the resource-service entity mapping.');
END;
/
