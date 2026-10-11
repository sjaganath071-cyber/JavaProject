-- Defense Logistics & Resource Management demo data.
-- Source: resource-service entities/schema.sql and logistics-service JPA entities.
-- No schema changes or deletes are performed by this script.
-- Logistics tables must already exist as created by the logistics service/Hibernate.
-- A per-run token avoids collisions with existing business-key values.

SET DEFINE OFF
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

-- Verify all targets and resource-service sequences exist in the connected schema
-- before compiling/executing the DML block. USER_* views intentionally avoid
-- assuming access to, or writing into, another owner's schema.
DECLARE
    TYPE t_text_tab IS TABLE OF VARCHAR2(4000) INDEX BY PLS_INTEGER;
    v_required t_text_tab;
    v_sequences t_text_tab;
    v_keys t_text_tab;
    v_table VARCHAR2(128);
    v_columns VARCHAR2(4000);
    v_column VARCHAR2(128);
    v_missing VARCHAR2(3000);
    v_key VARCHAR2(4000);
    v_key_table VARCHAR2(128);
    v_key_type VARCHAR2(1);
    v_key_column VARCHAR2(128);
    v_ref_table VARCHAR2(128);
    v_ref_column VARCHAR2(128);
    v_count NUMBER;
    v_separator PLS_INTEGER;
