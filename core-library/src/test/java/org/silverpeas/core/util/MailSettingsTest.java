/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.kernel.test.UnitTest;

import javax.mail.internet.InternetAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author ehugonnet
 */
@UnitTest
class MailSettingsTest {

  @AfterEach
  void tearDown() {
    MailSettings.reloadConfiguration(null);
  }

  /**
   * Test of isDomainAuthorized method, of class MailSettings.
   */
  @Test
  void testIsDomainAuthorized() {
    String email = "toto@silverpeas.com";
    boolean result = MailSettings.isDomainAuthorized(email);
    assertThat(result, is(true));
    MailSettings.reloadConfiguration("silverpeas.org");
    result = MailSettings.isDomainAuthorized(email);
    assertThat(result, is(false));
    MailSettings.reloadConfiguration("Silverpeas.COM,silverpeas.org");
    result = MailSettings.isDomainAuthorized(email);
    assertThat(result, is(true));
  }

  /**
   * Test of getAuthorizedEmail method, of class MailSettings.
   */
  @Test
  void testGetAuthorizedEmail() {
    MailSettings.reloadConfiguration("Silverpeas.COM,silverpeas.org");
    String authorizedEmail = "toto@silverpeas.com";
    String result = MailSettings.getAuthorizedEmail(authorizedEmail);
    assertThat(result, is(authorizedEmail));
    String unauthorizedEmail = "toto@slashdot.com";
    result = MailSettings.getAuthorizedEmail(unauthorizedEmail);
    assertThat(result, is("silverpeas@silverpeas.com"));
  }

  /**
   * Test of getAuthorizedEmail method, of class MailSettings.
   */
  @Test
  void testGetAuthorizedEmailWithNoDomain() {
    MailSettings.reloadConfiguration(null);
    String authorizedEmail = "toto@silverpeas.com";
    String result = MailSettings.getAuthorizedEmail(authorizedEmail);
    assertThat(result, is(authorizedEmail));
    MailSettings.reloadConfiguration("");
    authorizedEmail = "toto@slashdot.com";
    result = MailSettings.getAuthorizedEmail(authorizedEmail);
    assertThat(result, is(authorizedEmail));
  }

  @Test
  void testGetAuthorizedEmailAddress() throws Exception {
    MailSettings.reloadConfiguration("Silverpeas.COM,silverpeas.org");
    String authorizedEmail = "toto@silverpeas.com";
    InternetAddress result = MailSettings.getAuthorizedEmailAddress(authorizedEmail, "Toto");
    assertThat(result.getAddress(), is(authorizedEmail));
    assertThat(result.getPersonal(), is("Toto"));
    String unauthorizedEmail = "toto@slashdot.com";
    result = MailSettings.getAuthorizedEmailAddress(unauthorizedEmail, "toto");
    assertThat(result.getAddress(), is("silverpeas@silverpeas.com"));
    assertThat(result.getPersonal(), is("Silverpeas"));
  }
}
