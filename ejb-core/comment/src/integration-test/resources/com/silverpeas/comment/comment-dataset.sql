/* comments with instanceId set */
INSERT INTO uniqueId (maxId, tablename) VALUES (10, 'sb_comment_comment');

INSERT INTO sb_comment_comment (commentId, commentOwnerId, commentCreationDate, commentModificationDate,
                                commentComment, instanceId, resourceType, resourceId)
    VALUES (1000, 10, '2019/10/15', NULL, 'my comments', 'instanceId10', 'RtypeTest', '500');

INSERT INTO sb_comment_comment (commentId, commentOwnerId, commentCreationDate, commentModificationDate,
                                commentComment, instanceId, resourceType, resourceId)
    VALUES (1001, 12, '2019/10/18', '2020/06/16', 'my comments are good', 'instanceId10', 'RtypeTest', '500');

INSERT INTO sb_comment_comment (commentId, commentOwnerId, commentCreationDate, commentModificationDate,
                                commentComment, instanceId, resourceType, resourceId)
    VALUES (1002, 12, '2019/10/18', '2020/06/16', 'my comments are good', 'instanceId10', 'RtypeTestAutre', '500');

INSERT INTO sb_comment_comment (commentId, commentOwnerId, commentCreationDate, commentModificationDate,
                                commentComment, instanceId, resourceType, resourceId)
    VALUES (1010, 12, '2019/10/18', '2020/06/16', 'my comments are good', 'instanceId20', 'RtypeTest', '610');

INSERT INTO sb_comment_comment (commentId, commentOwnerId, commentCreationDate, commentModificationDate,
                                commentComment, instanceId, resourceType, resourceId)
    VALUES (1011, 12, '2019/10/18', '2020/06/16', 'my comments are good', 'instanceId20', 'RtypeTestAutre', '610');

/*comments without instanceId: comments on a resource from a tool */
INSERT INTO sb_comment_comment (commentId, commentOwnerId, commentCreationDate, commentModificationDate,
                                commentComment, instanceId, resourceType, resourceId)
    VALUES (1020, 12, '2019/10/18', '2020/06/16', 'comment inside a tool', 'NONE', 'RtypeUniqueTest', '700');

INSERT INTO sb_comment_comment (commentId, commentOwnerId, commentCreationDate, commentModificationDate,
                                commentComment, instanceId, resourceType, resourceId)
    VALUES (1021, 12, '2019/10/18', '2020/06/16', 'comment inside a tool', 'NONE', 'RtypeUniqueTest', '710');

/* users */
INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
    VALUES (1, 0, '1', 'aUser', 'aUser', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
    VALUES (10, 0, '10', 'user10', 'user10', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
    VALUES (12, 0, '12', 'user12', 'user12', 'U', 'VALID', '2012-01-01 00:00:00.000');