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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.socialnetwork.connectors;

import java.io.Serializable;

/**
 * A normalized model representing a service provider user profile. The structure of a "UserProfile"
 * varies across providers (see the difference between Facebook and Twitter, for example). That
 * said, there are generally a common set of profile fields that apply across providers. This model
 * provides access to those common fields in a uniform way. This is particularly useful for
 * pre-populating a local application registration form with provider profile data during a provider
 * sign-in attempt.
 *
 * @author Keith Donald
 */
public class UserProfile implements Serializable {

  /**
   * Shared, empty profile that when used indicates no profile data is available (all property
   * values are null).
   */
  public static final UserProfile EMPTY = new UserProfile(null, null, null, null, null);

  private final String name;

  private final String firstName;

  private final String lastName;

  private final String email;

  private final String username;

  /**
   * The user's registered full name e.g. Keith Donald. May be null if not exposed/supported by the
   * provider.
   */
  public String getName() {
    return name;
  }

  /**
   * The user's registered first name e.g. Keith. May be null if not exposed/supported by the
   * provider.
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * The user's registered last name e.g. Donald. May be null if not exposed/supported by the
   * provider.
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * The user's registered email address. May be null if not exposed/supported by the provider.
   */
  public String getEmail() {
    return email;
  }

  /**
   * The user's registered username e.g. kdonald. May be null if not exposed/supported by the
   * provider.
   */
  public String getUsername() {
    return username;
  }

  // builder only

  UserProfile(String name, String firstName, String lastName, String email, String username) {
    this.name = name;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.username = username;
  }

}

  