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

package com.silverpeas.comment.service;

import com.silverpeas.SilverpeasComponentService;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.model.CommentedPublicationInfo;
import com.stratelia.webactiv.util.WAPrimaryKey;

import java.util.List;

/**
 * User: ehugonnet Date: 21/03/11 Time: 09:54
 */
public interface CommentService extends SilverpeasComponentService {
  void createComment(Comment cmt);

  void createAndIndexComment(Comment cmt);

  void deleteComment(CommentPK pk);

  void deleteAllCommentsOnPublication(WAPrimaryKey pk);

  void deleteComment(Comment comment);

  void moveComments(WAPrimaryKey fromPK, WAPrimaryKey toPK);

  void moveAndReindexComments(WAPrimaryKey fromPK, WAPrimaryKey toPK);

  void updateComment(Comment cmt);

  void updateAndIndexComment(Comment cmt);

  Comment getComment(CommentPK pk);

  List<Comment> getAllCommentsOnPublication(WAPrimaryKey pk);

  List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(
      List<WAPrimaryKey> pks);

  List<CommentedPublicationInfo> getAllMostCommentedPublicationsInfo();

  int getCommentsCountOnPublication(WAPrimaryKey pk);

  void indexAllCommentsOnPublication(WAPrimaryKey pk);

  void unindexAllCommentsOnPublication(WAPrimaryKey pk);
}
