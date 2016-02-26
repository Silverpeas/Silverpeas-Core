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
package com.silverpeas.authentication.web;

import com.silverpeas.web.RESTWebServiceTest;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.sun.jersey.api.client.ClientResponse;
import java.io.UnsupportedEncodingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.util.Charsets;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import static com.silverpeas.authentication.web.WebTestResources.*;
import static com.silverpeas.web.UserPrivilegeValidation.HTTP_AUTHORIZATION;
import static com.silverpeas.web.UserPrivilegeValidation.HTTP_SESSIONKEY;

/**
 * Tests on the authentication process when accessing the WEB resources published as REST services.
 */
public class WebServiceAuthenticationTest extends RESTWebServiceTest<WebTestResources> {

  public WebServiceAuthenticationTest() {
    super(WEB_PACKAGES, SPRING_CONTEXT);
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{COMPONENT_ID};
  }

  @Before
  public void init() {
    UserFull user = getTestResources().getAUser();
    getTestResources().registerUser(user);
  }

  @Test
  public void accessDenied() {
    ClientResponse response = resource().path(WEB_RESOURCE_PATH).
        accept(MediaType.APPLICATION_JSON).
        get(ClientResponse.class);
    assertThat(response.getStatus(), is(Status.UNAUTHORIZED.getStatusCode()));
  }

  @Test
  public void authenticate() throws UnsupportedEncodingException {
    UserFull user = getTestResources().getAUser();
    ClientResponse response = resource().path(WEB_RESOURCE_PATH).
        header(HTTP_AUTHORIZATION, encodeCredentials(user.getId(), user.getPassword())).
        accept(MediaType.APPLICATION_JSON).
        get(ClientResponse.class);
    assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
    String sessionKey = response.getHeaders().getFirst(HTTP_SESSIONKEY);
    assertThat(sessionKey, notNullValue());
  }

  @Test
  public void accessWebResourcesInAnOpenedSession() {
    UserFull user = getTestResources().getAUser();
    ClientResponse response = resource().path(WEB_RESOURCE_PATH).
        header(HTTP_AUTHORIZATION, encodeCredentials(user.getId(), user.getPassword())).
        accept(MediaType.APPLICATION_JSON).
        get(ClientResponse.class);
    String sessionKey = response.getHeaders().getFirst(HTTP_SESSIONKEY);
    assertThat(response.getEntity(String.class), is(WEB_RESOURCE_ID));

    response = resource().path(WEB_RESOURCE_PATH).
        header(HTTP_SESSIONKEY, sessionKey).
        accept(MediaType.APPLICATION_JSON).
        get(ClientResponse.class);
    assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
    assertThat(response.getEntity(String.class), is(WEB_RESOURCE_ID));
  }

  @Test
  public void accessWebResourcesFromUserToken() {
    UserFull user = getTestResources().getAUser();
    getTestResources().registerUser(user);
    ClientResponse response = resource().path(WEB_RESOURCE_PATH).
        header(HTTP_SESSIONKEY, "token-" + user.getId()).
        accept(MediaType.APPLICATION_JSON).
        get(ClientResponse.class);
    assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
    assertThat(response.getEntity(String.class), is(WEB_RESOURCE_ID));
  }

  @Test
  public void openASessionAndAccessWebResourcesOutOfTheOpenedSession() {
    UserFull user = getTestResources().getAUser();
    ClientResponse response = resource().path(WEB_RESOURCE_PATH).
        header(HTTP_AUTHORIZATION, encodeCredentials(user.getId(), user.getPassword())).
        accept(MediaType.APPLICATION_JSON).
        get(ClientResponse.class);
    assertThat(response.getEntity(String.class), is(WEB_RESOURCE_ID));

    response = resource().path(WEB_RESOURCE_PATH).
        accept(MediaType.APPLICATION_JSON).
        get(ClientResponse.class);
    assertThat(response.getStatus(), is(Status.UNAUTHORIZED.getStatusCode()));
  }

  @Test
  public void openASessionAndAccessWebResourcesInAnInexistingSession() {
    UserFull user = getTestResources().getAUser();
    ClientResponse response = resource().path(WEB_RESOURCE_PATH).
        header(HTTP_AUTHORIZATION, encodeCredentials(user.getId(), user.getPassword())).
        accept(MediaType.APPLICATION_JSON).
        get(ClientResponse.class);
    String sessionKey = response.getHeaders().getFirst(HTTP_SESSIONKEY);
    assertThat(response.getEntity(String.class), is(WEB_RESOURCE_ID));

    response = resource().path(WEB_RESOURCE_PATH).
        accept(MediaType.APPLICATION_JSON).
        header(HTTP_SESSIONKEY, sessionKey + "3").
        get(ClientResponse.class);
    assertThat(response.getStatus(), is(Status.UNAUTHORIZED.getStatusCode()));
  }

  private String encodeCredentials(String login, String password) {
    String credentials = login + ":" + password;
    String encodedCredentials
        = new String(Base64.encodeBase64(credentials.getBytes(Charsets.UTF_8)),
            Charsets.UTF_8);
    return encodedCredentials;
  }
}
