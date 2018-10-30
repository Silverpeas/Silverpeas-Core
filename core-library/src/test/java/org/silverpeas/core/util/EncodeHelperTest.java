/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.util;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.UnitTest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author ehugonnet
 */
@UnitTest
public class EncodeHelperTest {

  /**
   * Test of encodeFilename method, of class WebEncodeHelper.
   *
   * @throws Exception
   */
  @Test
  public void testEncodeFilename() throws Exception {
    String filename = "test.pdf";
    String result = WebEncodeHelper.encodeFilename(filename);
    assertThat(result, is("=?UTF-8?B?dGVzdC5wZGY=?="));
    filename = "TestAccentué.pdf";
    result = WebEncodeHelper.encodeFilename(filename);
    assertThat(result, is("=?UTF-8?B?VGVzdEFjY2VudHXDqS5wZGY=?="));
  }

  @Test
  public void testEncodeHtmlParagraph() {
    String content = "Ceci est un test avec\nune nouvelle ligne\r\nun retour à la ligne\r\tindenté";
    String result = WebEncodeHelper.javaStringToHtmlParagraphe(content);
    assertThat(result, is("Ceci est un test avec<br/>une nouvelle ligne<br/>un retour &agrave; "
        + "la ligne&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;indent&eacute;"));
  }
}
