CREATE TABLE SB_Node_Node
(
    nodeId           int              NOT NULL,
    nodeName         varchar(1000)    NOT NULL,
    nodeDescription  varchar(2000)    NULL,
    nodeCreationDate varchar(10)      NOT NULL,
    nodeCreatorId    varchar(100)     NOT NULL,
    nodePath         varchar(1000)    NOT NULL,
    nodeLevelNumber  int              NOT NULL,
    nodeFatherId     int              NOT NULL,
    modelId          varchar(1000)    NULL,
    nodeStatus       varchar(1000)    NULL,
    instanceId       varchar(50)      NOT NULL,
    type             varchar(50)      NULL,
    orderNumber      int DEFAULT (0)  NULL,
    lang             char(2),
    rightsDependsOn  int default (-1) NOT NULL,
    constraint PK_Node_Node primary key (nodeId, instanceId)
);

CREATE TABLE SB_Publication_Publi
(
    pubId                int           NOT NULL,
    infoId               varchar(50)   NULL,
    pubName              varchar(400)  NOT NULL,
    pubDescription       varchar(2000) NULL,
    pubCreationDate      varchar(10)   NOT NULL,
    pubBeginDate         varchar(10)   NOT NULL,
    pubEndDate           varchar(10)   NOT NULL,
    pubCreatorId         varchar(100)  NOT NULL,
    pubImportance        int           NULL,
    pubVersion           varchar(100)  NULL,
    pubKeywords          varchar(1000) NULL,
    pubContent           varchar(2000) NULL,
    pubStatus            varchar(100)  NULL,
    pubUpdateDate        varchar(10)   NULL,
    instanceId           varchar(50)   NOT NULL,
    pubUpdaterId         varchar(100)  NULL,
    pubValidateDate      varchar(10)   NULL,
    pubValidatorId       varchar(50)   NULL,
    pubBeginHour         varchar(5)    NULL,
    pubEndHour           varchar(5)    NULL,
    pubAuthor            varchar(50)   NULL,
    pubTargetValidatorId varchar(50)   NULL,
    pubCloneId           int DEFAULT (-1),
    pubCloneStatus       varchar(50)   NULL,
    lang                 char(2)       NULL,
    pubdraftoutdate      varchar(10)   NULL,
    constraint PK_Publication_Publi primary key (pubId)
);

CREATE TABLE SB_Publication_PubliFather
(
    pubId       int             NOT NULL,
    nodeId      int             NOT NULL,
    instanceId  varchar(50)     NOT NULL,
    aliasUserId int,
    aliasDate   varchar(20),
    pubOrder    int DEFAULT (0) NULL,
    constraint PK_Publication_PubliFather primary key (pubId, nodeId, instanceId)
);

CREATE TABLE SB_Publication_PubliI18N
(
    id          int          NOT NULL,
    pubId       int          NOT NULL,
    lang        char(2)      NOT NULL,
    name        varchar(400) NOT NULL,
    description varchar(2000),
    keywords    varchar(1000),
    CONSTRAINT PK_Publication_PubliI18N PRIMARY KEY (id)
);

CREATE TABLE SB_Thumbnail_Thumbnail
(
    instanceId             varchar(50)  NOT NULL,
    objectId               int          NOT NULL,
    objectType             int          NOT NULL,
    originalAttachmentName varchar(250) NOT NULL,
    modifiedAttachmentName varchar(250) NULL,
    mimeType               varchar(250) NULL,
    xStart                 int          NULL,
    yStart                 int          NULL,
    xLength                int          NULL,
    yLength                int          NULL,
    CONSTRAINT PK_Thumbnail_Thumbnail PRIMARY KEY (objectId, objectType, instanceId)
);

-- Calendar API

