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

package com.silverpeas.comment.service;

import java.util.List;



import com.silverpeas.comment.dao.CommentDAO;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.model.CommentedPublicationInfo;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * A service that provide the features to handle the comments in Silverpeas. Such features are, for
 * example, retrieving comments on a given content, creating a comment into the business layer,
 * indexing them, notifying components interested by the creation or the deletion of a comment, and
 * so on. A comment is text written by users about a content published in Silverpeas. Such comment,
 * as any other contents in Silverpeas, can be indexed in order to be found by the Silverpeas search
 * engine. This service is managed by an IoC container and this be retrieved by dependency
 * injection.
 */
@Named("commentService")
public class CommentService {

  @Inject
  private CommentDAO commentDAO;

  /**
   * Constructs a new CommentService instance.
   */
  protected CommentService() {
  }

  /**
   * Gets the comment DAO with wich operations with the underlying data source can be performed.
   * @return the DAO on the comments.
   */
  protected CommentDAO getCommentDAO() {
    return commentDAO;
  }

  /**
   * Creates the specified comment into the business layer. Once created, the comment is saved in
   * this layer and can be uniquely identified by an identifier (a primary key). All callback
   * interested by the adding of a comment will be invoked through the CallBackManager. The callback
   * will recieve as invocation parameters respectively the identifier of the commented publication,
   * the component instance name, and the new comment.
   * @param cmt the comment to save.
   */
  public void createComment(final Comment cmt) {
    CommentPK newPK = getCommentDAO().saveComment(cmt);
    cmt.setCommentPK(newPK);
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.invoke(CallBackManager.ACTION_COMMENT_ADD,
        Integer.parseInt(cmt.getForeignKey().getId()),
        cmt.getForeignKey().getComponentName(), cmt);
  }

  /**
   * Creates and indexes the specified comment into the business layer. Once created, the comment is
   * saved in this layer and can be uniquely identified by an identifier (a primary key). It is
   * indexed so that it can be easily retrieved by the search engine of Silverpeas. All callback
   * interested by the adding of a comment will be invoked through the CallBackManager. The callback
   * will recieve as invocation parameters respectively the identifier of the commented publication,
   * the component instance name, and the new comment.
   * @param cmt the comment to save.
   */
  public void createAndIndexComment(final Comment cmt) {
    createComment(cmt);
    createIndex(cmt);
  }

  /**
   * Deletes the comment identified by the specified identifier. Any indexes on it are removed. All
   * callback interested by the deletion of a comment will be invoked through the CallBackManager.
   * The callback will recieve as invocation parameters respectively the identifier of the commented
   * publication, the component instance name, and the deleted comment. If no such comment exists
   * with the specified identifier, then a CommentRuntimeException is thrown.
   * @param pk the unique identifier of the comment to remove from the business layer (the primary
   * key).
   */
  public void deleteComment(final CommentPK pk) {
    Comment comment = getComment(pk);
    deleteComment(comment);
  }

  /**
   * Deletes all of the comments on the publication identified by the specified identifier. Any
   * indexes on it are removed. AAll callback interested by the deletion of a comment will be
   * invoked through the CallBackManager. The callback will recieve as invocation parameters
   * respectively the identifier of the commented publication, the component instance name, and the
   * deleted comment. If no such publication exists with the specified identifier, then a
   * CommentRuntimeException is thrown.
   * @param pk the identifier of the publication the comments are on.
   */
  public void deleteAllCommentsOnPublication(final WAPrimaryKey pk) {
    List<Comment> comments = getCommentDAO().getAllCommentsByForeignKey(new ForeignPK(pk));
    for (Comment comment : comments) {
      deleteComment(comment);
    }
  }

  /**
   * Deletes the specified comment. Any indexes on it are removed. If no such comment exists with
   * the specified identifier, then a CommentRuntimeException is thrown. All callback interested by
   * the deletion of a comment will be invoked through the CallBackManager. The callback will
   * recieve as invocation parameters respectively the identifier of the commented publication, the
   * component instance name, and the deleted comment.
   * @param comment the comment to remove.
   */
  public void deleteComment(final Comment comment) {
    deleteIndex(comment);
    getCommentDAO().removeComment(comment.getCommentPK());

    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.invoke(CallBackManager.ACTION_COMMENT_REMOVE,
        Integer.parseInt(comment.getForeignKey().getId()),
        comment.getForeignKey().getComponentName(),
        comment);
  }

  /**
   * Moves the comments on the specified publication to the another specified publication. The
   * resulting operation is that the comments will be on the another publication and all the
   * previous indexes on them are removed. If at least one of the publications doesn't exist with
   * the specified identifier, then a CommentRuntimeException is thrown.
   * @param fromPK the identifier of the source publication.
   * @param toPK the identifier of the destination publication.
   */
  public void moveComments(final WAPrimaryKey fromPK, final WAPrimaryKey toPK) {
    unindexAllCommentsOnPublication(fromPK);
    getCommentDAO().moveComments(new ForeignPK(fromPK), new ForeignPK(toPK));
  }

  /**
   * Moves the comments on the specified publication to the another specified publication and
   * reindexes them. The resulting operation is that the comments are on the another publication and
   * they are reindexed (or simply indexed if not already) accordingly to the new publication. If at
   * least one of the publications doesn't exist with the specified identifier, then a
   * CommentRuntimeException is thrown.
   * @param fromPK the identifier of the source publication.
   * @param toPK the identifier of the destination publication.
   */
  public void moveAndReindexComments(final WAPrimaryKey fromPK, final WAPrimaryKey toPK) {
    moveComments(fromPK, toPK);
    indexAllCommentsOnPublication(toPK);
  }

