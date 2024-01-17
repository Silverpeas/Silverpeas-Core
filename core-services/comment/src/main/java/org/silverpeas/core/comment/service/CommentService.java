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
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

/**
 * User: ehugonnet Date: 21/03/11 Time: 09:54
 */
public interface CommentService {

  static CommentService get() {
    return ServiceProvider.getSingleton(CommentService.class);
  }

  /**
   * Persists the specified comment and notifies any observers about the comment creation.
   * @param cmt the comment to save.
   * @return the created comment with its unique identifier.
   */
  Comment createComment(Comment cmt);

  /**
   * Persists and indexes the specified comment and notifies any observers about the comment
   * creation.
   * @param cmt the comment to save and to index.
   * @return the created comment with its unique identifier.
   */
  Comment createAndIndexComment(Comment cmt);

  /**
   * Deletes the comment identified by the specified identifier and notifies any observers about
   * the comment deletion. Any indexes on it are removed. If no such comment exists
   * with the specified identifier, then a CommentRuntimeException is thrown.
   *
   * @param commentId the unique identifier of the comment to remove.
   */
  void deleteComment(CommentId commentId);

  /**
   * Deletes all the comments on the specified resource and notifies any observers about those
   * comments deletion. If no such resource exists with the specified identifier then a
   * CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented resource.
   * @param resourceRef a reference to the resource the comments are on.
   */
  void deleteAllCommentsOnResource(final String resourceType, ResourceReference resourceRef);

  /**
   * Deletes the specified comment and notifies about any observers about his deletion. Any indexes
   * on it are removed. If no such comment exists with
   * the specified identifier, then a CommentRuntimeException is thrown.
   *
   * @param comment the comment to remove.
   */
  void deleteComment(Comment comment);

  /**
   * Moves the comments on the specified resource to the another specified resource. The
   * resulting operation is that the comments will be on the other resource and all the
   * previous indexes on them are removed. If at least one of the resources doesn't exist then a
   * CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented resource. Both the source and target resources
   *                     have to be of the same type.
   * @param fromResource a reference to the source resource.
   * @param toResource a reference to the destination resource.
   */
  void moveComments(final String resourceType, ResourceReference fromResource,
      ResourceReference toResource);

  /**
   * Moves the comments on the specified resource to the another specified resource and
   * indexes them again. The resulting operation is that the comments are on the other resource and
   * they are indexed again (or simply indexed if not already) accordingly to the new resource. If
   * at least one of the resources doesn't exist then a CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented resource. Both the source and target resources
   *                     have to be of the same type.
   * @param fromResource a reference to the source resource.
   * @param toResource a reference to the destination resource.
   */
  void moveAndReindexComments(final String resourceType, ResourceReference fromResource,
      ResourceReference toResource);

  /**
   * Updates the specified comment in the business layer. The comment to update is identified by its
   * unique identifier and the update information are carried by the given Comment instance.
   *
   * @param cmt the updated comment.
   */
  void updateComment(Comment cmt);

  /**
   * Updates and indexes the specified comment in the business layer. The comment to update in the
   * business layer is identified by its unique identifier and the update information are carried by
   * the given Comment instance.
   *
   * @param cmt the comment to update and to index.
   */
  void updateAndIndexComment(Comment cmt);

  /**
   * Gets the comment that identified by the specified identifier. If no such comment exists with
   * the specified identifier, then a CommentRuntimeException is thrown.
   *
   * @param commentId the identifier of the comment in the business layer.
   * @return the comment.
   */
  Comment getComment(CommentId commentId);

  /**
   * Gets all the comments on the resource identified by the resource type and the specified
   * reference. If no such publication exists with the specified resource reference, then a
   * CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented resource.
   * @param resourceRef a reference to the resource.
   * @return a list of the comments on the given resource. The list is empty if the resource
   * isn't commented.
   */
  List<Comment> getAllCommentsOnResource(final String resourceType, ResourceReference resourceRef);

  /**
   * Gets information about the commented resources among the specified ones. The resource
   * information are returned ordered down to the lesser commented resource.
   *
   * @param resourceType the type of the commented resource.
   * @param refs a collection of reference to the resources to get information and to
   * order by comments count.
   * @return an ordered list of information about the most commented resource. The list is sorted
   * by their comments count in a descendent order.
   */
  List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(final String resourceType,
      List<ResourceReference> refs);

  /**
   * Gets information about the most commented resources of the specified type. The resource
   * information are returned ordered down to the lesser commented resource.
   *
   * @param resourceType the type of the commented resource.
   * @return an ordered list of information about the most commented resources. The list is sorted
   * by their comments count in a descendent order.
   */
  List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(final String resourceType);

  /**
   * Gets information about all the commented resources in Silverpeas. The resource
   * information are returned ordered down to the lesser commented resource.
   *
   * @return an ordered list of information about the most commented resources. The list is sorted
   * by their comments count in a descendent order.
   */
  List<CommentedPublicationInfo> getAllMostCommentedPublicationsInfo();

  List<Comment> getLastComments(final String resourceType, int count);

  /**
   * Gets the count of comments on the resource identifier by the resource type and the specified
   * reference.
   *
   * @param resourceType the type of the commented resource.
   * @param resourceRef a reference on the resource.
   * @return the number of comments on the resource.
   */
  int getCommentsCountOnResource(final String resourceType, ResourceReference resourceRef);

  /**
   * Indexes all the comments on the resource identified by the resource type and the specified
   * reference. If no such resource exists with the specified reference, then a
   * CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented resource.
   * @param resourceRef a reference on the resource.
   */
  void indexAllCommentsOnPublication(final String resourceType, ResourceReference resourceRef);

  /**
   * Removes the indexes on all the comments of the resource identified by the resource type and
   * the specified reference. If no such resource exists with the specified reference, then a
   * CommentRuntimeException is thrown.
   *
   * @param resourceType the type of the commented publication.
   * @param resourceRef a reference on the resource.
   */
  void unindexAllCommentsOnPublication(final String resourceType, ResourceReference resourceRef);

  LocalizationBundle getComponentMessages(String language);

  /**
   * Get the list of SocialInformationComment added by the given user in a specified period.
   * @param resourceTypes the aimed resources types.
   * @param userId the author of comments.
   * @param period the period into which the comment has been created or modified.
   * @return List of {@link SocialInformation}
   */
  List<SocialInformationComment> getSocialInformationCommentsListByUserId(
      List<String> resourceTypes, String userId, Period period);

  /**
   * Gets the list of SocialInformationComment added by the specified contacts in the given period.
   *
   * @param resourceTypes the aimed resources types.
   * @param myContactsIds the aimed user identifiers of contacts.
   * @param instanceIds   the aimed identifiers of component instances.
   * @param period        the period into which the comment has been created or modified.
   * @return List of {@link SocialInformation}
   */
  List<SocialInformationComment> getSocialInformationCommentsListOfMyContacts(
      List<String> resourceTypes, List<String> myContactsIds, List<String> instanceIds,
      Period period);
}
