ALTER TABLE ST_Space ADD isPersonal int;

CREATE TABLE ST_UserFavoriteSpaces
(
  id          INT   NOT NULL,
  userid      INT   NOT NULL,
  spaceid     INT   NOT NULL
);

ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT PK_UserFavoriteSpaces PRIMARY KEY CLUSTERED(id);
ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT FK_UserFavoriteSpaces_1 FOREIGN KEY (userid) REFERENCES ST_User(id);
ALTER TABLE ST_UserFavoriteSpaces ADD CONSTRAINT FK_UserFavoriteSpaces_2 FOREIGN KEY (spaceid) REFERENCES ST_Space(id);