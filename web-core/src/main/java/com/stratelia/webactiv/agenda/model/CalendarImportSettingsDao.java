/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.webactiv.agenda.model;

import com.stratelia.webactiv.agenda.control.AgendaException;

public interface CalendarImportSettingsDao {
  /**
   * Get synchronisation user settings
   * @param userId Id of user whose settings belong to
   * @return CalendarImportSettings object containing user settings
   * @see com.stratelia.webactiv.agenda.model.CalendarImportSettings
   */
  public CalendarImportSettings getUserSettings(String userId);

  /**
   * Save synchronisation user settings
   * @param settings CalendarImportSettings object containing user settings
   * @see com.stratelia.webactiv.agenda.model.CalendarImportSettings
   */
  public void saveUserSettings(CalendarImportSettings settings)
      throws AgendaException;

  /**
   * Update synchronisation user settings
   * @param settings CalendarImportSettings object containing user settings
   * @see com.stratelia.webactiv.agenda.model.CalendarImportSettings
   */
  public void updateUserSettings(CalendarImportSettings settings)
      throws AgendaException;

}
