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
   * Test of getRequestParameter method, of class RequestHelper.
   */
  @Test
  public void testGetRequestParameter() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("paramNotNull")).thenReturn("toto");
    when(request.getParameter("paramNull")).thenReturn(null);
    when(request.getParameter("paramEmpty")).thenReturn("");
    String result = RequestHelper.getRequestParameter(request, "paramNotNull");
    assertEquals("toto", result);
    result = RequestHelper.getRequestParameter(request, "paramNull");
    assertNull(result);
    result = RequestHelper.getRequestParameter(request, "paramEmpty");
    assertEquals("", result);
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
