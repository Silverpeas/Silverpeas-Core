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
package org.silverpeas.core.webapi.sharing;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

import org.silverpeas.core.sharing.model.DownloadDetail;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.model.TicketFactory;
import org.silverpeas.core.webapi.base.WebEntity;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.DateUtil;

public class TicketEntity implements WebEntity {

  private static final long serialVersionUID = -3181971971218136594L;

  @XmlElement(defaultValue = "")
  private URI uri;

  /**
   * Main sharing ticket attributes
   */
  @XmlElement
  @NotNull
  protected String componentId;
  @XmlElement
  @NotNull
  protected String sharedObjectType;
  @XmlElement
  @NotNull
  protected long sharedObjectId;
  @XmlElement
  protected String creatorId;
  @XmlElement
  protected Long creationDate;
  @XmlElement
  protected String updaterId;
  @XmlElement
  protected Long updateDate;

  @XmlElement
  protected String validity;
  @XmlElement
  protected Long endDate;
  @XmlElement
  protected String endDateStr = null;
  @XmlElement
  protected String endDateFormat = null;
  @XmlElement
  @NotNull
  protected int nbAccessMax;
  @XmlElement
  protected int nbAccess;

  @XmlElement(defaultValue = "")
  protected String token;
  @XmlElement
  protected List<DownloadDetail> downloads = new ArrayList<>();
  @XmlElement(defaultValue = "")
  protected String url;

  /**
   * Notification parameter
   */
  @XmlElement(defaultValue="")
  protected String users;
  @XmlElement(defaultValue="")
  protected String externalEmails;
  @XmlElement(defaultValue="")
  protected String additionalMessage;


  public static TicketEntity fromTicket(final Ticket ticket, URI uri) {
    return new TicketEntity(ticket, uri);
  }

  /**
   * Default constructor
   */
  protected TicketEntity() {
  }

  public TicketEntity(Ticket ticket, URI uri) {
    this.uri = uri;
    this.componentId = ticket.getComponentId();
    this.sharedObjectId = ticket.getSharedObjectId();
    this.sharedObjectType = ticket.getSharedObjectType();
    this.token = ticket.getToken();
    this.creationDate = ticket.getCreationDate().getTime();
    this.creatorId = ticket.getCreatorId();
    if (ticket.getEndDate() != null) {
      this.endDate = ticket.getEndDate().getTime();
    }
    this.nbAccess = ticket.getNbAccess();
    this.nbAccessMax = ticket.getNbAccessMax();
    this.downloads = ticket.getDownloads();
    if (ticket.getUpdateDate() != null) {
      this.updateDate = ticket.getUpdateDate().getTime();
    }
    this.updaterId = ticket.getLastModifier();
  }

  public Ticket toTicket(UserDetail user) throws ParseException {
    Ticket ticket;
    Long sharedObjectId = this.sharedObjectId;
    if ("1".equals(this.validity)) {
      Date endDate =
          DateUtil.getEndOfDay(DateUtil.stringToDate(this.endDateStr, this.endDateFormat));
      int maxAccessNb = this.nbAccessMax;

      ticket =
          TicketFactory.aTicket(sharedObjectId.intValue(), this.componentId, user.getId(),
              new Date(), endDate, maxAccessNb, this.sharedObjectType);

    } else {
      ticket =
          TicketFactory.continuousTicket(sharedObjectId.intValue(), componentId, user.getId(),
              new Date(), this.sharedObjectType);

    }
    return ticket;
  }

  /**
   * @return the componentId
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * @param componentId the componentId to set
   */
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  /**
   * @return the sharedObjectType
   */
  public String getSharedObjectType() {
    return sharedObjectType;
  }

  /**
   * @param sharedObjectType the sharedObjectType to set
   */
  public void setSharedObjectType(String sharedObjectType) {
    this.sharedObjectType = sharedObjectType;
  }

  /**
   * @return the sharedObjectId
   */
  public long getSharedObjectId() {
    return sharedObjectId;
  }

  /**
   * @param sharedObjectId the sharedObjectId to set
   */
  public void setSharedObjectId(long sharedObjectId) {
    this.sharedObjectId = sharedObjectId;
  }

