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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.language;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@WebService
@Path(LanguageResource.PATH)
@Authenticated
public class LanguageResource extends RESTWebService {

  private static final String MY_PROFILE_SETTINGS_LANGUAGE_KEY = "myProfile.settings.language_";

  static final String PATH = "languages";

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return null;
  }

  /**
   * Gets all the languages that are available in this running Silverpeas.
   * @return a list of languages supported by the platform.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<LanguageEntity> getAvailableLanguages() {
    List<LanguageEntity> languages = new ArrayList<>();
    LocalizationBundle multilang =
        ResourceLocator.getLocalizationBundle("org.silverpeas.social.multilang.socialNetworkBundle",
            getUserPreferences().getLanguage());
    for (String language : DisplayI18NHelper.getLanguages()) {
      LanguageEntity entity = new LanguageEntity(language, multilang.getString(
          MY_PROFILE_SETTINGS_LANGUAGE_KEY + language));
      entity.setURI(getUri().getRequestUriBuilder().path(language).build());
      languages.add(entity);
    }
    return languages;
  }
}