  /**
   * Updates the specified comment in the business layer. The comment to update is identified by its
   * unique identifier and the update information are carried by the given Comment instance.
   * @param cmt the updated comment.
   */
  public void updateComment(final Comment cmt) {
    getCommentDAO().updateComment(cmt);
  }

  /**
   * Updates and indexes the specified comment in the business layer. The comment to update in the
   * business layer is identified by its unique identifier and the update information are carried by
   * the given Comment instance.
   * @param cmt the comment to update and to index.
   */
  public void updateAndIndexComment(final Comment cmt) {
    updateComment(cmt);
    createIndex(cmt);
  }

  /**
   * Gets the comment that identified by the specified identifier. If no such comment exists with
   * the specified identifier, then a CommentRuntimeException is thrown.
   * @param pk the identifier of the comment in the business layer.
   * @return the comment.
   */
  public Comment getComment(final CommentPK pk) {
    Comment newComment = null;
    newComment = getCommentDAO().getComment(pk);
    setOwnerDetail(newComment);
    return newComment;
  }

  /**
   * Gets all of the comments on the publication identified by the specified identifier. If no such
   * publication exists with the specified identifier, then a CommentRuntimeException is thrown.
   * @param pk the identifier of the publication.
   * @return a list of the comments on the given publication. The list is empty if the publication
   * isn't commented.
   */
  public List<Comment> getAllCommentsOnPublication(final WAPrimaryKey pk) {
    List<Comment> vComments = getCommentDAO().getAllCommentsByForeignKey(new ForeignPK(pk));
    for (Comment comment : vComments) {
      setOwnerDetail(comment);
    }
    return vComments;
  }

  /**
   * Gets information about the commented publications among the specified ones. The publication
   * information are returned ordered down to the lesser comment publication.
   * @param pks a collection of primary keys refering the publications to get information and to
   * order by comments count.
   * @return an ordered list of information about the most commented publication. The list is sorted
   * by their comments count in a descendent order.
   */
  public List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(
      final List<WAPrimaryKey> pks) {
    return getCommentDAO().getMostCommentedPublications(pks);
  }

  /**
   * Gets information about all the commented publications in Silverpeas. The publication
   * information are returned ordered down to the lesser comment publication.
   * @return an ordered list of information about the most commented publication. The list is sorted
   * by their comments count in a descendent order.
   */
  public List<CommentedPublicationInfo> getAllMostCommentedPublicationsInfo() {
    return getCommentDAO().getAllMostCommentedPublications();
  }

  /**
   * Gets the count of comments on the publication identified by the specified identifier.
   * @param pk the identifier of the publication.
   * @return the number of comments on the publication.
   */
  public int getCommentsCountOnPublication(final WAPrimaryKey pk) {
    return getCommentDAO().getCommentsCountByForeignKey(new ForeignPK(pk));
  }

  /**
   * Indexes all the comments on the publication identified by the specified identifier. If no such
   * publication exists with the specified identifier, then a CommentRuntimeException is thrown.
   * @param pk the identifier of the publication.
   */
  public void indexAllCommentsOnPublication(final WAPrimaryKey pk) {
    List<Comment> vComments = getCommentDAO().getAllCommentsByForeignKey(new ForeignPK(pk));
    for (Comment comment : vComments) {
      createIndex(comment);
    }
  }

  /**
   * Removes the indexes on all the comments of the publication identified by the specified
   * identifier. If no such publication exists with the specified identifier, then a
   * CommentRuntimeException is thrown.
   * @param pk the identifier of the publication.
   */
  public void unindexAllCommentsOnPublication(final WAPrimaryKey pk) {
    List<Comment> vComments = getCommentDAO().getAllCommentsByForeignKey(new ForeignPK(pk));
    for (Comment comment : vComments) {
      deleteIndex(comment);
    }
  }

  private void createIndex(final Comment cmt) {
    SilverTrace.debug("comment", getClass().getSimpleName() + ".createIndex", "cmt = "
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
      SilverTrace.warn("comment", getClass().getSimpleName() + ".createIndex()",
          "root.EX_INDEX_FAILED", e);
    }
  }

  private void deleteIndex(final Comment comment) {
    SilverTrace.debug("comment", getClass().getSimpleName() + ".deleteIndex",
        "Comment : deleteIndex()", "comment=" + comment.toString());

    String component = comment.getCommentPK().getComponentName();
    try {
      IndexEntryPK indexEntry = new IndexEntryPK(component, "Comment"
          + comment.getCommentPK().getId(), comment.getForeignKey().getId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverTrace.warn("comment",
          getClass().getSimpleName() + ".deleteIndex(CommentPK pk)",
          "root.EX_INDEX_DELETE_FAILED", e);
    }
  }

  private void setOwnerDetail(Comment comment) {
    comment.setOwnerDetail(getOrganizationController().getUserDetail(
        Integer.toString(comment.getOwnerId())));
  }

  /**
   * Gets an organization controller.
   * @return an OrganizationController instance.
   */
  protected OrganizationController getOrganizationController() {
    return new OrganizationController();
  }
}
