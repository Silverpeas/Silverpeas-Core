/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.comment.service;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.comment.dao.CommentDAO;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.service.notification.CommentEventNotifier;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.WAPrimaryKey;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.List;

/**
 * A service that provide the features to handle the comments in Silverpeas. Such features are, for
 * example, retrieving comments on a given content, creating a comment into the business layer,
 * indexing them, notifying components interested by the creation or the deletion of a comment, and
 * so on. A comment is text written by users about a content published in Silverpeas. Such comment,
 * as any other contents in Silverpeas, can be indexed in order to be found by the Silverpeas search
 * engine. This service is managed by an IoC container and this be retrieved by dependency
 * injection.
 */
@Singleton
@Named("commentService")
public class DefaultCommentService implements CommentService, ComponentInstanceDeletion {

  private static final String SETTINGS_PATH = "org.silverpeas.util.comment.Comment";
  private static final String MESSAGES_PATH = "org.silverpeas.util.comment.multilang.comment";
  private static final SettingBundle settings = ResourceLocator.getSettingBundle(SETTINGS_PATH);

  @Inject
  private CommentDAO commentDAO;
  @Inject
  private CommentEventNotifier notifier;

  /**
   * Constructs a new DefaultCommentService instance.
   */
  protected DefaultCommentService() {
  }

  /**
   * Gets the comment DAO with wich operations with the underlying data source can be performed.
   *
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
   *
   * @param cmt the comment to save.
   */
  @Override
  public void createComment(final Comment cmt) {
    CommentPK newPK = getCommentDAO().saveComment(cmt);
    cmt.setCommentPK(newPK);
    notifier.notifyEventOn(ResourceEvent.Type.CREATION, cmt);
  }

  /**
   * Creates and indexes the specified comment into the business layer. Once created, the comment is
   * saved in this layer and can be uniquely identified by an identifier (a primary key). It is
   * indexed so that it can be easily retrieved by the search engine of Silverpeas. All callback
   * interested by the adding of a comment will be invoked through the CallBackManager. The callback
   * will recieve as invocation parameters respectively the identifier of the commented publication,
   * the component instance name, and the new comment.
   *
   * @param cmt the comment to save.
   */
  @Override
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
   *
   * @param pk the unique identifier of the comment to remove from the business layer (the primary
   * key).
   */
  @Override
  public void deleteComment(final CommentPK pk) {
    Comment comment = getComment(pk);
    deleteComment(comment);
  }

  /**
   * Deletes all of the comments on the publication identified by the resource type and the
   * specified identifier. Any indexes on it are removed. All callback interested by the deletion of
   * a comment will be invoked through the CallBackManager. The callback will recieve as invocation
   * parameters respectively the identifier of the commented publication, the component instance
   * name, and the deleted comment. If no such publication exists with the specified identifier,
   * then a CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented publication.
   * @param pk the identifier of the publication the comments are on.
   */
  @Override
  public void deleteAllCommentsOnPublication(final String resourceType, final WAPrimaryKey pk) {
    List<Comment> comments = getCommentDAO().getAllCommentsByForeignKey(resourceType, new ForeignPK(
        pk));
    for (Comment comment : comments) {
      deleteComment(comment);
    }
  }

  /**
   * Deletes the specified comment. Any indexes on it are removed. If no such comment exists with
   * the specified identifier, then a CommentRuntimeException is thrown. All callback interested by
   * the deletion of a comment will be invoked through the CallBackManager. The callback will
   * receive as invocation parameters respectively the identifier of the commented publication, the
   * component instance name, and the deleted comment.
   *
   * @param comment the comment to remove.
   */
  @Override
  public void deleteComment(final Comment comment) {
    deleteIndex(comment);
    getCommentDAO().removeComment(comment.getCommentPK());
    notifier.notifyEventOn(ResourceEvent.Type.DELETION, comment);
  }

