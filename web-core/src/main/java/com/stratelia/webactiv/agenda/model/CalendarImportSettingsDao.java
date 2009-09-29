package com.stratelia.webactiv.agenda.model;

import com.stratelia.webactiv.agenda.control.AgendaException;

public interface CalendarImportSettingsDao {
  /**
   * Get synchronisation user settings
   * 
   * @param userId
   *          Id of user whose settings belong to
   * @return CalendarImportSettings object containing user settings
   * @see com.stratelia.webactiv.agenda.model.CalendarImportSettings
   */
  public CalendarImportSettings getUserSettings(String userId);

  /**
   * Save synchronisation user settings
   * 
   * @param settings
   *          CalendarImportSettings object containing user settings
   * @see com.stratelia.webactiv.agenda.model.CalendarImportSettings
   */
  public void saveUserSettings(CalendarImportSettings settings)
      throws AgendaException;

  /**
   * Update synchronisation user settings
   * 
   * @param settings
   *          CalendarImportSettings object containing user settings
   * @see com.stratelia.webactiv.agenda.model.CalendarImportSettings
   */
  public void updateUserSettings(CalendarImportSettings settings)
      throws AgendaException;

}
