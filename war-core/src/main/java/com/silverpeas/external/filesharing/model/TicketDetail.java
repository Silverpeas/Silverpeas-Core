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
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class TicketDetail implements Serializable {

  private static final long serialVersionUID = -612174156104966079L;
  private int fileId;
  private String componentId;
  private boolean versioning;
  private UserDetail creator;
  private Date creationDate;
  private UserDetail modifier;
  private Date updateDate;
  private Date endDate;
  private int nbAccessMax;
  private int nbAccess;
  private String keyFile;
  private List<DownloadDetail> downloads = new ArrayList<DownloadDetail>();

  /**
   * Creates a new file-downloading ticket with the specified information about the file to
   * download. The ticket is not limited in time or in quantity. It is a continuous ticket.
   * @param fileId the identifier of the file to download.
   * @param componentId the identifier of the Silverpeas component instance to which the file
   * belongs.
   * @param versioning is the file versionned?
   * @param creator the ticket creator.
   * @param creationDate the date at which the ticket was created.
   * @return a TicketDetail instance.
   */
  public static TicketDetail continuousTicket(int fileId, String componentId, boolean versioning,
      UserDetail creator, Date creationDate) {
    return new TicketDetail(fileId, componentId, versioning, creator, creationDate, null,
        -1);
  }

  /**
   * Creates a new file-downloading ticket with the specified information about the file to
   * download. The ticket can be limited in time and in quantity. The first threshold reached
   * expires the ticket. If the end date is null then no expiration date is defined for the ticket.
   * If the maximum number of access is null or negative then no maximum download is defined for the
   * ticket.
   * @param fileId the identifier of the file to download.
   * @param componentId the identifier of the Silverpeas component instance to which the file
   * belongs.
   * @param versioning is the file versionned?
   * @param creator the ticket creator.
   * @param creationDate the date at which the ticket was created.
   * @param endDate the date up to which the ticket is valid.
   * @param nbAccessMax the maximum number of authorized download before the ticket expired.
   * @return a TicketDetail instance.
   */
  public static TicketDetail aTicket(int fileId, String componentId, boolean versioning,
      UserDetail creator, Date creationDate, Date endDate, int nbAccessMax) {
    return new TicketDetail(fileId, componentId, versioning, creator, creationDate, endDate,
        nbAccessMax);
  }

  protected TicketDetail() {
  }

  protected TicketDetail(int fileId, String componentId, boolean versioning, UserDetail creator,
      Date creationDate, Date endDate, int nbAccessMax) {
    this.fileId = fileId;
    this.componentId = componentId;
    this.versioning = versioning;
    this.creator = creator;
    this.creationDate = new Date(creationDate.getTime());
    if (endDate != null) {
      this.endDate = new Date(endDate.getTime());
    }
    this.nbAccessMax = nbAccessMax;
  }

  public int getFileId() {
    return fileId;
  }

  public String getComponentId() {
    return componentId;
  }

  public boolean isVersioned() {
    return versioning;
  }

  public void setVersioning(boolean versioning) {
    this.versioning = versioning;
  }

  public UserDetail getCreator() {
    return creator;
  }

  public void setCreator(final UserDetail creator) {
    this.creator = creator;
  }

  public Date getCreationDate() {
    return new Date(creationDate.getTime());
  }

  public void setLastModifier(UserDetail modifier) {
    this.modifier = modifier;
  }

  public UserDetail getLastModifier() {
    return this.modifier;
  }

  public Date getUpdateDate() {
    return new Date(updateDate.getTime());
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = new Date(updateDate.getTime());
  }

  public Date getEndDate() {
    Date date = null;
    if (endDate != null) {
      date = new Date(endDate.getTime());
    }
    return date;
  }

  public void setEndDate(Date endDate) {
    this.endDate = new Date(endDate.getTime());
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
    return Collections.unmodifiableCollection(downloads);
  }

  public void setDownloads(Collection<DownloadDetail> downloads) {
    this.downloads.clear();
    this.downloads.addAll(downloads);
  }

  public String getUrl(HttpServletRequest request) {
    return URLManager.getFullApplicationURL(request) + getRelativeUrl();
  }

  /**
   * Gets the URL of this ticket relative to the web context it belongs to.
   * @return the relative path of the URL of this ticket.
   */
  public String getRelativeUrl() {
    return "/Ticket?Key=" + getKeyFile();
  }

  public boolean isValid() {
    if (StringUtil.isDefined(getKeyFile())) {
      boolean isValid = true;
      if (getEndDate() != null) {
        isValid &= getEndDate().after(new Date());
      }
      if (getNbAccessMax() > 0) {
        isValid &= getNbAccess() < getNbAccessMax();
      }
      return isValid;
    }
    return false;
  }

  /**
   * Is this ticket was modified?
   * @return true if this ticket was modified, false otherwise.
   */
  public boolean isModified() {
    return this.updateDate != null && this.modifier != null;
  }

  public AttachmentDetail getAttachmentDetail() {
    return AttachmentController.searchAttachmentByPK(new AttachmentPK("" + getFileId()));
  }

  public Document getDocument() {
    if (isVersioned()) {
      try {
        return new VersioningUtil().getDocument(new DocumentPK(getFileId(), getComponentId()));
      } catch (RemoteException e) {
        SilverTrace.error("fileSharing", "TicketDetail.getDocument", "root.MSG_GEN_PARAM_VALUE", e);
      }
    }
    return null;
  }

  /**
   * Is this ticket a continuous one, that is with no limitation in time and in quantity.
   * @return true if this ticket is a continuous one, false otherwise.
   */
  public boolean isContinuous() {
    return nbAccessMax <= 0 && endDate == null;
  }

  /**
   * Sets this ticket a continuous one.
   */
  public void setContinuous() {
    this.endDate = null;
    this.nbAccessMax = -1;
  }
}
