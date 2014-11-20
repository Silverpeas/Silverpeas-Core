CREATE TABLE IF NOT EXISTS UniqueId (
  maxId     INT          NOT NULL,
  tableName VARCHAR(100) NOT NULL
);

-- Space

CREATE TABLE ST_Space(
  id                   INT             NOT NULL,
  domainFatherId       INT,
  name                 VARCHAR(100)    NOT NULL,
  description          VARCHAR(400),
  createdBy            INT,
  firstPageType        INT             NOT NULL,
  firstPageExtraParam  VARCHAR(400),
  orderNum             INT DEFAULT (0) NOT NULL,
  createTime           VARCHAR(20),
  updateTime           VARCHAR(20),
  removeTime           VARCHAR(20),
  spaceStatus          CHAR(1),
  updatedBy            INT,
  removedBy            INT,
  lang                 CHAR(2),
  isInheritanceBlocked INT DEFAULT (0) NOT NULL,
  look                 VARCHAR(50),
  displaySpaceFirst    SMALLINT,
  isPersonal           SMALLINT
);
ALTER TABLE ST_Space ADD CONSTRAINT PK_Space PRIMARY KEY (id);
ALTER TABLE ST_Space ADD CONSTRAINT UN_Space_1 UNIQUE(domainFatherId, name);
ALTER TABLE ST_Space ADD CONSTRAINT FK_Space_1 FOREIGN KEY (createdBy) REFERENCES ST_User(id);
ALTER TABLE ST_Space ADD CONSTRAINT FK_Space_2 FOREIGN KEY (domainFatherId) REFERENCES ST_Space(id);

CREATE TABLE ST_SpaceI18N(
  id          INT          NOT NULL,
  spaceId     INT          NOT NULL,
  lang        CHAR(2)      NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(400)
);

-- Component

CREATE TABLE ST_Component (
  id            INT          NOT NULL,
  componentName VARCHAR(100) NOT NULL,
  description   VARCHAR(400) NULL
);
ALTER TABLE ST_Component ADD CONSTRAINT PK_Component PRIMARY KEY(id);
ALTER TABLE ST_Component ADD CONSTRAINT UN_ST_Component_1 UNIQUE(componentName);

CREATE TABLE ST_ComponentInstance(
  id                   INT              NOT NULL,
  spaceId              INT              NOT NULL,
  name                 VARCHAR(100)     NOT NULL,
  componentName        VARCHAR(100)     NOT NULL,
  description          VARCHAR(400),
  createdBy            INT,
  orderNum             INT DEFAULT (0)  NOT NULL,
  createTime           VARCHAR(20),
  updateTime           VARCHAR(20),
  removeTime           VARCHAR(20),
  componentStatus      CHAR(1),
  updatedBy            INT,
  removedBy            INT,
  isPublic             INT DEFAULT (0)  NOT NULL,
  isHidden             INT DEFAULT (0)  NOT NULL,
  lang                 CHAR(2),
  isInheritanceBlocked INT DEFAULT (0)  NOT NULL
);

CREATE TABLE ST_ComponentInstanceI18N(
  id          INT          NOT NULL,
  componentId INT          NOT NULL,
  lang        CHAR(2)      NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description VARCHAR(400)
);

CREATE TABLE ST_Instance_Data(
  id          INT          NOT NULL,
  componentId INT          NOT NULL,
  name        VARCHAR(100) NOT NULL,
  label       VARCHAR(100) NOT NULL,
  value       VARCHAR(400)
);