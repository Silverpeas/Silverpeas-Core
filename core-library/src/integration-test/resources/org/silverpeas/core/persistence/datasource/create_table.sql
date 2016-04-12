CREATE TABLE IF NOT EXISTS test_persons (
  id             VARCHAR(40) PRIMARY KEY NOT NULL,
  firstName      VARCHAR(30)             NOT NULL,
  lastName       VARCHAR(30)             NOT NULL,
  createDate     TIMESTAMP               NOT NULL,
  createdBy      VARCHAR(40)             NOT NULL,
  lastUpdateDate TIMESTAMP               NOT NULL,
  lastUpdatedBy  VARCHAR(40)             NOT NULL,
  version        INT8                    NOT NULL
);

CREATE TABLE IF NOT EXISTS test_animals (
  id             INT8 PRIMARY KEY NOT NULL,
  personId       VARCHAR(40)      NOT NULL,
  type           VARCHAR(10)      NOT NULL,
  name           VARCHAR(30)      NOT NULL,
  createDate     TIMESTAMP        NOT NULL,
  createdBy      VARCHAR(40)      NOT NULL,
  lastUpdateDate TIMESTAMP        NOT NULL,
  lastUpdatedBy  VARCHAR(40)      NOT NULL,
  version        INT8             NOT NULL
);

CREATE TABLE IF NOT EXISTS test_equipments (
  id             VARCHAR(40) PRIMARY KEY NOT NULL,
  animalId       INT8                    NOT NULL,
  name           VARCHAR(30)             NOT NULL,
  createDate     TIMESTAMP               NOT NULL,
  createdBy      VARCHAR(40)             NOT NULL,
  lastUpdateDate TIMESTAMP               NOT NULL,
  lastUpdatedBy  VARCHAR(40)             NOT NULL,
  version        INT8                    NOT NULL
);