/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.admin.user.notification.role;

import org.silverpeas.core.notification.system.AbstractResourceEvent;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Event relating a change has been done in a the list of users and user groups of a given role in
 * some component instances. For example, when a user doesn't play anymore a given role in a
 * component instance or he has been removed from a group playing a role in a component instance.
 *
 * @author mmoquillon
 */
public class UserRoleEvent extends AbstractResourceEvent<UserRoleEvent.UserSet> {

  private final Set<String> instanceIds = new HashSet<>();
  private String role;

  private UserRoleEvent(Type type, UserSet... users) {
    super(type, users);
  }

  /**
   * Gets all component instances for which the list of users of the underlying role has been
   * modified.
   *
   * @return the set of unique identifiers of the component instances concerned by the role change.
   */
  public Set<String> getInstanceIds() {
    return instanceIds;
  }

  /**
   * Gets all the users concerned by the type of operation in the list of users of the underlying
   * role. For example, the users who were removed from the role's list of users.
   *
   * @return a set of unique identifiers of the users targeted by the role change.
   */
  public Set<String> getUserIds() {
    return getTransition().getBefore() == null ? getTransition().getAfter() :
        getTransition().getBefore();
  }

  /**
   * Gets the name of the role for which the list of users has been modified.
   *
   * @return the role name.
   */
  public String getRole() {
    return role;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UserRoleEvent)) return false;
    if (!super.equals(o)) return false;
    UserRoleEvent that = (UserRoleEvent) o;
    return Objects.equals(instanceIds, that.instanceIds) && Objects.equals(role, that.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), instanceIds, role);
  }

  /**
   * Gets a builder of instances of {@link UserRoleEvent} for the specified notification type.
   *
   * @param type the type of the notification the event to build will carry.
   * @return the builder.
   */
  @SuppressWarnings("SameParameterValue")
  static Builder builderFor(Type type) {
    return new Builder(type);
  }

  /**
   * The builder to facilitate the construction of a {@link UserRoleEvent} instance.
   */
  static class Builder {

    private final UserRoleEvent event;

    private Builder(Type type) {
      UserSet users = new UserSet();
      this.event = new UserRoleEvent(type, users, users);
    }

    /**
     * Adds the user concerned by a modification of the users/groups list of a role.
     *
     * @param userId the unique identifier of the user.
     * @return itself.
     */
    Builder userId(String userId) {
      this.event.getUserIds().add(userId);
      return this;
    }

    /**
     * Adds the users concerned by a modification of the users/groups list of a role.
     *
     * @param userIds a collection of unique identifiers of the users.
     * @return itself.
     */
    Builder userIds(Collection<String> userIds) {
      this.event.getUserIds().addAll(userIds);
      return this;
    }

    /**
     * Sets the role for which the list of users/groups has been modified.
     *
     * @param role the name of the role.
     * @return itself.
     */
    Builder role(String role) {
      this.event.role = role;
      return this;
    }

    /**
     * Adds the component instance for which the list of users or groups playing the role has been
     * modified.
     *
     * @param instanceId the unique identifier of a component instance.
     * @return itself.
     */
    Builder instanceId(String instanceId) {
      this.event.instanceIds.add(instanceId);
      return this;
    }

    /**
     * Adds the component instances for which the list of users or groups playing the role has been
     * modified.
     *
     * @param instanceIds a collection of unique identifiers of component instances.
     * @return itself.
     */
    Builder instanceIds(Collection<String> instanceIds) {
      this.event.instanceIds.addAll(instanceIds);
      return this;
    }

    /**
     * Builds the {@link UserRoleEvent} instance with the information passed to the builder.
     *
     * @return a new {@link UserRoleEvent} instance.
     */
    UserRoleEvent build() {
      return event;
    }
  }

  /**
   * Set of the users concerned by a change in the list of users of a given role.
   */
  public static class UserSet extends HashSet<String> implements Serializable {

    private UserSet() {
      super();
    }
  }
}
  