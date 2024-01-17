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
package org.silverpeas.core.mail;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.UnitTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@UnitTest
class MailAddressTest {

  @Test
  void emptyEmail() {
    MailAddress mailAddress = MailAddress.eMail(null);
    assertThat(mailAddress.getEmail(), is(emptyString()));
    assertThat(mailAddress.getName(), is(emptyString()));

    mailAddress = MailAddress.eMail("");
    assertThat(mailAddress.getEmail(), is(emptyString()));
    assertThat(mailAddress.getName(), is(emptyString()));

    mailAddress = MailAddress.eMail("   ");
    assertThat(mailAddress.getEmail(), is(emptyString()));
    assertThat(mailAddress.getName(), is(emptyString()));

    mailAddress = MailAddress.eMail("null");
    assertThat(mailAddress.getEmail(), is(emptyString()));
    assertThat(mailAddress.getName(), is(emptyString()));
  }

  @Test
  void anEMailOnly() {
    String anEMail = "anEMail";
    MailAddress mailAddress = MailAddress.eMail(anEMail);
    assertThat(mailAddress.getEmail(), is(anEMail));
    assertThat(mailAddress.getName(), is(emptyString()));

    mailAddress = MailAddress.eMail(anEMail).withName(null);
    assertThat(mailAddress.getEmail(), is(anEMail));
    assertThat(mailAddress.getName(), is(emptyString()));

    mailAddress = MailAddress.eMail(anEMail).withName("");
    assertThat(mailAddress.getEmail(), is(anEMail));
    assertThat(mailAddress.getName(), is(emptyString()));

    mailAddress = MailAddress.eMail(anEMail).withName("   ");
    assertThat(mailAddress.getEmail(), is(anEMail));
    assertThat(mailAddress.getName(), is(emptyString()));

    mailAddress = MailAddress.eMail(anEMail).withName("null");
    assertThat(mailAddress.getEmail(), is(anEMail));
    assertThat(mailAddress.getName(), is(emptyString()));
  }

  @Test
  void anEMailWithName() {
    String anEMail = "anEMail";
    String aName = "aName";
    MailAddress mailAddress = MailAddress.eMail(anEMail).withName(aName);
    assertThat(mailAddress.getEmail(), is(anEMail));
    assertThat(mailAddress.getName(), is(aName));
  }

  @Test
  void equalsAndHashcode() {
    MailAddress base = MailAddress.eMail("email").withName("name");

    MailAddress test = MailAddress.eMail("email").withName("name");
    assertThat(test.equals(base), is(true));
    assertThat(test.hashCode(), is(base.hashCode()));

    test = MailAddress.eMail("email");
    assertThat(test.equals(base), is(false));
    assertThat(test.hashCode(), not(is(base.hashCode())));

    test = MailAddress.eMail("email_").withName("name");
    assertThat(test.equals(base), is(false));
    assertThat(test.hashCode(), not(is(base.hashCode())));
  }
}