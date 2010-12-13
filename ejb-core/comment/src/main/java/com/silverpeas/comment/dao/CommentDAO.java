/*
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

package com.silverpeas.comment.dao;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.util.ForeignPK;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

/**
 * A Data Access Object that provides an access to Comment objects persisted within a data source.
 *
 * The way the data source is accessed and the nature of the data source are wrapped by the
 * implementation of this interface.
 * For each provided methods, if an error occurs a CommentRuntimeException is thrown.
 */
public interface CommentDAO {

  /**
   * Saves the specified comment into the underlying data source.
   * @param cmt the comment to save.
   * @return the primary key of the comment in the data source.
   */
  CommentPK saveComment(final Comment cmt);

  /**
   * Deletes all the comments on the publication identified by the specified foreign key.
   * @param pk the foreign key refering the publication in the data source
   */
  void removeAllCommentsByForeignPk(final ForeignPK pk);

  /**
   * Deletes the comment identified by the specified primary key
   * @param pk
   * @throws RemoteException
   */
  void removeComment(final CommentPK pk);

  /**
   * Gets all the comments of the publication identified by the specified foreign key.
   * @param pk the foreign key refering the publication in the data source.
   * @return a list with all of the publication comments. If the publication isn't commented, then an
   * empty list is returned.
   */
  List<Comment> getAllCommentsByForeignKey(final ForeignPK pk);

  /**
   * Gets the comment identified by the specified primary key.
   * If no comment exist with a such primary key, then a CommentRuntimeException is thrown.
   * @param pk the primary key of the comment to get.
   * @return the comment.
   */
  Comment getComment(final CommentPK pk);

  /**
   * Gets the number of comments on the publication identified by the specified foreign key.
   * @param pk the foreign key refering the publication.
   * @return the number of the publication comments.
   */
  int getCommentsCountByForeignKey(final ForeignPK pk);

  /**
   * Among all the publications identified by the specified foreign keys, gets the most commented ones.
   * @param pks a collection of foreign keys refering some publications.
   * @param commentsCount the number of comment above which a publication is considered as most commented.
   * @return a list of information about the most commented publication (publication primary key,
   * number of comments, and so on).
   */
  List<CommentInfo> getMostCommentedPublications(final Collection<ForeignPK> pks, int commentsCount);

  /**
   * Among all available commented publications, gets the most commented ones.
   * @return a list of information about the most commented publication (publication primary key,
   * number of comments, and so on).
   */
  List<CommentInfo> getAllMostCommentedPublications();

  /**
   * Moves all the comments from the publication identified by the specified foreign key to the
   * publication identified by the second specified foreign key.
   * @param fromPK the foreign key refering the source publication.
   * @param toPK the foreign key refering the destination publication.
   */
  void moveComments(final ForeignPK fromPK, final ForeignPK toPK);

  /**
   * Updates the comment in the data source identified by the specified one with the values carried
   * by the specified comment.
   * @param cmt the comment to update in the data source.
   */
  void updateComment(final Comment cmt);

}
