/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.file;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.exception.RelativeFileAccessException;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * A processor to assert that the path of a {@link SilverpeasFile} does not contains relative parts.
 * It the file doesn't exist, then {@code NO_FILE} is returned.
 * @author Yohann Chastagnier
 */
@Service
public class RelativePathCheckingProcessor extends AbstractSilverpeasFileProcessor {

  private static final int PRIORITY = MAX_PRIORITY - 5;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @Override
  public String processBefore(final String path, ProcessingContext context) {
    return path;
  }

  @Override
  public SilverpeasFile processAfter(final SilverpeasFile file, ProcessingContext context) {
    try {
      FileUtil.assertPathNotRelative(file.getPath());
      return file;
    } catch (RelativeFileAccessException e) {
      SilverLogger.getLogger(this).error(e);
      return SilverpeasFile.NO_FILE;
    }
  }
}
