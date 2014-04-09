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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.silverpeas.rating.Rating;
import org.silverpeas.rating.RatingPK;

import com.silverpeas.notation.model.Notation;
import com.silverpeas.notation.model.NotationDAO;
import com.silverpeas.notation.model.comparator.NotationDetailComparator;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

@Stateless(name="Notation", description="Stateless session bean to manage notation of content.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class NotationBmEJB implements NotationBm {

  @Override
  public void updateRating(RatingPK pk, int note) {
    Connection con = openConnection();
    try {
      if (hasUserRating(pk)) {
        NotationDAO.updateNotation(con, pk, note);
      } else {
        NotationDAO.createNotation(con, pk, note);
      }
    } catch (Exception e) {
      throw new NotationRuntimeException("NotationBmEJB.updateNotation()",
          SilverpeasRuntimeException.ERROR,
          "notation.CREATING_NOTATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void moveRating(final RatingPK pk, final String componentInstanceId) {
    Connection con = openConnection();
    try {
      NotationDAO.moveNotation(con, pk, componentInstanceId);
    } catch (Exception e) {
      throw new NotationRuntimeException("NotationBmEJB.updateNotation()",
          SilverpeasRuntimeException.ERROR, "notation.CREATING_NOTATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteRating(RatingPK pk) {
    Connection con = openConnection();
    try {
      NotationDAO.deleteNotation(con, pk);
    } catch (Exception e) {
      throw new NotationRuntimeException("NotationBmEJB.deleteNotation()",
          SilverpeasRuntimeException.ERROR, "notation.DELETE_NOTATION_FAILED",
          e);
    } finally {
      DBUtil.close(con);
    }
  }
  
  @Override
  public void deleteUserRating(RatingPK pk) {
    Connection con = openConnection();
    try {
      NotationDAO.deleteNotationByUser(con, pk);
    } catch (Exception e) {
      throw new NotationRuntimeException("NotationBmEJB.deleteUserNotation()",
          SilverpeasRuntimeException.ERROR, "notation.DELETE_NOTATION_FAILED",
          e);
    } finally {
      DBUtil.close(con);
    }
  }
  
  @Override
  public void deleteAppRatings(String appId) {
    Connection con = openConnection();
    try {
      NotationDAO.deleteAppNotations(con, appId);
    } catch (Exception e) {
      throw new NotationRuntimeException("NotationBmEJB.deleteAppNotations()",
          SilverpeasRuntimeException.ERROR, "notation.DELETE_NOTATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }
  
  @Override
  public List<Rating> getRatings(RatingPK... pks) {
    List<Rating> notations = new ArrayList<Rating>();
    for (RatingPK pk : pks) {
      notations.add(getRating(pk));
    }
    return notations;
  }

  @Override
  public Rating getRating(RatingPK pk) {
    Rating rating = new Rating(pk);
    Collection<Notation> notations = null;
    Connection con = openConnection();
    try {
      notations = NotationDAO.getNotations(con, pk);
    } catch (Exception e) {
      throw new NotationRuntimeException("NotationBmEJB.getNotation()",
          SilverpeasRuntimeException.ERROR, "notation.GET_NOTE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }

    String userId = pk.getUserId();
    int notesCount = 0;
    float globalNote = 0;
    int userNote = 0;
    if (notations != null && !notations.isEmpty()) {
      notesCount = notations.size();
      float sum = 0;
      for (Notation notation : notations) {
        if (userId != null && userId.equals(notation.getAuthor())) {
          userNote = notation.getNote();
        }
        sum += notation.getNote();
      }
      globalNote = sum / notesCount;
    }
    rating.setNumberOfReviews(notesCount);
    rating.setOverallRating(globalNote);
    rating.setUserRating(userNote);
    return rating;
  }

  @Override
  public int countReviews(RatingPK pk) {
    Connection con = openConnection();
    try {
      return NotationDAO.countNotations(con, pk);
    } catch (Exception e) {
      throw new NotationRuntimeException("NotationBmEJB.countNotations()",
          SilverpeasRuntimeException.ERROR, "notation.COUNT_NOTATIONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public boolean hasUserRating(RatingPK pk) {
    Connection con = openConnection();
    try {
      return NotationDAO.hasUserNotation(con, pk);
    } catch (Exception e) {
      throw new NotationRuntimeException("NotationBmEJB.hasUserNotation()",
          SilverpeasRuntimeException.ERROR, "notation.HAS_USER_NOTATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<Rating> getBestRatings(RatingPK pk, int notationsCount) {
    Connection con = openConnection();
    Collection<RatingPK> notationPKs = null;
    try {
      notationPKs = NotationDAO.getRatingPKs(con, pk);
    } catch (Exception e) {
      throw new NotationRuntimeException("NotationBmEJB.getBestNotations()",
          SilverpeasRuntimeException.ERROR, "notation.HAS_USER_NOTATION_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
    return getBestRatings(notationPKs, notationsCount);
  }

  @Override
  public Collection<Rating> getBestRatings(Collection<RatingPK> pks, int notationsCount) {
    List<Rating> notations = new ArrayList<Rating>();
    if (pks != null && !pks.isEmpty()) {
      for (RatingPK pk : pks) {
        notations.add(getRating(pk));
      }
      Collections.sort(notations, new NotationDetailComparator());
      if (notations.size() > notationsCount) {
        return notations.subList(0, notationsCount);
      }
    }
    return notations;
  }

  private Connection openConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.NODE_DATASOURCE);
    } catch (Exception e) {
      throw new NotationRuntimeException("NotationBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }
}