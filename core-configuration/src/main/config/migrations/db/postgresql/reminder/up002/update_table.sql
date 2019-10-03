ALTER TABLE sb_reminder
    RENAME trigger_durationprop TO trigger_prop;

ALTER TABLE sb_reminder
    ADD COLUMN process_name VARCHAR(200);

UPDATE sb_reminder
SET process_name = 'CalendarEventUserNotification'
WHERE sb_reminder.trigger_prop = 'NEXT_START_DATE_TIME';

ALTER TABLE sb_reminder
    ALTER COLUMN process_name SET NOT NULL;