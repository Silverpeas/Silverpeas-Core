/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.web.mock;

import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.util.Default;
import javax.inject.Named;
import static org.mockito.Mockito.mock;

/**
 * A wrapper around a PersonalizationController mock for testing purpose. It is managed by the IoC
 * container and it plays the role of a PersonalizationController instance for the business objects
 * involved in a test. For doing, it delegates the invoked methods to the wrapped mock. You can get
 * the wrapped mock for registering some behaviours an PersonalizationController instance should
 * have in the tests.
 */
@Named("personalizationService")
@Default
public class PersonalizationServiceMockWrapper implements PersonalizationService {

  private PersonalizationService mock;

  public PersonalizationServiceMockWrapper() {
    mock = mock(PersonalizationService.class);
  }
  
  public PersonalizationService getPersonalizationServiceMock() {
    return mock;
  }

  @Override
  public void saveUserSettings(UserPreferences userPreferences) {
    mock.saveUserSettings(userPreferences);
  }

  @Override
  public UserPreferences getUserSettings(String userId) {
    return mock.getUserSettings(userId);
  }

  @Override
  public void resetDefaultSpace(String spaceId) {
    mock.resetDefaultSpace(spaceId);
  }
  
}
