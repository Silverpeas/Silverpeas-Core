package org.silverpeas.core.webapi.preferences;

import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authenticated;

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
@Service
@RequestScoped
@Path("mypreferences")
@Authenticated
public class MyPreferencesResource extends RESTWebService {
  @Override
  public String getComponentId() {
    return null;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public UserPreferencesEntity setMyPreferences(final UserPreferencesEntity preferences) {
    UserPreferences userPref = getUserPreferences();
    userPref.setLanguage(preferences.getLanguage());
    PersonalizationServiceProvider.getPersonalizationService().saveUserSettings(userPref);
    preferences.setURI(getUriInfo().getRequestUri());
    return preferences;
  }
}
