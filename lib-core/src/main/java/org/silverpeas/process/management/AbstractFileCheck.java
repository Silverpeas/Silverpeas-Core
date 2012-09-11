/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.process.management;

import org.silverpeas.process.check.AbstractCheck;
import org.silverpeas.process.check.CheckType;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.session.Session;

/**
 * Abstract extension of <code>AbstractCheck</code> oriented on data verifications.
 * @author Yohann Chastagnier
 * @see AbstractCheck
 */
public abstract class AbstractFileCheck extends AbstractCheck {

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.check.Check#getType()
   */
  @Override
  public CheckType getType() {
    return CheckType.FILESYSTEM;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.silverpeas.process.check.Check#check(org.silverpeas.process.management.ProcessExecutionContext
   * , org.silverpeas.process.session.Session)
   */
  @Override
  public final void check(final ProcessExecutionContext processExecutionProcess,
      final Session session) throws Exception {
    checkFiles(processExecutionProcess, session, processExecutionProcess.getFileHandler());
  }

  /**
   * Contains the treatment of the verification. The file handler (@see {@link FileHandler})
   * associated to the current execution of chained Silverpeas processes is passed.
   * @param processExecutionProcess
   * @param session
   * @param fileHandler
   * @throws Exception
   */
  abstract public void checkFiles(ProcessExecutionContext processExecutionProcess,
      final Session session, final FileHandler fileHandler) throws Exception;
}
