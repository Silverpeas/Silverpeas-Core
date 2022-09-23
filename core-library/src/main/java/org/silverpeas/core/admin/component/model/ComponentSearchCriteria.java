/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.user.model.UserDetail;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Criteria used to search some component instances in Silverpeas. The component instances must be
 * searchable.
 *
 * @author mmoquillon
 */
public class ComponentSearchCriteria implements Serializable {
  private static final long serialVersionUID = -8747314498972863730L;

  private List<String> componentInstanceIds;
  private String workspaceId;
  private UserDetail user;

  public ComponentSearchCriteria onWorkspace(String workspaceId) {
    this.workspaceId = workspaceId;
    return this;
  }

  public ComponentSearchCriteria onComponentInstances(List<String> instanceIds) {
    this.componentInstanceIds = ofNullable(instanceIds)
        .stream()
        .flatMap(Collection::stream)
        .sorted()
        .collect(Collectors.toList());
    return this;
  }

  public ComponentSearchCriteria onUser(final UserDetail user) {
    this.user = user;
    return this;
  }

  public boolean hasCriterionOnComponentInstances() {
    return this.componentInstanceIds != null && !this.componentInstanceIds.isEmpty();
  }

  public boolean hasCriterionOnWorkspace() {
    return this.workspaceId != null;
  }

  @SuppressWarnings("unused")
  public boolean hasCriterionOnUser() {
    return this.user != null;
  }

  public List<String> getComponentInstanceIds() {
    return componentInstanceIds;
  }

  public String getWorkspaceId() {
    return workspaceId;
  }

  public UserDetail getUser() {
    return this.user;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ComponentSearchCriteria that = (ComponentSearchCriteria) o;
    return Objects.equals(componentInstanceIds, that.componentInstanceIds) &&
        Objects.equals(workspaceId, that.workspaceId) && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(componentInstanceIds, workspaceId, user);
  }
}
