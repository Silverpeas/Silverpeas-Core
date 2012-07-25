/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import static com.silverpeas.authentication.web.WebTestResources.*;
import static com.silverpeas.web.UserPriviledgeValidation.*;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.codec.binary.Base64;
import static org.hamcrest.Matchers.*;
import org.junit.Before;

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
  
  private String encodeCredentials(String login, String password) {
    String credentials = login + ":" + password;
    String encodedCredentials = null;
    try {
      encodedCredentials = new String(Base64.encodeBase64(credentials.getBytes("UTF-8")), "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      Logger.getLogger(WebServiceAuthenticationTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    return encodedCredentials;
  }
}
