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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.language.web;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.annotation.Authenticated;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.util.ResourceLocator;

@Service
@RequestScoped
@Path("lang")
@Authenticated
public class LanguageResource extends RESTWebService {

  private static final String MY_PROFILE_SETTINGS_LANGUAGE_KEY = "myProfile.settings.language_";

  @Override
  public String getComponentId() {
    return null;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<LanguageEntity> getAvailableLanguages() {
    List<LanguageEntity> languages = new ArrayList<LanguageEntity>();
    ResourceLocator multilang =
        new ResourceLocator("com.silverpeas.social.multilang.socialNetworkBundle",
            getUserPreferences().getLanguage());
    for (String language : DisplayI18NHelper.getLanguages()) {
      languages.add(new LanguageEntity(language, multilang.getString(
          MY_PROFILE_SETTINGS_LANGUAGE_KEY + language, StringUtil.EMPTY)));
    }
    return languages;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response setLanguage(final LanguageEntity newLanguage) {
    UserPreferences userPref = getUserPreferences();
    userPref.setLanguage(newLanguage.getLang());
    SilverpeasServiceProvider.getPersonalizationService().saveUserSettings(userPref);
    return Response.ok(newLanguage).build();
  }
}
