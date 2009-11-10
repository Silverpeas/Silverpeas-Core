/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.comment.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.comment.model.Comment;
import com.stratelia.silverpeas.comment.model.CommentDAO;
import com.stratelia.silverpeas.comment.model.CommentInfo;
import com.stratelia.silverpeas.comment.model.CommentInfoComparator;
import com.stratelia.silverpeas.comment.model.CommentPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public class CommentBmEJB implements SessionBean {

  private Connection openConnection() {
    try {
      Connection con = DBUtil.makeConnection(JNDINames.NODE_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new CommentRuntimeException("CommentBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void closeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("comment", "CommentBmEJB.closeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  public CommentPK createComment(Comment cmt) throws RemoteException {
    Connection con = openConnection();
    CommentPK commentPK;
    try {
      commentPK = CommentDAO.createComment(con, cmt);
      if (commentPK == null) {
        throw new CommentRuntimeException("CommentBmEJB.createComment()",
            SilverpeasRuntimeException.ERROR,
            "comment.CREATING_NEW_COMMENT_FAILED");
      }
      return commentPK;
    } catch (Exception re) {
      throw new CommentRuntimeException("CommentBmEJB.createComment()",
          SilverpeasRuntimeException.ERROR,
          "comment.CREATING_NEW_COMMENT_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }

  public void deleteComment(CommentPK pk) throws RemoteException {
    Connection con = openConnection();
    try {
      CommentDAO.deleteComment(con, pk);
    } catch (Exception re) {
      throw new CommentRuntimeException("CommentBmEJB.deleteComment()",
          SilverpeasRuntimeException.ERROR, "comment.DELETE_COMMENT_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }

  public void updateComment(Comment cmt) throws RemoteException {
    Connection con = openConnection();
    try {
      CommentDAO.updateComment(con, cmt);
    } catch (Exception re) {
      throw new CommentRuntimeException("CommentBmEJB.updateComment()",
          SilverpeasRuntimeException.ERROR, "comment.UPDATE_COMMENT_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }

  public Comment getComment(CommentPK pk) throws RemoteException {
    Connection con = openConnection();
    Comment comment;
    try {
      comment = CommentDAO.getComment(con, pk);
      if (comment == null) {
        throw new CommentRuntimeException("CommentBmEJB.getComment()",
            SilverpeasRuntimeException.ERROR, "comment.GET_COMMENT_FAILED");
      }
      return comment;
    } catch (Exception re) {
      throw new CommentRuntimeException("CommentBmEJB.getComment()",
          SilverpeasRuntimeException.ERROR, "comment.GET_COMMENT_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }

  public Collection getMostCommentedAllPublications() throws RemoteException {
    Connection con = openConnection();
    try {
      return CommentDAO.getMostCommentedAllPublications(con);
    } catch (Exception e) {
      throw new RemoteException("Problème Base de données", e);
    } finally {
      closeConnection(con);
    }
  }

  public Collection getMostCommented(Collection pks, int commentsCount)
      throws RemoteException {
    ArrayList comments = new ArrayList();
    Connection con = openConnection();
    if (pks != null && !pks.isEmpty()) {
      try {
        Iterator iter = pks.iterator();
        while (iter.hasNext()) {
          CommentPK commentPk = (CommentPK) iter.next();
          comments.add(new CommentInfo(CommentDAO.getCommentsCount(con,
              commentPk), commentPk.getInstanceId(), commentPk.getId()));
        }
        Collections.sort(comments, new CommentInfoComparator());
        if (comments.size() > commentsCount) {
          comments.subList(0, commentsCount);
        }
      } catch (Exception e) {

      } finally {
        closeConnection(con);
      }
    }
    return comments;
  }

  public int getCommentsCount(WAPrimaryKey foreign_pk) {
    Connection con = openConnection();
    int publicationCommentsCount = 0;
    try {
      publicationCommentsCount = CommentDAO.getCommentsCount(con, foreign_pk);
    } catch (Exception re) {
      throw new CommentRuntimeException("CommentBmEJB.getCommentsCount()",
          SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED",
          re);
    } finally {
      closeConnection(con);
    }
    return publicationCommentsCount;
  }

  public Vector getAllComments(WAPrimaryKey foreign_pk) throws RemoteException {
    Connection con = openConnection();
    Vector vRet;
    try {
      vRet = CommentDAO.getAllComments(con, foreign_pk);
      if (vRet == null) {
        throw new CommentRuntimeException("CommentBmEJB.getAllComments()",
            SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED");
      }
      return vRet;
    } catch (Exception re) {
      throw new CommentRuntimeException("CommentBmEJB.getAllComments()",
          SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED",
          re);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * TODO: methode utilisée par les taglibs : elle positionne le nom de
   * l'auteur du commentaire (fait habituellement dans le CommentController => a
   * voir si on modifie l'ejb pour que ce soit systematiquement fait ou non)
   * 
   * @param foreign_pk
   * @return
   * @throws RemoteException
   */
  public Vector getAllCommentsWithUserName(WAPrimaryKey foreign_pk)
      throws RemoteException {
    Connection con = openConnection();
    Vector vRet;
    Vector vReturn = new Vector();
    Comment comment;
    try {
      vRet = CommentDAO.getAllComments(con, foreign_pk);
      if (vRet == null) {
        throw new CommentRuntimeException("CommentBmEJB.getAllComments()",
            SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED");
      } else {
        for (Enumeration e = vRet.elements(); e.hasMoreElements();) {
          comment = (Comment) e.nextElement();
          comment.setOwner(getUserName(comment));
          vReturn.addElement(comment);
        }
      }
      return vReturn;
    } catch (Exception re) {
      throw new CommentRuntimeException("CommentBmEJB.getAllComments()",
          SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED",
          re);
    } finally {
      closeConnection(con);
    }
  }

  private static String getUserName(UserDetail userDetail) {
    return userDetail.getFirstName() + " " + userDetail.getLastName();
  }

  private static String getUserName(Comment cmt) {
    UserDetail userDetail = (new OrganizationController()).getUserDetail(String
        .valueOf(cmt.getOwnerId()));
    return getUserName(userDetail);
  }

  public void deleteAllComments(ForeignPK foreign_pk) throws RemoteException {
    Connection con = openConnection();
    try {
      CommentDAO.deleteAllComments(con, foreign_pk);
    } catch (Exception re) {
      throw new CommentRuntimeException("CommentBmEJB.getAllComments()",
          SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED",
          re);
    } finally {
      closeConnection(con);
    }
  }

  public void moveComments(ForeignPK fromPK, ForeignPK toPK)
      throws RemoteException {
    Connection con = openConnection();
    try {
      CommentDAO.moveComments(con, fromPK, toPK);
    } catch (Exception re) {
      throw new CommentRuntimeException("CommentBmEJB.moveComments()",
          SilverpeasRuntimeException.ERROR, "comment.GET_ALL_COMMENTS_FAILED",
          re);
    } finally {
      closeConnection(con);
    }
  }

  public void ejbCreate() throws CreateException {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbRemove() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbActivate() {
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void ejbPassivate() {
  }

  /**
   * Method declaration
   * 
   * 
   * @param sc
   * 
   * @see
   */
  public void setSessionContext(SessionContext sc) {
  }
}
