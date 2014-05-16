/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.token;

import org.junit.Test;
import org.silverpeas.web.token.TokenSettingTemplate.Parameter;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on the compilation of the template on the token setting.
 *
 * @author mmoquillon
 */
public class TokenSettingTemplateTest {

  private static final String TOKEN_NAME = "TokenName";
  private static final String TOKEN_VALUE = "TokenValue";

  public TokenSettingTemplateTest() {
  }

  @Test
  public void applyTheTokenSettingTemplate() {
    TokenSettingTemplate template = new TokenSettingTemplate();
    String result = template.apply(
        new Parameter(TokenSettingTemplate.SESSION_TOKEN_NAME_PARAMETER, TOKEN_NAME),
        new Parameter(TokenSettingTemplate.SESSION_TOKEN_VALUE_PARAMETER, TOKEN_VALUE));

    assertThat(result, containsString(TOKEN_NAME));
    assertThat(result, containsString(TOKEN_VALUE));
    assertThat(result, not(containsString(TokenSettingTemplate.SESSION_TOKEN_NAME_PARAMETER)));
    assertThat(result, not(containsString(TokenSettingTemplate.SESSION_TOKEN_VALUE_PARAMETER)));
  }

  @Test
  public void applyTheTokenSettingTemplateWithMissingParameters() {
    TokenSettingTemplate template = new TokenSettingTemplate();
    String result = template.apply();

    assertThat(result, not(containsString(TokenSettingTemplate.SESSION_TOKEN_NAME_PARAMETER)));
    assertThat(result, not(containsString(TokenSettingTemplate.SESSION_TOKEN_VALUE_PARAMETER)));
    assertThat(result, not(containsString(TOKEN_NAME)));
    assertThat(result, not(containsString(TOKEN_VALUE)));
  }

}
