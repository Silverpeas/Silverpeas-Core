CREATE TABLE uniqueId (
  tableName VARCHAR(150) NOT NULL,
  maxId     BIGINT       NOT NULL
);
ALTER TABLE uniqueId  ADD CONSTRAINT PK_UniqueId PRIMARY KEY (tableName);
