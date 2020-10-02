/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.file;

import org.silverpeas.core.annotation.Service;

/**
 * A processor to check the existence of a {@link SilverpeasFile}. It the
 * file doesn't exist, then {@code NO_FILE} is returned.
 * @author mmoquillon
 */
@Service
public class FileExistenceCheckingProcessor extends AbstractSilverpeasFileProcessor{

  @Override
  public String processBefore(final String path, ProcessingContext context) {
    return path;
  }

  @Override
  public SilverpeasFile processAfter(final SilverpeasFile file, ProcessingContext context) {
    SilverpeasFile validatedFile = file;
    switch (context) {
      case GETTING:
      case MOVING:
        if (!validatedFile.exists() && !validatedFile.isFile()) {
          validatedFile = SilverpeasFile.NO_FILE;
        }
        break;
    }
    return validatedFile;
  }

}
