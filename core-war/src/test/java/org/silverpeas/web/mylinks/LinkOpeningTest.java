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
package org.silverpeas.web.mylinks;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the way a favorite link is opened from the page of the links.
 */
class LinkOpeningTest {

  private static final String SERVER_URL = "https://intranet.silverpeas.org";

  @ParameterizedTest(name = "{0} (popup: {1}) is opened {2}")
  @CsvSource({
      // a link to Silverpeas is displayed in the content area, as any page of Silverpeas
      "/silverpeas/Rkmelia/kmelia1/Main,              false, IN_CONTENT",
      "/website/doc/index.html,                       false, IN_CONTENT",
      "https://intranet.silverpeas.org/silverpeas/Main, false, IN_CONTENT",
      "HTTPS://Intranet.Silverpeas.org/silverpeas/Main, false, IN_CONTENT",
      // an external page is isolated from Silverpeas into a sandboxed frame
      "https://www.silverpeas.org/docs,               false, IN_SANDBOXED_FRAME",
      "https://intranet.silverpeas.org.evil.com/page, false, IN_SANDBOXED_FRAME",
      // an unsecured or non web page can't be framed by Silverpeas
      "http://www.silverpeas.org,                     false, IN_NEW_WINDOW",
      "ftp://ftp.silverpeas.org/pub,                  false, IN_NEW_WINDOW",
      // the user asked for a new window
      "https://www.silverpeas.org/docs,               true,  IN_NEW_WINDOW",
      "/silverpeas/Rkmelia/kmelia1/Main,              true,  IN_NEW_WINDOW"
  })
  void aLinkIsOpenedAccordingToItsTargetAndToTheChoiceOfTheUser(String url, boolean popup,
      LinkOpening expected) {
    assertThat(LinkOpening.of(url, popup, SERVER_URL), is(expected));
  }
}
