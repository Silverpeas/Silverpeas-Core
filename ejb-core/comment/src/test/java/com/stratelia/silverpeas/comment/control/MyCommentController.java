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

package com.stratelia.silverpeas.comment.control;

import com.stratelia.silverpeas.comment.model.Comment;
import java.util.List;
import com.stratelia.silverpeas.comment.ejb.CommentBm;
import com.stratelia.silverpeas.comment.model.CommentPK;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.WAPrimaryKey;
import java.rmi.RemoteException;
import java.util.ArrayList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This a wrapper of the comment controller in order to mock some of the inner methods like, for
 * example, the access to EJBs.
 */
public class MyCommentController extends CommentController {

  private CommentBm mockedBm = null;
  private OrganizationController mockedController = null;

  /**
   * Constructs a new comment controller.
   */
  public MyCommentController() {
    super();
    try {
      Comment aComment = CommentBuilder.getBuilder().buildWith("Toto", "Vu à la télé");
      List<Comment> comments = new ArrayList<Comment>();
      comments.add(aComment);
      comments.add(CommentBuilder.getBuilder().buildWith("Titi", "Repasses demain"));
      mockedBm = mock(CommentBm.class);
      when(mockedBm.getAllComments(any(WAPrimaryKey.class))).thenReturn(comments);
      when(mockedBm.getComment(any(CommentPK.class))).thenReturn(aComment);

      UserDetail userDetail = new UserDetail();
      userDetail.setFirstName("Toto");
      userDetail.setLastName("Chez-les-papoos");
      mockedController = mock(OrganizationController.class);
      when(mockedController.getUserDetail(anyString())).thenReturn(userDetail);
    } catch (RemoteException ex) {
      fail(ex.getMessage());
    }
  }

  @Override
  protected CommentBm getCommentBm() {
    return mockedBm;
  }

  @Override
  protected OrganizationController getOrganizationController() {
    return mockedController;
  }

}
