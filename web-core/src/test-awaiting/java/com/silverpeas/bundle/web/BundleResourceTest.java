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
package com.silverpeas.bundle.web;

import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.sun.jersey.api.client.UniformInterfaceException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import static com.silverpeas.bundle.web.BundleTestResources.JAVA_PACKAGE;
import static com.silverpeas.bundle.web.BundleTestResources.SPRING_CONTEXT;

/**
 * The bundle resource represents in the WEB a localized bundle of messages. This WEB service is an
 * entry point to access the different bundles in use in Silverpeas. It can be accessed only by
 * authenticated users. Ony the localized texts can be actually accessed, no settings.
 *
 * The localized bundled is refered in the URI by its absolute classpath location and only the one
 * that matches the prefered language of the current user in the session is taken into account. If
 * no bunble exists in the language of the current user, then the default one is sent back.
 */
public class BundleResourceTest extends ResourceGettingTest<BundleTestResources> {

  private String sessionKey = null;
  private UserDetail user;

  public BundleResourceTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
  }

  @Test
  @Override
  public void gettingAResourceByANonAuthenticatedUser() {
    String messages = resource().path(aResourceURI()).
        accept(MediaType.TEXT_PLAIN).
        get(getWebEntityClass());
    assertThat(messages.contains("ceci.est.un.text=ceci est un texte"), is(true));

    messages =
        getAt(aOrgResourceURI() + ".properties", MediaType.TEXT_PLAIN_TYPE, getWebEntityClass());
    assertThat(messages.contains("ceci.est.un.text=ceci est un texte"), is(true));
  }

  @Ignore
  @Test
  @Override
  public void gettingAResourceWithAnExpiredSession() {
  }

  @Ignore
  @Test
  @Override
  public void gettingAResourceByAnUnauthorizedUser() {
  }

  @Test
  @Override
  public void gettingAnUnexistingResource() {
    try {
      getAt(anUnexistingResourceURI(), MediaType.TEXT_PLAIN_TYPE, getWebEntityClass());
      fail("A user shouldn't get an unexisting resource");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int notFound = Response.Status.NOT_FOUND.getStatusCode();
      assertThat(receivedStatus, is(notFound));
    }
  }

  @Test
  public void getAnExistingBundleInFrench() {
    String messages = getAt(aResourceURI(), MediaType.TEXT_PLAIN_TYPE, getWebEntityClass());
    assertThat(messages.contains("ceci.est.un.text=ceci est un texte"), is(true));

    messages = getAt(aOrgResourceURI(), MediaType.TEXT_PLAIN_TYPE, getWebEntityClass());
    assertThat(messages.contains("ceci.est.un.text=ceci est un texte"), is(true));
  }

  @Test
  public void getAnExistingBundleByItsNameAndExtension() {
    String messages = getAt(aResourceURI() + ".properties", MediaType.TEXT_PLAIN_TYPE,
        getWebEntityClass());
    assertThat(messages.contains("ceci.est.un.text=ceci est un texte"), is(true));

    messages = getAt(aOrgResourceURI() + ".properties", MediaType.TEXT_PLAIN_TYPE,
        getWebEntityClass());
    assertThat(messages.contains("ceci.est.un.text=ceci est un texte"), is(true));
  }

  @Test
  public void getAnExistingBundleInEnglish() {
    PersonalizationService p = getPersonalizationServiceMock();
    p.getUserSettings(user.getId()).setLanguage("en");
    String messages = getAt(aResourceURI(), MediaType.TEXT_PLAIN_TYPE, getWebEntityClass());
    assertThat(messages.contains("ceci.est.un.text=this one is a text"), is(true));

    messages = getAt(aOrgResourceURI(), MediaType.TEXT_PLAIN_TYPE, getWebEntityClass());
    assertThat(messages.contains("ceci.est.un.text=this one is a text"), is(true));
  }

  @Test
  public void getAnExistingBundleExplicitlyInEnglish() {
    String messages = getAt(aResourceURI() + "_en", MediaType.TEXT_PLAIN_TYPE, getWebEntityClass());
    assertThat(messages.contains("ceci.est.un.text=this one is a text"), is(true));

    messages = getAt(aOrgResourceURI() + "_en", MediaType.TEXT_PLAIN_TYPE, getWebEntityClass());
    assertThat(messages.contains("ceci.est.un.text=this one is a text"), is(true));
  }

  @Test
  public void getAComponentSettingsInPlaceOfLocalizedBundle() {
    try {
      String settingsURI = "bundles/com/silverpeas/bundle/web/componentSettings";
      getAt(settingsURI, MediaType.TEXT_PLAIN_TYPE, getWebEntityClass());
      fail("A user shouldn't get a bundle with component settings");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Response.Status.BAD_REQUEST.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }

  @Test
  public void getAComponentSettings() {
    String messages =
        getAt(aSettingsResourceURI(), MediaType.TEXT_PLAIN_TYPE, getWebEntityClass());
    assertThat(messages.contains("ceci.est.un.parametre=1"), is(true));
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[0];
  }

  @Override
  public String aResourceURI() {
    return "bundles/com/silverpeas/bundle/web/multilang/mytranslations";
  }

  public String aOrgResourceURI() {
    return "bundles/org/silverpeas/bundle/web/multilang/mytranslations";
  }

  public String aSettingsResourceURI() {
    return "bundles/settings/com/silverpeas/bundle/web/componentSettings";
  }

  @Override
  public String anUnexistingResourceURI() {
    return "bundles/com/silverpeas/bundle/web/multilang/toto";
  }

  @Override
  public String aResource() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<String> getWebEntityClass() {
    return String.class;
  }
}
