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
