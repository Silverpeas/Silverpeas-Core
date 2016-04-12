/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
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
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.rating.service;

import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.contribution.rating.model.Rating;
import org.silverpeas.core.contribution.rating.model.ContributionRating;
import org.silverpeas.core.contribution.rating.model.ContributionRatingPK;
import org.silverpeas.core.contribution.rating.model.RaterRatingPK;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Transactional
@Singleton
public class DefaultRatingService implements RatingService, ComponentInstanceDeletion {

  @Inject
  private RatingRepository repository;

  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try {
      // Delete the notations
      RatingService.get().deleteComponentRatings(componentInstanceId);
    } catch (Exception e) {
      // No exceptions are thrown because of those information are not sensible.
    }
  }

  @Override
  public void updateRating(RaterRatingPK pk, int note) {
    Rating rating = repository.getRating(pk);
    if (rating == null) {
      rating = new Rating();
      rating.setAuthorId(pk.getRater().getId());
      rating.setContributionId(pk.getContributionId());
      rating.setContributionType(pk.getContributionType());
      rating.setInstanceId(pk.getInstanceId());
    }
    rating.setNote(note);
    repository.save(rating);
  }

  @Override
  public void moveRating(final ContributionRatingPK pk, final String componentInstanceId) {
    repository.moveAllRatingsOfAContribution(pk, componentInstanceId);
  }

  @Override
  public void deleteRating(ContributionRatingPK pk) {
    repository.deleteAllRatingsOfAContribution(pk);
  }

  @Override
  public void deleteRaterRating(RaterRatingPK pk) {
    Rating rating = repository.getRating(pk);
    if (rating != null) {
      repository.delete(rating);
    }
  }

  @Override
  public void deleteComponentRatings(String componentInstanceId) {
    repository.deleteAllRatingsInComponentInstance(componentInstanceId);
  }

  @Override
  public Map<String, ContributionRating> getRatings(SilverpeasContent... contributions) {
    Map<String, ContributionRating> indexedContributionRatings =
        new HashMap<>(contributions.length);
    for (SilverpeasContent contribution : contributions) {
      indexedContributionRatings.put(contribution.getId(), getRating(contribution));
    }
    return indexedContributionRatings;
  }

  @Override
  public ContributionRating getRating(SilverpeasContent contribution) {
    return getRating(new ContributionRatingPK(contribution.getId(), contribution.getComponentInstanceId(),
        contribution.getContributionType()));
  }

  @Override
  public ContributionRating getRating(ContributionRatingPK pk) {
    return getRatingIndexedByContributionIds(pk.getInstanceId(), pk.getContributionType(),
        pk.getContributionId()).values().iterator().next();
  }

  private Map<String, ContributionRating> getRatingIndexedByContributionIds(String componentInstanceId,
      String contributionType, String... contributionIds) {
    Map<String, ContributionRating> ratingsByContribution = repository
        .getAllRatingByContributions(componentInstanceId, contributionType, contributionIds);
    for (String contributionId : contributionIds) {
      if (!ratingsByContribution.containsKey(contributionId)) {
        ContributionRating contributionRating = new ContributionRating(
            new ContributionRatingPK(contributionId, componentInstanceId, contributionType));
        ratingsByContribution.put(contributionId, contributionRating);
      }
    }
    return ratingsByContribution;
  }

  @Override
  public boolean hasUserRating(RaterRatingPK pk) {
    return repository.getRating(pk) != null;
  }
}