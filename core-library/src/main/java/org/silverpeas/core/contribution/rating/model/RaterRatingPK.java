/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.silverpeas.core.admin.user.model.UserDetail;

/**
 * This class represents a technical primary key of a rater rating.
 */
public class RaterRatingPK extends ContributionRatingPK {
  private static final long serialVersionUID = -7143887879838137369L;

  private UserDetail rater;

  public RaterRatingPK(String id, String componentId, String type, UserDetail rater) {
    super(id, componentId, type);
    this.rater = rater;
  }

  public UserDetail getRater() {
    return rater;
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
    return ((other instanceof RaterRatingPK) && (toString().equals(other.toString())));
  }

  @Override
  public String toString() {
    return super.toString() + ", userId = " + getRater().getId();
  }
}