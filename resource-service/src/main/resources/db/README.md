# Resource Service Oracle schema scripts

## Purpose

`schema.sql` is a manually executed Oracle Database 21c XE PL/SQL script for the Personnel, Asset, Inventory, and Maintenance tables and sequences mapped by the Java entities. It checks the current schema catalog and creates each missing object; it does not alter, drop, truncate, or replace an existing table or sequence.

Spring Boot must continue to use `spring.jpa.hibernate.ddl-auto=none`. The application does not automatically execute this script.

## Tables and sequences

| Module | Table | Sequence |
| --- | --- | --- |
| Vehicle | `VEHICLES` (already exists) | `VEHICLE_SEQ` (already exists) |
| Personnel | `PERSONNEL` | `PERSONNEL_SEQ` |
| Asset | `ASSETS` | `ASSET_SEQ` |
| Inventory item | `INVENTORY_ITEMS` | `INVENTORY_ITEM_SEQ` |
| Inventory transaction | `INVENTORY_TRANSACTIONS` | `INVENTORY_TRANSACTION_SEQ` |
| Maintenance | `MAINTENANCE_RECORDS` | `MAINTENANCE_SEQ` |

The script does not contain create, alter, or drop statements targeting `VEHICLES` or `VEHICLE_SEQ`. It assumes these existing objects are to be preserved:

- `VEHICLES.VEHICLE_ID NUMBER(19)` primary key
- `VEHICLE_NUMBER VARCHAR2(30) NOT NULL UNIQUE`
- `VEHICLE_TYPE VARCHAR2(50) NOT NULL`
- `CAPACITY NUMBER(10) NOT NULL`, constrained to be greater than zero
- `UNIT_ID NUMBER(19)` nullable
- `LOCATION_CODE VARCHAR2(50)` nullable
- `CONDITION_STATUS VARCHAR2(30) NOT NULL`
- `STATUS VARCHAR2(30) DEFAULT 'AVAILABLE' NOT NULL`
- `CREATED_AT TIMESTAMP NOT NULL`
- `UPDATED_AT TIMESTAMP NOT NULL`
- Existing sequence: `VEHICLE_SEQ`

The script creates new table/sequence objects only for the modules mapped by source files found during this task. Existing non-Vehicle tables are also skipped without schema changes; if their schema differs from the Java mappings, reconcile that separately after review and approval rather than relying on this create-if-absent script to migrate them.

Inventory `quantityAvailable` is calculated by Java as `quantityOnHand - quantityReserved` and is not stored as a column. `INVENTORY_TRANSACTIONS.ITEM_ID` references `INVENTORY_ITEMS.ITEM_ID` without cascade-delete behavior. Maintenance stores a polymorphic `RESOURCE_TYPE` and `RESOURCE_ID`; it has no foreign key to Vehicle or Asset.

## Inspect before execution

Connect as the intended application schema owner and inspect the current objects before running anything:

```sql
SELECT table_name
FROM user_tables
WHERE table_name IN (
    'VEHICLES', 'PERSONNEL', 'ASSETS', 'INVENTORY_ITEMS',
    'INVENTORY_TRANSACTIONS', 'MAINTENANCE_RECORDS'
)
ORDER BY table_name;

SELECT sequence_name
FROM user_sequences
WHERE sequence_name IN (
    'VEHICLE_SEQ', 'PERSONNEL_SEQ', 'ASSET_SEQ',
    'INVENTORY_ITEM_SEQ', 'INVENTORY_TRANSACTION_SEQ', 'MAINTENANCE_SEQ'
)
ORDER BY sequence_name;

SELECT table_name, column_name, data_type, data_length, data_precision,
       data_scale, nullable, data_default
FROM user_tab_columns
WHERE table_name IN (
    'VEHICLES', 'PERSONNEL', 'ASSETS', 'INVENTORY_ITEMS',
    'INVENTORY_TRANSACTIONS', 'MAINTENANCE_RECORDS'
)
ORDER BY table_name, column_id;

SELECT table_name, constraint_name, constraint_type, status
FROM user_constraints
WHERE table_name IN (
    'VEHICLES', 'PERSONNEL', 'ASSETS', 'INVENTORY_ITEMS',
    'INVENTORY_TRANSACTIONS', 'MAINTENANCE_RECORDS'
)
ORDER BY table_name, constraint_name;
```

