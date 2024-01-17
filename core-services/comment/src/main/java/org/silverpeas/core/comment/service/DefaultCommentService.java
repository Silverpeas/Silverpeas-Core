/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.comment.service;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.comment.dao.CommentDAO;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.service.notification.CommentEventNotifier;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Named;
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
@Service
@Named("commentService")
public class DefaultCommentService implements CommentService, ComponentInstanceDeletion {

  private static final String MESSAGES_PATH = "org.silverpeas.util.comment.multilang.comment";

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

  @Override
  public Comment createComment(final Comment cmt) {
    Comment comment = getCommentDAO().saveComment(cmt);
    notifier.notifyEventOn(ResourceEvent.Type.CREATION, comment);
    return comment;
  }

  @Override
  public Comment createAndIndexComment(final Comment cmt) {
    Comment comment = createComment(cmt);
    createIndex(comment);
    return comment;
  }

  @Override
  public void deleteComment(final CommentId commentId) {
    Comment comment = getComment(commentId);
    deleteComment(comment);
  }

  @Override
  public void deleteAllCommentsOnResource(final String resourceType,
      final ResourceReference resourceRef) {
    List<Comment> comments = getCommentDAO().getAllCommentsByForeignKey(resourceType, resourceRef);
    for (Comment comment : comments) {
      deleteComment(comment);
    }
  }

  @Override
  public void deleteComment(final Comment comment) {
    deleteIndex(comment);
    getCommentDAO().removeComment(comment.getIdentifier());
    notifier.notifyEventOn(ResourceEvent.Type.DELETION, comment);
  }

  @Override
  public void moveComments(final String resourceType, final ResourceReference fromResource,
      final ResourceReference toResource) {
    unindexAllCommentsOnPublication(resourceType, fromResource);
    getCommentDAO().moveComments(resourceType, fromResource, toResource);
  }

  @Override
  public void moveAndReindexComments(final String resourceType,
      final ResourceReference fromResource, final ResourceReference toResource) {
    moveComments(resourceType, fromResource, toResource);
    indexAllCommentsOnPublication(resourceType, toResource);
  }

  @Override
  public void updateComment(final Comment cmt) {
    getCommentDAO().updateComment(cmt);
  }

  @Override
  public void updateAndIndexComment(final Comment cmt) {
    updateComment(cmt);
    createIndex(cmt);
  }

  @Override
  public Comment getComment(final CommentId commentId) {
    return getCommentDAO().getComment(commentId);
  }

  @Override
  public List<Comment> getAllCommentsOnResource(final String resourceType,
      final ResourceReference resourceRef) {
    return getCommentDAO().getAllCommentsByForeignKey(resourceType,
        new ResourceReference(resourceRef));
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(final String resourceType,
      final List<ResourceReference> resourceRefs) {
    return getCommentDAO().getMostCommentedPublications(resourceType, resourceRefs);
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(
      final String resourceType) {
    return getCommentDAO().getMostCommentedPublications(resourceType);
  }

  @Override
  public List<CommentedPublicationInfo> getAllMostCommentedPublicationsInfo() {
    return getCommentDAO().getAllMostCommentedPublications();
  }

  @Override
  public int getCommentsCountOnResource(final String resourceType, final ResourceReference ref) {
    return getCommentDAO().getCommentsCountByForeignKey(resourceType, ref);
  }

  @Override
  public void indexAllCommentsOnPublication(final String resourceType,
      final ResourceReference ref) {
    List<Comment> vComments = getCommentDAO().getAllCommentsByForeignKey(resourceType, ref);
    for (Comment comment : vComments) {
      createIndex(comment);
    }
  }

  @Override
  public void unindexAllCommentsOnPublication(final String resourceType,
      final ResourceReference ref) {
    List<Comment> vComments = getCommentDAO().getAllCommentsByForeignKey(resourceType, ref);
    for (Comment comment : vComments) {
      deleteIndex(comment);
    }
  }

  private void createIndex(final Comment cmt) {
    String commentMessage = cmt.getMessage();
    String component = cmt.getIdentifier().getComponentInstanceId();
    String resId = cmt.getResourceReference().getLocalId();
    try {
      FullIndexEntry indexEntry =
          new FullIndexEntry(new IndexEntryKey(component, "Comment",
              cmt.getIdentifier().getLocalId(), resId));
      indexEntry.setCreationDate(cmt.getCreationDate());
      indexEntry.setCreationUser(cmt.getCreatorId());
      indexEntry.setTitle(StringUtil.EMPTY);
      indexEntry.setPreview(commentMessage);
      indexEntry.addTextContent(commentMessage);
      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private void deleteIndex(final Comment comment) {
    String componentId = comment.getIdentifier().getComponentInstanceId();
    try {
      IndexEntryKey indexEntry =
          new IndexEntryKey(componentId, "Comment",
              comment.getIdentifier().getLocalId(),
              comment.getResourceReference().getLocalId());
      IndexEngineProxy.removeIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
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
  public LocalizationBundle getComponentMessages(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }

  @Override
  public List<Comment> getLastComments(String resourceType, int count) {
    return getCommentDAO().getLastComments(resourceType, count);
  }

  @Override
  public List<SocialInformationComment> getSocialInformationCommentsListByUserId(
      List<String> resourceTypes, String userId, Period period) {
    return getCommentDAO().getSocialInformationCommentsListByUserId(resourceTypes, userId, period);
  }

  @Override
  public List<SocialInformationComment> getSocialInformationCommentsListOfMyContacts(
      List<String> resourceTypes, List<String> myContactsIds, List<String> instanceIds,
      Period period) {
    return getCommentDAO().getSocialInformationCommentsListOfMyContacts(resourceTypes,
        myContactsIds, instanceIds, period);
  }

  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    getCommentDAO().removeAllCommentsByForeignPk(null,
        new ResourceReference(null, componentInstanceId));
  }
}
