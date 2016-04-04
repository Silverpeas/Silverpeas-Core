/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.rating.service;

import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.rating.model.ContributionRating;
import org.silverpeas.core.contribution.rating.model.ContributionRatingPK;
import org.silverpeas.core.contribution.rating.model.RaterRatingPK;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Map;

public interface RatingService {

  static RatingService get() {
    return ServiceProvider.getService(RatingService.class);
  }

  /**
   * Save user notation. Create it or update it if it already exists.
   * @param pk identifying the rated resource and the rater
   * @param note the rate given to this resource by the user
   */
  void updateRating(RaterRatingPK pk, int note);

  /**
   * Remove all notations of identified resource to the specified component instance identifier.
   * @param pk identifying the resource
   * @param componentInstanceId the target component identified by its instance id.
   */
  void moveRating(ContributionRatingPK pk, String componentInstanceId);

  /**
   * Remove all notations of identified resource
   * @param pk identifying the resource
   */
  void deleteRating(ContributionRatingPK pk);

  /**
   * Remove rater rating of identified resource
   * @param pk identifying the resource and the rater
   */
  void deleteRaterRating(RaterRatingPK pk);

  /**
   * Remove all resources notations of given app
   * @param componentInstanceId identitier of the component instance.
   */
  void deleteComponentRatings(String componentInstanceId);

  /**
   * Getting notation about the given contributions.
   * If a contribution has no notation, a {@link ContributionRating} instance is returned anyway.
   * @param contributions the contributions which returned ratings must be attached.
   * @return {@link ContributionRating} instances ralated to the given contributions indexed by contribution
   * identifier.
   */
  Map<String, ContributionRating> getRatings(SilverpeasContent... contributions);

  /**
   * Getting notation about the given contribution.
   * If the contribution has no notation, a {@link ContributionRating} instance is returned anyway.
   * @param contribution the contribution which returned ratings must be attached.
   * @return {@link ContributionRating} instance ralated to the given contribution.
   */
  ContributionRating getRating(SilverpeasContent contribution);

  /**
   * Getting notation about once given resource identified by its PK.
   * If the resource has no notation, a NotationDetail is returned anyway.
   * @param pk identity of resource
   * @return Notation of identified resource
   */
  ContributionRating getRating(ContributionRatingPK pk);

  /**
   * Checking if user has given a rating on this resource
   * @param pk identity of resource and rater
   * @return true if user has already given a rate
   */
  boolean hasUserRating(RaterRatingPK pk);
}