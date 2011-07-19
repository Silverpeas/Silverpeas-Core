/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.personalization.service;

import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.util.Default;
import javax.inject.Named;

/**
 *
 * @author emmanuel.hugonnet@silverpeas.org
 */
@Named
@Default
public class MockablePersonalizationService implements PersonalizationService {
private PersonalizationService service;

  public void setPersonalizationService(PersonalizationService questionManager) {
    this.service = questionManager;
  }

  public PersonalizationService getPersonalizationService() {
    return service;
  }
  @Override
  public void saveUserSettings(UserPreferences userPreferences) {
    service.saveUserSettings(userPreferences);
  }

  @Override
  public UserPreferences getUserSettings(String userId) {
    return service.getUserSettings(userId);
  }

}