BEGIN
    v_required(1) := 'PERSONNEL:PERSONNEL_ID,PERSONNEL_CODE,FULL_NAME,ROLE,UNIT_ID,POSTING_LOCATION,AVAILABILITY,ACTIVE,CREATED_AT,UPDATED_AT';
    v_required(2) := 'ASSETS:ASSET_ID,ASSET_TAG,ASSET_NAME,CATEGORY,UNIT_ID,LOCATION,CONDITION_STATUS,STATUS,CREATED_AT,UPDATED_AT';
    v_required(3) := 'VEHICLES:VEHICLE_ID,REGISTRATION_NUMBER,VEHICLE_NAME,VEHICLE_TYPE,UNIT_ID,LOCATION,STATUS,VEHICLE_NUMBER,CAPACITY,LOCATION_CODE,CONDITION_STATUS,CREATED_AT,UPDATED_AT';
    v_required(4) := 'INVENTORY_ITEMS:ITEM_ID,ITEM_CODE,ITEM_NAME,CATEGORY,UNIT_OF_MEASURE,UNIT_ID,LOCATION,QUANTITY_ON_HAND,QUANTITY_RESERVED,REORDER_THRESHOLD,CREATED_AT,UPDATED_AT';
    v_required(5) := 'INVENTORY_TRANSACTIONS:TRANSACTION_ID,ITEM_ID,TRANSACTION_TYPE,QUANTITY,REASON,CREATED_AT';
    v_required(6) := 'MAINTENANCE_RECORDS:MAINTENANCE_ID,RESOURCE_TYPE,RESOURCE_ID,MAINTENANCE_TYPE,DESCRIPTION,SCHEDULED_DATE,START_DATE,COMPLETION_DATE,STATUS,SERVICE_NOTES,TECHNICIAN,CREATED_AT,UPDATED_AT';
    v_required(7) := 'LOGISTICS_REQUESTS:ID,REQUEST_NUMBER,REQUESTING_UNIT,REQUESTER_NAME,PURPOSE,PRIORITY,REQUIRED_FROM,REQUIRED_UNTIL,STATUS,CREATED_AT,UPDATED_AT';
    v_required(8) := 'LOGISTICS_REQUEST_ITEMS:ID,REQUEST_ID,RESOURCE_TYPE,RESOURCE_REFERENCE,QUANTITY,NOTES,FULFILLED_QUANTITY';
    v_required(9) := 'LOGISTICS_ALLOCATIONS:ID,REQUEST_ID,REQUEST_LINE_ITEM_ID,RESOURCE_TYPE,RESOURCE_REFERENCE,QUANTITY,STATUS,ALLOCATED_AT,RELEASED_AT,COMPLETED_AT,RESERVATION_REFERENCE';
    v_required(10) := 'VEHICLE:ID,VEHICLE_NAME,VEHICLE_TYPE,REGISTRATION_NUMBER,STATUS';
    v_required(11) := 'LOGISTICS_SHIPMENTS:ID,SHIPMENT_NUMBER,ORIGIN,DESTINATION,CARGO_DESCRIPTION,STATUS,VEHICLE_ID';
    v_required(12) := 'SHIPMENT_TRACKING:ID,SHIPMENT_ID,LOCATION,STATUS,REMARKS,UPDATED_AT';

    v_sequences(1) := 'PERSONNEL_SEQ';
    v_sequences(2) := 'ASSET_SEQ';
    v_sequences(3) := 'VEHICLE_SEQ';
    v_sequences(4) := 'INVENTORY_ITEM_SEQ';
    v_sequences(5) := 'INVENTORY_TRANSACTION_SEQ';
    v_sequences(6) := 'MAINTENANCE_SEQ';

    v_keys(1) := 'PERSONNEL|P|PERSONNEL_ID';
    v_keys(2) := 'ASSETS|P|ASSET_ID';
    v_keys(3) := 'VEHICLES|P|VEHICLE_ID';
    v_keys(4) := 'INVENTORY_ITEMS|P|ITEM_ID';
    v_keys(5) := 'INVENTORY_TRANSACTIONS|P|TRANSACTION_ID';
    v_keys(6) := 'MAINTENANCE_RECORDS|P|MAINTENANCE_ID';
    v_keys(7) := 'LOGISTICS_REQUESTS|P|ID';
    v_keys(8) := 'LOGISTICS_REQUEST_ITEMS|P|ID';
    v_keys(9) := 'LOGISTICS_ALLOCATIONS|P|ID';
    v_keys(10) := 'VEHICLE|P|ID';
    v_keys(11) := 'LOGISTICS_SHIPMENTS|P|ID';
    v_keys(12) := 'SHIPMENT_TRACKING|P|ID';
    v_keys(13) := 'VEHICLES|U|VEHICLE_NUMBER';
    v_keys(14) := 'LOGISTICS_REQUESTS|U|REQUEST_NUMBER';
    v_keys(15) := 'LOGISTICS_SHIPMENTS|U|SHIPMENT_NUMBER';
    v_keys(16) := 'INVENTORY_TRANSACTIONS|R|ITEM_ID|INVENTORY_ITEMS|ITEM_ID';
    v_keys(17) := 'LOGISTICS_REQUEST_ITEMS|R|REQUEST_ID|LOGISTICS_REQUESTS|ID';
    v_keys(18) := 'LOGISTICS_ALLOCATIONS|R|REQUEST_ID|LOGISTICS_REQUESTS|ID';
    v_keys(19) := 'LOGISTICS_ALLOCATIONS|R|REQUEST_LINE_ITEM_ID|LOGISTICS_REQUEST_ITEMS|ID';

    IF SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') <>
       SYS_CONTEXT('USERENV', 'SESSION_USER') THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'Current schema differs from the login owner. Connect directly as the schema owner before running this file.'
        );
    END IF;

    FOR i IN 1 .. v_required.COUNT LOOP
        v_separator := INSTR(v_required(i), ':');
        v_table := SUBSTR(v_required(i), 1, v_separator - 1);
        v_columns := SUBSTR(v_required(i), v_separator + 1);

        SELECT COUNT(*) INTO v_count
        FROM USER_TABLES
        WHERE TABLE_NAME = v_table;

        IF v_count = 0 THEN
            v_missing := v_missing || ' TABLE ' || v_table || ';';
        ELSE
            FOR j IN 1 .. REGEXP_COUNT(v_columns, ',') + 1 LOOP
                v_column := REGEXP_SUBSTR(v_columns, '[^,]+', 1, j);
                SELECT COUNT(*) INTO v_count
                FROM USER_TAB_COLUMNS
                WHERE TABLE_NAME = v_table
                  AND COLUMN_NAME = v_column;

                IF v_count = 0 THEN
                    v_missing := v_missing || ' COLUMN ' || v_table || '.' || v_column || ';';
                END IF;
            END LOOP;
        END IF;
    END LOOP;

    FOR i IN 1 .. v_sequences.COUNT LOOP
        SELECT COUNT(*) INTO v_count
        FROM USER_SEQUENCES
        WHERE SEQUENCE_NAME = v_sequences(i);

        IF v_count = 0 THEN
            v_missing := v_missing || ' SEQUENCE ' || v_sequences(i) || ';';
        END IF;
    END LOOP;

    FOR i IN 1 .. v_keys.COUNT LOOP
        v_key := v_keys(i);
        v_key_table := REGEXP_SUBSTR(v_key, '[^|]+', 1, 1);
        v_key_type := REGEXP_SUBSTR(v_key, '[^|]+', 1, 2);
        v_key_column := REGEXP_SUBSTR(v_key, '[^|]+', 1, 3);

        IF v_key_type IN ('P', 'U') THEN
            SELECT COUNT(*) INTO v_count
            FROM USER_CONSTRAINTS c
            JOIN USER_CONS_COLUMNS cc
              ON cc.CONSTRAINT_NAME = c.CONSTRAINT_NAME
             AND cc.TABLE_NAME = c.TABLE_NAME
            WHERE c.TABLE_NAME = v_key_table
              AND c.CONSTRAINT_TYPE = v_key_type
              AND c.STATUS = 'ENABLED'
              AND cc.COLUMN_NAME = v_key_column;
        ELSE
            v_ref_table := REGEXP_SUBSTR(v_key, '[^|]+', 1, 4);
            v_ref_column := REGEXP_SUBSTR(v_key, '[^|]+', 1, 5);
            SELECT COUNT(*) INTO v_count
            FROM USER_CONSTRAINTS c
            JOIN USER_CONS_COLUMNS cc
              ON cc.CONSTRAINT_NAME = c.CONSTRAINT_NAME
             AND cc.TABLE_NAME = c.TABLE_NAME
            JOIN USER_CONSTRAINTS r
              ON r.CONSTRAINT_NAME = c.R_CONSTRAINT_NAME
            JOIN USER_CONS_COLUMNS rc
              ON rc.CONSTRAINT_NAME = r.CONSTRAINT_NAME
             AND rc.TABLE_NAME = r.TABLE_NAME
             AND rc.POSITION = cc.POSITION
            WHERE c.TABLE_NAME = v_key_table
              AND c.CONSTRAINT_TYPE = 'R'
              AND c.STATUS = 'ENABLED'
              AND cc.COLUMN_NAME = v_key_column
              AND r.TABLE_NAME = v_ref_table
              AND rc.COLUMN_NAME = v_ref_column;
        END IF;

        IF v_count = 0 THEN
            v_missing := v_missing || ' CONSTRAINT ' || v_key || ';';
        END IF;
    END LOOP;

    SELECT COUNT(*) INTO v_count
    FROM USER_CONSTRAINTS
    WHERE TABLE_NAME = 'VEHICLES'
      AND CONSTRAINT_TYPE = 'C'
      AND STATUS = 'ENABLED'
      AND REPLACE(REPLACE(REPLACE(REPLACE(UPPER(SEARCH_CONDITION_VC),
          ' ', ''), CHR(9), ''), '(', ''), ')', '') LIKE '%CAPACITY>0%';
    IF v_count = 0 THEN
        v_missing := v_missing || ' CHECK VEHICLES(CAPACITY > 0);';
    END IF;

    IF v_missing IS NOT NULL THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'Required objects are missing from the connected schema ' ||
            SYS_CONTEXT('USERENV', 'SESSION_USER') || ':' ||
            SUBSTR(v_missing, 1, 1500) ||
            ' Run the resource schema setup and, for legacy VEHICLES, ' ||
            'migrate_legacy_vehicles.sql after supplying verified capacities.'
        );
    END IF;

    DBMS_OUTPUT.PUT_LINE(
        'Preflight passed for schema ' || SYS_CONTEXT('USERENV', 'SESSION_USER') ||
        ': all 12 tables, columns, required keys, and 6 resource sequences are present.'
    );
