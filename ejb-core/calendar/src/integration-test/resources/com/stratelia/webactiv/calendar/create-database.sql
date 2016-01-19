CREATE TABLE calendarJournal (
  id               INT           NOT NULL,
  name             VARCHAR(2000) NOT NULL,
  description      VARCHAR(4000) NULL,
  delegatorId      VARCHAR(100)  NOT NULL,
  startDay         VARCHAR(50)   NOT NULL,
  endDay           VARCHAR(50)   NULL,
  startHour        VARCHAR(50)   NULL,
  endHour          VARCHAR(50)   NULL,
  classification   VARCHAR(20)   NULL,
  priority         INT           NULL,
  lastModification VARCHAR(50)   NULL,
  externalid       VARCHAR(50)   NULL
);

CREATE TABLE calendarToDo (
  id               INT           NOT NULL,
  name             VARCHAR(2000) NOT NULL,
  description      VARCHAR(4000) NULL,
  delegatorId      VARCHAR(100)  NOT NULL,
  startDay         VARCHAR(50)   NULL,
  endDay           VARCHAR(50)   NULL,
  startHour        VARCHAR(50)   NULL,
  endHour          VARCHAR(50)   NULL,
  classification   VARCHAR(20)   NULL,
  priority         INT           NULL,
  lastModification VARCHAR(50)   NULL,
  percentCompleted INT           NULL,
  completedDay     VARCHAR(20)   NULL,
  duration         INT           NULL,
  componentId      VARCHAR(100)  NULL,
  spaceId          VARCHAR(100)  NULL,
  externalId       VARCHAR(100)  NULL
);


CREATE TABLE calendarToDoAttendee (
  todoId              INT          NOT NULL,
  userId              VARCHAR(100) NOT NULL,
  participationStatus VARCHAR(50)  NULL
);