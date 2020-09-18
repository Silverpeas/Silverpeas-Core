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
package org.silverpeas.core.process.management.interceptor;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.process.management.AbstractFileProcessCheck;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.io.file.FileBasePath;
import org.silverpeas.core.process.io.file.FileHandler;

import java.util.logging.Logger;

/**
 * @author Yohann Chastagnier
 */
@Service
public class SimulationActionTestFileCheck extends AbstractFileProcessCheck {

  private int callCount = 0;

  @Override
  public void init() {
    super.init();
  }

  @Override
  public void release() {
    super.release();
    callCount = 0;
  }

  @Override
  public void checkFiles(final ProcessExecutionContext processExecutionContext,
      final FileHandler fileHandler) {
    Logger.getAnonymousLogger().info("The check after processes execution is performed...");
    Logger.getAnonymousLogger().info("File handler session: " +
        fileHandler.getHandledFile(FileBasePath.UPLOAD_PATH, "interceptor.test").getFile()
            .getPath());
    callCount++;
  }

  public int getCallCount() {
    return callCount;
  }
}
