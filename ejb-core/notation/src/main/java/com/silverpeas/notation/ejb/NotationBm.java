/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.notation.ejb;

import java.util.Collection;
import java.util.List;

import javax.ejb.Local;

import org.silverpeas.rating.Rating;
import org.silverpeas.rating.RatingPK;

@Local
public interface NotationBm {

  /**
   * Save user notation. Create it or update it if it already exists.
   * @param pk identifying the rated resource and the user
   * @param note the rate given to this resource by the user
   */
  public void updateRating(RatingPK pk, int note);

  /**
   * Remove all notations of identified resource 
   * @param pk identifying the resource
   */
  public void deleteRating(RatingPK pk);
  
  /**
   * Remove user notation of identified resource 
   * @param pk identifying the resource and the user
   */
  public void deleteUserRating(RatingPK pk);
  
  /**
   * Remove all resources notations of given app
   * @param appId identity of app
   */
  public void deleteAppRatings(String appId);
 
  /**
   * Getting notation about given resources identified by PKs.
   * If a resource has no notation, a NotationDetail is returned anyway.
   * @param pks identities of resource
   * @return Notations of each identified resource
   */
  public List<Rating> getRatings(RatingPK... pks);

  /**
   * Getting notation about once given resource identified by its PK.
   * If the resource has no notation, a NotationDetail is returned anyway.
   * @param pk identity of resource
   * @return Notation of identified resource
   */
  public Rating getRating(RatingPK pk);

  /**
   * Getting number of rating about once given resource identified by its PK.
   * @param pk identity of resource
   * @return Number of rates of identified resource. If it has no rate, 0 is returned.
   */
  public int countReviews(RatingPK pk);

  /**
   * Checking if user has given a rating on this resource
   * @param pk identity of resource and user
   * @return true if user has already given a rate
   */
  public boolean hasUserRating(RatingPK pk);

  /**
   * Returns best rated resources of given app (sorted by best descending global rating).
   * If two resources have the same rating then the one with the most votes is placed ahead.
   * @param pk identity of app and type of resource
   * @param notationsCount number of returned notations
   * @return Notations according to global rating and number of total votes
   */
  public Collection<Rating> getBestRatings(RatingPK pk, int notationsCount);

  /**
   * Returns best rated resources of given resources list (sorted by best descending global rating).
   * If two resources have the same rating then the one with the most votes is placed ahead.
   * @param pks identity of resources
   * @param notationsCount number of returned notations
   * @return Notations according to global rating and number of total votes
   */
  public Collection<Rating> getBestRatings(Collection<RatingPK> pks, int notationsCount);
}