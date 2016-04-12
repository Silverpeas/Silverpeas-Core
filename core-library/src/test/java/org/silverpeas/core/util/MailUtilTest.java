/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util;

import javax.mail.internet.InternetAddress;

import org.junit.After;
import org.junit.Test;
import org.silverpeas.core.util.MailUtil;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
public class MailUtilTest {

  @After
  public void tearDown() {
    MailUtil.reloadConfiguration(null);
  }

  /**
   * Test of isDomainAuthorized method, of class MailUtil.
   */
  @Test
  public void testIsDomainAuthorized() {
    String email = "toto@silverpeas.com";
    boolean result = MailUtil.isDomainAuthorized(email);
    assertThat(result, is(true));
    MailUtil.reloadConfiguration("silverpeas.org");
    result = MailUtil.isDomainAuthorized(email);
    assertThat(result, is(false));
    MailUtil.reloadConfiguration("Silverpeas.COM,silverpeas.org");
    result = MailUtil.isDomainAuthorized(email);
    assertThat(result, is(true));
  }

  /**
   * Test of getAuthorizedEmail method, of class MailUtil.
   */
  @Test
  public void testGetAuthorizedEmail() {
    MailUtil.reloadConfiguration("Silverpeas.COM,silverpeas.org");
    String authorizedEmail = "toto@silverpeas.com";
    String result = MailUtil.getAuthorizedEmail(authorizedEmail);
    assertThat(result, is(authorizedEmail));
    String unauthorizedEmail = "toto@slashdot.com";
    result = MailUtil.getAuthorizedEmail(unauthorizedEmail);
    assertThat(result, is("silverpeas@silverpeas.com"));
  }

  /**
   * Test of getAuthorizedEmail method, of class MailUtil.
   */
  @Test
  public void testGetAuthorizedEmailWithNoDomain() {
    MailUtil.reloadConfiguration(null);
    String authorizedEmail = "toto@silverpeas.com";
    String result = MailUtil.getAuthorizedEmail(authorizedEmail);
    assertThat(result, is(authorizedEmail));
    MailUtil.reloadConfiguration("");
    authorizedEmail = "toto@slashdot.com";
    result = MailUtil.getAuthorizedEmail(authorizedEmail);
    assertThat(result, is(authorizedEmail));
  }

  @Test
  public void testGetAuthorizedEmailAddress() throws Exception {
    MailUtil.reloadConfiguration("Silverpeas.COM,silverpeas.org");
    String authorizedEmail = "toto@silverpeas.com";
    InternetAddress result = MailUtil.getAuthorizedEmailAddress(authorizedEmail, "Toto");
    assertThat(result.getAddress(), is(authorizedEmail));
    assertThat(result.getPersonal(), is("Toto"));
    String unauthorizedEmail = "toto@slashdot.com";
    result = MailUtil.getAuthorizedEmailAddress(unauthorizedEmail, "toto");
    assertThat(result.getAddress(), is("silverpeas@silverpeas.com"));
    assertThat(result.getPersonal(), is("Silverpeas"));
  }
}
