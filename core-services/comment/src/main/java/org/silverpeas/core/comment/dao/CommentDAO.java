/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.comment.dao;

import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.WAPrimaryKey;

import java.util.List;

/**
 * A Data Access Object that provides an access to Comment objects persisted within a data source.
 * The way the data source is accessed and the nature of the data source are wrapped by the
 * implementation of this interface. For each provided methods, if an error occurs a
 * CommentRuntimeException is thrown.
 */
public interface CommentDAO {

  /**
   * Saves the specified comment into the underlying data source.
   * @param cmt the comment to save.
   * @return the primary key of the comment in the data source.
   */
  CommentPK saveComment(final Comment cmt);

  /**
   * Deletes all the comments on the publication identified by the resource type and the specified
   * foreign key.
   * @param resourceType type of the commented publication.
   * @param pk the foreign key refering the publication in the data source
   */
  void removeAllCommentsByForeignPk(final String resourceType, final ForeignPK pk);

  /**
   * Deletes the comment identified by the specified primary key
   * @param pk
   */
  void removeComment(final CommentPK pk);

  /**
   * Gets all the comments of the publication identified by the resource type and the specified
   * foreign key.
   * @param resourceType type of the commented publication.
   * @param pk the foreign key refering the publication in the data source.
   * @return a list with all of the publication comments. If the publication isn't commented, then
   * an empty list is returned.
   */
  List<Comment> getAllCommentsByForeignKey(final String resourceType, final ForeignPK pk);

  /**
   * Gets the comment identified by the specified primary key. If no comment exist with a such
   * primary key, then a CommentRuntimeException is thrown.
   * @param pk the primary key of the comment to get.
   * @return the comment.
   */
  Comment getComment(final CommentPK pk);

  /**
   * Gets the number of comments on the publication identified by the resource type and the
   * specified foreign key.
   * @param resourceType type of the commented publication.
   * @param pk the foreign key refering the publication.
   * @return the number of the publication comments.
   */
  int getCommentsCountByForeignKey(final String resourceType, final ForeignPK pk);

  /**
   * Among all the publications identified by the resource type and the specified primary keys, gets
   * the most commented ones.
   * @param resourceType type of the commented publication.
   * @param pks a list of primary keys refering some publications.
   * @return a list of information about the most commented publication (publication primary key,
   * number of comments, and so on).
   */
  List<CommentedPublicationInfo> getMostCommentedPublications(final String resourceType,
      final List<? extends WAPrimaryKey> pks);

  /**
   * Among all available commented publications of the specified type, gets the moste commented
   * ones.
   * @param resourceType the type of the publication.
   * @return a list of information about the most commented publication sorted in descendent order.
   */
  List<CommentedPublicationInfo> getMostCommentedPublications(final String resourceType);

  /**
   * Among all available commented publications, gets the most commented ones.
   * @return a list of information about the most commented publication (publication primary key,
   * number of comments, and so on).
   */
  List<CommentedPublicationInfo> getAllMostCommentedPublications();

  /**
   * Moves all the comments from the publication identified by the resource type and the specified
   * foreign key to the publication identified by the second specified foreign key.
   * @param resourceType type of source and destination publication.
   * @param fromPK the foreign key refering the source publication.
   * @param toPK the foreign key refering the destination publication.
   */
  void moveComments(final String resourceType, final ForeignPK fromPK, final ForeignPK toPK);

  /**
   * Moves all the comments from the publication identified by the resource type and the specified
   * foreign key to the publication identified by the second resource type and specified foreign
   * key.
   * @param fromResourceType source type the source publication.
   * @param fromPK the foreign key refering the source publication.
   * @param toResourceType type of the destination publication.
   * @param toPK the foreign key refering the destination publication.
   */
  void moveComments(final String fromResourceType, final ForeignPK fromPK,
      final String toResourceType, final ForeignPK toPK);

  /**
   * Updates the comment in the data source identified by the specified one with the values carried
   * by the specified comment.
   * @param cmt the comment to update in the data source.
   */
  void updateComment(final Comment cmt);

  /**
   * Gets the last comments posted to the publications in the specified component instance.
   * @param instanceId the unique identifier of the component instance.
   * @param count the maximum number of comments to fetch. Lesser or equal to 0 means no limit.
   * @return a list of the last comments.
   */
  List<Comment> getLastComments(String instanceId, int count);

  /**
   * Get the list of SocialInformationComment added by userId in a period
   * @param resourceTypes the aimed resources types.
   * @param userId the author of comments.
   * @param period the period into which the comment has been created or modified.
   * @return List of {@link SocialInformation}
   */
  List<SocialInformationComment> getSocialInformationCommentsListByUserId(
      List<String> resourceTypes, String userId, Period period);

  /**
   * Gets the list of SocialInformationComment added by myContactsIds in a period
   * @param resourceTypes the aimed resources types.
   * @param myContactsIds the aimed user identifiers of contacts.
   * @param instanceIds the aimed identifiers of component instances.
   * @param period the period into which the comment has been created or modified.
   * @return List of {@link SocialInformation}
   */
  List<SocialInformationComment> getSocialInformationCommentsListOfMyContacts(
      List<String> resourceTypes, List<String> myContactsIds, List<String> instanceIds,
      Period period);

}
