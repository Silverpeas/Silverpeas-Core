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

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.SettingBundleStub;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

import static java.lang.String.format;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoField.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.util.URLUtil.*;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class URLUtilTest {

  private static final String SILVERPEAS_VERSION = "6.1.1";
  private static final String APP_URL = URLUtil.getApplicationURL();

  private File tempFile;

  protected SettingBundleStub urlSettings = new SettingBundleStub("org.silverpeas.util.url");

  @BeforeEach
  void setup() throws Exception {
    tempFile = File.createTempFile("cache",".busting");
    urlSettings = new SettingBundleStub(URLUtil.class, "settings");
    urlSettings.beforeEach(null);
  }

  @AfterEach
  void clean() throws Exception {
    FileUtils.deleteQuietly(tempFile);
    urlSettings.afterEach(null);
  }

  @Test
  void verifyAddFingerprintVersionOn() {
    setSilverpeasVersion(SILVERPEAS_VERSION);
    assertVariousResourceURLs("611");
  }

  @Test
  void verifyAddFingerprintVersionOnWithLastFileModificationMethod() {
    urlSettings.put("cache.busting.method", "LAST_FILE_MODIFICATION");
    urlSettings.put("cache.busting.lastFileModification.path", tempFile.getPath());
    setSilverpeasVersion(SILVERPEAS_VERSION);
    final String fingerprintBase = new DateTimeFormatterBuilder()
        .appendValue(MONTH_OF_YEAR, 2)
        .appendValue(DAY_OF_MONTH, 2)
        .appendValue(HOUR_OF_DAY, 2)
        .appendValue(MINUTE_OF_HOUR, 2)
        .toFormatter()
        .format(new Date(tempFile.lastModified()).toInstant().atZone(systemDefault()));
    assertVariousResourceURLs(fingerprintBase);
  }

  @Test
  void verifyAddFingerprintVersionOnWithWrongLastFileModificationMethod() {
    urlSettings.put("cache.busting.method", "LAST_FILE_MODIFICATION");
    setSilverpeasVersion(SILVERPEAS_VERSION);
    assertVariousResourceURLs("611");
  }

  @Test
  void verifyAddFingerprintVersionOnWithServerStartMethod() {
    urlSettings.put("cache.busting.method", "SERVER_START");
    setSilverpeasVersion(SILVERPEAS_VERSION);
    final String fingerprintBase = new DateTimeFormatterBuilder()
        .appendValue(MONTH_OF_YEAR, 2)
        .appendValue(DAY_OF_MONTH, 2)
        .appendValue(HOUR_OF_DAY, 2)
        .appendValue(MINUTE_OF_HOUR, 2)
        .toFormatter()
        .format(LocalDateTime.now());
    assertVariousResourceURLs(fingerprintBase);
  }

  private void assertVariousResourceURLs(final String expectedFingerprintBase) {
    assertThat(getSilverpeasFingerprint(), is("." + expectedFingerprintBase));
    assertThat(addFingerprintVersionOn(""), is("?v=" + expectedFingerprintBase));
    assertThat(addFingerprintVersionOn("toto.css"), is("toto.css?v=" + expectedFingerprintBase));
    assertThat(addFingerprintVersionOn("/toto.css"), is("/toto.css?v=" + expectedFingerprintBase));
    assertThat(addFingerprintVersionOn("/toto.css?id=34"), is("/toto.css?id=34&v=" + expectedFingerprintBase));
    assertThat(addFingerprintVersionOn(APP_URL + "toto.css"), is(format("%stoto.%s.css", APP_URL, expectedFingerprintBase)));
    assertThat(addFingerprintVersionOn(APP_URL + "toto.css?id=34"), is(format("%stoto.%s.css?id=34", APP_URL, expectedFingerprintBase)));
    assertThat(addFingerprintVersionOn(APP_URL + "toto.css?logo=sp.png"), is(format("%stoto.%s.css?logo=sp.png", APP_URL, expectedFingerprintBase)));
    assertThat(addFingerprintVersionOn(APP_URL + "toto?logo=sp.png"), is(format("%stoto?logo=sp.png&v=%s", APP_URL, expectedFingerprintBase)));
  }
}