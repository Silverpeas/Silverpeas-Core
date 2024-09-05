import java.nio.file.Files
import java.nio.file.Path
import java.sql.SQLException

/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception.  You should have received a copy of the text describi
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

log.info 'Fix the name of the component in which the additional user data forms are stored: ' +
        'rename "directory" to "users"'

Path workspacePath = settings.SILVERPEAS_DATA_HOME.asPath().resolve('workspaces')
Path userFormDataPath = workspacePath.resolve('directory')
Path newUserFormDataPath = workspacePath.resolve('users')

def userTemplates = sql.rows(
        'SELECT templateId, externalId FROM SB_FormTemplate_Template WHERE externalId like ?',
        ['directory:%'])

String updateMsg = userTemplates.isEmpty() ? '' : 'Update them'
log.info "${userTemplates.size()} templates of user data form are found. ${updateMsg}"

userTemplates.each { r ->
    String newName = (r.externalId as String).replace('directory', 'users')
    log.info "Update template ${r.externalId} to ${newName}..."
    int templateId = r.templateId as int
    int result = sql.executeUpdate(
            'UPDATE SB_FormTemplate_Template SET externalId = ? WHERE templateId = ?',
            [newName, templateId])
    if (result != 1) {
        throw new SQLException('Unable to update ')
    }
}

if (Files.exists(userFormDataPath) && Files.isDirectory(userFormDataPath)) {
    log.info "Rename folder ${userFormDataPath.toString()} to ${newUserFormDataPath.toString()}..."
    Files.move(userFormDataPath, newUserFormDataPath)
} else {
    log.info "Folder ${userFormDataPath} doesn't exist!"
}
