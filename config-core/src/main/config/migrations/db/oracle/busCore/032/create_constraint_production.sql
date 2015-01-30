ALTER TABLE UniqueId  ADD
	CONSTRAINT PK_UniqueId PRIMARY KEY
	(
		tableName
	);

ALTER TABLE Personalization  ADD
	CONSTRAINT PK_Personalization PRIMARY KEY
	(
		id
	);

ALTER TABLE readingControl  ADD
	CONSTRAINT PK_readingControl PRIMARY KEY
	(
		pubId, actorId, space, componentName
	);

ALTER TABLE model_contact  ADD
	CONSTRAINT PK_model_contact PRIMARY KEY
	(
		id
	);

ALTER TABLE model  ADD
	CONSTRAINT PK_model PRIMARY KEY
	(
		id
	);

ALTER TABLE favorit  ADD
	CONSTRAINT PK_favorit PRIMARY KEY
	(
		actorId, nodeId, space, componentName
	);

ALTER TABLE calendarJournal  ADD
	CONSTRAINT PK_CalendarJournal PRIMARY KEY
	(
		id
	);

ALTER TABLE calendarCategory  ADD
	CONSTRAINT PK_CalendarCategory PRIMARY KEY
	(
		categoryId
	);

ALTER TABLE calendarJournalAttendee  ADD
	CONSTRAINT PK_calendarJournalAttendee PRIMARY KEY
	(
		journalId, userId
	);


ALTER TABLE calendarJournalCategory  ADD
	CONSTRAINT PK_calendarJournalCategory PRIMARY KEY
	(
		journalId, categoryId
	);

ALTER TABLE calendarToDo  ADD
	CONSTRAINT PK_calendarToDo PRIMARY KEY
	(
		id
	);

ALTER TABLE calendarToDoAttendee  ADD
	CONSTRAINT PK_calendarToDoAttendee PRIMARY KEY
	(
		todoId, userId
	);

ALTER TABLE ST_FormDesigner_FormDesign  ADD
	CONSTRAINT PK_ST_FormDesigner_FormDesign PRIMARY KEY
	(
		ID
	);

ALTER TABLE ST_FormDesigner_Connectors  ADD
	CONSTRAINT PK_ST_FormDesigner_Connectors PRIMARY KEY
	(
		ID
	);

ALTER TABLE ST_FormEditor_FormEdited  ADD
	CONSTRAINT PK_ST_FormEditor_FormEdited PRIMARY KEY
	(
		ID
	);

ALTER TABLE ST_FormEditor_FormEditedData  ADD
	CONSTRAINT PK_ST_FormEditor_FED PRIMARY KEY
	(
		ID
	);

ALTER TABLE sb_agenda_import_settings ADD
	CONSTRAINT PK_sb_agenda_import_settings PRIMARY KEY
	(
		userid
	);
