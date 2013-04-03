ALTER TABLE UniqueId WITH NOCHECK ADD 
	CONSTRAINT PK_UniqueId PRIMARY KEY  NONCLUSTERED 
	(
		tableName
	);

ALTER TABLE Personalization WITH NOCHECK ADD 
	CONSTRAINT PK_Personalization PRIMARY KEY  NONCLUSTERED 
	(
		id
	);

ALTER TABLE readingControl WITH NOCHECK ADD 
	CONSTRAINT PK_readingControl PRIMARY KEY  NONCLUSTERED 
	(
		pubId, actorId, space, componentName
	);

ALTER TABLE model_contact WITH NOCHECK ADD 
	CONSTRAINT PK_model_contact PRIMARY KEY  NONCLUSTERED 
	(
		id
	);

ALTER TABLE model WITH NOCHECK ADD 
	CONSTRAINT PK_model PRIMARY KEY  NONCLUSTERED 
	(
		id
	);

ALTER TABLE favorit WITH NOCHECK ADD 
	CONSTRAINT PK_favorit PRIMARY KEY  NONCLUSTERED 
	(
		actorId, nodeId, space, componentName
	);

ALTER TABLE calendarJournal WITH NOCHECK ADD 
	CONSTRAINT PK_CalendarJournal PRIMARY KEY  NONCLUSTERED 
	(
		id
	);

ALTER TABLE calendarCategory WITH NOCHECK ADD 
	CONSTRAINT PK_CalendarCategory PRIMARY KEY  NONCLUSTERED 
	(
		categoryId
	);

ALTER TABLE calendarJournalAttendee WITH NOCHECK ADD 
	CONSTRAINT PK_calendarJournalAttendee PRIMARY KEY  NONCLUSTERED 
	(
		journalId, userId
	);


ALTER TABLE calendarJournalCategory WITH NOCHECK ADD 
	CONSTRAINT PK_calendarJournalCategory PRIMARY KEY  NONCLUSTERED 
	(
		journalId, categoryId
	);

ALTER TABLE calendarToDo WITH NOCHECK ADD 
	CONSTRAINT PK_calendarToDo PRIMARY KEY  NONCLUSTERED 
	(
		id
	);

ALTER TABLE calendarToDoAttendee WITH NOCHECK ADD 
	CONSTRAINT PK_calendarToDoAttendee PRIMARY KEY  NONCLUSTERED 
	(
		todoId, userId
	);

ALTER TABLE ST_FormDesigner_FormDesign WITH NOCHECK ADD 
	CONSTRAINT PK_ST_FormDesigner_FormDesign PRIMARY KEY  NONCLUSTERED 
	(
		ID
	);

ALTER TABLE ST_FormDesigner_Connectors WITH NOCHECK ADD 
	CONSTRAINT PK_ST_FormDesigner_Connectors PRIMARY KEY  NONCLUSTERED 
	(
		ID
	);

ALTER TABLE ST_FormEditor_FormEdited WITH NOCHECK ADD 
	CONSTRAINT PK_ST_FormEditor_FormEdited PRIMARY KEY  NONCLUSTERED 
	(
		ID
	);

ALTER TABLE ST_FormEditor_FormEditedData WITH NOCHECK ADD 
	CONSTRAINT PK_ST_FormEditor_FED PRIMARY KEY  NONCLUSTERED 
	(
		ID
	);

ALTER TABLE sb_agenda_import_settings WITH NOCHECK ADD 
	CONSTRAINT PK_sb_agenda_import_settings_SET PRIMARY KEY  NONCLUSTERED 
	(
		userid
	);


