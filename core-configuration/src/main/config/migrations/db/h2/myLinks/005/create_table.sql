CREATE TABLE SB_MyLinks_Link
(
    linkId      INT          NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NULL,
    url         VARCHAR(255) NOT NULL,
    visible     INT          NOT NULL,
    popup       INT          NOT NULL,
    userId      VARCHAR(50)  NOT NULL,
    instanceId  VARCHAR(50)  NULL,
    objectId    VARCHAR(50)  NULL,
    position    INT          NULL,
    CONSTRAINT PK_SB_MyLink_Link PRIMARY KEY (linkId)
);

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