/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.contribution.rating.model;

import org.silverpeas.core.ResourceReference;

import java.io.Serializable;

/**
 * This class represents a technical primary key of a contribution rating.
 */
public class ContributionRatingPK extends ResourceReference implements Serializable {

  private static final long serialVersionUID = -4144961919465268637L;
  private String contributionType;

  public ContributionRatingPK(String id, String componentId, String type) {
    super(id, componentId);
    this.contributionType = type;
  }

  public String getContributionId() {
    return getId();
  }

  public String getContributionType() {
    return contributionType;
  }

  public void setContributionType(String contributionType) {
    this.contributionType = contributionType;
  }

  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }

  /**
   * Comparison between two notation primary key. Since various attributes of the both elements can
   * be null, using toString() method to compare the elements avoids to check null cases for each
   * attribute.
   * @param other
   */
  @Override
  public boolean equals(Object other) {
    return ((other instanceof ContributionRatingPK) && (toString().equals(other.toString())));
  }

  @Override
  public String toString() {
    return "id = " + getContributionId() + ", componentId = " + getComponentName() + ", type = " +
        getContributionType();
  }
}