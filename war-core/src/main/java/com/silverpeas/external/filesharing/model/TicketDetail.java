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
package com.silverpeas.external.filesharing.model;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import javax.servlet.http.HttpServletRequest;

public class TicketDetail implements Serializable {
  private static final long serialVersionUID = -612174156104966079L;

  private int fileId;
  private String componentId;
  private boolean versioning;
  private String creatorId;
  private String creatorName;
  private Date creationDate;
  private String updateId;
  private String updateName;
  private Date updateDate;
  private Date endDate;
  private int nbAccessMax;
  private int nbAccess;
  private String keyFile;
  private Collection<DownloadDetail> downloads;

  /**
   * Creates a new download ticket with the specified information about the file to download.
   * The ticket is not limited in time or in quantity. It is a continuous ticket.
   * @param fileId the identifier of the file to download.
   * @param componentId the identifier of the Silverpeas component instance to which the file belongs.
   * @param versioning is the file versionned?
   * @param creatorId the identifier of the ticket creator.
   * @param creationDate the date at which the ticket was created.
   * @return a TicketDetail instance.
   */
  public static TicketDetail continuousTicket(int fileId, String componentId, boolean versioning,
      String creatorId, Date creationDate) {
    return new TicketDetail(fileId, componentId, versioning, creatorId, creationDate, null,
        -1);
  }

  /**
   * Creates a new download ticket with the specified information about the file to download.
   * The ticket can be limited in time and in quantity. The first threshold reached expires the ticket.
   * If the end date is null then no expiration date is defined for the ticket. If the maximum number
   * of access is null or negative then no maximum download is defined for the ticket.
   * @param fileId the identifier of the file to download.
   * @param componentId the identifier of the Silverpeas component instance to which the file belongs.
   * @param versioning is the file versionned?
   * @param creatorId the identifier of the ticket creator.
   * @param creationDate the date at which the ticket was created.
   * @param endDate the date up to which the ticket is valid.
   * @param nbAccessMax the maximum number of authorized download before the ticket expired.
   * @return a TicketDetail instance.
   */
  public static TicketDetail aTicket(int fileId, String componentId, boolean versioning, String creatorId,
      Date creationDate, Date endDate, int nbAccessMax) {
    return new TicketDetail(fileId, componentId, versioning, creatorId, creationDate, endDate,
        nbAccessMax);
  }

  protected TicketDetail() {
  }

  protected TicketDetail(int fileId, String componentId, boolean versioning, String creatorId,
      Date creationDate, Date endDate, int nbAccessMax) {
    this.fileId = fileId;
    this.componentId = componentId;
    this.versioning = versioning;
    this.creatorId = creatorId;
    this.creationDate = new Date(creationDate.getTime());
    this.endDate = new Date(endDate.getTime());
    this.nbAccessMax = nbAccessMax;
  }

  public int getFileId() {
    return fileId;
  }

  public void setFileId(int fileId) {
    this.fileId = fileId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public boolean isVersioning() {
    return versioning;
  }

  public void setVersioning(boolean versioning) {
    this.versioning = versioning;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getUpdateId() {
    return updateId;
  }

  public void setUpdateId(String updateId) {
    this.updateId = updateId;
  }

  public String getUpdateName() {
    return updateName;
  }

  public void setUpdateName(String updateName) {
    this.updateName = updateName;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public int getNbAccessMax() {
    return nbAccessMax;
  }

  public void setNbAccessMax(int nbAccessMax) {
    this.nbAccessMax = nbAccessMax;
  }

  public int getNbAccess() {
    return nbAccess;
  }

  public void setNbAccess(int nbAccess) {
    this.nbAccess = nbAccess;
  }

  public String getKeyFile() {
    return keyFile;
  }

  public void setKeyFile(String keyFile) {
    this.keyFile = keyFile;
  }

  public Collection<DownloadDetail> getDownloads() {
    return downloads;
  }

  public void setDownloads(Collection<DownloadDetail> downloads) {
    this.downloads = downloads;
  }

  public String getUrl(HttpServletRequest request) {
    return URLManager.getFullApplicationURL(request) + "/Ticket?Key=" + getKeyFile();
  }

  public boolean isValid() {
    if (StringUtil.isDefined(getKeyFile())) {
      return (getEndDate().after(new Date()) && getNbAccess() < getNbAccessMax());
    }
    return false;
  }

  public AttachmentDetail getAttachmentDetail() {
    return AttachmentController.searchAttachmentByPK(new AttachmentPK("" + getFileId()));
  }

  public Document getDocument() {
    try {
      return new VersioningUtil().getDocument(new DocumentPK(getFileId(), getComponentId()));
    } catch (RemoteException e) {
      SilverTrace.error("fileSharing", "TicketDetail.getDocument", "root.MSG_GEN_PARAM_VALUE", e);
    }
    return null;
  }
}
