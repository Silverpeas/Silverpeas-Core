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
package org.silverpeas.core.comment.service;

import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentPK;
import org.silverpeas.core.comment.model.CommentedPublicationInfo;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.WAPrimaryKey;

import java.util.List;

/**
 * User: ehugonnet Date: 21/03/11 Time: 09:54
 */
public interface CommentService {

  void createComment(Comment cmt);

  void createAndIndexComment(Comment cmt);

  void deleteComment(CommentPK pk);

  void deleteAllCommentsOnPublication(final String resourceType, WAPrimaryKey pk);

  void deleteComment(Comment comment);

  void moveComments(final String resourceType, WAPrimaryKey fromPK, WAPrimaryKey toPK);

  void moveAndReindexComments(final String resourceType, WAPrimaryKey fromPK, WAPrimaryKey toPK);

  void updateComment(Comment cmt);

  void updateAndIndexComment(Comment cmt);

  Comment getComment(CommentPK pk);

  List<Comment> getAllCommentsOnPublication(final String resourceType, WAPrimaryKey pk);

  List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(final String resourceType,
      List<? extends WAPrimaryKey> pks);

  List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(final String resourceType);

  List<CommentedPublicationInfo> getAllMostCommentedPublicationsInfo();

  List<Comment> getLastComments(final String resourceType, int count);

  int getCommentsCountOnPublication(final String resourceType, WAPrimaryKey pk);

  void indexAllCommentsOnPublication(final String resourceType, WAPrimaryKey pk);

  void unindexAllCommentsOnPublication(final String resourceType, WAPrimaryKey pk);

  LocalizationBundle getComponentMessages(String language);

  SettingBundle getComponentSettings();

  List<SocialInformationComment> getSocialInformationCommentsListByUserId(
      List<String> listResourceType, String userId, Period period);

  List<SocialInformationComment> getSocialInformationCommentsListOfMyContacts(
      List<String> listResourceType, List<String> myContactsIds, List<String> listInstanceId,
      Period period);
}
