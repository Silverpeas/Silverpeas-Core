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
package com.silverpeas.pdc.web;

import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.HashSet;
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
  private Set<AxisValueCriterion> axisValues = new HashSet<AxisValueCriterion>();
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

  /**
   * A criterion on the axis' value to take into account.
   */
  public static class AxisValueCriterion {

    private String axisId;
    private String valueId;

    /**
     * Constructs a new criterion on the specified axis' value
     *
     * @param axisId the unique identifier of the axis.
     * @param valuePath the path of the value of the axis above from the root axis value.
     */
    public AxisValueCriterion(String axisId, String valuePath) {
      this.axisId = axisId;
      this.valueId = valuePath;
    }

    /**
     * Gets the unique identifier of a PdC axis.
     *
     * @return the axis identifier.
     */
    public String getAxisId() {
      return this.axisId;
    }

    /**
     * Gets the unique identifier of a PdC axis as an integer.
     *
     * @return the axis identifier.
     */
    public int getAxisIdAsInt() {
      return Integer.valueOf(axisId);
    }

    /**
     * Gets the path of the axis' value from the root axis' value.
     *
     * @return the value path.
     */
    public String getValuePath() {
      return this.valueId;
    }
  }
}
