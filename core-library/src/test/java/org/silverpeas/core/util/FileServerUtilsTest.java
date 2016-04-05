/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.util;

import org.junit.BeforeClass;
import org.junit.Test;
import org.silverpeas.core.util.file.FileServerUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author ehugonnet
 */
public class FileServerUtilsTest {

  @BeforeClass
  public static void setUp() {
    // code that will be invoked before this test starts
    }

  @Test
  public void testGetAttachmentURL() {
    String url = FileServerUtils.getAttachmentURL("myComponent12", "toto_le_beau.JPG", "18512", "");
    assertNotNull(url);
    assertEquals("/attached_file/componentId/myComponent12/attachmentId/18512/lang//name/toto_le_beau.JPG",
        url);
    url = FileServerUtils.getAttachmentURL("myComponent12", "toto_le_beau.JPG", "18512", null);
    assertNotNull(url);
    assertEquals("/attached_file/componentId/myComponent12/attachmentId/18512/lang/fr/name/toto_le_beau.JPG",
        url);
  }

  @Test
  public void testGetUrlToTempDir()
      throws Exception {
    String url =  FileServerUtils.getUrlToTempDir("hello_world.pdf");
    assertEquals("/silverpeas/TempFileServer/hello_world.pdf", url);
    url =  FileServerUtils.getUrlToTempDir("Mon œuvre.pdf");
    assertEquals("/silverpeas/TempFileServer/Mon%2520%25C5%2593uvre.pdf", url);
    url =  FileServerUtils.getUrlToTempDir("Mon œuvre & mon été.pdf");
    assertEquals("/silverpeas/TempFileServer/Mon%2520%25C5%2593uvre%2520&%2520mon%2520%25C3%25A9t%25C3%25A9.pdf", url);
  }

}
