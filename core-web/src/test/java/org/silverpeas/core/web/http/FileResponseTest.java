/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.web.http;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.UnitTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.silverpeas.core.web.http.FileResponse.encodeAttachmentFilenameAsUtf8;
import static org.silverpeas.core.web.http.FileResponse.encodeInlineFilenameAsUtf8;

/**
 * @author silveryocha
 */
@UnitTest
public class FileResponseTest {

  @Test
  public void testEncodeAttachmentFilenameWithCombinedCharactersAsUtf8() throws Exception {
    String source = "3- mémoi déf_Paie1_22673_src.pdf";
    String result = "attachment; filename*=UTF-8''3-%20m%C3%A9moi%20d%C3%A9f_Paie1_22673_src.pdf";
    assertThat(encodeAttachmentFilenameAsUtf8(source), Matchers.is(result));
  }

  @Test
  public void testEncodeFilenameWithCombinedCharactersAsUtf8() throws Exception {
    String source = "3- mémoi déf_Paie1_22673_src.pdf";
    String result = "inline; filename*=UTF-8''3-%20m%C3%A9moi%20d%C3%A9f_Paie1_22673_src.pdf";
    assertThat(encodeInlineFilenameAsUtf8(source), Matchers.is(result));
  }

  @Test
  public void testEncodeFilenameWithSpecialCharactersAsUtf8() throws Exception {
    String source = "N_d├®ro'x-pr├®p\"s-MN";
    String result = "inline; filename*=UTF-8''N_d%E2%94%9C%C2%AEro%27x-pr%E2%94%9C%C2%AEp%22s-MN";
    assertThat(encodeInlineFilenameAsUtf8(source), Matchers.is(result));
  }

  @Test
  public void testEncodeFilenameWithCommonCharactersAsUtf8() throws Exception {
    String source = "²1&234567890°+=})]à@ç_`è-|[({'#\"~é?,.;:!§ù%*µ¤$£";
    String result =
        "inline; filename*=UTF-8''%C2%B21%26234567890%C2%B0%2B%3D%7D%29%5D%C3%A0%40%C3%A7_%60%C3" +
            "%A8-%7C%5B%28%7B%27%23%22%7E%C3%A9%3F%2C" +
            ".%3B%3A%21%C2%A7%C3%B9%25*%C2%B5%C2%A4%24%C2%A3";
    assertThat(encodeInlineFilenameAsUtf8(source), Matchers.is(result));
  }
}