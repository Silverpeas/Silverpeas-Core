/* Table */
CREATE TABLE sb_calendar (
  id             VARCHAR(40)   NOT NULL,
  instanceId     VARCHAR(30)   NOT NULL,
  title          VARCHAR(2000) NOT NULL,
  createDate     TIMESTAMP     NOT NULL,
  createdBy      VARCHAR(40)   NOT NULL,
  lastUpdateDate TIMESTAMP     NOT NULL,
  lastUpdatedBy  VARCHAR(40)   NOT NULL,
  version        INT8          NOT NULL
);

/* Table constraints */
ALTER TABLE sb_calendar
  ADD CONSTRAINT const_sb_calendar_pk PRIMARY KEY (id);