  /**
   * Moves the comments on the specified publication to the another specified publication. The
   * resulting operation is that the comments will be on the another publication and all the
   * previous indexes on them are removed. If at least one of the publications doesn't exist with
   * the specified identifier, then a CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented publication.
   * @param fromPK the identifier of the source publication.
   * @param toPK the identifier of the destination publication.
   */
  @Override
  public void moveComments(final String resourceType, final WAPrimaryKey fromPK,
      final WAPrimaryKey toPK) {
    unindexAllCommentsOnPublication(resourceType, fromPK);
    getCommentDAO().moveComments(resourceType, new ForeignPK(fromPK), new ForeignPK(toPK));
  }

  /**
   * Moves the comments on the specified publication to the another specified publication and
   * reindexes them. The resulting operation is that the comments are on the another publication and
   * they are reindexed (or simply indexed if not already) accordingly to the new publication. If at
   * least one of the publications doesn't exist with the specified identifier, then a
   * CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented publication.
   * @param fromPK the identifier of the source publication.
   * @param toPK the identifier of the destination publication.
   */
  @Override
  public void moveAndReindexComments(final String resourceType, final WAPrimaryKey fromPK,
      final WAPrimaryKey toPK) {
    moveComments(resourceType, fromPK, toPK);
    indexAllCommentsOnPublication(resourceType, toPK);
  }

  /**
   * Updates the specified comment in the business layer. The comment to update is identified by its
   * unique identifier and the update information are carried by the given Comment instance.
   *
   * @param cmt the updated comment.
   */
  @Override
  public void updateComment(final Comment cmt) {
    getCommentDAO().updateComment(cmt);
  }

  /**
   * Updates and indexes the specified comment in the business layer. The comment to update in the
   * business layer is identified by its unique identifier and the update information are carried by
   * the given Comment instance.
   *
   * @param cmt the comment to update and to index.
   */
  @Override
  public void updateAndIndexComment(final Comment cmt) {
    updateComment(cmt);
    createIndex(cmt);
  }

  /**
   * Gets the comment that identified by the specified identifier. If no such comment exists with
   * the specified identifier, then a CommentRuntimeException is thrown.
   *
   * @param pk the identifier of the comment in the business layer.
   * @return the comment.
   */
  @Override
  public Comment getComment(final CommentPK pk) {
    Comment newComment = null;
    newComment = getCommentDAO().getComment(pk);
    setOwnerDetail(newComment);
    return newComment;
  }

  /**
   * Gets all of the comments on the publication identified by the resource type and the specified
   * identifier. If no such publication exists with the specified identifier, then a
   * CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented publication.
   * @param pk the identifier of the publication.
   * @return a list of the comments on the given publication. The list is empty if the publication
   * isn't commented.
   */
  @Override
  public List<Comment> getAllCommentsOnPublication(final String resourceType, final WAPrimaryKey pk) {
    List<Comment> vComments = getCommentDAO().getAllCommentsByForeignKey(resourceType,
        new ForeignPK(pk));
    for (Comment comment : vComments) {
      setOwnerDetail(comment);
    }
    return vComments;
  }

  /**
   * Gets information about the commented publications among the specified ones. The publication
   * information are returned ordered down to the lesser comment publication.
   *
   * @param resourceType the type of the commented publication.
   * @param pks a collection of primary keys refering the publications to get information and to
   * order by comments count.
   * @return an ordered list of information about the most commented publication. The list is sorted
   * by their comments count in a descendent order.
   */
  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(final String resourceType,
      final List<? extends WAPrimaryKey> pks) {
    return getCommentDAO().getMostCommentedPublications(resourceType, pks);
  }

  /**
   * Gets information about the most commented publications of the specified type. The publication
   * information are returned ordered down to the lesser comment publication.
   *
   * @param resourceType the type of the commented publication.
   * @return an ordered list of information about the most commented publication. The list is sorted
   * by their comments count in a descendent order.
   */
  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(final String resourceType) {
    return getCommentDAO().getMostCommentedPublications(resourceType);
  }

  /**
   * Gets information about all the commented publications in Silverpeas. The publication
   * information are returned ordered down to the lesser comment publication.
   *
   * @return an ordered list of information about the most commented publication. The list is sorted
   * by their comments count in a descendent order.
   */
  @Override
  public List<CommentedPublicationInfo> getAllMostCommentedPublicationsInfo() {
    return getCommentDAO().getAllMostCommentedPublications();
  }

