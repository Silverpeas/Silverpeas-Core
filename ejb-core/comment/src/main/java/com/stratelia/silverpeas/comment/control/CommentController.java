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
package com.stratelia.silverpeas.comment.control;

import java.rmi.RemoteException;
import java.util.Vector;

import javax.ejb.RemoveException;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.comment.ejb.CommentBm;
import com.stratelia.silverpeas.comment.ejb.CommentBmHome;
import com.stratelia.silverpeas.comment.ejb.CommentRuntimeException;
import com.stratelia.silverpeas.comment.model.Comment;
import com.stratelia.silverpeas.comment.model.CommentPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

public class CommentController {
  // the home interface of session bean CommentBmEJB
  private static CommentBm commentBm = null;

  /**
   * the constructor.
   */
  public CommentController() {
  }

  /**
   * to initilize the home object of Comment EJB
   * @return void
   */
  private static CommentBm getCommentBm() {
    if (commentBm == null) {
      try {
        CommentBmHome verHome = (CommentBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.COMMENT_EJBHOME, CommentBmHome.class);
        commentBm = verHome.create();
      } catch (Exception e) {
        throw new CommentRuntimeException("CommentController.initHome()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return commentBm;
  }

  public static CommentPK createComment(Comment cmt) throws RemoteException {
    return createComment(cmt, true);
  }

  public static CommentPK createComment(Comment cmt, boolean indexIt)
      throws RemoteException {
    CommentPK newPK = getCommentBm().createComment(cmt);
    cmt.setCommentPK(newPK);

    if (indexIt)
      createIndex(cmt);

    return newPK;
  }

  public static void deleteComment(CommentPK pk) throws RemoteException {
    deleteIndex(pk);
    getCommentBm().deleteComment(pk);
  }

  public static void deleteCommentsByForeignPK(WAPrimaryKey pk)
      throws RemoteException {
    Vector<Comment> comments = getCommentBm().getAllComments(pk);
    for (Comment comment : comments) {
      getCommentBm().deleteComment(comment.getCommentPK());
      deleteIndex(comment);
    }
  }

  public static void moveComments(ForeignPK fromPK, ForeignPK toPK,
      boolean indexIt) throws RemoteException {
    unindexCommentsByForeignKey(fromPK);

    getCommentBm().moveComments(fromPK, toPK);

    if (indexIt)
      indexCommentsByForeignKey(toPK);
  }

  public static void updateComment(Comment cmt) throws RemoteException {
    updateComment(cmt, true);
  }

  public static void updateComment(Comment cmt, boolean indexIt)
      throws RemoteException {
    getCommentBm().updateComment(cmt);
    if (indexIt)
      createIndex(cmt);
  }

  public static Comment getComment(CommentPK pk) throws RemoteException {
    Comment newComment = null;
    newComment = getCommentBm().getComment(pk);
    newComment.setOwner(getUserName(newComment));
    return newComment;
  }

  public static Vector<Comment> getAllComments(WAPrimaryKey foreign_pk)
      throws RemoteException {
    Vector<Comment> vComments = getCommentBm().getAllComments(foreign_pk);
    for (Comment comment : vComments) {
      comment.setOwner(getUserName(comment));
    }
    return vComments;
  }

  public static Vector<Comment> getAllCommentsWithUserName(WAPrimaryKey foreign_pk)
      throws RemoteException {
    Vector<Comment> vComments = getCommentBm().getAllComments(foreign_pk);
    for (Comment comment : vComments) {
      comment.setOwner(getUserName(comment));
    }
    return vComments;
  }

  public static void indexCommentsByForeignKey(WAPrimaryKey foreignKey)
      throws RemoteException {
    Vector<Comment> vComments = getCommentBm().getAllComments(foreignKey);
    for (Comment comment : vComments) {
      createIndex(comment);
    }
  }

  public static void unindexCommentsByForeignKey(WAPrimaryKey foreignKey)
      throws RemoteException {
    Vector<Comment> vComments = getCommentBm().getAllComments(foreignKey);
    for (Comment comment : vComments) {
      deleteIndex(comment);
    }
  }

  private static void createIndex(Comment cmt) throws RemoteException {
    SilverTrace.debug("comment", "CommentController.createIndex", "cmt = "
        + cmt.toString());

    int titleLength = 30;
    String commentMessage = cmt.getMessage();
    String commentTitle = commentMessage;
    if (commentMessage != null && commentTitle.length() > titleLength)
      commentTitle = commentTitle.substring(0, titleLength) + "...";

    String component = cmt.getCommentPK().getComponentName();
    String fk = cmt.getForeignKey().getId();

    try {
      FullIndexEntry indexEntry = new FullIndexEntry(component, "Comment"
          + cmt.getCommentPK().getId(), fk);
      indexEntry.setTitle(commentTitle);
      indexEntry.setPreView(commentMessage);
      indexEntry.setCreationDate(cmt.getCreationDate());
      indexEntry.setCreationUser(cmt.getOwner());
      indexEntry.addTextContent(commentMessage);
      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverTrace.warn("comment", "CommentController.createIndex()",
          "root.EX_INDEX_FAILED", e);
    }
  }

  private static void deleteIndex(CommentPK pk) throws RemoteException {
    SilverTrace.debug("comment", "CommentController.deleteIndex",
        "Comment : deleteIndex()", "PK=" + pk.toString());

    Comment cmt = getCommentBm().getComment(pk);
    String component = pk.getComponentName();
    try {
      IndexEntryPK indexEntry = new IndexEntryPK(component, "Comment"
          + pk.getId(), cmt.getForeignKey().getId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverTrace.warn("comment",
          "CommentController.deleteIndex(CommentPK pk)",
          "root.EX_INDEX_DELETE_FAILED", e);
    }
  }

  private static void deleteIndex(Comment comment) throws RemoteException {
    SilverTrace.debug("comment", "CommentController.deleteIndex",
        "Comment : deleteIndex()", "comment=" + comment.toString());

    String component = comment.getCommentPK().getComponentName();
    try {
      IndexEntryPK indexEntry = new IndexEntryPK(component, "Comment"
          + comment.getCommentPK().getId(), comment.getForeignKey().getId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverTrace.warn("comment",
          "CommentController.deleteIndex(CommentPK pk)",
          "root.EX_INDEX_DELETE_FAILED", e);
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

  public void close() {
    try {
      if (getCommentBm() != null)
        commentBm.remove();
    } catch (RemoteException e) {
      SilverTrace.error("comment", "CommentController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("comment", "CommentController.close", "", e);
    }
  }
}