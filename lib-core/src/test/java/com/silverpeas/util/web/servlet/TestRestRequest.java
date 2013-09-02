/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.util.web.servlet;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;

public class TestRestRequest extends TestCase {

  public void testNotRestRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");    
    request.setPathInfo("/attached_file/componentId/kmelia3/attachmentId/275/lang/null/name/Pr%C5%BDsentation%20PPT%20-%2024H%20chrono.ppt");
    request.setRequestURI("/silverpeas/attached_file/componentId/kmelia3/attachmentId/275/lang/null/name/Pr%C5%BDsentation%20PPT%20-%2024H%20chrono.ppt");
    RestRequest rest = new RestRequest(request, "");
    assertEquals(RestRequest.FIND, rest.getAction());
    assertNull(rest.getElements().get("Main"));
    assertNotNull(rest.getElements().get("componentId"));
    assertEquals("kmelia3", rest.getElementValue("componentId"));
    assertEquals("275", rest.getElementValue("attachmentId"));
    assertEquals("Pr%C5%BDsentation%20PPT%20-%2024H%20chrono.ppt", rest.getElementValue("name"));
  }

  public void testSearchResultRestRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45/searchResult?Type=message&Id=6");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45/searchResult?Type=message&Id=6");
    RestRequest rest = new RestRequest(request, "");
    assertEquals(RestRequest.FIND, rest.getAction());
    assertNull(rest.getElements().get("searchResult"));
  }

  public void testRestRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45/list/45");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45/list/45");
    RestRequest rest = new RestRequest(request, "");
    assertEquals(RestRequest.FIND, rest.getAction());
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45/message/45/mailingListAttachment/18/");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45/message/45/mailingListAttachment/18/");
    rest = new RestRequest(request, "");
    assertEquals(RestRequest.FIND, rest.getAction());
  }


  public void testDoubleRestRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45//list/45");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45//list/45");
    RestRequest rest = new RestRequest(request, "mailingList45");
    assertEquals(RestRequest.FIND, rest.getAction());
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45//message/45/mailingListAttachment/18/");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45//message/45/mailingListAttachment/18/");
    rest = new RestRequest(request, "");
    assertEquals(RestRequest.FIND, rest.getAction());
  }

  public void testActionRestRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("localhost");
    request.setMethod("PUT");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList45/message/45");
    request.setRequestURI("/silverpeas/RmailingList/mailingList45/message/45");
    RestRequest rest = new RestRequest(request, "mailingList45");
    assertEquals(RestRequest.UPDATE, rest.getAction());
    request.setRemoteHost("localhost");
    request.setMethod("GET");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList/45");
    request.setRequestURI("/silverpeas/RmailingList/mailingList/45");
    rest = new RestRequest(request, "mailingList45");
    assertEquals(RestRequest.FIND, rest.getAction());
    assertEquals("45", rest.getElementValue("mailingList"));
    request.setRemoteHost("localhost");
    request.setMethod("POST");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList/45");
    request.setRequestURI("/silverpeas/RmailingList/mailingList/45");
    rest = new RestRequest(request, "");
    assertEquals(RestRequest.CREATE, rest.getAction());
    assertEquals("45", rest.getElementValue("mailingList"));
    request.setRemoteHost("localhost");
    request.setMethod("DELETE");
    request.setRemotePort(8000);
    request.setScheme("http://");
    request.setContextPath("/silverpeas");
    request.setPathInfo("/RmailingList/mailingList/45");
    request.setRequestURI("/silverpeas/RmailingList/mailingList/45");
    rest = new RestRequest(request, "");
    assertEquals(RestRequest.DELETE, rest.getAction());
    assertEquals("45", rest.getElementValue("mailingList"));
  }

  public void testDaisyRestRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost("daisy");
    request.setMethod("GET");
    request.setRemotePort(80);
    request.setScheme("http://");
    request.setContextPath("/webUnifaf");
    request.setPathInfo("/SilverpeasWebFileServer/componentId/kmelia24/attachmentId/797/lang/fr/name/CIBC%20HABILITES.pdf");
    request.setRequestURI("/webUnifaf/SilverpeasWebFileServer/componentId/kmelia24/attachmentId/797/lang/fr/name/CIBC%20HABILITES.pdf");
    RestRequest rest = new RestRequest(request, "");
    assertEquals(RestRequest.FIND, rest.getAction());
    assertEquals("kmelia24", rest.getElementValue("componentId"));
    assertEquals("797", rest.getElementValue("attachmentId"));
    assertEquals("fr", rest.getElementValue("lang"));
    assertEquals("CIBC%20HABILITES.pdf", rest.getElementValue("name"));
  }


}
