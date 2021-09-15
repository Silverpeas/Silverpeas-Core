CREATE TABLE SB_MyLinks_Cat
(
    catId       INT          NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NULL,
    userId      VARCHAR(50)  NOT NULL,
    position    INT          NULL,
    CONSTRAINT PK_SB_MyLink_Cat PRIMARY KEY (catId)
);

CREATE TABLE SB_MyLinks_LinkCat
(
    linkId INT NOT NULL,
    catId  INT NOT NULL,
    CONSTRAINT FK_MyLinks_Link_Id FOREIGN KEY (linkId) REFERENCES SB_MyLinks_Link(linkId),
    CONSTRAINT FK_MyLinks_Cat_Id FOREIGN KEY (catId) REFERENCES SB_MyLinks_Cat(catId)
);