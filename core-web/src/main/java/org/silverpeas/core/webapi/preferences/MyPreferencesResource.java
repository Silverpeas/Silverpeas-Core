/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.webapi.preferences;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The preferences of the current user in Silverpeas. This web service provides a way to get and
 * to change its preferences. Currently, only the change of the language is implemented.
 * @author mmoquillon
 */
@WebService
@Path(MyPreferencesResource.PATH)
@Authenticated
public class MyPreferencesResource extends RESTWebService {

  static final String PATH = "mypreferences";

  @Override
  public String getComponentId() {
    return null;
  }

  /**
   * Updates the preferences of the user behind the current request.
   * @param preferences the user preferences to set.
   * @return the updated user preferences.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public UserPreferencesEntity setMyPreferences(final UserPreferencesEntity preferences) {
    UserPreferences userPref = getUserPreferences();
    userPref.setLanguage(preferences.getLanguage());
    PersonalizationServiceProvider.getPersonalizationService().saveUserSettings(userPref);
    preferences.setURI(getUri().getRequestUri());
    return preferences;
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }
}
