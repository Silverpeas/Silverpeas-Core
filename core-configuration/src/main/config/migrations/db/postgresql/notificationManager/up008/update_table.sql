ALTER TABLE st_delayednotification ADD COLUMN message2 character varying(10000);
ALTER TABLE st_delayednotification DROP COLUMN message;
ALTER TABLE st_delayednotification RENAME COLUMN message2 to message;
