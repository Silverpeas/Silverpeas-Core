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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import com.silverpeas.comment.dao.CommentDAO;
import com.silverpeas.comment.model.Comment;
import java.util.List;
import com.silverpeas.comment.model.CommentPK;
import com.stratelia.webactiv.beans.admin.DefaultOrganizationController;
import org.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.core.admin.service.OrganizationController;

import java.util.ArrayList;
import javax.inject.Named;
import static org.mockito.Mockito.*;

/**
 * This a wrapper of the comment service in order to mock some of the inner methods like, for
 * example, the access to the data source.
 */
@Named("commentServiceForTest")
public class MyDefaultCommentService extends DefaultCommentService {

  private CommentDAO mockedDAO = null;
  private OrganizationController mockedController = null;

  /**
   * Constructs a new comment service and mocks some of the underlying resource.
   */
  public MyDefaultCommentService() {
    super();
    Comment aComment1 = CommentBuilder.getBuilder().buildWith("Toto", "Vu à la télé");
    Comment aComment2 = CommentBuilder.getBuilder().buildWith("Titi", "Repasses demain");
    List<Comment> comments = new ArrayList<Comment>();
    comments.add(aComment1);
    comments.add(aComment2);
    mockedDAO = mock(CommentDAO.class);
    when(
        mockedDAO.getAllCommentsByForeignKey(any(String.class),
            any(ForeignPK.class))).thenReturn(comments);
    when(mockedDAO.getComment(any(CommentPK.class))).thenReturn(aComment1);
    when(mockedDAO.saveComment(any(Comment.class))).thenReturn(aComment1.getCommentPK());

    UserDetail userDetail = new UserDetail();
    userDetail.setFirstName("Toto");
    userDetail.setLastName("Chez-les-papoos");
    mockedController = mock(DefaultOrganizationController.class);
    when(mockedController.getUserDetail(anyString())).thenReturn(userDetail);
  }

  @Override
  protected CommentDAO getCommentDAO() {
    return mockedDAO;
  }

  @Override
  protected OrganizationController getOrganisationController() {
    return mockedController;
  }

}
