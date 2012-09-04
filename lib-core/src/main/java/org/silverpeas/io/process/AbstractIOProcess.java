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
package org.silverpeas.io.process;

import org.silverpeas.io.IOErrorType;
import org.silverpeas.io.file.FileHandler;
import org.silverpeas.io.session.IOSession;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractIOProcess<R> {

  /**
   * Containing treatment that is realized before all
   * @param session
   * @return
   */
  public void processBefore(final IOSession session) throws Exception {
    // Nothing to do by default
  }

  /**
   * Containing the file treatment
   * @param session
   * @return
   */
  abstract public void processFiles(IOSession session, FileHandler fileHandler) throws Exception;

  /**
   * Containing the treatment
   * @param session
   * @return
   */
  public R onSuccessful(final IOSession session) throws Exception {
    // Nothing to do by default
    return null;
  }

  /**
   * Containing the treatment when on a fail
   * @param session
   * @param errorType
   * @param exception
   */
  public void onFailure(final IOSession session, final IOErrorType errorType,
      final Exception exception) throws Exception {
    throw exception;
  }
}
