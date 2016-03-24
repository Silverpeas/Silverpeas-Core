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
package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.admin.user.model.UserDetail;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Criteria used to filter some parts of a PdC. Only the axis and axis' values matching these
 * criteria have to be taken.
 *
 * @author mmoquillon
 */
public class PdcFilterCriteria {

  private String componentInstanceId;
  private String workspaceId;
  private Set<AxisValueCriterion> axisValues = new HashSet<>();
  private boolean secondaryAxisInclusion = false;
  private UserDetail user;

  public PdcFilterCriteria onWorkspace(String workspaceId) {
    this.workspaceId = workspaceId;
    return this;
  }

  public PdcFilterCriteria onComponentInstance(String instanceId) {
    this.componentInstanceId = instanceId;
    return this;
  }

  public PdcFilterCriteria onAxisValue(final AxisValueCriterion criterion) {
    this.axisValues.add(criterion);
    return this;
  }

  public PdcFilterCriteria onAxisValues(final List<AxisValueCriterion> criteria) {
    this.axisValues.addAll(criteria);
    return this;
  }

  public PdcFilterCriteria onUser(final UserDetail user) {
    this.user = user;
    return this;
  }

  public PdcFilterCriteria onSecondaryAxisInclusion(boolean includeSecondaryAxis) {
    this.secondaryAxisInclusion = includeSecondaryAxis;
    return this;
  }

  public boolean hasCriterionOnComponentInstance() {
    return this.componentInstanceId != null;
  }

  public boolean hasCriterionOnWorkspace() {
    return this.workspaceId != null;
  }

  public boolean hasCriterionOnAxisValues() {
    return !this.axisValues.isEmpty();
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

  public Set<AxisValueCriterion> getAxisValues() {
    return axisValues;
  }

  public UserDetail getUser() {
    return this.user;
  }

  public boolean hasSecondaryAxisToBeIncluded() {
    return this.secondaryAxisInclusion;
  }
}
