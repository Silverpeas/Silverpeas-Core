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
 * Represents the rating data associated to a rater.
 * The rater is represented by a {@link UserDetail}
 * @author: Yohann Chastagnier
 */
public class RaterRating {

  private ContributionRating contributionRating;
  private UserDetail rater;
  private int value;
  private boolean isRatingDone;

  /**
   * The default constructor.
   * @param contributionRating the {@link ContributionRating} instance which this rater rating is
   * associated.
   * @param rater the user that is the rater.
   */
  RaterRating(ContributionRating contributionRating, UserDetail rater) {
    this.contributionRating = contributionRating;
    this.rater = rater;
    this.isRatingDone = false;
  }

  /**
   * The default constructor.
   * @param contributionRating the {@link ContributionRating} instance which this rater rating is
   * associated.
   * @param rater the user that is the rater.
   * @param value the value of the rating of the rater.
   */
  RaterRating(ContributionRating contributionRating, UserDetail rater, int value) {
    this(contributionRating, rater);
    this.value = value;
    this.isRatingDone = true;
  }

  /**
   * Gets the global informations about the rating.
   * @return the global rating informations.
   */
  public ContributionRating getRating() {
    return contributionRating;
  }

  /**
   * Gets the {@link UserDetail} instance that represents the rater.
   * @return the rater of the current instance.
   */
  public UserDetail getRater() {
    return rater;
  }

  /**
   * Gets the value of the rating of the rater.
   * @return the rating value of the current instance.
   */
  public int getValue() {
    return value;
  }

  /**
   * Indicates if the rater has done its rating.
   * @return true if the rater has done its rating, false otherwise.
   */
  public boolean isRatingDone() {
    return isRatingDone;
  }
}
