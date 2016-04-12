/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.bundle;

import org.silverpeas.core.webapi.base.annotation.Authenticated;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.i18n.I18NHelper;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.util.MissingResourceException;
import java.util.Properties;

/**
 * The bundle resource represents either a settings bundle or an i18n messages bundle.
 *
 * This WEB service is an entry point to access the different bundles in use in Silverpeas. It can
 * be accessed only by authenticated users so that is is easy to know the language of the bundle to
 * sent back.
 *
 * The i18n bundled is referred in the URI by its absolute location in the classpath of the
 * Silverpeas portal with as well / or . as path separators, and it can be or not suffixed with
 * properties. For i18n bundles, the language can be indicated with the resource bundle name,
 * otherwise the language of the current user underlying at the HTTP request is taken. If the
 * specified language isn't supported by Silverpeas, then the default language in Silverpeas (yet
 * the French), is taken.
 *
 * In order to add some flexibility, particularly with client-side scripts, the language of the user
 * can be explicitly indicated with the i18n bundle name, whatever it is and without knowing it, by
 * using the wildcard $$ as language code; this wildcard means whatever the language (then takes the
 * preferred language of the current user in the session). This parameter isn't taken into account
 * with the settings bundles.
 */

@RequestScoped
@Path("bundles")
@Authenticated
public class BundleResource extends RESTWebService {

  private static final String GENERAL_SETTINGS = "org.silverpeas.general";

  @Override
  public String getComponentId() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * User authentication is not necessary for this WEB Service. The authentication processing is
   * used here to identify the user behind the call if possible.
   *
   * @param validation the validation instance to use.
   * @throws WebApplicationException
   */
  @Override
  public void validateUserAuthentication(final UserPrivilegeValidation validation)
      throws WebApplicationException {
    try {
      super.validateUserAuthentication(validation);
    } catch (WebApplicationException wae) {
      if (Response.Status.UNAUTHORIZED.getStatusCode() != wae.getResponse().getStatus()) {
        throw wae;
      }
    }
  }

  /**
   * Asks for an i18n resource bundle either in the language of the current user in the session or
   * in the specified language. The returned bundle is a merge of both the asked i18n properties and
   * the general Silverpeas i18n texts.
   *
   * The resource bundle is specified by its absolute path in the classpath of the WEB service.
   *
   * If the language is specified with the name of the bundle, it will be considered in place of the
   * language of the current user in the underlying WEB session. For doing, the langage has to be
   * indicated as expected with localized resource bundles. If the language isn't supported by
   * Silverpeas, the default language will be taken. In order to work with some javascript plugins
   * in charge of i18n texts, the method accepts also the particular wildcard $$ to specify
   * explicitly the language of the current user.
   *
   * @see java.util.ResourceBundle
   * @param bundle the absolute path of the resource bundle in the classpath of Silverpeas.
   * @return an HTTP response with the asked properties or an HTTP error.
   * @throws IOException if an error occurs while accessing the resource bundle.
   */
  @GET
  @Path("{bundle: (com|org)/[a-zA-Z0-9/._$]+}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getLocalizedBundle(@PathParam("bundle") final String bundle) throws IOException {
    String language = getLanguage();
    String localizedBundle = bundle;
    if (bundle.endsWith(".properties")) {
      localizedBundle = bundle.substring(0, bundle.indexOf(".properties"));
    }
    if (localizedBundle.lastIndexOf("_") == localizedBundle.length() - 3) {
      String askedLanguage = localizedBundle.substring(bundle.lastIndexOf("_") + 1);
      if (!askedLanguage.equals("$$")) {
        language = askedLanguage;
      }
      localizedBundle = localizedBundle.substring(0, bundle.lastIndexOf("_"));
    }
    localizedBundle = localizedBundle.replaceAll("/", ".");
    LocalizationBundle resource = ResourceLocator.getLocalizationBundle(localizedBundle, language);
    String bundleName = resource.getBaseBundleName() + " - " + resource.getLocale().getLanguage();
    String generalBundleName =
        LocalizationBundle.GENERAL_BUNDLE_NAME + " - " + resource.getLocale().getLanguage();
    try {
      if (!bundle.trim().isEmpty() && bundle.contains("multilang")) {
        StringWriter messages = new StringWriter();
        Properties properties = new Properties();
        Properties generalProperties = new Properties();
        for (String key : resource.keySet()) {
          if (key.startsWith("GML.")) {
            generalProperties.setProperty(key, resource.getString(key));
          } else {
            properties.setProperty(key, resource.getString(key));
          }
        }
        properties.store(messages, bundleName);
        generalProperties.store(messages, generalBundleName);
        return Response.ok(messages.toString()).build();
      } else {
        return Response.status(Response.Status.BAD_REQUEST).entity(
            "It is not a localized bundle with translations").build();
      }
    } catch (MissingResourceException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Asks for a settings bundle. The returned bundle is a merge of both the asked settings and the
   * general Silverpeas settings.
   *
   * The resource bundle is specified by its absolute path in the classpath of the WEB service.
   *
   * @see java.util.ResourceBundle
   * @param bundle the absolute path of the resource bundle in the classpath of Silverpeas.
   * @return an HTTP response with the asked properties or an HTTP error.
   * @throws IOException if an error occurs while accessing the resource bundle.
   */
  @GET
  @Path("settings/{bundle: (com|org)/[a-zA-Z0-9/._$]+}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getSettingsBundle(@PathParam("bundle") final String bundle) throws IOException {
    String settingsBundle = bundle;
    if (bundle.endsWith(".properties")) {
      settingsBundle = bundle.substring(0, bundle.indexOf(".properties"));
    }
    if (settingsBundle.lastIndexOf("_") == settingsBundle.length() - 3) {
      settingsBundle = settingsBundle.substring(0, bundle.lastIndexOf("_"));
    }
    settingsBundle = settingsBundle.replaceAll("/", ".");
    SettingBundle generalSettings = ResourceLocator.getSettingBundle(GENERAL_SETTINGS);
    SettingBundle settings = ResourceLocator.getSettingBundle(settingsBundle);
    try {
      if (!bundle.trim().isEmpty() && !bundle.contains("multilang")) {
        StringWriter messages = new StringWriter();
        Properties properties = new Properties();
        Properties generalProperties = new Properties();
        for (String key : generalSettings.keySet()) {
          generalProperties.setProperty(key, generalSettings.getString(key));
        }
        for (String key : settings.keySet()) {
          properties.setProperty(key, settings.getString(key));
        }
        properties.store(messages, settingsBundle);
        generalProperties.store(messages, GENERAL_SETTINGS);
        return Response.ok(messages.toString()).build();
      } else {
        return Response.status(Response.Status.BAD_REQUEST).entity(
            "It is not a settings bundle").build();
      }
    } catch (MissingResourceException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Due to the particularity of this WEB Service according to authentication, the language is
   * handled at this level.
   *
   * @return the language of the user or the default language.
   */
  private String getLanguage() {
    String language = I18NHelper.defaultLanguage;
    if (getUserDetail() != null) {
      language = getUserPreferences().getLanguage();
    }
    return language;
  }
}
