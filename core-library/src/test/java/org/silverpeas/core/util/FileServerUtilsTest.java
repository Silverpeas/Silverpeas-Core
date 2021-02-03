/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.util.file.FileServerUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author ehugonnet
 */
@UnitTest
public class FileServerUtilsTest {

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

  @Test
  public void replaceAccentChars() {
    String allInput = "²1&234567890°+=})]à@ç\\_`è-|[({'#\"~é?,.;:!§ù%*µ¤$£";
    String resInput = "²1&234567890_+=})]a@c\\_`e-|[({'#\"~e?,.;:!§u%*µ¤$£";
    Map<String, String> expectedResultMapping = new HashMap<>();
    expectedResultMapping.put("é", "e");
    expectedResultMapping.put("è", "e");
    expectedResultMapping.put("ë", "e");
    expectedResultMapping.put("ê", "e");
    expectedResultMapping.put("ö", "o");
    expectedResultMapping.put("ô", "o");
    expectedResultMapping.put("õ", "o");
    expectedResultMapping.put("ò", "o");
    expectedResultMapping.put("ï", "i");
    expectedResultMapping.put("î", "i");
    expectedResultMapping.put("ì", "i");
    expectedResultMapping.put("ñ", "n");
    expectedResultMapping.put("ü", "u");
    expectedResultMapping.put("û", "u");
    expectedResultMapping.put("ù", "u");
    expectedResultMapping.put("ç", "c");
    expectedResultMapping.put("à", "a");
    expectedResultMapping.put("ä", "a");
    expectedResultMapping.put("ã", "a");
    expectedResultMapping.put("â", "a");
    expectedResultMapping.put("°", "_");
    expectedResultMapping.put(allInput, resInput);

    expectedResultMapping.forEach((key, value) -> {
      String result = FileServerUtils.replaceAccentChars(key);
      assertEquals(value, result);
    });
  }
}
