/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Created on 8 nov. 2004
 *
 */
package com.silverpeas.comment;

import java.sql.Connection;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * @author neysseri
 */
public class CommentInstanciator implements ComponentsInstanciatorIntf {

  public CommentInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("comment", "CommentInstanciator.create()",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("comment", "CommentInstanciator.delete()",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);

    try {
      CommentServiceFactory.getFactory().getCommentService()
          .deleteAllCommentsByComponentInstanceId(componentId);
    } catch (Exception e) {
      throw new InstanciationException("CommentInstanciator.delete()",
          InstanciationException.ERROR, "root.EX_RECORD_DELETION_FAILED", e);
    }
  }
}