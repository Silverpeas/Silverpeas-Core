CREATE TABLE SB_Cal_Calendar (
  id             VARCHAR(40)   NOT NULL,
  instanceId     VARCHAR(30)   NOT NULL,
  title          VARCHAR(255)  NOT NULL,
  zoneId         VARCHAR(40)   NOT NULL,
  externalUrl    VARCHAR(250),
  synchroDate    TIMESTAMP,
  createDate     TIMESTAMP     NOT NULL,
  createdBy      VARCHAR(40)   NOT NULL,
  lastUpdateDate TIMESTAMP     NOT NULL,
  lastUpdatedBy  VARCHAR(40)   NOT NULL,
  version        INT8          NOT NULL,
  CONSTRAINT PK_CALENDAR PRIMARY KEY (id)
);

CREATE TABLE SB_Cal_Recurrence (
  id                   VARCHAR(40)  NOT NULL,
  recur_periodInterval INT          NOT NULL,
  recur_periodUnit     VARCHAR(5)   NOT NULL,
  recur_count          INT          DEFAULT 0,
  recur_endDate        TIMESTAMP,
  CONSTRAINT PK_RECURRENCE PRIMARY KEY (id)
);

CREATE TABLE SB_Cal_Recurrence_DayOfWeek (
  recurrenceId    VARCHAR(40) NOT NULL,
  recur_nth       INT         NOT NULL,
  recur_dayOfWeek INT         NOT NULL,
  CONSTRAINT FK_Recurrence_DayOfWeek FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence(id)
);

CREATE TABLE SB_Cal_Recurrence_Exception (
  recurrenceId        VARCHAR(40) NOT NULL,
  recur_exceptionDate TIMESTAMP   NOT NULL,
  CONSTRAINT FK_Recurrence_Exception FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence(id)
);

CREATE TABLE SB_Cal_Components (
  id             VARCHAR(40)   NOT NULL,
  calendarId     VARCHAR(40)   NOT NULL,
  startDate      TIMESTAMP     NOT NULL,
  endDate        TIMESTAMP     NOT NULL,
  inDays         BOOLEAN       NOT NULL,
  title          VARCHAR(255)  NOT NULL,
  description    VARCHAR(2000) NOT NULL,
  location       VARCHAR(255)  NULL,
  attributes     VARCHAR(40)   NULL,
  priority       INT           NOT NULL,
  sequence       INT8          NOT NULL DEFAULT 0,
  createDate     TIMESTAMP     NOT NULL,
  createdBy      VARCHAR(40)   NOT NULL,
  lastUpdateDate TIMESTAMP     NOT NULL,
  lastUpdatedBy  VARCHAR(40)   NOT NULL,
  version        INT8          NOT NULL,
  CONSTRAINT PK_CalComponent PRIMARY KEY (id),
  CONSTRAINT FK_Calendar     FOREIGN KEY (calendarId) REFERENCES SB_Cal_Calendar(id)
);

CREATE TABLE SB_Cal_Event (
  id             VARCHAR(40)   NOT NULL,
  externalId     VARCHAR(255)  NULL,
  synchroDate    TIMESTAMP,
  componentId    VARCHAR(40)   NOT NULL,
  visibility     VARCHAR(50)   NOT NULL,
  recurrenceId   VARCHAR(40)   NULL,
  CONSTRAINT PK_Event PRIMARY KEY (id),
  CONSTRAINT FK_Event_Component  FOREIGN KEY (componentId)  REFERENCES SB_Cal_Components(id),
  CONSTRAINT FK_Event_Recurrence FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence(id)
);

CREATE TABLE SB_Cal_Occurrences (
  id             VARCHAR(60)   NOT NULL,
  eventId        VARCHAR(40)   NOT NULL,
  componentId    VARCHAR(40)   NOT NULL,
  CONSTRAINT PK_Occurrence           PRIMARY KEY (id),
  CONSTRAINT FK_Occurrence_Event     FOREIGN KEY (eventId)     REFERENCES SB_Cal_Event,
  CONSTRAINT FK_Occurrence_Component FOREIGN KEY (componentId) REFERENCES SB_Cal_Components(id)
);

CREATE TABLE SB_Cal_Attributes (
  id         VARCHAR(40)  NOT NULL,
  name       VARCHAR(255) NOT NULL,
  "value"      VARCHAR(255) NOT NULL,
  CONSTRAINT PK_Attributes PRIMARY KEY (id, name)
);

CREATE TABLE SB_Cal_Categories (
  id       VARCHAR(40) NOT NULL,
  category VARCHAR(255) NOT NULL,
  CONSTRAINT Pk_Categories PRIMARY KEY (id, category)
);

CREATE TABLE SB_Cal_Attendees (
  id                VARCHAR(40) NOT NULL,
  attendeeId        VARCHAR(40) NOT NULL,
  componentId       VARCHAR(40) NOT NULL,
  type              INT         NOT NULL,
  participation     VARCHAR(12) NOT NULL DEFAULT 'AWAITING',
  presence          VARCHAR(12) NOT NULL DEFAULT 'REQUIRED',
  delegate          VARCHAR(40) NULL,
  createDate        TIMESTAMP   NOT NULL,
  createdBy         VARCHAR(40) NOT NULL,
  lastUpdateDate    TIMESTAMP   NOT NULL,
  lastUpdatedBy     VARCHAR(40) NOT NULL,
  version           INT8        NOT NULL,
  CONSTRAINT PK_Attendee PRIMARY KEY (id),
  CONSTRAINT FK_Attendee_Component FOREIGN KEY (componentId) REFERENCES SB_Cal_Components(id),
  CONSTRAINT FK_Delegate FOREIGN KEY (delegate) REFERENCES SB_Cal_Attendees(id)
);
