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

package com.silverpeas.comment.web.mock;

import com.silverpeas.comment.dao.CommentDAO;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.service.CommentService;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * A mock of the CommentService class.
 */
@Named("commentService")
public class CommentServiceMock extends CommentService {

  @Inject
  private CommentDAO commentDAO;

  private Map<String, UserDetail> users = new HashMap<String, UserDetail>();

  public void addUserForComments(final UserDetail user) {
    this.users.put(user.getId(), user);
  }

  @Override
  public void createComment(Comment cmt) {
    if (cmt.getOwnerDetail() != null) {
      addUserForComments(cmt.getOwnerDetail());
    }
    CommentPK pk = commentDAO.saveComment(cmt);
    cmt.setCommentPK(pk);
  }



  @Override
  protected OrganizationController getOrganizationController() {
    return new MyOrganizationController();
  }

  private class MyOrganizationController extends OrganizationController {
    private static final long serialVersionUID = 1L;

    @Override
    public UserDetail getUserDetail(String sUserId) {
      return users.get(sUserId);
    }


  }
}
