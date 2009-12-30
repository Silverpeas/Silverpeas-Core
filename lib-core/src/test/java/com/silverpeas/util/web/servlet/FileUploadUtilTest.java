/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.util.web.servlet;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class FileUploadUtilTest {

  public FileUploadUtilTest() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testIsRequestMultipart() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    assertFalse(FileUploadUtil.isRequestMultipart(request));
    request.setContentType(FileUploadBase.MULTIPART_FORM_DATA);
    assertFalse(FileUploadUtil.isRequestMultipart(request));
    request.setContentType(null);
    request.setMethod("POST");
    assertFalse(FileUploadUtil.isRequestMultipart(request));
    request.setContentType("text/html");
    assertFalse(FileUploadUtil.isRequestMultipart(request));
    request.setContentType(FileUploadBase.MULTIPART_FORM_DATA);
    assertTrue(FileUploadUtil.isRequestMultipart(request));
    request.setContentType(FileUploadBase.MULTIPART_MIXED);
    assertTrue(FileUploadUtil.isRequestMultipart(request));
    request.setContentType(FileUploadBase.MULTIPART);
    assertTrue(FileUploadUtil.isRequestMultipart(request));
  }

  @Test
  public void testGetFile() throws Exception {
    MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
    request.setMethod("POST");
    request.setContentType(FileUploadBase.MULTIPART_FORM_DATA);
    request.addParameter("champ1", "valeur1");    
    byte[] content = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("FrenchScrum.odp"));
    assertNotNull(content);
    request.addFile(new MockMultipartFile("FrenchScrum.odp", content));
    assertNotNull(content);
  }
}
