ALTER TABLE UniqueId DROP CONSTRAINT PK_UniqueId;
ALTER TABLE Personalization DROP CONSTRAINT PK_Personalization;
ALTER TABLE readingControl DROP	CONSTRAINT PK_readingControl;
ALTER TABLE subscribe DROP CONSTRAINT PK_subscribe;
ALTER TABLE model_contact DROP CONSTRAINT PK_model_contact;
ALTER TABLE model DROP CONSTRAINT PK_model;
ALTER TABLE favorit DROP CONSTRAINT PK_favorit;

ALTER TABLE calendarJournal DROP CONSTRAINT PK_CalendarJournal;
ALTER TABLE calendarCategory DROP CONSTRAINT PK_CalendarCategory;
ALTER TABLE calendarJournalAttendee DROP CONSTRAINT PK_calendarJournalAttendee;
ALTER TABLE calendarJournalCategory DROP CONSTRAINT PK_calendarJournalCategory;

ALTER TABLE calendarToDo DROP CONSTRAINT PK_calendarToDo;
ALTER TABLE calendarToDoAttendee DROP CONSTRAINT PK_calendarToDoAttendee;

ALTER TABLE ST_FormEditor_FormEdited DROP CONSTRAINT PK_ST_FormEditor_FormEdited;
ALTER TABLE ST_FormEditor_FormEditedData DROP CONSTRAINT PK_ST_FormEditor_FED;

ALTER TABLE sb_agenda_import_settings DROP CONSTRAINT PK_sb_agenda_import_settings_SET;



