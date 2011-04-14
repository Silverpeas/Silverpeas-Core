/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.util.web;

import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class RequestHelperTest {

  public RequestHelperTest() {
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
   * Test of getIntParameter method, of class RequestHelper.
   */
  @Test
  public void testGetIntParameter() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("paramNotNull")).thenReturn("10");
    when(request.getParameter("paramNotNullWithWhiteSpaces")).thenReturn(" 12 ");
    when(request.getParameter("paramNull")).thenReturn(null);
    when(request.getParameter("paramEmpty")).thenReturn("");
    int result = RequestHelper.getIntParameter(request, "paramNotNull", -1);
    assertEquals(10, result);
    result = RequestHelper.getIntParameter(request, "paramNotNullWithWhiteSpaces", -1);
    assertEquals(12, result);
    result = RequestHelper.getIntParameter(request, "paramNull", -1);
    assertEquals(-1, result);
    result = RequestHelper.getIntParameter(request, "paramEmpty", -1);
    assertEquals(-1, result);
  }
}
