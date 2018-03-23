/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.delegation;

import java.util.Objects;

/**
 * A responsibility delegated from a user to another in a Silverpeas component instance; it is the
 * role a delegate is going to play in the context of a delegation of roles between two users.
 * <p>
 * A delegated responsibility is a role a user can play in a given component instance in place of
 * another user. Such a responsibility is then always related to a delegation. Playing such a role
 * gives a delegate the entitlement to perform a set of functions in this component instance even
 * if he hasn't initially the entitlement to do so.
 * </p>
 * @author mmoquillon
 */
public class DelegatedResponsibility {

  private String role;
  private Delegation delegation;

  protected DelegatedResponsibility() {
    // for JPA
  }

  /**
   * Constructs a new delegated responsibility defined by the specified role to play and by the
   * delegation under which the role will be played. The role must be one of the delegator.
   * <p>
   * Each role provides some rights to perform a set of well-defined operations in an application
   * instance.
   * </p>
   * @param delegation the existing delegation for which this responsibility will be taken.
   * @param role the name of a role of the delegator that the delegate can play.
   */
  public DelegatedResponsibility(Delegation delegation, final String role) {
    Objects.requireNonNull(role, "The role mustn't be null");
    Objects.requireNonNull(delegation, "The delegation mustn't be null");

    this.role = role;
    this.delegation = delegation;
  }

  /**
   * Gets the role that is delegated to the delegate by the delegator in this responsibility.
   * @return the name of a delegator's role.
   */
  public String getRole() {
    return role;
  }

  /**
   * Gets the delegation to which this delegated responsibility is related.
   * @return a {@link Delegation} instance.
   */
  public Delegation getDelegation() {
    return delegation;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DelegatedResponsibility)) {
      return false;
    }
    final DelegatedResponsibility that = (DelegatedResponsibility) o;
    return Objects.equals(role, that.role) && Objects.equals(delegation, that.delegation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, delegation);
  }
}
  