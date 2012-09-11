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
package org.silverpeas.process;

import java.util.ArrayList;
import java.util.List;

import org.silverpeas.process.check.CheckType;

/**
 * This enumeration represents different types of process. At each type of process is associated a
 * type of check. By this mechanism, the API is able to know which types of checkings have to be
 * done for a chaining process execution.
 * @author Yohann Chastagnier
 */
public enum ProcessType {

  /** the process is oriented on data manipulations (database transactions for example) */
  DATA(CheckType.DATA),

  /** the process is oriented on file system manipulations (photo creations for example) */
  FILESYSTEM(CheckType.FILESYSTEM),

  /** the process is oriented on file system manipulations (photo creations for example) */
  DATA_AND_FILESYSTEM(CheckType.DATA, CheckType.FILESYSTEM);

  private final List<CheckType> checkTypesToProcess = new ArrayList<CheckType>();

  private ProcessType(final CheckType... checkTypes) {
    for (final CheckType checkType : checkTypes) {
      checkTypesToProcess.add(checkType);
    }
  }

  public List<CheckType> getCheckTypesToProcess() {
    return checkTypesToProcess;
  }
}
