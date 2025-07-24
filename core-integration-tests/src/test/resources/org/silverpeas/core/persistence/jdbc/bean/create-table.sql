CREATE TABLE IF NOT EXISTS UniqueId (
  maxId     INT          NOT NULL,
  tableName VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS SB_Person (
    id          INT PRIMARY KEY,
    firstName   VARCHAR NOT NULL,
    lastName    VARCHAR NOT NULL,
    age         INT NOT NULL,
    address     VARCHAR
);

CREATE TABLE IF NOT EXISTS SB_Asso (
    id       INT PRIMARY KEY,
    name     VARCHAR NOT NULL,
    creation DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS SB_MEMBERSHIP (
    assoId INT NOT NULL,
    personId INT NOT NULL
);
