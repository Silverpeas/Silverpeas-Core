/**
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

package com.stratelia.silverpeas.comment.ejb;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.comment.model.*;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Vector;
import com.stratelia.webactiv.util.WAPrimaryKey;

public interface CommentBm extends javax.ejb.EJBObject {

  public CommentPK createComment(Comment cmt) throws RemoteException;

  public void deleteComment(CommentPK pk) throws RemoteException;

  public void updateComment(Comment cmt) throws RemoteException;

  public Comment getComment(CommentPK pk) throws RemoteException;

  public int getCommentsCount(WAPrimaryKey foreign_pk) throws RemoteException;

  public Vector<Comment> getAllComments(WAPrimaryKey foreign_pk) throws RemoteException;

  public Vector<Comment> getAllCommentsWithUserName(WAPrimaryKey foreign_pk)
      throws RemoteException;

  public Collection<CommentInfo> getMostCommentedAllPublications() throws RemoteException;

  public Collection<CommentInfo> getMostCommented(Collection<CommentPK> pks, int notationsCount)
      throws RemoteException;

  public void deleteAllComments(ForeignPK foreign_pk) throws RemoteException;

  public void moveComments(ForeignPK fromPK, ForeignPK toPK)
      throws RemoteException;

}