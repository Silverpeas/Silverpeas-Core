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
 * "https://www.silverpeas.org/legal/licensing"
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

import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.regex.Pattern;

/**
 * An email address representation.
 *
 * @author mmoquillon
 */
public class EmailAddress {

  private static final String EMAIL_PATTERN
      = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}$";

  private final String address;

  public EmailAddress(final String addressEmail) {
    if (!isValid(addressEmail)) {
      throw new IllegalArgumentException("The specified address isn't a valid email one: " +
          addressEmail);
    }
    this.address = addressEmail;
  }

  public InternetAddress asInternetAddress() {
    try {
      return new InternetAddress(address);
    } catch (AddressException e) {
      // should never occurs
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Validate the form of an email address.
   * <p> Returns <tt>true</tt> only if
   * <ul>
   * <li><tt>aEmailAddress</tt> can successfully construct an
   * {@link javax.mail.internet.InternetAddress}</li>
   * <li>when parsed with "@" as delimiter, <tt>aEmailAddress</tt> contains two tokens which
   * satisfy</li>
   * </ul>
   * <p>
   * The second condition arises since local email addresses, simply of the form "<tt>albert</tt>",
   * for example, are valid for {@link javax.mail.internet.InternetAddress}, but almost always
   * undesired.
   *
   * @param address the address to be validated
   * @return true is the address is a valid email address - false otherwise.
   */
  public static boolean isValid(final String address) {
    if (address == null) {
      return false;
    }
    boolean isOk;
    try {
      new InternetAddress(address);
      isOk = Pattern.matches(EMAIL_PATTERN, address);
    } catch (AddressException ex) {
      isOk = false;
    }
    return isOk;
  }

  @Override
  public String toString() {
    return address;
  }
}
