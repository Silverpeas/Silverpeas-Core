/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.webapi.bundle;

import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.silverpeas.core.webapi.base.annotation.Authenticated;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;


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
@Path(BundleResource.PATH)
@Authenticated
public class BundleResource extends RESTWebService {

  static final String PATH = "bundles";
  private static final String GENERAL_SETTINGS = "org.silverpeas.general";
  private static final String PROPERTIES_FILE_EXT = ".properties";
  private static final String LANG_SEPARATOR = "_";

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * User authentication is not necessary for this WEB Service. The authentication processing is
   * used here to identify the user behind the call if possible.
   *
   * @param validation the validation instance to use.
   */
  @Override
  public void validateUserAuthentication(final UserPrivilegeValidation validation) {
    try {
      super.validateUserAuthentication(
          validation.skipLastUserAccessTimeRegistering(getHttpServletRequest()));
    } catch (WebApplicationException wae) {
      if (Response.Status.UNAUTHORIZED.getStatusCode() != wae.getResponse().getStatus()) {
        throw wae;
      }
    }
  }

  /**
   * Asks for an i18n resource bundle either in the language of the current user in the session or
   * in the specified language. The returned bundle does not provide the general Silverpeas i18n
   * texts.
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
  @Path("just/{bundle: org/silverpeas/[a-zA-Z0-9/._$]+}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getLocalizedBundle(@PathParam("bundle") final String bundle) throws IOException {
    return getLocalizedBundle(bundle, true);
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
   * @param withoutGeneral true if the general bundle must not be merged into response.
   * @return an HTTP response with the asked properties or an HTTP error.
   * @throws IOException if an error occurs while accessing the resource bundle.
   */
  @GET
  @Path("{bundle: org/silverpeas/[a-zA-Z0-9/._$]+}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getLocalizedBundle(@PathParam("bundle") final String bundle,
      @QueryParam("withoutGeneral") final boolean withoutGeneral) throws IOException {
    boolean withGeneral = !withoutGeneral;
    final LocalizationBundle resource = getBundle(bundle);
    final String language = resource.getLocale().getLanguage();
    final String bundleName = resource.getBaseBundleName() + " - " + language;
    final String generalBundleName = LocalizationBundle.GENERAL_BUNDLE_NAME + " - " + language;
    try {
      if (!bundle.trim().isEmpty() && bundle.contains("multilang")) {
        final StringWriter messages = new StringWriter();
        final Properties properties = new Properties();
        final Properties generalProperties = new Properties();
        final Set<String> keys = withGeneral ? resource.keySet() : resource.specificKeySet();
        for (String key : keys) {
          if (withGeneral && key.startsWith("GML.")) {
            generalProperties.setProperty(key, resource.getString(key));
          } else {
            properties.setProperty(key, resource.getString(key));
          }
        }
        properties.store(messages, bundleName);
        if (withGeneral) {
          generalProperties.store(messages, generalBundleName);
        }
        // Escaping single ' in order to be compliant with jQuery i18n plugin...
        return Response.ok(messages.toString().replaceAll("([^'])[']([^'])", "$1''$2")).build();
      } else {
        return Response.status(Response.Status.BAD_REQUEST).entity(
            "It is not a localized bundle with translations").build();
      }
    } catch (MissingResourceException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  private LocalizationBundle getBundle(final String bundle) {
    String language = getLanguage();
    String localizedBundle = bundle;
    if (bundle.endsWith(PROPERTIES_FILE_EXT)) {
      localizedBundle = bundle.substring(0, bundle.indexOf(PROPERTIES_FILE_EXT));
    }
    if (localizedBundle.lastIndexOf(LANG_SEPARATOR) == localizedBundle.length() - 3) {
      String askedLanguage = localizedBundle.substring(bundle.lastIndexOf(LANG_SEPARATOR) + 1);
      if (!askedLanguage.equals("$$")) {
        language = askedLanguage;
      }
      localizedBundle = localizedBundle.substring(0, bundle.lastIndexOf(LANG_SEPARATOR));
    }
    localizedBundle = localizedBundle.replaceAll("/", ".");
    return ResourceLocator.getLocalizationBundle(localizedBundle, language);
  }

  /**
   * Asks for a settings bundle. The returned bundle is a merge of both the asked settings and the
   * general Silverpeas settings.
   *
   * The resource bundle is specified by its absolute path in the classpath of the WEB service.
   *
   * @see java.util.ResourceBundle
   * @param bundle the absolute path of the resource bundle in the classpath of Silverpeas.
   * @param withGeneral true if the general settings must be added into response.
   * @return an HTTP response with the asked properties or an HTTP error.
   * @throws IOException if an error occurs while accessing the resource bundle.
   */
  @GET
  @Path("settings/{bundle: org/silverpeas/[a-zA-Z0-9/._$]+}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getSettingsBundle(@PathParam("bundle") final String bundle,
      @QueryParam("withGeneral") final boolean withGeneral) throws IOException {
    String settingsBundle = bundle;
    if (bundle.endsWith(PROPERTIES_FILE_EXT)) {
      settingsBundle = bundle.substring(0, bundle.indexOf(PROPERTIES_FILE_EXT));
    }
    if (settingsBundle.lastIndexOf(LANG_SEPARATOR) == settingsBundle.length() - 3) {
      settingsBundle = settingsBundle.substring(0, bundle.lastIndexOf(LANG_SEPARATOR));
    }
    settingsBundle = settingsBundle.replaceAll("/", ".");
    final SettingBundle settings = ResourceLocator.getSettingBundle(settingsBundle);
    try {
      if (!bundle.trim().isEmpty() && !bundle.contains("multilang")) {
        final StringWriter messages = new StringWriter();
        final Properties properties = new Properties();
        final Properties generalProperties = new Properties();
        if (withGeneral) {
          final SettingBundle generalSettings = ResourceLocator.getSettingBundle(GENERAL_SETTINGS);
          for (String key : generalSettings.keySet()) {
            generalProperties.setProperty(key, generalSettings.getString(key));
          }
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
    if (getUser() != null) {
      language = getUserPreferences().getLanguage();
    }
    return language;
  }
}
