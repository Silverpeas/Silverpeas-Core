/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of
 * the text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.stratelia.silverpeas.peasCore;

import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.*;
import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class URLManagerTest {

  public URLManagerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getURL method, of class URLManager.
   */
  @Test
  public void testGetURLWithComponentNameSpaceIdAndComponentId() {
    String sSpace = "WA12";
    String sComponentId = "kmelia158";
    String sComponentName = "kmelia";
    String result = URLManager.getURL(sComponentName, sSpace, sComponentId);
    assertThat(result, is("/Rkmelia/kmelia158/"));
  }

  /**
   * Test of getURL method, of class URLManager.
   */
  @Test
  public void testGetURLWithComponentName() {
    String sComponentName = "kmelia";
    String result = URLManager.getURL(sComponentName);
    assertThat(result, is("/Rkmelia/null/"));
  }

  /**
   * Test of getURL method, of class URLManager.
   */
  @Test
  public void testGetURLWithSpaceIdAndComponentId() {
    String sSpace = "WA12";
    String sComponentId = "kmelia158";
    String result = URLManager.getURL(sSpace, sComponentId);
    assertThat(result, is("/Rkmelia/kmelia158/"));
  }

  /**
   * Test of getNewComponentURL method, of class URLManager.
   */
  @Test
  public void testGetNewComponentURL() {
    String spaceId = "WA21";
    String componentId = "kmelia128";
    String result = URLManager.getNewComponentURL(spaceId, componentId);
    assertThat(result, is("/Rkmelia/kmelia128/"));
  }

  /**
   * Test of getEndURL method, of class URLManager.
   */
  @Test
  public void testGetEndURL() {
    String spaceId = "WA21";
    String componentId = "kmelia128";
    String result = URLManager.getEndURL(spaceId, componentId);
    assertThat(result, is("&componentId=kmelia128&spaceId=WA21"));
  }

  /**
   * Test of getComponentNameFromComponentId method, of class URLManager.
   */
  @Test
  public void testGetComponentNameFromComponentId() {
    String result = URLManager.getComponentNameFromComponentId("kmelia125");
    assertThat(result, is("kmelia"));
    result = URLManager.getComponentNameFromComponentId("kmelia125");
    assertThat(result, is("kmelia"));

  }

  /**
   * Test of getApplicationURL method, of class URLManager.
   */
  @Test
  public void testGetApplicationURL() {
    String result = URLManager.getApplicationURL();
    assertThat(result, is("/silverpeas/"));
  }

  /**
   * Test of getFullApplicationURL method, of class URLManager.
   */
  @Test
  public void testGetFullApplicationURL() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getScheme()).thenReturn("https");
    when(request.getServerName()).thenReturn("www.silverpeas.org");
    when(request.getServerPort()).thenReturn(8443);
    String result = URLManager.getFullApplicationURL(request);
    assertThat(result, is("https://www.silverpeas.org:8443/silverpeas/"));
    verify(request, times(2)).getServerPort();
    reset(request);
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("www.silverpeas.org");
    when(request.getServerPort()).thenReturn(80);
    result = URLManager.getFullApplicationURL(request);
    assertThat(result, is("http://www.silverpeas.org/silverpeas/"));
    verify(request, times(1)).getServerPort();
  }

  /**
   * Test of getServerURL method, of class URLManager.
   */
  @Test
  public void testGetServerURL() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getScheme()).thenReturn("https");
    when(request.getServerName()).thenReturn("www.silverpeas.org");
    when(request.getServerPort()).thenReturn(8443);
    String result = URLManager.getServerURL(request);
    assertThat(result, is("https://www.silverpeas.org:8443"));
    verify(request, times(2)).getServerPort();
    reset(request);
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("www.silverpeas.org");
    when(request.getServerPort()).thenReturn(80);
    result = URLManager.getServerURL(request);
    assertThat(result, is("http://www.silverpeas.org"));
    verify(request, times(1)).getServerPort();
  }

  /**
   * Test of getHttpMode method, of class URLManager.
   */
  @Test
  public void testGetHttpMode() {
    String result = URLManager.getHttpMode();
    assertThat(result, is("http://"));
  }

  /**
   * Test of displayUniversalLinks method, of class URLManager.
   */
  @Test
  public void testDisplayUniversalLinks() {
    boolean result = URLManager.displayUniversalLinks();
    assertThat(result, is(true));
  }

  /**
   * Test of getSimpleURL method, of class URLManager.
   */
  @Test
  public void testGetSimpleURLByTypeIdAndComponentid() {
    int type = URLManager.URL_SPACE;
    String id = "WA21";
    String componentId = "kmelia518";
    String result = URLManager.getSimpleURL(URLManager.URL_SPACE, id, componentId);
    assertThat(result, is("/silverpeas/Space/WA21"));
    id = "kmelia518";
    result = URLManager.getSimpleURL(URLManager.URL_COMPONENT, id, componentId);
    assertThat(result, is("/silverpeas/Component/kmelia518"));
  }

  /**
   * Test of getSimpleURL method, of class URLManager.
   */
  @Test
  public void testGetSimpleURLToForum() {
    String id = "59";
    String componentId = "kmelia518";
    String forumId = "forum38";
    boolean appendContext = false;
    String result = URLManager.getSimpleURL(URLManager.URL_SPACE, id, componentId, appendContext,
            forumId);
    assertThat(result, is(""));
    result = URLManager.getSimpleURL(URLManager.URL_MESSAGE, id, componentId, appendContext, forumId);
    assertThat(result, is("/ForumsMessage/59?ForumId=forum38"));
  }

  /**
   * Test of getSimpleURL method, of class URLManager.
   */
  @Test
  public void testGetSimpleURByTypeIdAndComponentidWithContexteAppended() {
    int type = URLManager.URL_SPACE;
    String id = "WA21";
    String componentId = "kmelia518";
    boolean appendContext = true;
    String result = URLManager.getSimpleURL(type, id, componentId, appendContext);
    assertThat(result, is("/silverpeas/Space/WA21"));
    id = "1978";
    result = URLManager.getSimpleURL(URLManager.URL_PUBLI, id, componentId, appendContext);
    assertThat(result, is("/silverpeas/Publication/1978?ComponentId=kmelia518"));
  }

  /**
   * Test of getSimpleURL method, of class URLManager.
   */
  @Test
  public void testGetSimpleURLByTypeAndId() {
    int type = URLManager.URL_COMPONENT;
    String id = "kmelia518";
    String result = URLManager.getSimpleURL(type, id);
    assertThat(result, is("/silverpeas/Component/kmelia518"));
  }

  /**
   * Test of getSimpleURL method, of class URLManager.
   */
  @Test
  public void testGetSimpleURLByTypeAndIdWithContexteNotAppended() {
    int type = URLManager.URL_COMPONENT;
    String id = "kmelia518";
    boolean appendContext = false;
    String result = URLManager.getSimpleURL(type, id, appendContext);
    assertThat(result, is("/Component/kmelia518"));
  }
}
