import groovy.sql.Sql

import java.sql.SQLException

/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

Sql sql = sql;

log.info 'Update the table SB_Reminder to generalize its use'

// Instead of writing an SQL script, we provide here a Groovy one within which each SQL statements
// are executed by using their own database connection so that the side-effect of the previous
// execution is available and hence visible to the next statement.
// A SQL script won't be worked with MSSQL because of both its transactional issues and its non
// support of some standard SQL statement grammars.

String columnRenaming
String  columnAdding
String columnAltering
switch (settings.DB_SERVERTYPE) {
  case 'MSSQL':
    columnRenaming = "sp_rename 'sb_reminder.trigger_durationProp', trigger_prop, 'COLUMN'"
    columnAdding = 'ALTER TABLE sb_reminder ADD process_name VARCHAR(200)'
    columnAltering = 'ALTER TABLE sb_reminder ALTER COLUMN process_name VARCHAR(200) NOT NULL'
    break
  case 'POSTGRESQL':
    columnRenaming = 'ALTER TABLE sb_reminder RENAME trigger_durationprop TO trigger_prop'
    columnAdding = 'ALTER TABLE sb_reminder ADD COLUMN process_name VARCHAR(200)'
    columnAltering = 'ALTER TABLE sb_reminder ALTER COLUMN process_name SET NOT NULL'
    break
  case 'H2':
    columnRenaming = 'ALTER TABLE sb_reminder RENAME COLUMN trigger_durationprop TO trigger_prop'
    columnAdding = 'ALTER TABLE sb_reminder ADD COLUMN process_name VARCHAR(200)'
    columnAltering = 'ALTER TABLE sb_reminder ALTER COLUMN process_name SET NOT NULL'
    break
  case 'ORACLE':
    columnRenaming = 'ALTER TABLE sb_reminder RENAME COLUMN trigger_durationprop TO trigger_prop'
    columnAdding = 'ALTER TABLE sb_reminder ADD process_name VARCHAR(200)'
    columnAltering = 'ALTER TABLE sb_reminder MODIFY process_name NOT NULL'
    break
  default:
    throw new SQLException("The database type ${settings.DB_SERVERTYPE} isn't yet supported!")
}

sql.execute(columnRenaming)
sql.execute(columnAdding)
sql.execute('''
UPDATE sb_reminder
SET process_name = 'CalendarEventUserNotification'
WHERE sb_reminder.trigger_prop = 'NEXT_START_DATE_TIME'
''')
sql.execute(columnAltering)