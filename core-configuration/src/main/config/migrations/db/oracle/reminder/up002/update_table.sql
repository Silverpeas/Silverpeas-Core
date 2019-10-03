ALTER TABLE sb_reminder
    RENAME COLUMN trigger_durationprop TO trigger_prop;

ALTER TABLE sb_reminder
    ADD process_name VARCHAR(200);

UPDATE sb_reminder
SET process_name = 'CalendarEventUserNotification'
WHERE sb_reminder.trigger_prop = 'NEXT_START_DATE_TIME';

ALTER TABLE sb_reminder
    MODIFY process_name NOT NULL;