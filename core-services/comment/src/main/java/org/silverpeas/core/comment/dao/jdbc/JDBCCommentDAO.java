/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.comment.dao.jdbc;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.comment.dao.CommentDAO;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.persistence.jdbc.DBUtil.openConnection;

@Repository
@Named("commentDAO")
public class JDBCCommentDAO implements CommentDAO {

  protected static final long serialVersionUID = -4880326368611108874L;
  private static final String IN_COMPONENT_INSTANCE = " in component instance ";

  @Inject
  private JDBCCommentRequester theRequester;

  private JDBCCommentRequester getRequester() {
    return theRequester;
  }

  @Override
  public CommentPK saveComment(final Comment cmt) {
    CommentPK commentPK;
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      commentPK = requester.saveComment(con, cmt);
      if (commentPK == null) {
        throw new SilverpeasRuntimeException("Cannot save comment for resource " +
            cmt.getForeignKey().getId() + " of type " + cmt.getResourceType() +
            IN_COMPONENT_INSTANCE + cmt.getComponentInstanceId());
      }
      return commentPK;
    } catch (Exception re) {
      throw new SilverpeasRuntimeException(re.getMessage(), re);
    }
  }

  @Override
  public void removeComment(final CommentPK pk) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      requester.deleteComment(con, pk);
    } catch (Exception re) {
      throw new SilverpeasRuntimeException(re.getMessage(), re);
    }
  }

  @Override
  public void updateComment(final Comment cmt) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      requester.updateComment(con, cmt);
    } catch (Exception re) {
      throw new SilverpeasRuntimeException(re.getMessage(), re);
    }
  }

  @Override
  public Comment getComment(final CommentPK pk) {
    Comment comment;
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      comment = requester.getComment(con, pk);
      if (comment == null) {
        throw new SilverpeasRuntimeException("Cannot get comment " + pk.getId() + IN_COMPONENT_INSTANCE + pk.getInstanceId());
      }
      return comment;
    } catch (Exception re) {
      throw new SilverpeasRuntimeException(re.getMessage(), re);
    }
  }

  @Override
  public List<CommentedPublicationInfo> getAllMostCommentedPublications() {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      return requester.getMostCommentedAllPublications(con, null);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublications(final String resourceType,
      final List<? extends WAPrimaryKey> pks) {
    List<CommentedPublicationInfo> commentedPubs = null;
    JDBCCommentRequester requester = getRequester();
    if (pks != null && !pks.isEmpty()) {
      try (Connection con = openConnection()) {
        commentedPubs = requester.getMostCommentedPublications(con, pks);
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e.getMessage(), e);
      }
    }
    return (commentedPubs == null ? new ArrayList<CommentedPublicationInfo>() : commentedPubs);
  }

  @Override
  public int getCommentsCountByForeignKey(final String resourceType, final ResourceReference foreignPk) {
    int publicationCommentsCount = 0;
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      publicationCommentsCount = requester.getCommentsCount(con, resourceType, foreignPk);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
    return publicationCommentsCount;
  }

  @Override
  public List<Comment> getAllCommentsByForeignKey(final String resourceType,
      final ResourceReference foreignPk) {
    List<Comment> vRet;
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      vRet = requester.getAllComments(con, resourceType, foreignPk);
      if (vRet == null) {
        throw new SilverpeasRuntimeException("Cannot get all comments for resource " + foreignPk.getId() +
            " of type " + resourceType + IN_COMPONENT_INSTANCE + foreignPk.getInstanceId());
      }
      return vRet;
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public void removeAllCommentsByForeignPk(final String resourceType, final ResourceReference foreignPk) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      requester.deleteAllComments(con, resourceType, foreignPk);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public void moveComments(String resourceType, ResourceReference fromPK, ResourceReference toPK) {
    moveComments(resourceType, fromPK, resourceType, toPK);
  }

  @Override
  public void moveComments(final String fromResourceType, final ResourceReference fromPK,
      final String toResourceType, final ResourceReference toPK) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      requester.moveComments(con, fromResourceType, fromPK, toResourceType, toPK);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublications(String resourceType) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      return requester.getMostCommentedAllPublications(con, resourceType);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public List<Comment> getLastComments(String instanceId, int count) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      return requester.getLastComments(con, instanceId, count);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public List<SocialInformationComment> getSocialInformationCommentsListByUserId(
      List<String> resourceTypes, String userId, Period period) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      return requester.getSocialInformationComments(con, resourceTypes,
          StringUtil.isDefined(userId) ? Collections.singletonList(userId) : null, null, period);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public List<SocialInformationComment> getSocialInformationCommentsListOfMyContacts(
      List<String> resourceTypes, List<String> myContactsIds, List<String> instanceIds,
      Period period) {
    try (Connection con = openConnection()) {
      JDBCCommentRequester requester = getRequester();
      return requester
          .getSocialInformationComments(con, resourceTypes, myContactsIds, instanceIds, period);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }
}
