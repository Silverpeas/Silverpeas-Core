/*
COMMONS
 */

INSERT INTO st_domain (id, name, description, propFileName, className, authenticationServer,
                       silverpeasserverurl)
VALUES (0, 'domainSilverpeas', 'default domain for Silverpeas',
        'org.silverpeas.domains.domainSP',
        'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver', 'autDomainSP',
        'https://www.silverpeas.org');

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
VALUES (9, 0, '9', 'LastName9', 'Login51', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (50, 0, '50', 'LastName50', 'Login50', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (51, 0, '51', 'LastName51', 'Login51', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (52, 0, '52', 'LastName52', 'Login52', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (53, 0, '53', 'LastName53', 'Login53', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (54, 0, '54', 'LastName54', 'Login54', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (55, 0, '55', 'LastName55', 'Login55', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (56, 0, '56', 'LastName56', 'Login56', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (60, 0, '60', 'LastName60', 'Login60', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (70, 0, '70', 'LastName70', 'Login70', 'U', 'VALID', '2012-01-01 00:00:00.000'),
       (80, 0, '80', 'LastName80', 'Login80', 'U', 'VALID', '2012-01-01 00:00:00.000');

INSERT INTO st_notificationresource (id, resourceId, resourceType, resourceName, resourceDescription, resourceLocation, resourceUrl, componentInstanceId)
VALUES (1, '10', 'publication', 'Test resource name', 'Test resource description',
        'Test > Resource > Location', 'Test resource URL', 'aComponentInstanceId');
INSERT INTO st_notificationresource (id, resourceId, resourceType, resourceName, resourceDescription, resourceLocation, resourceUrl, componentInstanceId)
VALUES (2, '10', 'publication', 'Test resource name', 'Test resource description',
        'Test > Resource > Location', 'Test resource URL', 'aComponentInstanceId');
INSERT INTO st_notificationresource (id, resourceId, resourceType, resourceName, resourceLocation, resourceUrl, componentInstanceId)
VALUES
  (10, '100', 'publication', 'Test resource name no desc', 'Test > Resource > Location no desc',
   'Test resource URL no desc', 'aComponentInstanceId');
INSERT INTO st_notificationresource (id, resourceId, resourceType, resourceName, resourceLocation, resourceUrl, componentInstanceId)
VALUES
  (20, '200', 'publication', 'Test resource name no desc', 'Test > Resource > Location no desc',
   'Test resource URL no desc', 'aComponentInstanceId');
INSERT INTO st_notificationresource (id, resourceId, resourceType, resourceName, resourceDescription, resourceLocation, resourceUrl, componentInstanceId)
VALUES (40, '400', 'delegateResourceTest', 'Delegate test resource', 'Test resource description',
        'Test > Delegate > Resource > Location', 'Delegate test resource', 'aComponentInstanceId');

INSERT INTO uniqueId (maxId, tablename) VALUES (50, 'st_notificationresource');
INSERT INTO uniqueId (maxId, tablename) VALUES (1000, 'st_delayednotification');
INSERT INTO uniqueId (maxId, tablename) VALUES (100, 'st_delayednotifusersetting');

/*
A first set of data
 */

INSERT INTO st_delayednotifusersetting (id, userId, channel, frequency) VALUES (10, 1, 1, 'D');
INSERT INTO st_delayednotifusersetting (id, userId, channel, frequency) VALUES (20, 1, 4, 'M');
INSERT INTO st_delayednotifusersetting (id, userId, channel, frequency) VALUES (30, 10, 1, 'N');
INSERT INTO st_delayednotifusersetting (id, userId, channel, frequency) VALUES (50, 20, 1, 'W');

INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language, message)
VALUES (100, 50, 10, 1, 1, 1, '2012-01-01 08:10:25.023', 'fr', '');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (200, 50, 10, 4, 2, 1, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (300, 60, 10, 1, 2, 1, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (400, 60, 10, 1, 3, 1, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (500, 70, 10, 1, 4, 1, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (600, 70, 10, 1, 4, 1, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language, message)
VALUES (700, 80, 10, 1, 1, 10, '2012-01-01 08:10:25.023', 'fr', 'bouh');

/*
A second set of data
 */

INSERT INTO st_delayednotifusersetting (id, userId, channel, frequency) VALUES (70, 51, 1, 'D');
INSERT INTO st_delayednotifusersetting (id, userId, channel, frequency) VALUES (-70, 51, 4, 'N');
INSERT INTO st_delayednotifusersetting (id, userId, channel, frequency) VALUES (71, 53, 1, 'M');
INSERT INTO st_delayednotifusersetting (id, userId, channel, frequency) VALUES (-71, 53, 4, 'N');
INSERT INTO st_delayednotifusersetting (id, userId, channel, frequency) VALUES (72, 54, 1, 'W');
INSERT INTO st_delayednotifusersetting (id, userId, channel, frequency) VALUES (73, 55, 1, 'N');
INSERT INTO st_delayednotifusersetting (id, userId, channel, frequency) VALUES (74, 56, 6, 'D');

INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (800, 51, 500, 1, 1, 40, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (801, 51, 500, 4, 1, 40, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (802, 51, 70, 1, 2, 40, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (810, 52, 80, 1, 1, 40, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (811, 52, 80, 4, 1, 40, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (812, 52, 90, 1, 2, 40, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language, message)
VALUES (813, 52, 90, 1, 4, 40, '2012-01-01 08:10:25.023', 'fr', 'Message U2');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (820, 53, 20, 1, 1, 40, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (821, 53, 20, 4, 1, 40, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language, message)
VALUES (822, 53, 100, 1, 4, 40, '2012-01-01 08:10:24.023', 'fr', 'Message U3
A la ligne');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (823, 53, 500, 1, 2, 10, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language, message)
VALUES (824, 53, 500, 1, 5, 10, '2012-01-01 08:10:25.023', 'fr', 'Message U3 (2ème)
A la ligne');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (830, 54, 500, 1, 3, 40, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (831, 54, 500, 4, 2, 40, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (840, 55, 500, 1, 2, 40, '2012-01-01 08:10:25.023', 'fr');
INSERT INTO st_delayednotification (id, userId, fromUserId, channel, action, notificationResourceId, creationDate, language)
VALUES (850, 56, 500, 6, 3, 40, '2012-01-01 08:10:25.023', 'fr');