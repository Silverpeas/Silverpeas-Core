/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.mail;

import org.silverpeas.core.util.MailUtil;
import org.silverpeas.core.util.StringUtil;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;

/**
 * Specifies an email address.
 * @author Yohann Chastagnier
 */
public class MailAddress {

  private String email = "";
  private String name = "";

  /**
   * Gets a new instance of {@link MailAddress}.
   * @param email the email.
   * @return a new instance of {@link MailAddress}.
   */
  public static MailAddress eMail(String email) {
    MailAddress mailAddress = new MailAddress();
    if (StringUtil.isDefined(email)) {
      mailAddress.email = email;
    }
    return mailAddress;
  }

  /**
   * Hidden constructor.
   */
  private MailAddress() {
  }

  /**
   * Permits to specify the name of the person or organization linked to the email.
   * @param name the functional name associated to the email. If the name is not defined, then an
   * empty string is taken into account.
   * @return the completed instance.
   */
  public MailAddress withName(String name) {
    this.name = StringUtil.defaultStringIfNotDefined(name);
    return this;
  }

  /**
   * Gets the email.
   * @return the email.
   */
  public String getEmail() {
    return email;
  }

  /**
   * Gets the name.
   * @return the functional name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets from this defined mail address the corresponding authorized {@link InternetAddress}
   * according to the ruled defined by {@link MailUtil#getAuthorizedEmailAddress(String, String)}.
   * @return the authorized {@link MailUtil#getAuthorizedEmailAddress(String, String)}.
   * @throws UnsupportedEncodingException
   * @throws AddressException
   */
  public InternetAddress getAuthorizedInternetAddress()
      throws UnsupportedEncodingException, AddressException {
    return MailUtil.getAuthorizedEmailAddress(getEmail(), getName());
  }

  @SuppressWarnings("NonFinalFieldReferenceInEquals")
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final MailAddress that = (MailAddress) o;
    return email.equals(that.email) && name.equals(that.name);
  }

  @SuppressWarnings("NonFinalFieldReferencedInHashCode")
  @Override
  public int hashCode() {
    int result = email.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
