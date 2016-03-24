/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.user.model.UserDetail;

/**
 * Criteria used to search some component instances in Silverpeas. The component instances must be
 * searchable.
 *
 * @author mmoquillon
 */
public class ComponentSearchCriteria {

  private String componentInstanceId;
  private String workspaceId;
  private UserDetail user;

  public ComponentSearchCriteria onWorkspace(String workspaceId) {
    this.workspaceId = workspaceId;
    return this;
  }

  public ComponentSearchCriteria onComponentInstance(String instanceId) {
    this.componentInstanceId = instanceId;
    return this;
  }

  public ComponentSearchCriteria onUser(final UserDetail user) {
    this.user = user;
    return this;
  }

  public boolean hasCriterionOnComponentInstance() {
    return this.componentInstanceId != null;
  }

  public boolean hasCriterionOnWorkspace() {
    return this.workspaceId != null;
  }

  public boolean hasCriterionOnUser() {
    return this.user != null;
  }

  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  public String getWorkspaceId() {
    return workspaceId;
  }

  public UserDetail getUser() {
    return this.user;
  }
}
