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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.util.attachment.control;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.sql.Connection;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;

public interface AttachmentBm {
  public AttachmentDetail createAttachment(AttachmentDetail attachDetail)
      throws AttachmentException;

  public void updateAttachment(AttachmentDetail attachDetail)
      throws AttachmentException;

  public Vector<AttachmentDetail> getAttachmentsByForeignKey(AttachmentPK foreignKey)
      throws AttachmentException;

  public AttachmentDetail getAttachmentByPrimaryKey(AttachmentPK primaryKey)
      throws AttachmentException;

  public Vector<AttachmentDetail> getAttachmentsByWorkerId(String workerId)
      throws AttachmentException;

  public AttachmentDetail findPrevious(AttachmentDetail ad)
      throws AttachmentException;

  public AttachmentDetail findNext(AttachmentDetail ad)
      throws AttachmentException;

  public Vector<AttachmentDetail> getAttachmentsByPKAndParam(AttachmentPK foreignKey,
      String nameAttribut, String valueAttribut) throws AttachmentException;

  public Vector<AttachmentDetail> getAttachmentsByPKAndContext(AttachmentPK foreignKey,
      String context, Connection con) throws AttachmentException;

  public void deleteAttachment(AttachmentPK primaryKey)
      throws AttachmentException;

  public void updateForeignKey(AttachmentPK pk, String foreignKey)
      throws AttachmentException;

  // pour la gestion des retards sur les reservations de fichiers
  public Collection<AttachmentDetail> getAllAttachmentByDate(Date date, boolean alert)
      throws AttachmentException;

  public Collection<AttachmentDetail> getAllAttachmentToLib(Date date) throws AttachmentException;

  public void notifyUser(NotificationMetaData notifMetaData, String senderId,
      String componentId) throws AttachmentException;

  public void updateXmlForm(AttachmentPK pk, String language, String xmlFormName)
      throws AttachmentException;

  public void sortAttachments(List<AttachmentPK> attachmentPKs) throws AttachmentException;
}