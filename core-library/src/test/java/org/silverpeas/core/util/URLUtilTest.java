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

package org.silverpeas.core.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.util.URLUtil.addFingerprintVersionOn;
import static org.silverpeas.core.util.URLUtil.setSilverpeasVersion;

/**
 * @author silveryocha
 */
class URLUtilTest {

  private static final String SILVERPEAS_VERSION = "6.1.1";
  private static final String NORMALIZED_SILVERPEAS_VERSION = "611";
  private static final String APP_URL = URLUtil.getApplicationURL();

  @BeforeAll
  static void setup() {
    setSilverpeasVersion(SILVERPEAS_VERSION);
  }

  @Test
  void verifyAddFingerprintVersionOn() {
    assertThat(addFingerprintVersionOn(""), is("?v=" + NORMALIZED_SILVERPEAS_VERSION));
    assertThat(addFingerprintVersionOn("toto.css"), is("toto.css?v=" + NORMALIZED_SILVERPEAS_VERSION));
    assertThat(addFingerprintVersionOn("/toto.css"), is("/toto.css?v=" + NORMALIZED_SILVERPEAS_VERSION));
    assertThat(addFingerprintVersionOn("/toto.css?id=34"), is("/toto.css?id=34&v=" + NORMALIZED_SILVERPEAS_VERSION));
    assertThat(addFingerprintVersionOn(APP_URL + "toto.css"), is(format("%stoto.%s.css", APP_URL, NORMALIZED_SILVERPEAS_VERSION)));
    assertThat(addFingerprintVersionOn(APP_URL + "toto.css?id=34"), is(format("%stoto.%s.css?id=34", APP_URL, NORMALIZED_SILVERPEAS_VERSION)));
    assertThat(addFingerprintVersionOn(APP_URL + "toto.css?logo=sp.png"), is(format("%stoto.%s.css?logo=sp.png", APP_URL, NORMALIZED_SILVERPEAS_VERSION)));
    assertThat(addFingerprintVersionOn(APP_URL + "toto?logo=sp.png"), is(format("%stoto?logo=sp.png&v=%s", APP_URL, NORMALIZED_SILVERPEAS_VERSION)));
  }
}