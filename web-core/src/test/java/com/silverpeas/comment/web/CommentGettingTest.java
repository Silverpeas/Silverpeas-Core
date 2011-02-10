/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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
package com.silverpeas.comment.web;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.springframework.web.context.ContextLoaderListener;
import com.sun.jersey.test.framework.JerseyTest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests on the web service that handle the comments.
 */
public class CommentResourceTest extends JerseyTest {

  private static final String HEADER_SESSION_KEY = "X-Silverpeas-SessionKey";
  private static final String COMPONENT_ID = "kmelia2";
  private static final String CONTENT_ID = "1";
  private static final String RESOURCE_PATH = "comments/" + COMPONENT_ID + "/" + CONTENT_ID;

  public CommentResourceTest() {
    super(new WebAppDescriptor.Builder("com.silverpeas.comment.web").contextPath("silverpeas").
        contextParam("contextConfigLocation", "classpath:/spring-comment.xml").
        requestListenerClass(org.springframework.web.context.request.RequestContextListener.class).
        servletClass(SpringServlet.class).contextListenerClass(ContextLoaderListener.class).
        build());
  }

  @Test
  public void testCallByANonAuthenticatedUser() {
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH + "/3").accept(MediaType.APPLICATION_JSON).get(String.class);
      fail("A non authenticated user shouldn't access the comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }
}