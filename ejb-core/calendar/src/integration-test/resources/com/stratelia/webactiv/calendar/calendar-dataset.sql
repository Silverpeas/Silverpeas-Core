/* Calendar Journal */
INSERT INTO CalendarJournal (id, name, description, delegatorId, startDay, endDay, startHour, endHour,
                             classification, priority, lastModification, externalId)
    VALUES (1, 'RDV1', 'bla blab', '1', '2011/07/08', '2011/07/08', '14:00', '', 'private', 2, '', '');

INSERT INTO CalendarJournal (id, name, description, delegatorId, startDay, endDay, startHour, endHour,
                             classification, priority, lastModification, externalId)
    VALUES (2, 'RDV1', 'bla blab', '2', '2011/07/08', '2011/07/08', '14:00', '', 'private', 2, '', '');

INSERT INTO CalendarJournal (id, name, description, delegatorId, startDay, endDay, startHour, endHour,
                             classification, priority, lastModification, externalId)
    VALUES (3, 'RDV3', 'bla2 blab2', '1', '2011/07/09', '2011/07/09', '15:00', '', 'public', 2, '', '');

INSERT INTO CalendarJournal (id, name, description, delegatorId, startDay, endDay, startHour, endHour,
                             classification, priority, lastModification, externalId)
    VALUES (4, 'RDV4', 'bla4 blab4', '1', '2011/07/09', '2011/07/09', '09:00', '', 'public', 2, '', '');

/* Calendar ToDos */
INSERT INTO CalendarToDo (id, name, description, delegatorId, startDay, endDay, startHour, endHour,
                          classification, priority, lastModification, externalId, componentId)
    VALUES (1, 'ToDo1', 'bla blab', '1', '2011/07/08', '2011/07/08', '14:00', '', 'private', 2, '',
            '', 'inst10');

INSERT INTO CalendarToDo (id, name, description, delegatorId, startDay, endDay, startHour, endHour,
                             classification, priority, lastModification, externalId, componentId)
    VALUES (2, 'ToDo1', 'bla blab', '2', '2011/07/08', '2011/07/08', '14:00', '', 'private', 2, '',
            '', 'inst10');

INSERT INTO CalendarToDo (id, name, description, delegatorId, startDay, endDay, startHour, endHour,
                             classification, priority, lastModification, externalId, componentId)
    VALUES (3, 'ToDo3', 'bla2 blab2', '1', '2011/07/09', '2011/07/09', '15:00', '', 'public', 2, '',
            '', 'inst10');

INSERT INTO CalendarToDo (id, name, description, delegatorId, startDay, endDay, startHour, endHour,
                             classification, priority, lastModification, externalId, componentId)
    VALUES (4, 'ToDo4', 'bla4 blab4', '1', '2011/07/09', '2011/07/09', '09:00', '', 'public', 2, '',
            '', 'inst20');

/* ToDos Attendees */
INSERT INTO CalendarToDoAttendee (todoId, userId) VALUES (2, '2');
INSERT INTO CalendarToDoAttendee (todoId, userId) VALUES (2, '3');
INSERT INTO CalendarToDoAttendee (todoId, userId) VALUES (4, '2');
