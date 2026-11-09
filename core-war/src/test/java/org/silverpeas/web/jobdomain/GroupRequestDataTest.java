/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.jobdomain;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the reading of the {@link GroupRequestData} from an HTTP request.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class GroupRequestDataTest {

  private HttpServletRequest httpServletRequestMock;

  @BeforeEach
  void setup() {
    httpServletRequestMock = mock(HttpServletRequest.class);
    when(httpServletRequestMock.getMethod()).thenReturn("POST");
  }

  @Test
  void groupDataAreTakenAsTypedWithoutBeingHtmlEncoded() {
    setHttpParameter("groupName", "Direction de l'eau");
    setHttpParameter("groupDescription", "Agents <permanents> & \"vacataires\"");
    setHttpParameter("groupRule", "(DC_title = 'Chef d''équipe') & (DS_AccessLevel = U)");

    GroupRequestData data = decode();

    assertThat(data.getName(), is("Direction de l'eau"));
    assertThat(data.getDescription(), is("Agents <permanents> & \"vacataires\""));
    assertThat(data.getRule(), is("(DC_title = 'Chef d''équipe') & (DS_AccessLevel = U)"));
  }

  @Test
  void htmlEntitiesLeftInGroupDataAreDecoded() {
    setHttpParameter("groupName", "Direction de l&#39;eau");
    setHttpParameter("groupDescription", "Eau &amp; assainissement");
    setHttpParameter("groupRule", "(DC_a = x) &amp; (DC_b = y)");

    GroupRequestData data = decode();

    assertThat(data.getName(), is("Direction de l'eau"));
    assertThat(data.getDescription(), is("Eau & assainissement"));
    assertThat(data.getRule(), is("(DC_a = x) & (DC_b = y)"));
  }

  private GroupRequestData decode() {
    return RequestParameterDecoder.decode(HttpRequest.decorate(httpServletRequestMock),
        GroupRequestData.class);
  }

  private void setHttpParameter(String parameterName, String parameterValue) {
    when(httpServletRequestMock.getParameter(parameterName)).thenReturn(parameterValue);
  }
}
