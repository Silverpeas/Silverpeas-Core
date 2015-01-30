-- Copy temporary notation table with current data
SELECT
  *
INTO sb_notation_notation_tmp
FROM sb_notation_notation;

-- Drop current table
DROP TABLE sb_notation_notation;

-- Create notation table with its new structure
CREATE TABLE sb_notation_notation
(
  id           INT         NOT NULL,
  instanceId   VARCHAR(50) NOT NULL,
  externalId   VARCHAR(50) NOT NULL,
  externalType VARCHAR(50) NOT NULL,
  author       VARCHAR(50) NOT NULL,
  note         INT         NOT NULL
);

-- Copy current data into new structure
INSERT INTO sb_notation_notation
(id, instanceid, externalId, externalType, author, note)
  SELECT
    id,
    instanceid,
    externalid,
    CASE WHEN externalType = 1 THEN 'Forum'
    ELSE 'ForumMessage' END,
    author,
    note
  FROM sb_notation_notation_tmp;

-- Drop temporary notation table
DROP TABLE sb_notation_notation_tmp;