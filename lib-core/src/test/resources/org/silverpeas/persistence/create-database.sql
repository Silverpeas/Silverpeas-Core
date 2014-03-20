/* Tables */
CREATE TABLE test_persons (
  id             VARCHAR(40) NOT NULL,
  firstName      VARCHAR(30) NOT NULL,
  lastName       VARCHAR(30) NOT NULL,
  createDate     TIMESTAMP   NOT NULL,
  createdBy      VARCHAR(40) NOT NULL,
  lastUpdateDate TIMESTAMP   NOT NULL,
  lastUpdatedBy  VARCHAR(40) NOT NULL,
  version        INT8        NOT NULL
);

/* Constraints */
ALTER TABLE test_persons ADD CONSTRAINT const_test_persons_pk PRIMARY KEY (id);

/* Tables */
CREATE TABLE test_animals (
  id             INT8        NOT NULL,
  personId       VARCHAR(40) NOT NULL,
  type           VARCHAR(10) NOT NULL,
  name           VARCHAR(30) NOT NULL,
  createDate     TIMESTAMP   NOT NULL,
  createdBy      VARCHAR(40) NOT NULL,
  lastUpdateDate TIMESTAMP   NOT NULL,
  lastUpdatedBy  VARCHAR(40) NOT NULL,
  version        INT8        NOT NULL
);

/* Constraints */
ALTER TABLE test_animals ADD CONSTRAINT const_test_animals_pk PRIMARY KEY (id);

/* Tables */
CREATE TABLE test_equipments (
  id             VARCHAR(40) NOT NULL,
  animalId       INT8        NOT NULL,
  name           VARCHAR(30) NOT NULL,
  createDate     TIMESTAMP   NOT NULL,
  createdBy      VARCHAR(40) NOT NULL,
  lastUpdateDate TIMESTAMP   NOT NULL,
  lastUpdatedBy  VARCHAR(40) NOT NULL,
  version        INT8        NOT NULL
);

/* Constraints */
ALTER TABLE test_equipments ADD CONSTRAINT const_test_equipments_pk PRIMARY KEY (id);

/* Tables */
CREATE TABLE IF NOT EXISTS uniqueId (
  maxId     INT          NOT NULL,
  tableName VARCHAR(100) NOT NULL
);