Compare the results with the entity `@Table`, `@Column`, `@SequenceGenerator`, and relationship annotations before proceeding. Check the connected schema/user; `USER_*` views show only that user's objects. For objects owned by another schema, use the corresponding `ALL_*` views and confirm privileges and intended owner first.

## Manual execution

1. Back up and inspect the target schema using your normal database change process.
2. Confirm the SQL Developer/SQL*Plus connection is to the intended Oracle 21c XE service/schema.
3. Review `schema.sql` completely; this script is not a migration tool and does not modify existing table definitions.
4. In Oracle SQL Developer, open `schema.sql` and use **Run Script** (F5), not statement execution. In SQL*Plus, run the file with `@src/main/resources/db/schema.sql` from the project root (or provide the correct absolute path). `SET SERVEROUTPUT ON` displays which objects the block created or skipped.
5. Re-inspect tables, sequences, constraints, and indexes after manual execution.

The script checks Oracle catalog views and creates missing objects dynamically instead of using unsupported `CREATE TABLE IF NOT EXISTS` syntax. If it finds a table, it skips that table rather than attempting to alter or repair it. For a missing sequence corresponding to an existing table, it starts the new sequence above that table's current maximum ID; an existing sequence is never reset. Run this only in a controlled, single-operator schema change window to avoid concurrent object-creation races.

## Case-insensitive unique values

Personnel codes, asset tags, and inventory item codes are protected by Oracle function-based unique indexes on `UPPER(column)`. This enforces case-insensitive uniqueness in the database while preserving the original stored text. The Java entities intentionally do not declare raw-column `unique=true` constraints for these three fields; the case-insensitive indexes are the authoritative unique constraints. Service-level duplicate prechecks remain in place for readable conflict responses, while the indexes provide protection against concurrent duplicate inserts.

Before creating these indexes on an existing database, find and manually resolve any existing case-only duplicates. Do not delete or rewrite records automatically. Run the checks and review all returned rows before applying a separately approved correction:

```sql
SELECT UPPER(PERSONNEL_CODE) AS NORMALIZED_CODE, COUNT(*) AS ROW_COUNT
FROM PERSONNEL
GROUP BY UPPER(PERSONNEL_CODE)
HAVING COUNT(*) > 1;

SELECT UPPER(ASSET_TAG) AS NORMALIZED_TAG, COUNT(*) AS ROW_COUNT
FROM ASSETS
GROUP BY UPPER(ASSET_TAG)
HAVING COUNT(*) > 1;

SELECT UPPER(ITEM_CODE) AS NORMALIZED_CODE, COUNT(*) AS ROW_COUNT
FROM INVENTORY_ITEMS
GROUP BY UPPER(ITEM_CODE)
HAVING COUNT(*) > 1;
```

The schema script creates the named function-based indexes only when those index names are absent. It does not automatically remove any pre-existing raw unique constraints or indexes. Such existing constraints may be redundant; inspect them and handle any removal through a separate reviewed migration. If case-only duplicates remain, Oracle will reject creation of the corresponding function-based unique index; resolve those records manually before rerunning the relevant index statement/script.

## Mapping and preservation notes

The table and sequence names must remain aligned with the Java entity mappings:

- `Vehicle`: `VEHICLES` / `VEHICLE_SEQ`
- `Personnel`: `PERSONNEL` / `PERSONNEL_SEQ`
- `Asset`: `ASSETS` / `ASSET_SEQ`
- `InventoryItem`: `INVENTORY_ITEMS` / `INVENTORY_ITEM_SEQ`
- `InventoryTransaction`: `INVENTORY_TRANSACTIONS` / `INVENTORY_TRANSACTION_SEQ`
- `MaintenanceRecord`: `MAINTENANCE_RECORDS` / `MAINTENANCE_SEQ`

The existing `VEHICLES` table and `VEHICLE_SEQ` must not be recreated, reset, or otherwise modified by this script. Do not enable automatic production schema creation in Hibernate.

No SQL was executed as part of this task. The file has not been tested against a live Oracle instance; review and validate it in a controlled environment before use.