  /**
   * @return the creatorId
   */
  public String getCreatorId() {
    return creatorId;
  }

  /**
   * @param creatorId the creatorId to set
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * @return the creationDate
   */
  public Long getCreationDate() {
    return creationDate;
  }

  /**
   * @param creationDate the creationDate to set
   */
  public void setCreationDate(Long creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * @return the updaterId
   */
  public String getUpdaterId() {
    return updaterId;
  }

  /**
   * @param updaterId the updaterId to set
   */
  public void setUpdaterId(String updaterId) {
    this.updaterId = updaterId;
  }

  /**
   * @return the updateDate
   */
  public Long getUpdateDate() {
    return updateDate;
  }

  /**
   * @param updateDate the updateDate to set
   */
  public void setUpdateDate(Long updateDate) {
    this.updateDate = updateDate;
  }

  /**
   * @return the validity
   */
  public String getValidity() {
    return validity;
  }

  /**
   * @param validity the validity to set
   */
  public void setValidity(String validity) {
    this.validity = validity;
  }

  /**
   * @return the endDate
   */
  public Long getEndDate() {
    return endDate;
  }

  /**
   * @param endDate the endDate to set
   */
  public void setEndDate(Long endDate) {
    this.endDate = endDate;
  }

  /**
   * @return the endDateStr
   */
  public String getEndDateStr() {
    return endDateStr;
  }

  /**
   * @param endDateStr the endDateStr to set
   */
  public void setEndDateStr(String endDateStr) {
    this.endDateStr = endDateStr;
  }

  /**
   * @return the endDateFormat
   */
  public String getEndDateFormat() {
    return endDateFormat;
  }

  /**
   * @param endDateFormat the endDateFormat to set
   */
  public void setEndDateFormat(String endDateFormat) {
    this.endDateFormat = endDateFormat;
  }

  /**
   * @return the nbAccessMax
   */
  public int getNbAccessMax() {
    return nbAccessMax;
  }

  /**
   * @param nbAccessMax the nbAccessMax to set
   */
  public void setNbAccessMax(int nbAccessMax) {
    this.nbAccessMax = nbAccessMax;
  }

  /**
   * @return the nbAccess
   */
  public int getNbAccess() {
    return nbAccess;
  }

  /**
   * @param nbAccess the nbAccess to set
   */
  public void setNbAccess(int nbAccess) {
    this.nbAccess = nbAccess;
  }

  /**
   * @return the token
   */
  public String getToken() {
    return token;
  }

  /**
   * @param token the token to set
   */
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * @return the users
   */
  public String getUsers() {
    return users;
  }

  /**
   * @param users the users to set
   */
  public void setUsers(String users) {
    this.users = users;
  }

  /**
   * @return the externalEmails
   */
  public String getExternalEmails() {
    return externalEmails;
  }

  /**
   * @param externalEmails the externalEmails to set
   */
  public void setExternalEmails(String externalEmails) {
    this.externalEmails = externalEmails;
  }

  /**
   * @return the additionalMessage
   */
  public String getAdditionalMessage() {
    return additionalMessage;
  }

  /**
   * @param additionalMessage the additionalMessage to set
   */
  public void setAdditionalMessage(String additionalMessage) {
    this.additionalMessage = additionalMessage;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @param downloads the downloads to set
   */
  public void setDownloads(List<DownloadDetail> downloads) {
    this.downloads = downloads;
  }

  public List<DownloadDetail> getDownloads() {
    return Collections.unmodifiableList(downloads);
  }

  public boolean isValid() {
    if (StringUtil.isDefined(getToken())) {
      boolean isValid = true;
      if (getEndDate() != null) {
        isValid = (getEndDate().compareTo(new Date().getTime()) < 0);
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
    return this.updateDate != null && StringUtil.isDefined(updaterId);
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
    this.nbAccessMax = 0;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + (this.token != null ? this.token.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TicketEntity other = (TicketEntity) obj;
    if (this.token != other.token && (this.token == null || !this.token.equals(other.token))) {
      return false;
    }
    return true;
  }

  @Override
  public URI getURI() {
    return uri;
  }

}
