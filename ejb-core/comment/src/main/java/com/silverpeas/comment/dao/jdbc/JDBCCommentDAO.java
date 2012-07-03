/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.comment.dao.jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.silverpeas.comment.CommentRuntimeException;
import com.silverpeas.comment.dao.CommentDAO;
import com.silverpeas.comment.dao.CommentedPublicationInfoComparator;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.model.CommentedPublicationInfo;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
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
      JDBCCommentRequester commentDAO = getCommentDAO();
      commentPK = commentDAO.saveComment(con, cmt);
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
      JDBCCommentRequester commentDAO = getCommentDAO();
      commentDAO.deleteComment(con, pk);
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
      JDBCCommentRequester commentDAO = getCommentDAO();
      commentDAO.updateComment(con, cmt);
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
      JDBCCommentRequester commentDAO = getCommentDAO();
      comment = commentDAO.getComment(con, pk);
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
      JDBCCommentRequester commentDAO = getCommentDAO();
      return commentDAO.getMostCommentedAllPublications(con);
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
  public List<CommentedPublicationInfo> getMostCommentedPublications(final String resourceType,
      final List<WAPrimaryKey> pks) {
    List<CommentedPublicationInfo> commentedPubs = new ArrayList<CommentedPublicationInfo>();
    Connection con = openConnection();
    JDBCCommentRequester commentDAO = getCommentDAO();
    if (pks != null && !pks.isEmpty()) {
      try {
        for (WAPrimaryKey pubKey : pks) {
          commentedPubs.add(new CommentedPublicationInfo(resourceType, pubKey.getId(), pubKey
              .getInstanceId(),
              commentDAO.getCommentsCount(con, resourceType, pubKey)));
        }
        Collections.sort(commentedPubs, new CommentedPublicationInfoComparator());
      } catch (Exception e) {
        throw new CommentRuntimeException(getClass().getSimpleName() + ".getCommentsCount()",
            SilverpeasRuntimeException.ERROR, "comment.GET_MOST_COMMENTED_ITEMS", e);
      } finally {
        closeConnection(con);
      }
    }
    return commentedPubs;
  }

  @Override
  public int getCommentsCountByForeignKey(final String resourceType, final ForeignPK foreign_pk) {
    Connection con = openConnection();
    int publicationCommentsCount = 0;
    try {
      JDBCCommentRequester commentDAO = getCommentDAO();
      publicationCommentsCount = commentDAO.getCommentsCount(con, resourceType, foreign_pk);
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
      JDBCCommentRequester commentDAO = getCommentDAO();
      vRet = commentDAO.getAllComments(con, resourceType, foreign_pk);
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
      JDBCCommentRequester commentDAO = getCommentDAO();
      commentDAO.deleteAllComments(con, resourceType, foreign_pk);
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
      JDBCCommentRequester commentDAO = getCommentDAO();
      commentDAO.moveComments(con, fromResourceType, fromPK, toResourceType, toPK);
    } catch (Exception re) {
      throw new CommentRuntimeException(getClass().getSimpleName() + ".moveComments()",
          SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED",
          re);
    } finally {
      closeConnection(con);
    }
  }
}
