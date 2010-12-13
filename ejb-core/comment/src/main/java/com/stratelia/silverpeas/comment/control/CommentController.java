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
 * FLOSS exception.  You should have received a copy of the text describing
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
import javax.ejb.RemoveException;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.comment.ejb.CommentBm;
import com.stratelia.silverpeas.comment.ejb.CommentBmHome;
import com.stratelia.silverpeas.comment.ejb.CommentRuntimeException;
import com.stratelia.silverpeas.comment.model.Comment;
import com.stratelia.silverpeas.comment.model.CommentPK;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
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
import java.util.List;

/**
 * A service providing features to handle comments in Silverpeas.
 *
 * A comment is text written by users about a content that was published in Silverpeas. It is
 * about commenting a content. Such comment, as any other contents in Silverpeas, can be indexed
 * in order to be found by the search engine of Silverpeas.
 * The CommentController provides the different operations for creating, getting, updating and
 * deleting a comment.
 */
public class CommentController {
  private CommentBm commentBm = null;

  /**
   * Constructs a new CommentController instance.
   */
  public CommentController() {
  }

  /**
   * Gets the business session bean that will responsible to perform the different operations on
   * the comments.
   * @return a session EJB.
   */
  protected CommentBm getCommentBm() {
    if (commentBm == null) {
      try {
        CommentBmHome commentHome = (CommentBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.COMMENT_EJBHOME, CommentBmHome.class);
        commentBm = commentHome.create();
      } catch (Exception e) {
        throw new CommentRuntimeException("CommentController.initHome()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return commentBm;
  }

  /**
   * Creates the specified comment into the business layer. Once created, the comment is saved in
   * this layer and can be uniquely identified by an identifier (a primary key).
   *
   * The comment is by default indexed and thus can be found by the search engine of Silverpeas.
   * All callback registered for the actions of adding a comment are invoked.
   * @param cmt the comment to save.
   * @return the unique identifier of the comment within the business layer. This identifier is
   * set to the specified comment.
   * @throws RemoteException if an error occurs while creating the comment.
   */
  public CommentPK createComment(Comment cmt) throws RemoteException {
    return createComment(cmt, true);
  }

  /**
   * Creates the specified comment into the business layer and indicates if it should be indexed to
   * be found by the search engine of Silverpeas.
   *
   * Once created, the comment is saved in this layer and can be uniquely identified by an
   * identifier (a primary key).
   * All callback registered for the actions of adding a comment are invoked.
   *
   * @param cmt the comment to save.
   * @param indexIt indicates if the comment should be indexed in order to be found by the search
   * engine of Silverpeas.
   * @return the unique identifier of the comment within the business layer. This identifier is
   * set to the specified comment.
   * @throws RemoteException if an error occurs while creating the comment.
   */
  public CommentPK createComment(Comment cmt, boolean indexIt)
      throws RemoteException {
    CommentPK newPK = getCommentBm().createComment(cmt);
    cmt.setCommentPK(newPK);

    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.invoke(CallBackManager.ACTION_COMMENT_ADD, cmt.getOwnerId(),
        cmt.getForeignKey().getId(), cmt);

    if (indexIt) {
      createIndex(cmt);
    }

    return newPK;
  }

  /**
   * Deletes the comment identified by the specified identifier.
   * All callback registered for the actions of deleting a comment are invoked.
   * @param pk the unique identifier of the comment to remove from the business layer (the primary
   * key in the EJB jargon).
   * @throws RemoteException if an error occurs while deleting the comment in the business layer.
   */
  public void deleteComment(CommentPK pk) throws RemoteException {
    Comment comment = getComment(pk);
    deleteComment(comment);
  }

  /**
   * Deletes all of the comments that refer the resource identified by the specified identifier.
   * All callback registered for the actions of deleting a comment are invoked.
   * @param pk the identifier of the content the comments refer to (aka the foreign key).
   * @throws RemoteException if an error occurs while deleting the given comments.
   */
  public void deleteCommentsByForeignPK(WAPrimaryKey pk)
      throws RemoteException {
    List<Comment> comments = getCommentBm().getAllComments(pk);
    for (Comment comment : comments) {
      deleteComment(comment);
    }
  }

  /**
   * Moves the comments that refer to a resource in order to refer another resource.
   * @param fromPK the identifier of the source content (aka source foreign key).
   * @param toPK the identifier of the destination content (aka destination foreign key).
   * @param indexIt indicates if the comments should be reindexed (or simply indexed) to be found
   * by the search engine of Silverpeas.
   * @throws RemoteException if the move operation in the business layer has failed.
   */
  public void moveComments(ForeignPK fromPK, ForeignPK toPK,
      boolean indexIt) throws RemoteException {
    unindexCommentsByForeignKey(fromPK);

    getCommentBm().moveComments(fromPK, toPK);

    if (indexIt) {
      indexCommentsByForeignKey(toPK);
    }
  }

  /**
   * Updates the specified comment in the business layer.
   * The specified comment refers by the unique identifier its representation in the business layer
   * and it carries the updated information from which its representation will be updated.
   * @param cmt the updated comment.
   * @throws RemoteException if an error occurs while updating the comment in the business layer.
   */
  public void updateComment(Comment cmt) throws RemoteException {
    updateComment(cmt, true);
  }

  public void updateComment(Comment cmt, boolean indexIt)
      throws RemoteException {
    getCommentBm().updateComment(cmt);
    if (indexIt) {
      createIndex(cmt);
    }
  }

  public Comment getComment(CommentPK pk) throws RemoteException {
    Comment newComment = null;
    newComment = getCommentBm().getComment(pk);
    newComment.setOwner(getUserName(newComment));
    return newComment;
  }

  public List<Comment> getAllComments(WAPrimaryKey foreign_pk)
      throws RemoteException {
    return getAllCommentsWithUserName(foreign_pk);
  }

  public List<Comment> getAllCommentsWithUserName(WAPrimaryKey foreign_pk)
      throws RemoteException {
    List<Comment> vComments = getCommentBm().getAllComments(foreign_pk);
    for (Comment comment : vComments) {
      comment.setOwner(getUserName(comment));
    }
    return vComments;
  }

  public void indexCommentsByForeignKey(WAPrimaryKey foreignKey)
      throws RemoteException {
    List<Comment> vComments = getCommentBm().getAllComments(foreignKey);
    for (Comment comment : vComments) {
      createIndex(comment);
    }
  }

  public void unindexCommentsByForeignKey(WAPrimaryKey foreignKey)
      throws RemoteException {
    List<Comment> vComments = getCommentBm().getAllComments(foreignKey);
    for (Comment comment : vComments) {
      deleteIndex(comment);
    }
  }

  private void createIndex(Comment cmt) throws RemoteException {
    SilverTrace.debug("comment", "CommentController.createIndex", "cmt = "
        + cmt.toString());

    int titleLength = 30;
    String commentMessage = cmt.getMessage();
    String commentTitle = commentMessage;
    if (commentMessage != null && commentTitle.length() > titleLength) {
      commentTitle = commentTitle.substring(0, titleLength) + "...";
    }

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

  private void deleteIndex(CommentPK pk) throws RemoteException {
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

  private void deleteIndex(Comment comment) throws RemoteException {
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

  private String getUserName(UserDetail userDetail) {
    return userDetail.getFirstName() + " " + userDetail.getLastName();
  }

  private String getUserName(Comment cmt) {
    UserDetail userDetail = getOrganizationController().getUserDetail(String
        .valueOf(cmt.getOwnerId()));
    return getUserName(userDetail);
  }

  public void close() {
    try {
      if (getCommentBm() != null) {
        commentBm.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("comment", "CommentController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("comment", "CommentController.close", "", e);
    }
  }

  /**
   * Deletes the specified comment and remove all indexes on it.
   * @param comment the comment to remove.
   * @throws RemoteException  if an error occurs while delegating the remove operation to the
   * EJB.
   */
  private void deleteComment(final Comment comment) throws RemoteException {
    deleteIndex(comment);
    getCommentBm().deleteComment(comment.getCommentPK());

    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.invoke(CallBackManager.ACTION_COMMENT_REMOVE, comment.getOwnerId(),
        comment.getForeignKey().getId(), comment);
  }

  /**
   * Gets an organization controller.
   * @return an OrganizationController instance.
   */
  protected OrganizationController getOrganizationController() {
    return new OrganizationController();
  }
}