CREATE TABLE IF NOT EXISTS SB_Cal_Calendar
(
    id             VARCHAR(40)  NOT NULL,
    instanceId     VARCHAR(30)  NOT NULL,
    title          VARCHAR(255) NOT NULL,
    zoneId         VARCHAR(40)  NOT NULL,
    externalUrl    VARCHAR(250),
    synchroDate    TIMESTAMP,
    createDate     TIMESTAMP    NOT NULL,
    createdBy      VARCHAR(40)  NOT NULL,
    lastUpdateDate TIMESTAMP    NOT NULL,
    lastUpdatedBy  VARCHAR(40)  NOT NULL,
    version        INT8         NOT NULL,
    CONSTRAINT PK_CALENDAR PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Recurrence
(
    id                   VARCHAR(40) NOT NULL,
    recur_periodInterval INT         NOT NULL,
    recur_periodUnit     VARCHAR(5)  NOT NULL,
    recur_count          INT,
    recur_endDate        TIMESTAMP,
    CONSTRAINT PK_RECURRENCE PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Recurrence_DayOfWeek
(
    recurrenceId    VARCHAR(40) NOT NULL,
    recur_nth       INT         NOT NULL,
    recur_dayOfWeek INT         NOT NULL,
    CONSTRAINT FK_Recurrence_DayOfWeek FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence (id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Recurrence_Exception
(
    recurrenceId        VARCHAR(40) NOT NULL,
    recur_exceptionDate TIMESTAMP   NOT NULL,
    CONSTRAINT FK_Recurrence_Exception FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence (id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Components
(
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
    CONSTRAINT FK_Calendar FOREIGN KEY (calendarId) REFERENCES SB_Cal_Calendar (id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Event
(
    id           VARCHAR(40)  NOT NULL,
    externalId   VARCHAR(255) NULL,
    synchroDate  TIMESTAMP,
    componentId  VARCHAR(40)  NOT NULL,
    visibility   VARCHAR(50)  NOT NULL,
    recurrenceId VARCHAR(40)  NULL,
    CONSTRAINT PK_Event PRIMARY KEY (id),
    CONSTRAINT FK_Event_Component FOREIGN KEY (componentId) REFERENCES SB_Cal_Components (id),
    CONSTRAINT FK_Event_Recurrence FOREIGN KEY (recurrenceId) REFERENCES SB_Cal_Recurrence (id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Occurrences
(
    id          VARCHAR(60) NOT NULL,
    eventId     VARCHAR(40) NOT NULL,
    componentId VARCHAR(40) NOT NULL,
    CONSTRAINT PK_Occurrence PRIMARY KEY (id),
    CONSTRAINT FK_Occurrence_Event FOREIGN KEY (eventId) REFERENCES SB_Cal_Event,
    CONSTRAINT FK_Occurrence_Component FOREIGN KEY (componentId) REFERENCES SB_Cal_Components (id)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Attributes
(
    id    VARCHAR(40)  NOT NULL,
    name  VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    CONSTRAINT PK_Attributes PRIMARY KEY (id, name)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Categories
(
    id       VARCHAR(40)  NOT NULL,
    category VARCHAR(255) NOT NULL,
    CONSTRAINT Pk_Categories PRIMARY KEY (id, category)
);

CREATE TABLE IF NOT EXISTS SB_Cal_Attendees
(
    id             VARCHAR(40) NOT NULL,
    attendeeId     VARCHAR(40) NOT NULL,
    componentId    VARCHAR(40) NOT NULL,
    type           INT         NOT NULL,
    participation  VARCHAR(12) NOT NULL DEFAULT 'AWAITING',
    presence       VARCHAR(12) NOT NULL DEFAULT 'REQUIRED',
    delegate       VARCHAR(40) NULL,
    createDate     TIMESTAMP   NOT NULL,
    createdBy      VARCHAR(40) NOT NULL,
    lastUpdateDate TIMESTAMP   NOT NULL,
    lastUpdatedBy  VARCHAR(40) NOT NULL,
    version        INT8        NOT NULL,
    CONSTRAINT PK_Attendee PRIMARY KEY (id),
    CONSTRAINT FK_Attendee_Component FOREIGN KEY (componentId) REFERENCES SB_Cal_Components (id),
    CONSTRAINT FK_Delegate FOREIGN KEY (delegate) REFERENCES SB_Cal_Attendees (id)
);