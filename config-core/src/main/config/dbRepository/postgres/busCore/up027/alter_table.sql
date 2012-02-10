ALTER TABLE sc_resources_managers ALTER COLUMN managerId TYPE BIGINT;
ALTER TABLE sc_resources_managers ALTER COLUMN resourceId TYPE BIGINT;

ALTER TABLE sc_resources_reservedresource ALTER COLUMN reservationId TYPE BIGINT;
ALTER TABLE sc_resources_reservedresource ALTER COLUMN resourceId TYPE BIGINT;

ALTER TABLE sc_resources_reservation ALTER COLUMN userId TYPE INTEGER;
ALTER TABLE sc_resources_reservation ALTER COLUMN id TYPE BIGINT;

ALTER TABLE sc_resources_resource ALTER COLUMN id TYPE BIGINT;
ALTER TABLE sc_resources_resource ALTER COLUMN responsibleid TYPE INTEGER;
ALTER TABLE sc_resources_resource ALTER COLUMN categoryid TYPE BIGINT;

ALTER TABLE sc_resources_category ALTER COLUMN id TYPE BIGINT;
ALTER TABLE sc_resources_category ALTER COLUMN responsibleid TYPE INTEGER;

UPDATE uniqueid SET TABLENAME='sc_resources_reservation' WHERE TABLENAME='SC_Resources_Reservation';
UPDATE uniqueid SET TABLENAME='sc_resources_resource' WHERE TABLENAME='SC_Resources_Resource';
UPDATE uniqueid SET TABLENAME='sc_resources_category' WHERE TABLENAME='SC_Resources_Category';