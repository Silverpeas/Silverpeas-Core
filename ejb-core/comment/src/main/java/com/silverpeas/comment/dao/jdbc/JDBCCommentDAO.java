/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.comment.dao.jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.comment.CommentRuntimeException;
import com.silverpeas.comment.dao.CommentDAO;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.model.CommentedPublicationInfo;
import org.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.JNDINames;
import org.silverpeas.util.WAPrimaryKey;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import javax.inject.Named;

@Named("commentDAO")
public class JDBCCommentDAO implements CommentDAO {

  protected static final long serialVersionUID = -4880326368611108874L;

  private Connection openConnection() {
    try {
      Connection con = DBUtil.makeConnection(JNDINames.NODE_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new CommentRuntimeException(getClass().getSimpleName() + ".getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void closeConnection(final Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("comment", getClass().getSimpleName() + ".closeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  private JDBCCommentRequester getCommentDAO() {
    return new JDBCCommentRequester();
  }

  @Override
  public CommentPK saveComment(final Comment cmt) {
    Connection con = openConnection();
    CommentPK commentPK;
    try {
      JDBCCommentRequester requester = getCommentDAO();
      commentPK = requester.saveComment(con, cmt);
      if (commentPK == null) {
        throw new CommentRuntimeException(getClass().getSimpleName() + ".createComment()",
            SilverpeasRuntimeException.ERROR,
            "comment.CREATING_NEW_COMMENT_FAILED");
      }
      return commentPK;
    } catch (Exception re) {
      throw new CommentRuntimeException(getClass().getSimpleName() + ".createComment()",
          SilverpeasRuntimeException.ERROR,
          "comment.CREATING_NEW_COMMENT_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public void removeComment(final CommentPK pk) {
    Connection con = openConnection();
    try {
      JDBCCommentRequester requester = getCommentDAO();
      requester.deleteComment(con, pk);
    } catch (Exception re) {
      throw new CommentRuntimeException(getClass().getSimpleName() + ".deleteComment()",
          SilverpeasRuntimeException.ERROR, "comment.DELETE_COMMENT_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public void updateComment(final Comment cmt) {
    Connection con = openConnection();
    try {
      JDBCCommentRequester requester = getCommentDAO();
      requester.updateComment(con, cmt);
    } catch (Exception re) {
      throw new CommentRuntimeException(getClass().getSimpleName() + ".updateComment()",
          SilverpeasRuntimeException.ERROR, "comment.UPDATE_COMMENT_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public Comment getComment(final CommentPK pk) {
    Connection con = openConnection();
    Comment comment;
    try {
      JDBCCommentRequester requester = getCommentDAO();
      comment = requester.getComment(con, pk);
      if (comment == null) {
        throw new CommentRuntimeException(getClass().getSimpleName() + ".getComment()",
            SilverpeasRuntimeException.ERROR, "comment.GET_COMMENT_FAILED");
      }
      return comment;
    } catch (Exception re) {
      throw new CommentRuntimeException(getClass().getSimpleName() + ".getComment()",
          SilverpeasRuntimeException.ERROR, "comment.GET_COMMENT_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public List<CommentedPublicationInfo> getAllMostCommentedPublications() {
    Connection con = openConnection();
    try {
      JDBCCommentRequester requester = getCommentDAO();
      return requester.getMostCommentedAllPublications(con, null);
    } catch (Exception e) {
      throw new CommentRuntimeException(getClass().getSimpleName()
          + ".getMostCommentedAllPublications()",
          SilverpeasRuntimeException.FATAL,
          "comment.GET_MOST_COMMENTED_ITEMS",
          e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublications(final String resourceType,
      final List<? extends WAPrimaryKey> pks) {
    List<CommentedPublicationInfo> commentedPubs = null;
    Connection con = openConnection();
    JDBCCommentRequester requester = getCommentDAO();
    if (pks != null && !pks.isEmpty()) {
      try {
        commentedPubs = requester.getMostCommentedPublications(con, pks);
      } catch (Exception e) {
        throw new CommentRuntimeException(getClass().getSimpleName()
            + ".getMostCommentedPublications()", SilverpeasRuntimeException.ERROR,
            "comment.GET_MOST_COMMENTED_ITEMS", e);
      } finally {
        closeConnection(con);
      }
    }
    return (commentedPubs == null ? new ArrayList<CommentedPublicationInfo>() : commentedPubs);
  }

  @Override
  public int getCommentsCountByForeignKey(final String resourceType, final ForeignPK foreign_pk) {
    Connection con = openConnection();
    int publicationCommentsCount = 0;
    try {
      JDBCCommentRequester requester = getCommentDAO();
      publicationCommentsCount = requester.getCommentsCount(con, resourceType, foreign_pk);
    } catch (Exception re) {
      throw new CommentRuntimeException(getClass().getSimpleName() + ".getCommentsCount()",
          SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED",
          re);
    } finally {
      closeConnection(con);
    }
    return publicationCommentsCount;
  }

  @Override
  public List<Comment> getAllCommentsByForeignKey(final String resourceType,
      final ForeignPK foreign_pk) {
    Connection con = openConnection();
    List<Comment> vRet;
    try {
      JDBCCommentRequester requester = getCommentDAO();
      vRet = requester.getAllComments(con, resourceType, foreign_pk);
      if (vRet == null) {
        throw new CommentRuntimeException(getClass().getSimpleName() + ".getAllComments()",
            SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED");
      }
      return vRet;
    } catch (Exception re) {
      throw new CommentRuntimeException(getClass().getSimpleName() + ".getAllComments()",
          SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED",
          re);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public void removeAllCommentsByForeignPk(final String resourceType, final ForeignPK foreign_pk) {
    Connection con = openConnection();
    try {
      JDBCCommentRequester requester = getCommentDAO();
      requester.deleteAllComments(con, resourceType, foreign_pk);
    } catch (Exception re) {
      throw new CommentRuntimeException(getClass().getSimpleName() + ".getAllComments()",
          SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED",
          re);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public void moveComments(String resourceType, ForeignPK fromPK, ForeignPK toPK) {
    moveComments(resourceType, fromPK, resourceType, toPK);
  }

  @Override
  public void moveComments(final String fromResourceType, final ForeignPK fromPK,
      final String toResourceType, final ForeignPK toPK) {
    Connection con = openConnection();
    try {
      JDBCCommentRequester requester = getCommentDAO();
      requester.moveComments(con, fromResourceType, fromPK, toResourceType, toPK);
    } catch (Exception re) {
      throw new CommentRuntimeException(getClass().getSimpleName() + ".moveComments()",
          SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED",
          re);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublications(String resourceType) {
    Connection con = openConnection();
    try {
      JDBCCommentRequester requester = getCommentDAO();
      return requester.getMostCommentedAllPublications(con, resourceType);
    } catch (Exception e) {
      throw new CommentRuntimeException(getClass().getSimpleName()
          + ".getMostCommentedAllPublications()",
          SilverpeasRuntimeException.FATAL,
          "comment.GET_COMMENT_FAILED",
          e);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public List<Comment> getLastComments(String instanceId, int count) {
    Connection con = openConnection();
    try {
      JDBCCommentRequester requester = getCommentDAO();
      return requester.getLastComments(con, instanceId, count);
    } catch (Exception e) {
      throw new CommentRuntimeException(getClass().getSimpleName()
          + ".getMostCommentedAllPublications()",
          SilverpeasRuntimeException.FATAL,
          "comment.GET_COMMENT_FAILED",
          e);
    } finally {
      closeConnection(con);
    }
  }
}
