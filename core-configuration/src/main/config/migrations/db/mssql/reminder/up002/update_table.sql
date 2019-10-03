exec sp_rename 'sb_reminder.trigger_durationProp', trigger_prop, 'COLUMN';

ALTER TABLE sb_reminder
    ADD process_name VARCHAR(200);

UPDATE sb_reminder
SET process_name = 'CalendarEventUserNotification'
WHERE sb_reminder.trigger_prop = 'NEXT_START_DATE_TIME';

ALTER TABLE sb_reminder
    ALTER COLUMN process_name VARCHAR(200) NOT NULL;