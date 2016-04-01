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
package org.silverpeas.core.process;

import org.silverpeas.core.process.management.ProcessErrorType;
import org.silverpeas.core.process.management.ProcessExecutionContext;

/**
 * The abstract root implementation of <code>SilverpeasProcess</code> interface where
 * <code>process</code> and <code>getProcessType</code> methods are the only ones that are not
 * implemented there.
 * @author Yohann Chastagnier
 */
public abstract class AbstractProcess<C extends ProcessExecutionContext> implements
    SilverpeasProcess<C> {

  /*
   * (non-Javadoc)
   * @see SilverpeasProcess#onSuccessful()
   */
  @Override
  public void onSuccessful() throws Exception {
    // Nothing to do by default
  }

  /*
   * (non-Javadoc)
   * @see SilverpeasProcess#onFailure(org.silverpeas.process.management.
   * ProcessErrorType, java.lang.Exception)
   */
  @Override
  public void onFailure(final ProcessErrorType errorType, final Exception exception)
      throws Exception {
    if (!ProcessErrorType.OTHER_PROCESS_FAILED.equals(errorType)) {
      throw exception;
    }
  }
}
