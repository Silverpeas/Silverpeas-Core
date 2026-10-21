/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
CREATE TABLE SB_FormTemplate_Template
(templateId INT PRIMARY KEY, externalId VARCHAR(255), templateName VARCHAR(255));
CREATE TABLE SB_FormTemplate_Record
(recordId INT PRIMARY KEY, templateId INT NOT NULL, externalId VARCHAR(255), lang VARCHAR(10));
CREATE TABLE SB_FormTemplate_TextField
(recordId INT NOT NULL, fieldName VARCHAR(255), fieldValue VARCHAR(4000) CHECK (fieldValue <> 'FAIL'), fieldValueIndex INT);
INSERT INTO SB_FormTemplate_Template VALUES (1, 'webPages42:example', NULL);
INSERT INTO UniqueId (tableName, maxId) VALUES ('sb_formtemplate_record', 0);
