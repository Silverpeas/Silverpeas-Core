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

package org.silverpeas.core.workflow.api.instance;

/**
 * Status of the execution of an action. Used to report about the result of a process flow.
 * @author mmoquillon
 */
public enum ActionStatus {
  PROCESS_FAILED(-1),
  TO_BE_PROCESSED(0),
  PROCESSED(1),
  AFFECTATIONS_DONE(2),
  SAVED(3);

  private int code;

  ActionStatus(int code) {
    this.code = code;
  }

  public static ActionStatus from(int statusCode) {
    if (statusCode > 3 || statusCode < -1) {
      return null;
    }
    return ActionStatus.values()[statusCode + 1];
  }

  public int getCode() {
    return code;
  }
}
  