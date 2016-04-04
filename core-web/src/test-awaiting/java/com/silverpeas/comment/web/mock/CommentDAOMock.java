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
package com.silverpeas.comment.web.mock;

import java.util.Collections;
import com.silverpeas.comment.CommentRuntimeException;
import com.silverpeas.comment.dao.CommentDAO;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.model.CommentedPublicationInfo;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.WAPrimaryKey;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import org.silverpeas.core.date.period.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Named;
import static org.silverpeas.core.util.StringUtil.*;

/**
 * A mock on a Comment
 */
@Named("commentDAO")
public class CommentDAOMock implements CommentDAO {

  private Map<String, Comment> comments = Collections
      .synchronizedMap(new HashMap<String, Comment>());

  @Override
  public CommentPK saveComment(Comment cmt) {
    CommentPK pk = cmt.getCommentPK();
    pk.setId(UUID.randomUUID().toString());
    comments.put(cmt.getCommentPK().getId(), cmt);
    return cmt.getCommentPK();
  }

  @Override
  public void removeAllCommentsByForeignPk(String resourceType, ForeignPK pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeComment(CommentPK pk) {
    if (comments.remove(pk.getId()) == null) {
      throw new CommentRuntimeException(getClass().getSimpleName(),
          SilverpeasRuntimeException.ERROR,
          "No comment with id " + pk.getId());
    }
  }

  @Override
  public List<Comment> getAllCommentsByForeignKey(String resourceType, ForeignPK pk) {
    List<Comment> allComments = new ArrayList<Comment>();
    for (Comment comment : comments.values()) {
      if (comment.getForeignKey().getId().equals(pk.getId())) {
        allComments.add(comment);
      }
    }
    return allComments;
  }

  @Override
  public Comment getComment(CommentPK pk) {
    Comment comment = comments.get(pk.getId());
    if (comment == null) {
      throw new CommentRuntimeException(getClass().getSimpleName(),
          SilverpeasRuntimeException.ERROR,
          "No comment with id " + pk.getId());
    }
    return comment;
  }

  @Override
  public int getCommentsCountByForeignKey(String resourceType, ForeignPK pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublications(String resourceType,
      List<? extends WAPrimaryKey> pks) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<CommentedPublicationInfo> getAllMostCommentedPublications() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void moveComments(String resourceType, ForeignPK fromPK, ForeignPK toPK) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void moveComments(String fromResourceType, ForeignPK fromPK, String toResourceType,
      ForeignPK toPK) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateComment(Comment cmt) {
    if (isDefined(cmt.getCommentPK().getId()) && comments.containsKey(cmt.getCommentPK().getId())) {
      comments.put(cmt.getCommentPK().getId(), cmt);
    } else {
      throw new CommentRuntimeException(getClass().getSimpleName(),
          SilverpeasRuntimeException.ERROR,
          "No comment with id " + cmt.getCommentPK().getId());
    }
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublications(String resourceType) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Comment> getLastComments(String instanceId, int count) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<SocialInformationComment> getSocialInformationCommentsListByUserId(
      final List<String> resourceTypes, final String userId, final Period period) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<SocialInformationComment> getSocialInformationCommentsListOfMyContacts(
      final List<String> resourceTypes, final List<String> myContactsIds,
      final List<String> instanceIds, final Period period) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
