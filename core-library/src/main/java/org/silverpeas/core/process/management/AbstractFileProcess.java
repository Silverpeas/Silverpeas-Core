/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.process.management;

import org.silverpeas.core.process.AbstractProcess;
import org.silverpeas.core.process.ProcessType;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.core.process.session.ProcessSession;

/**
 * Abstract extension of <code>AbstractProcess</code> oriented in the file system manipulations.
 * @author Yohann Chastagnier
 * @see AbstractProcess
 */
public abstract class AbstractFileProcess<C extends ProcessExecutionContext> extends
    AbstractProcess<C> {

  /*
   * (non-Javadoc)
   * @see SilverpeasProcess#getProcessType()
   */
  @Override
  public ProcessType getProcessType() {
    return ProcessType.FILESYSTEM;
  }

  /*
   * (non-Javadoc)
   * @see SilverpeasProcess#process(org.silverpeas.process.management.
   * ProcessExecutionContext, ProcessSession)
   */
  @Override
  public final void process(final C processExecutionContext, final ProcessSession session)
      throws Exception {
    processFiles(processExecutionContext, session, processExecutionContext.getFileHandler());
  }

  /**
   * Containing main treatment of the process.
   * @param processExecutionContext
   * @param session
   * @param fileHandler
   * @throws Exception
   */
  abstract public void processFiles(final C processExecutionContext, final ProcessSession session,
      FileHandler fileHandler) throws Exception;
}
