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

import com.silverpeas.SilverpeasContent;
import com.silverpeas.notation.model.RatingDAO;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.rating.RaterRatingPK;
import org.silverpeas.rating.ContributionRating;
import org.silverpeas.rating.ContributionRatingPK;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Stateless(name = "Rating", description = "Stateless session bean to manage notation of content.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class RatingBmEJB implements RatingBm {

  @Override
  public void updateRating(RaterRatingPK pk, int note) {
    Connection con = openConnection();
    try {
      if (hasUserRating(pk)) {
        RatingDAO.updateRaterRating(con, pk, note);
      } else {
        RatingDAO.createRaterRating(con, pk, note);
      }
    } catch (Exception e) {
      throw new RatingRuntimeException("RatingBmEJB.updateNotation()",
          SilverpeasRuntimeException.ERROR, "notation.CREATING_NOTATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void moveRating(final ContributionRatingPK pk, final String componentInstanceId) {
    Connection con = openConnection();
    try {
      RatingDAO.moveRatings(con, pk, componentInstanceId);
    } catch (Exception e) {
      throw new RatingRuntimeException("RatingBmEJB.updateNotation()",
          SilverpeasRuntimeException.ERROR, "notation.CREATING_NOTATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteRating(ContributionRatingPK pk) {
    Connection con = openConnection();
    try {
      RatingDAO.deleteRatings(con, pk);
    } catch (Exception e) {
      throw new RatingRuntimeException("RatingBmEJB.deleteNotation()",
          SilverpeasRuntimeException.ERROR, "notation.DELETE_NOTATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteRaterRating(RaterRatingPK pk) {
    Connection con = openConnection();
    try {
      RatingDAO.deleteRaterRating(con, pk);
    } catch (Exception e) {
      throw new RatingRuntimeException("RatingBmEJB.deleteUserNotation()",
          SilverpeasRuntimeException.ERROR, "notation.DELETE_NOTATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteComponentRatings(String componentInstanceId) {
    Connection con = openConnection();
    try {
      RatingDAO.deleteComponentRatings(con, componentInstanceId);
    } catch (Exception e) {
      throw new RatingRuntimeException("RatingBmEJB.deleteAppNotations()",
          SilverpeasRuntimeException.ERROR, "notation.DELETE_NOTATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Map<String, ContributionRating> getRatings(SilverpeasContent... contributions) {
    Map<String, ContributionRating> indexedContributionRatings = new HashMap<String, ContributionRating>();
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
        Collections.singleton(pk.getContributionId())).values().iterator().next();
  }

  private Map<String, ContributionRating> getRatingIndexedByContributionIds(String componentInstanceId,
      String contributionType, Collection<String> contributionIds) {
    Connection con = openConnection();
    try {
      return RatingDAO.getRatings(con, componentInstanceId, contributionType, contributionIds);
    } catch (Exception e) {
      throw new RatingRuntimeException("RatingBmEJB.getNotation()",
          SilverpeasRuntimeException.ERROR, "notation.GET_NOTE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public boolean hasUserRating(RaterRatingPK pk) {
    Connection con = openConnection();
    try {
      return RatingDAO.existRaterRating(con, pk);
    } catch (Exception e) {
      throw new RatingRuntimeException("RatingBmEJB.hasUserNotation()",
          SilverpeasRuntimeException.ERROR, "notation.HAS_USER_NOTATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private Connection openConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.NODE_DATASOURCE);
    } catch (Exception e) {
      throw new RatingRuntimeException("RatingBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }
}