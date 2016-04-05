/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.comment.dao.jdbc;

import org.silverpeas.core.comment.dao.CommentDAO;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.WAPrimaryKey;

import javax.inject.Named;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.persistence.jdbc.DBUtil.openConnection;

@Named("commentDAO")
public class JDBCCommentDAO implements CommentDAO {

  protected static final long serialVersionUID = -4880326368611108874L;

  private JDBCCommentRequester getCommentDAO() {
    return new JDBCCommentRequester();
  }

  @Override
  public CommentPK saveComment(final Comment cmt) {
    CommentPK commentPK;
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      commentPK = requester.saveComment(con, cmt);
      if (commentPK == null) {
        throw new RuntimeException("Cannot save comment for resource " +
            cmt.getForeignKey().getId() + " of type " + cmt.getResourceType() +
            " in component instance " + cmt.getComponentInstanceId());
      }
      return commentPK;
    } catch (Exception re) {
      throw new RuntimeException(re.getMessage(), re);
    }
  }

  @Override
  public void removeComment(final CommentPK pk) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      requester.deleteComment(con, pk);
    } catch (Exception re) {
      throw new RuntimeException(re.getMessage(), re);
    }
  }

  @Override
  public void updateComment(final Comment cmt) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      requester.updateComment(con, cmt);
    } catch (Exception re) {
      throw new RuntimeException(re.getMessage(), re);
    }
  }

  @Override
  public Comment getComment(final CommentPK pk) {
    Comment comment;
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      comment = requester.getComment(con, pk);
      if (comment == null) {
        throw new RuntimeException("Cannot get comment " + pk.getId() +
            " in component instance " + pk.getInstanceId());
      }
      return comment;
    } catch (Exception re) {
      throw new RuntimeException(re.getMessage(), re);
    }
  }

  @Override
  public List<CommentedPublicationInfo> getAllMostCommentedPublications() {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      return requester.getMostCommentedAllPublications(con, null);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublications(final String resourceType,
      final List<? extends WAPrimaryKey> pks) {
    List<CommentedPublicationInfo> commentedPubs = null;
    JDBCCommentRequester requester = getCommentDAO();
    if (pks != null && !pks.isEmpty()) {
      try (Connection con = openConnection()) {
        commentedPubs = requester.getMostCommentedPublications(con, pks);
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
    return (commentedPubs == null ? new ArrayList<CommentedPublicationInfo>() : commentedPubs);
  }

  @Override
  public int getCommentsCountByForeignKey(final String resourceType, final ForeignPK foreign_pk) {
    int publicationCommentsCount = 0;
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      publicationCommentsCount = requester.getCommentsCount(con, resourceType, foreign_pk);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    return publicationCommentsCount;
  }

  @Override
  public List<Comment> getAllCommentsByForeignKey(final String resourceType,
      final ForeignPK foreign_pk) {
    List<Comment> vRet;
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      vRet = requester.getAllComments(con, resourceType, foreign_pk);
      if (vRet == null) {
        throw new RuntimeException("Cannot get all comments for resource " + foreign_pk.getId() +
            " of type " + resourceType + " in component instance " + foreign_pk.getInstanceId());
      }
      return vRet;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public void removeAllCommentsByForeignPk(final String resourceType, final ForeignPK foreign_pk) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      requester.deleteAllComments(con, resourceType, foreign_pk);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public void moveComments(String resourceType, ForeignPK fromPK, ForeignPK toPK) {
    moveComments(resourceType, fromPK, resourceType, toPK);
  }

  @Override
  public void moveComments(final String fromResourceType, final ForeignPK fromPK,
      final String toResourceType, final ForeignPK toPK) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      requester.moveComments(con, fromResourceType, fromPK, toResourceType, toPK);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublications(String resourceType) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      return requester.getMostCommentedAllPublications(con, resourceType);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public List<Comment> getLastComments(String instanceId, int count) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      return requester.getLastComments(con, instanceId, count);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public List<SocialInformationComment> getSocialInformationCommentsListByUserId(
      List<String> resourceTypes, String userId, Period period) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      return requester.getSocialInformationComments(con, resourceTypes,
          StringUtil.isDefined(userId) ? Collections.singletonList(userId) : null, null, period);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public List<SocialInformationComment> getSocialInformationCommentsListOfMyContacts(
      List<String> resourceTypes, List<String> myContactsIds, List<String> instanceIds,
      Period period) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getCommentDAO();
      return requester
          .getSocialInformationComments(con, resourceTypes, myContactsIds, instanceIds, period);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
