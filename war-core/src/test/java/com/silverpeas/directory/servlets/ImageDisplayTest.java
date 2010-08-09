package com.silverpeas.directory.servlets;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class ImageDisplayTest {

  @Test
  public void getAvatarName() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getPathInfo()).thenReturn("/display/avatar/nbourakbi.jpg");
    ImageDisplay display = new ImageDisplay();
    String result = display.getAvatar(request);
    String expectedResult = "nbourakbi.jpg";
    assertEquals("Verifying avatar name", expectedResult, result);
  }
}
