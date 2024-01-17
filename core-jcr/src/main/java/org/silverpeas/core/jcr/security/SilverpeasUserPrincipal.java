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
 *
 */

package org.silverpeas.core.jcr.security;

import org.silverpeas.core.admin.user.model.User;

import javax.annotation.Nonnull;
import java.security.Principal;
import java.util.Objects;

/**
 * Principal representing a user accessing the JCR. This user can be either a user in Silverpeas or
 * the JCR system user. The JCR system user is a predefined one, and it is a virtual user. To check
 * if the user behind the scene is the system one, just invoke
 * {@link SilverpeasUserPrincipal#isSystem()}.
 * @author mmoquillon
 */
public class SilverpeasUserPrincipal implements Principal {

  private final User user;
  private final AccessContext context;

  public SilverpeasUserPrincipal(@Nonnull final User user) {
    Objects.requireNonNull(user);
    this.user = user;
    this.context = AccessContext.EMPTY;
  }

  public SilverpeasUserPrincipal(@Nonnull final User user, final AccessContext context) {
    Objects.requireNonNull(user);
    this.user = user;
    this.context = context;
  }

  @Override
  public String getName() {
    return user.getId();
  }

  /**
   * Gets optionally the user in Silverpeas behind this principal. Nothing is returned when the
   * represented user is the JCR system one as it presents nobody in the user database, being a
   * virtual user.
   * @return a user in Silverpeas or nothing if this user is the JCR system one.
   */
  public User getUser() {
    return this.user;
  }

  /**
   * Gets the context under which the user denoted by this principal accesses the JCR.
   * @return the access context of the underlying user.
   */
  public AccessContext getAccessContext() {
    return context;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SilverpeasUserPrincipal that = (SilverpeasUserPrincipal) o;
    return Objects.equals(this.user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user);
  }

  /**
   * Is this principal represents the JCR system user?
   * @return true if this principal is on the system user in JCR.
   */
  public boolean isSystem() {
    return user.isSystem();
  }
}