END;
/

DECLARE
    v_token CONSTANT VARCHAR2(16) := 'DEFLOG20261011';
    v_id NUMBER;
    v_count NUMBER;

    v_item_1 NUMBER;
    v_item_2 NUMBER;
    v_item_3 NUMBER;
    v_item_4 NUMBER;
    v_item_5 NUMBER;
    v_resource_vehicle_1 NUMBER;
    v_resource_vehicle_2 NUMBER;
    v_resource_vehicle_3 NUMBER;
    v_resource_vehicle_4 NUMBER;
    v_resource_vehicle_5 NUMBER;

    v_request_1 NUMBER;
    v_request_2 NUMBER;
    v_request_3 NUMBER;
    v_request_4 NUMBER;
    v_request_5 NUMBER;
    v_line_1 NUMBER;
    v_line_2 NUMBER;
    v_line_3 NUMBER;
    v_line_4 NUMBER;
    v_line_5 NUMBER;
    v_log_vehicle_1 NUMBER;
    v_log_vehicle_2 NUMBER;
    v_log_vehicle_3 NUMBER;
    v_log_vehicle_4 NUMBER;
    v_log_vehicle_5 NUMBER;
    v_shipment_1 NUMBER;
    v_shipment_2 NUMBER;
    v_shipment_3 NUMBER;
    v_shipment_4 NUMBER;
    v_shipment_5 NUMBER;

    FUNCTION next_free_id(
        p_sequence IN VARCHAR2,
        p_table IN VARCHAR2,
        p_column IN VARCHAR2
    ) RETURN NUMBER IS
        v_id NUMBER;
        v_count NUMBER;
    BEGIN
        LOOP
            EXECUTE IMMEDIATE 'SELECT ' || p_sequence || '.NEXTVAL FROM DUAL'
                INTO v_id;
            EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM ' || p_table ||
                              ' WHERE ' || p_column || ' = :id'
                INTO v_count USING v_id;
            EXIT WHEN v_count = 0;
        END LOOP;
        RETURN v_id;
    END next_free_id;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM PERSONNEL
    WHERE PERSONNEL_CODE = 'P-' || v_token || '-01';

    IF v_count > 0 THEN
        DBMS_OUTPUT.PUT_LINE(
            'Demo data set ' || v_token ||
            ' already exists; no rows were inserted.'
        );
    ELSE
    -- Resource-service parent/reference tables.
    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into PERSONNEL...');
    v_id := next_free_id('PERSONNEL_SEQ', 'PERSONNEL', 'PERSONNEL_ID');
    INSERT INTO PERSONNEL
        (PERSONNEL_ID, PERSONNEL_CODE, FULL_NAME, ROLE, UNIT_ID,
         POSTING_LOCATION, AVAILABILITY, ACTIVE, CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'P-' || v_token || '-01', 'Maya Srinivasan', 'Logistics Officer', 101,
         'Northern Depot', 'AVAILABLE', 1, LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('PERSONNEL_SEQ', 'PERSONNEL', 'PERSONNEL_ID');
    INSERT INTO PERSONNEL
        (PERSONNEL_ID, PERSONNEL_CODE, FULL_NAME, ROLE, UNIT_ID,
         POSTING_LOCATION, AVAILABILITY, ACTIVE, CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'P-' || v_token || '-02', 'Arjun Mehta', 'Vehicle Technician', 101,
         'Northern Depot', 'AVAILABLE', 1, LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('PERSONNEL_SEQ', 'PERSONNEL', 'PERSONNEL_ID');
    INSERT INTO PERSONNEL
        (PERSONNEL_ID, PERSONNEL_CODE, FULL_NAME, ROLE, UNIT_ID,
         POSTING_LOCATION, AVAILABILITY, ACTIVE, CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'P-' || v_token || '-03', 'Leena Das', 'Supply Specialist', 202,
         'Central Supply Base', 'DEPLOYED', 1, LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('PERSONNEL_SEQ', 'PERSONNEL', 'PERSONNEL_ID');
    INSERT INTO PERSONNEL
        (PERSONNEL_ID, PERSONNEL_CODE, FULL_NAME, ROLE, UNIT_ID,
         POSTING_LOCATION, AVAILABILITY, ACTIVE, CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'P-' || v_token || '-04', 'Kabir Rao', 'Transport Coordinator', 202,
         'Central Supply Base', 'AVAILABLE', 1, LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('PERSONNEL_SEQ', 'PERSONNEL', 'PERSONNEL_ID');
    INSERT INTO PERSONNEL
        (PERSONNEL_ID, PERSONNEL_CODE, FULL_NAME, ROLE, UNIT_ID,
         POSTING_LOCATION, AVAILABILITY, ACTIVE, CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'P-' || v_token || '-05', 'Nadia Thomas', 'Field Medic', 303,
         'Forward Support Point', 'ON_LEAVE', 1, LOCALTIMESTAMP, LOCALTIMESTAMP);

    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into ASSETS...');
    v_id := next_free_id('ASSET_SEQ', 'ASSETS', 'ASSET_ID');
    INSERT INTO ASSETS
        (ASSET_ID, ASSET_TAG, ASSET_NAME, CATEGORY, UNIT_ID, LOCATION,
         CONDITION_STATUS, STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'A-' || v_token || '-01', 'Portable Field Generator', 'Power', 101,
         'Northern Depot', 'SERVICEABLE', 'AVAILABLE', LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('ASSET_SEQ', 'ASSETS', 'ASSET_ID');
    INSERT INTO ASSETS
        (ASSET_ID, ASSET_TAG, ASSET_NAME, CATEGORY, UNIT_ID, LOCATION,
         CONDITION_STATUS, STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'A-' || v_token || '-02', 'Mobile Water Purifier', 'Water Systems', 101,
         'Northern Depot', 'SERVICEABLE', 'AVAILABLE', LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('ASSET_SEQ', 'ASSETS', 'ASSET_ID');
    INSERT INTO ASSETS
        (ASSET_ID, ASSET_TAG, ASSET_NAME, CATEGORY, UNIT_ID, LOCATION,
         CONDITION_STATUS, STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'A-' || v_token || '-03', 'Tactical Radio Set', 'Communications', 202,
         'Central Supply Base', 'GOOD', 'ISSUED', LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('ASSET_SEQ', 'ASSETS', 'ASSET_ID');
    INSERT INTO ASSETS
        (ASSET_ID, ASSET_TAG, ASSET_NAME, CATEGORY, UNIT_ID, LOCATION,
         CONDITION_STATUS, STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'A-' || v_token || '-04', 'Portable Lighting Tower', 'Engineering', 202,
         'Central Supply Base', 'NEEDS_INSPECTION', 'MAINTENANCE', LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('ASSET_SEQ', 'ASSETS', 'ASSET_ID');
    INSERT INTO ASSETS
        (ASSET_ID, ASSET_TAG, ASSET_NAME, CATEGORY, UNIT_ID, LOCATION,
         CONDITION_STATUS, STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'A-' || v_token || '-05', 'Medical Refrigeration Unit', 'Medical', 303,
         'Forward Support Point', 'SERVICEABLE', 'AVAILABLE', LOCALTIMESTAMP, LOCALTIMESTAMP);

    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into resource-service VEHICLES...');
    v_resource_vehicle_1 := next_free_id('VEHICLE_SEQ', 'VEHICLES', 'VEHICLE_ID');
    INSERT INTO VEHICLES
        (VEHICLE_ID, REGISTRATION_NUMBER, VEHICLE_NAME, VEHICLE_TYPE, UNIT_ID,
         LOCATION, STATUS, VEHICLE_NUMBER, CAPACITY, LOCATION_CODE,
         CONDITION_STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        (v_resource_vehicle_1, 'RV-' || v_token || '-01', 'Convoy Cargo Truck Alpha',
         'Cargo Truck', 101, 'NORTH-01', 'AVAILABLE',
         'RV-' || v_token || '-01', 8500, 'NORTH-01', 'SERVICEABLE',
         LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_resource_vehicle_2 := next_free_id('VEHICLE_SEQ', 'VEHICLES', 'VEHICLE_ID');
    INSERT INTO VEHICLES
        (VEHICLE_ID, REGISTRATION_NUMBER, VEHICLE_NAME, VEHICLE_TYPE, UNIT_ID,
         LOCATION, STATUS, VEHICLE_NUMBER, CAPACITY, LOCATION_CODE,
         CONDITION_STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        (v_resource_vehicle_2, 'RV-' || v_token || '-02', 'Utility Vehicle Bravo',
         'Utility Vehicle', 101, 'NORTH-01', 'AVAILABLE',
         'RV-' || v_token || '-02', 1200, 'NORTH-01', 'GOOD',
         LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_resource_vehicle_3 := next_free_id('VEHICLE_SEQ', 'VEHICLES', 'VEHICLE_ID');
    INSERT INTO VEHICLES
        (VEHICLE_ID, REGISTRATION_NUMBER, VEHICLE_NAME, VEHICLE_TYPE, UNIT_ID,
         LOCATION, STATUS, VEHICLE_NUMBER, CAPACITY, LOCATION_CODE,
         CONDITION_STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        (v_resource_vehicle_3, 'RV-' || v_token || '-03', 'Fuel Tanker Charlie',
         'Fuel Tanker', 202, 'CENTRAL-02', 'IN_SERVICE',
         'RV-' || v_token || '-03', 12000, 'CENTRAL-02', 'SERVICEABLE',
         LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_resource_vehicle_4 := next_free_id('VEHICLE_SEQ', 'VEHICLES', 'VEHICLE_ID');
    INSERT INTO VEHICLES
        (VEHICLE_ID, REGISTRATION_NUMBER, VEHICLE_NAME, VEHICLE_TYPE, UNIT_ID,
         LOCATION, STATUS, VEHICLE_NUMBER, CAPACITY, LOCATION_CODE,
         CONDITION_STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        (v_resource_vehicle_4, 'RV-' || v_token || '-04', 'Medical Ambulance Delta',
         'Ambulance', 303, 'FORWARD-03', 'AVAILABLE',
         'RV-' || v_token || '-04', 900, 'FORWARD-03', 'GOOD',
         LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_resource_vehicle_5 := next_free_id('VEHICLE_SEQ', 'VEHICLES', 'VEHICLE_ID');
    INSERT INTO VEHICLES
        (VEHICLE_ID, REGISTRATION_NUMBER, VEHICLE_NAME, VEHICLE_TYPE, UNIT_ID,
         LOCATION, STATUS, VEHICLE_NUMBER, CAPACITY, LOCATION_CODE,
         CONDITION_STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        (v_resource_vehicle_5, 'RV-' || v_token || '-05', 'Recovery Vehicle Echo',
         'Recovery Vehicle', 202, 'CENTRAL-02', 'AVAILABLE',
         'RV-' || v_token || '-05', 5000, 'CENTRAL-02', 'FAIR',
         LOCALTIMESTAMP, LOCALTIMESTAMP);

    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into INVENTORY_ITEMS...');
    v_item_1 := next_free_id('INVENTORY_ITEM_SEQ', 'INVENTORY_ITEMS', 'ITEM_ID');
    INSERT INTO INVENTORY_ITEMS
        (ITEM_ID, ITEM_CODE, ITEM_NAME, CATEGORY, UNIT_OF_MEASURE, UNIT_ID, LOCATION,
         QUANTITY_ON_HAND, QUANTITY_RESERVED, REORDER_THRESHOLD, CREATED_AT, UPDATED_AT)
    VALUES
        (v_item_1, 'I-' || v_token || '-01', 'Field Ration Pack', 'Food', 'CASE', 101,
         'Northern Depot', 240, 20, 60, LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_item_2 := next_free_id('INVENTORY_ITEM_SEQ', 'INVENTORY_ITEMS', 'ITEM_ID');
    INSERT INTO INVENTORY_ITEMS
        (ITEM_ID, ITEM_CODE, ITEM_NAME, CATEGORY, UNIT_OF_MEASURE, UNIT_ID, LOCATION,
         QUANTITY_ON_HAND, QUANTITY_RESERVED, REORDER_THRESHOLD, CREATED_AT, UPDATED_AT)
    VALUES
        (v_item_2, 'I-' || v_token || '-02', 'Diesel Fuel', 'Fuel', 'LITRE', 202,
         'Central Supply Base', 5200, 500, 1000, LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_item_3 := next_free_id('INVENTORY_ITEM_SEQ', 'INVENTORY_ITEMS', 'ITEM_ID');
    INSERT INTO INVENTORY_ITEMS
        (ITEM_ID, ITEM_CODE, ITEM_NAME, CATEGORY, UNIT_OF_MEASURE, UNIT_ID, LOCATION,
         QUANTITY_ON_HAND, QUANTITY_RESERVED, REORDER_THRESHOLD, CREATED_AT, UPDATED_AT)
    VALUES
        (v_item_3, 'I-' || v_token || '-03', 'Trauma Dressing Kit', 'Medical', 'KIT', 303,
         'Forward Support Point', 180, 25, 40, LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_item_4 := next_free_id('INVENTORY_ITEM_SEQ', 'INVENTORY_ITEMS', 'ITEM_ID');
    INSERT INTO INVENTORY_ITEMS
        (ITEM_ID, ITEM_CODE, ITEM_NAME, CATEGORY, UNIT_OF_MEASURE, UNIT_ID, LOCATION,
         QUANTITY_ON_HAND, QUANTITY_RESERVED, REORDER_THRESHOLD, CREATED_AT, UPDATED_AT)
    VALUES
        (v_item_4, 'I-' || v_token || '-04', 'Water Container 20L', 'Water', 'EACH', 101,
         'Northern Depot', 360, 30, 80, LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_item_5 := next_free_id('INVENTORY_ITEM_SEQ', 'INVENTORY_ITEMS', 'ITEM_ID');
    INSERT INTO INVENTORY_ITEMS
        (ITEM_ID, ITEM_CODE, ITEM_NAME, CATEGORY, UNIT_OF_MEASURE, UNIT_ID, LOCATION,
         QUANTITY_ON_HAND, QUANTITY_RESERVED, REORDER_THRESHOLD, CREATED_AT, UPDATED_AT)
    VALUES
        (v_item_5, 'I-' || v_token || '-05', 'Radio Battery Pack', 'Communications', 'EACH', 202,
         'Central Supply Base', 95, 10, 25, LOCALTIMESTAMP, LOCALTIMESTAMP);

    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into INVENTORY_TRANSACTIONS...');
    v_id := next_free_id('INVENTORY_TRANSACTION_SEQ', 'INVENTORY_TRANSACTIONS', 'TRANSACTION_ID');
    INSERT INTO INVENTORY_TRANSACTIONS
        (TRANSACTION_ID, ITEM_ID, TRANSACTION_TYPE, QUANTITY, REASON, CREATED_AT)
    VALUES
        (v_id, v_item_1, 'RECEIVE', 100, 'Quarterly ration replenishment', LOCALTIMESTAMP);
    v_id := next_free_id('INVENTORY_TRANSACTION_SEQ', 'INVENTORY_TRANSACTIONS', 'TRANSACTION_ID');
    INSERT INTO INVENTORY_TRANSACTIONS
        (TRANSACTION_ID, ITEM_ID, TRANSACTION_TYPE, QUANTITY, REASON, CREATED_AT)
    VALUES
        (v_id, v_item_2, 'ISSUE', 350, 'Fuel issued for scheduled convoy', LOCALTIMESTAMP);
    v_id := next_free_id('INVENTORY_TRANSACTION_SEQ', 'INVENTORY_TRANSACTIONS', 'TRANSACTION_ID');
    INSERT INTO INVENTORY_TRANSACTIONS
        (TRANSACTION_ID, ITEM_ID, TRANSACTION_TYPE, QUANTITY, REASON, CREATED_AT)
    VALUES
        (v_id, v_item_3, 'RECEIVE', 60, 'Medical resupply received', LOCALTIMESTAMP);
    v_id := next_free_id('INVENTORY_TRANSACTION_SEQ', 'INVENTORY_TRANSACTIONS', 'TRANSACTION_ID');
    INSERT INTO INVENTORY_TRANSACTIONS
        (TRANSACTION_ID, ITEM_ID, TRANSACTION_TYPE, QUANTITY, REASON, CREATED_AT)
    VALUES
        (v_id, v_item_4, 'ADJUST', 12, 'Count correction after depot audit', LOCALTIMESTAMP);
    v_id := next_free_id('INVENTORY_TRANSACTION_SEQ', 'INVENTORY_TRANSACTIONS', 'TRANSACTION_ID');
    INSERT INTO INVENTORY_TRANSACTIONS
        (TRANSACTION_ID, ITEM_ID, TRANSACTION_TYPE, QUANTITY, REASON, CREATED_AT)
    VALUES
        (v_id, v_item_5, 'ISSUE', 8, 'Replacement batteries issued to signal team', LOCALTIMESTAMP);

    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into MAINTENANCE_RECORDS...');
    v_id := next_free_id('MAINTENANCE_SEQ', 'MAINTENANCE_RECORDS', 'MAINTENANCE_ID');
    INSERT INTO MAINTENANCE_RECORDS
        (MAINTENANCE_ID, RESOURCE_TYPE, RESOURCE_ID, MAINTENANCE_TYPE, DESCRIPTION,
         SCHEDULED_DATE, START_DATE, COMPLETION_DATE, STATUS, SERVICE_NOTES, TECHNICIAN,
         CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'VEHICLE', v_resource_vehicle_1, 'Preventive service',
         'Inspect engine, brakes, and load restraints before convoy duty',
         TIMESTAMP '2026-10-16 08:00:00', NULL, NULL, 'SCHEDULED', NULL, 'Arjun Mehta',
         LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('MAINTENANCE_SEQ', 'MAINTENANCE_RECORDS', 'MAINTENANCE_ID');
    INSERT INTO MAINTENANCE_RECORDS
        (MAINTENANCE_ID, RESOURCE_TYPE, RESOURCE_ID, MAINTENANCE_TYPE, DESCRIPTION,
         SCHEDULED_DATE, START_DATE, COMPLETION_DATE, STATUS, SERVICE_NOTES, TECHNICIAN,
         CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'VEHICLE', v_resource_vehicle_2, 'Electrical inspection',
         'Diagnose intermittent charging fault and test alternator output',
         TIMESTAMP '2026-10-12 09:00:00', TIMESTAMP '2026-10-12 09:10:00', NULL,
         'IN_PROGRESS', 'Replacement regulator requested', 'Arjun Mehta',
         LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('MAINTENANCE_SEQ', 'MAINTENANCE_RECORDS', 'MAINTENANCE_ID');
    INSERT INTO MAINTENANCE_RECORDS
        (MAINTENANCE_ID, RESOURCE_TYPE, RESOURCE_ID, MAINTENANCE_TYPE, DESCRIPTION,
         SCHEDULED_DATE, START_DATE, COMPLETION_DATE, STATUS, SERVICE_NOTES, TECHNICIAN,
         CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'VEHICLE', v_resource_vehicle_3, 'Fuel system service',
         'Replace primary filter and verify tanker pump operation',
         TIMESTAMP '2026-10-05 08:00:00', TIMESTAMP '2026-10-05 08:30:00',
         TIMESTAMP '2026-10-05 11:45:00', 'COMPLETED',
         'Road test passed; unit returned to service', 'Arjun Mehta',
         LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('MAINTENANCE_SEQ', 'MAINTENANCE_RECORDS', 'MAINTENANCE_ID');
    INSERT INTO MAINTENANCE_RECORDS
        (MAINTENANCE_ID, RESOURCE_TYPE, RESOURCE_ID, MAINTENANCE_TYPE, DESCRIPTION,
         SCHEDULED_DATE, START_DATE, COMPLETION_DATE, STATUS, SERVICE_NOTES, TECHNICIAN,
         CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'VEHICLE', v_resource_vehicle_4, 'Safety inspection',
         'Inspect patient restraint points and emergency warning equipment',
         TIMESTAMP '2026-10-18 10:00:00', NULL, NULL, 'SCHEDULED', NULL, 'Arjun Mehta',
         LOCALTIMESTAMP, LOCALTIMESTAMP);
    v_id := next_free_id('MAINTENANCE_SEQ', 'MAINTENANCE_RECORDS', 'MAINTENANCE_ID');
    INSERT INTO MAINTENANCE_RECORDS
        (MAINTENANCE_ID, RESOURCE_TYPE, RESOURCE_ID, MAINTENANCE_TYPE, DESCRIPTION,
         SCHEDULED_DATE, START_DATE, COMPLETION_DATE, STATUS, SERVICE_NOTES, TECHNICIAN,
         CREATED_AT, UPDATED_AT)
    VALUES
        (v_id, 'VEHICLE', v_resource_vehicle_5, 'Recovery equipment repair',
         'Repair winch cable guide and complete load test',
         TIMESTAMP '2026-10-09 08:00:00', TIMESTAMP '2026-10-09 08:15:00',
         TIMESTAMP '2026-10-09 09:00:00', 'CANCELLED',
         'Work cancelled; replacement component unavailable', 'Arjun Mehta',
         LOCALTIMESTAMP, LOCALTIMESTAMP);

    -- Logistics request parent rows, followed by their required child line items.
    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into LOGISTICS_REQUESTS...');
    INSERT INTO LOGISTICS_REQUESTS
        (REQUEST_NUMBER, REQUESTING_UNIT, REQUESTER_NAME, PURPOSE, PRIORITY,
         REQUIRED_FROM, REQUIRED_UNTIL, STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        ('REQ-' || v_token || '-01', 'Northern Support Battalion', 'Maya Srinivasan',
         'Resupply forward teams with rations and potable water', 'HIGH',
         DATE '2026-10-12', DATE '2026-10-15', 'SUBMITTED', LOCALTIMESTAMP, LOCALTIMESTAMP)
    RETURNING ID INTO v_request_1;
    INSERT INTO LOGISTICS_REQUESTS
        (REQUEST_NUMBER, REQUESTING_UNIT, REQUESTER_NAME, PURPOSE, PRIORITY,
         REQUIRED_FROM, REQUIRED_UNTIL, STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        ('REQ-' || v_token || '-02', 'Central Transport Group', 'Kabir Rao',
         'Prepare fuel and transport capacity for planned convoy', 'URGENT',
         DATE '2026-10-13', DATE '2026-10-14', 'SUBMITTED', LOCALTIMESTAMP, LOCALTIMESTAMP)
    RETURNING ID INTO v_request_2;
    INSERT INTO LOGISTICS_REQUESTS
        (REQUEST_NUMBER, REQUESTING_UNIT, REQUESTER_NAME, PURPOSE, PRIORITY,
         REQUIRED_FROM, REQUIRED_UNTIL, STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        ('REQ-' || v_token || '-03', 'Forward Medical Detachment', 'Nadia Thomas',
         'Replenish trauma care kits for field treatment stations', 'HIGH',
         DATE '2026-10-14', DATE '2026-10-20', 'DRAFT', LOCALTIMESTAMP, LOCALTIMESTAMP)
    RETURNING ID INTO v_request_3;
    INSERT INTO LOGISTICS_REQUESTS
        (REQUEST_NUMBER, REQUESTING_UNIT, REQUESTER_NAME, PURPOSE, PRIORITY,
         REQUIRED_FROM, REQUIRED_UNTIL, STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        ('REQ-' || v_token || '-04', 'Signals Support Company', 'Leena Das',
         'Issue replacement power packs for field communications equipment', 'NORMAL',
         DATE '2026-10-15', DATE '2026-10-18', 'SUBMITTED', LOCALTIMESTAMP, LOCALTIMESTAMP)
    RETURNING ID INTO v_request_4;
    INSERT INTO LOGISTICS_REQUESTS
        (REQUEST_NUMBER, REQUESTING_UNIT, REQUESTER_NAME, PURPOSE, PRIORITY,
         REQUIRED_FROM, REQUIRED_UNTIL, STATUS, CREATED_AT, UPDATED_AT)
    VALUES
        ('REQ-' || v_token || '-05', 'Northern Support Battalion', 'Maya Srinivasan',
         'Request recovery support for scheduled vehicle readiness activity', 'LOW',
         DATE '2026-10-19', DATE '2026-10-21', 'CANCELLED', LOCALTIMESTAMP, LOCALTIMESTAMP)
    RETURNING ID INTO v_request_5;

    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into LOGISTICS_REQUEST_ITEMS...');
    INSERT INTO LOGISTICS_REQUEST_ITEMS
        (REQUEST_ID, RESOURCE_TYPE, RESOURCE_REFERENCE, QUANTITY, NOTES, FULFILLED_QUANTITY)
    VALUES
        (v_request_1, 'SUPPLY', 'I-' || v_token || '-01', 40,
         'Cases for the forward resupply window', 10)
    RETURNING ID INTO v_line_1;
    INSERT INTO LOGISTICS_REQUEST_ITEMS
        (REQUEST_ID, RESOURCE_TYPE, RESOURCE_REFERENCE, QUANTITY, NOTES, FULFILLED_QUANTITY)
    VALUES
        (v_request_2, 'SUPPLY', 'I-' || v_token || '-02', 1200,
         'Litres staged for convoy departure', 0)
    RETURNING ID INTO v_line_2;
    INSERT INTO LOGISTICS_REQUEST_ITEMS
        (REQUEST_ID, RESOURCE_TYPE, RESOURCE_REFERENCE, QUANTITY, NOTES, FULFILLED_QUANTITY)
    VALUES
        (v_request_3, 'SUPPLY', 'I-' || v_token || '-03', 25,
         'Trauma kits for medical teams', 0)
    RETURNING ID INTO v_line_3;
    INSERT INTO LOGISTICS_REQUEST_ITEMS
        (REQUEST_ID, RESOURCE_TYPE, RESOURCE_REFERENCE, QUANTITY, NOTES, FULFILLED_QUANTITY)
    VALUES
        (v_request_4, 'SUPPLY', 'I-' || v_token || '-05', 15,
         'Battery packs for radio section', 5)
    RETURNING ID INTO v_line_4;
    INSERT INTO LOGISTICS_REQUEST_ITEMS
        (REQUEST_ID, RESOURCE_TYPE, RESOURCE_REFERENCE, QUANTITY, NOTES, FULFILLED_QUANTITY)
    VALUES
        (v_request_5, 'VEHICLE', 'RV-' || v_token || '-05', 1,
         'Recovery vehicle support; request later cancelled', 0)
    RETURNING ID INTO v_line_5;

    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into LOGISTICS_ALLOCATIONS...');
    INSERT INTO LOGISTICS_ALLOCATIONS
        (REQUEST_ID, REQUEST_LINE_ITEM_ID, RESOURCE_TYPE, RESOURCE_REFERENCE,
         QUANTITY, STATUS, ALLOCATED_AT, RELEASED_AT, COMPLETED_AT, RESERVATION_REFERENCE)
    VALUES
        (v_request_1, v_line_1, 'SUPPLY', 'I-' || v_token || '-01',
         10, 'ALLOCATED', LOCALTIMESTAMP, NULL, NULL, 'RES-' || v_token || '-01');
    INSERT INTO LOGISTICS_ALLOCATIONS
        (REQUEST_ID, REQUEST_LINE_ITEM_ID, RESOURCE_TYPE, RESOURCE_REFERENCE,
         QUANTITY, STATUS, ALLOCATED_AT, RELEASED_AT, COMPLETED_AT, RESERVATION_REFERENCE)
    VALUES
        (v_request_2, v_line_2, 'SUPPLY', 'I-' || v_token || '-02',
         300, 'COMPLETED', LOCALTIMESTAMP, NULL, LOCALTIMESTAMP, 'RES-' || v_token || '-02');
    INSERT INTO LOGISTICS_ALLOCATIONS
        (REQUEST_ID, REQUEST_LINE_ITEM_ID, RESOURCE_TYPE, RESOURCE_REFERENCE,
         QUANTITY, STATUS, ALLOCATED_AT, RELEASED_AT, COMPLETED_AT, RESERVATION_REFERENCE)
    VALUES
        (v_request_3, v_line_3, 'SUPPLY', 'I-' || v_token || '-03',
         12, 'ALLOCATED', LOCALTIMESTAMP, NULL, NULL, 'RES-' || v_token || '-03');
    INSERT INTO LOGISTICS_ALLOCATIONS
        (REQUEST_ID, REQUEST_LINE_ITEM_ID, RESOURCE_TYPE, RESOURCE_REFERENCE,
         QUANTITY, STATUS, ALLOCATED_AT, RELEASED_AT, COMPLETED_AT, RESERVATION_REFERENCE)
    VALUES
        (v_request_4, v_line_4, 'SUPPLY', 'I-' || v_token || '-05',
         5, 'RELEASED', LOCALTIMESTAMP, LOCALTIMESTAMP, NULL, 'RES-' || v_token || '-04');
    INSERT INTO LOGISTICS_ALLOCATIONS
        (REQUEST_ID, REQUEST_LINE_ITEM_ID, RESOURCE_TYPE, RESOURCE_REFERENCE,
         QUANTITY, STATUS, ALLOCATED_AT, RELEASED_AT, COMPLETED_AT, RESERVATION_REFERENCE)
    VALUES
        (v_request_5, v_line_5, 'VEHICLE', 'RV-' || v_token || '-05',
         1, 'RELEASED', LOCALTIMESTAMP, LOCALTIMESTAMP, NULL, 'RES-' || v_token || '-05');

    -- Logistics service's singular VEHICLE entity/table (separate from resource-service VEHICLES).
    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into logistics-service VEHICLE...');
    INSERT INTO VEHICLE (VEHICLE_NAME, VEHICLE_TYPE, REGISTRATION_NUMBER, STATUS)
    VALUES ('Convoy Truck Alpha', 'Cargo Truck', 'LV-' || v_token || '-01', 'AVAILABLE')
    RETURNING ID INTO v_log_vehicle_1;
    INSERT INTO VEHICLE (VEHICLE_NAME, VEHICLE_TYPE, REGISTRATION_NUMBER, STATUS)
    VALUES ('Convoy Truck Bravo', 'Cargo Truck', 'LV-' || v_token || '-02', 'AVAILABLE')
    RETURNING ID INTO v_log_vehicle_2;
    INSERT INTO VEHICLE (VEHICLE_NAME, VEHICLE_TYPE, REGISTRATION_NUMBER, STATUS)
    VALUES ('Medical Transport Charlie', 'Ambulance', 'LV-' || v_token || '-03', 'AVAILABLE')
    RETURNING ID INTO v_log_vehicle_3;
    INSERT INTO VEHICLE (VEHICLE_NAME, VEHICLE_TYPE, REGISTRATION_NUMBER, STATUS)
    VALUES ('Fuel Carrier Delta', 'Fuel Tanker', 'LV-' || v_token || '-04', 'IN_SERVICE')
    RETURNING ID INTO v_log_vehicle_4;
    INSERT INTO VEHICLE (VEHICLE_NAME, VEHICLE_TYPE, REGISTRATION_NUMBER, STATUS)
    VALUES ('Utility Vehicle Echo', 'Utility Vehicle', 'LV-' || v_token || '-05', 'AVAILABLE')
    RETURNING ID INTO v_log_vehicle_5;

    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into LOGISTICS_SHIPMENTS...');
    INSERT INTO LOGISTICS_SHIPMENTS
        (SHIPMENT_NUMBER, ORIGIN, DESTINATION, CARGO_DESCRIPTION, STATUS, VEHICLE_ID)
    VALUES
        ('SHP-' || v_token || '-01', 'Central Supply Base', 'Northern Depot',
         'Field rations and water containers', 'PLANNED', v_log_vehicle_1)
    RETURNING ID INTO v_shipment_1;
    INSERT INTO LOGISTICS_SHIPMENTS
        (SHIPMENT_NUMBER, ORIGIN, DESTINATION, CARGO_DESCRIPTION, STATUS, VEHICLE_ID)
    VALUES
        ('SHP-' || v_token || '-02', 'Central Supply Base', 'Forward Support Point',
         'Diesel fuel for support convoy', 'IN_TRANSIT', v_log_vehicle_2)
    RETURNING ID INTO v_shipment_2;
    INSERT INTO LOGISTICS_SHIPMENTS
        (SHIPMENT_NUMBER, ORIGIN, DESTINATION, CARGO_DESCRIPTION, STATUS, VEHICLE_ID)
    VALUES
        ('SHP-' || v_token || '-03', 'Medical Stores', 'Forward Support Point',
         'Trauma dressing kits and medical supplies', 'DELIVERED', v_log_vehicle_3)
    RETURNING ID INTO v_shipment_3;
    INSERT INTO LOGISTICS_SHIPMENTS
        (SHIPMENT_NUMBER, ORIGIN, DESTINATION, CARGO_DESCRIPTION, STATUS, VEHICLE_ID)
    VALUES
        ('SHP-' || v_token || '-04', 'Northern Depot', 'Central Supply Base',
         'Returned serviceable communications equipment', 'CANCELLED', v_log_vehicle_4)
    RETURNING ID INTO v_shipment_4;
    INSERT INTO LOGISTICS_SHIPMENTS
        (SHIPMENT_NUMBER, ORIGIN, DESTINATION, CARGO_DESCRIPTION, STATUS, VEHICLE_ID)
    VALUES
        ('SHP-' || v_token || '-05', 'Central Supply Base', 'Northern Depot',
         'Portable power generator and repair components', 'PLANNED', v_log_vehicle_5)
    RETURNING ID INTO v_shipment_5;

    DBMS_OUTPUT.PUT_LINE('Inserting 5 rows into SHIPMENT_TRACKING...');
    INSERT INTO SHIPMENT_TRACKING
        (SHIPMENT_ID, LOCATION, STATUS, REMARKS, UPDATED_AT)
    VALUES
        (v_shipment_1, 'Central Supply Base', 'PLANNED',
         'Manifest checked; awaiting dispatch window', LOCALTIMESTAMP);
    INSERT INTO SHIPMENT_TRACKING
        (SHIPMENT_ID, LOCATION, STATUS, REMARKS, UPDATED_AT)
    VALUES
        (v_shipment_2, 'North Route Checkpoint 1', 'IN_TRANSIT',
         'Convoy departed under escort', LOCALTIMESTAMP);
    INSERT INTO SHIPMENT_TRACKING
        (SHIPMENT_ID, LOCATION, STATUS, REMARKS, UPDATED_AT)
    VALUES
        (v_shipment_3, 'Forward Support Point', 'DELIVERED',
         'Medical stores received and verified', LOCALTIMESTAMP);
    INSERT INTO SHIPMENT_TRACKING
        (SHIPMENT_ID, LOCATION, STATUS, REMARKS, UPDATED_AT)
    VALUES
        (v_shipment_4, 'Northern Depot', 'CANCELLED',
         'Return movement cancelled pending equipment inspection', LOCALTIMESTAMP);
    INSERT INTO SHIPMENT_TRACKING
        (SHIPMENT_ID, LOCATION, STATUS, REMARKS, UPDATED_AT)
    VALUES
        (v_shipment_5, 'Central Supply Base', 'PLANNED',
         'Loading team assigned for next scheduled movement', LOCALTIMESTAMP);

    DBMS_OUTPUT.PUT_LINE('Inserted 5 demo rows into each of the 12 mapped tables.');
    DBMS_OUTPUT.PUT_LINE('Demo data set: ' || v_token);
    END IF;
END;
/

COMMIT;