  /**
   * Gets the count of comments on the publication identified by the resource type and the specified
   * identifier.
   *
   * @param resourceType the type of the commented publication.
   * @param pk the identifier of the publication.
   * @return the number of comments on the publication.
   */
  @Override
  public int getCommentsCountOnPublication(final String resourceType, final WAPrimaryKey pk) {
    return getCommentDAO().getCommentsCountByForeignKey(resourceType, new ForeignPK(pk));
  }

  /**
   * Indexes all the comments on the publication identified by the resource type and the specified
   * identifier. If no such publication exists with the specified identifier, then a
   * CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented publication.
   * @param pk the identifier of the publication.
   */
  @Override
  public void indexAllCommentsOnPublication(final String resourceType, final WAPrimaryKey pk) {
    List<Comment> vComments = getCommentDAO().getAllCommentsByForeignKey(resourceType,
        new ForeignPK(pk));
    for (Comment comment : vComments) {
      createIndex(comment);
    }
  }

  /**
   * Removes the indexes on all the comments of the publication identified by the resource type and
   * the specified identifier. If no such publication exists with the specified identifier, then a
   * CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented publication.
   * @param pk the identifier of the publication.
   */
  @Override
  public void unindexAllCommentsOnPublication(final String resourceType, final WAPrimaryKey pk) {
    List<Comment> vComments = getCommentDAO().getAllCommentsByForeignKey(resourceType,
        new ForeignPK(pk));
    for (Comment comment : vComments) {
      deleteIndex(comment);
    }
  }

  private void createIndex(final Comment cmt) {
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
    String component = comment.getCommentPK().getComponentName();
    try {
      IndexEntryKey indexEntry = new IndexEntryKey(component, "Comment"
          + comment.getCommentPK().getId(), comment.getForeignKey().getId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverTrace.warn("comment",
          getClass().getSimpleName() + ".deleteIndex(CommentPK pk)",
          "root.EX_INDEX_DELETE_FAILED", e);
    }
  }

  private void setOwnerDetail(Comment comment) {
    comment.setOwnerDetail(getOrganisationController().getUserDetail(
        Integer.toString(comment.getOwnerId())));
  }

  /**
   * Gets an organization controller.
   *
   * @return an OrganizationController instance.
   */
  protected OrganizationController getOrganisationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }

  @Override
  public SettingBundle getComponentSettings() {
    return settings;
  }

  @Override
  public LocalizationBundle getComponentMessages(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }

  @Override
  public List<Comment> getLastComments(String resourceType, int count) {
    return getCommentDAO().getLastComments(resourceType, count);
  }

  /**
   * Get the list of SocialInformationComment added by userId in a period
   * @param resourceTypes the aimed resources types.
   * @param userId the author of comments.
   * @param period the period into which the comment has been created or modified.
   * @return List of {@link SocialInformation}
   */
  @Override
  public List<SocialInformationComment> getSocialInformationCommentsListByUserId(
      List<String> resourceTypes, String userId, Period period) {
    return getCommentDAO().getSocialInformationCommentsListByUserId(resourceTypes, userId, period);
  }

  /**
   * Gets the list of SocialInformationComment added by myContactsIds in a period
   * @param resourceTypes the aimed resources types.
   * @param myContactsIds the aimed user identifiers of contacts.
   * @param instanceIds the aimed identifiers of component instances.
   * @param period the period into which the comment has been created or modified.
   * @return List of {@link SocialInformation}
   */
  @Override
  public List<SocialInformationComment> getSocialInformationCommentsListOfMyContacts(
      List<String> resourceTypes, List<String> myContactsIds, List<String> instanceIds,
      Period period) {
    return getCommentDAO()
        .getSocialInformationCommentsListOfMyContacts(resourceTypes, myContactsIds, instanceIds,
            period);
  }

  /**
   * Deletes all of the comments related to the specified the component instance. If there is no
   * comments for the specified component instance, then nothing is done.
   * @param componentInstanceId the identifier of the component instance that is in deletion.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    getCommentDAO().removeAllCommentsByForeignPk(null, new ForeignPK(null, componentInstanceId));
  }
}
