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

package org.silverpeas.core.admin.user.service;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.ServiceProvider;

/**
 * A factory to constructs new users. The implementation of the user is taken in charge by the
 * implementation of this interface. This factory is to allow to decorrelate the knowledge of the
 * implementation details of the {@link org.silverpeas.core.admin.user.model.User} from the client
 * code. The factory delegate the concrete {@link User} construction to a builder.
 *
 * @author mmoquillon
 */
public interface UserFactory {

  /**
   * Gets the instance of the implementation of the interface.
   *
   * @return an implementation of {@link UserFactory}.
   */
  static UserFactory get() {
    return ServiceProvider.getService(UserFactory.class);
  }

  /**
   * Gets a new {@link UserBuilder} to construct a new {@link User} object.
   *
   * @return a {@link UserBuilder} object.
   */
  UserBuilder builder();

  /**
   * A builder of {@link User}s. It is a facility provided by the factory to create a new plain
   * Silverpeas user.
   */
  interface UserBuilder {

    /**
     * Sets the unique identifier of the user. For a new user to be persisted in Silverpeas, this
     * will be ignored by the persistence layer.
     *
     * @param id the unique identifier of a user.
     * @return the builder itself.
     */
    UserBuilder setId(String id);

    /**
     * Sets the firstname of the user.
     * @param firstName the firstname.
     * @return the builder itself.
     */
    UserBuilder setFirstName(String firstName);

    /**
     * Sets the lastname of the user.
     * @param lastName the lastname
     * @return the builder itself.
     */
    UserBuilder setLastName(String lastName);

    /**
     * Sets the email address of the user.
     * @param email the email address.
     * @return the builder itself.
     */
    UserBuilder setEmailAddress(String email);

    /**
     * Constructs a {@link User} object by using the previous set properties.
     * @return a {@link User}
     */
    User build();
  }
}
