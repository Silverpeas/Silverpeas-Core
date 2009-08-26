/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.util;

import org.junit.*;
import static org.junit.Assert.*;

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
    assertEquals("/silverpeas/attached_file/componentId/myComponent12/attachmentId/18512/lang//name/toto_le_beau.JPG",
        url);
    url = FileServerUtils.getAttachmentURL("myComponent12", "toto_le_beau.JPG", "18512", null);
    assertNotNull(url);
    assertEquals("/silverpeas/attached_file/componentId/myComponent12/attachmentId/18512/lang/fr/name/toto_le_beau.JPG",
        url);
  }
}
