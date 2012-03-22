/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.sharing.model;

import com.silverpeas.sharing.security.ShareableAccessControl;
import com.silverpeas.sharing.security.ShareableResource;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.util.UuidPk;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "shared_object_type")
@Table(name = "sb_filesharing_ticket")
public abstract class Ticket implements Serializable {

  public static final String FILE_TYPE = "Attachment";
  public static final String VERSION_TYPE = "Versionned";
  public static final String NODE_TYPE = "Node";
  private static final long serialVersionUID = -612174156104966079L;
  @Column(name = "shared_object_type", nullable = false, insertable = false, updatable = false)
  protected String sharedObjectType;
  @Column(name = "shared_object")
  protected long sharedObjectId;
  @Column(name = "componentid")
  protected String componentId;
  @Column(name = "creatorid")
  protected String creatorId;
  @Column(name = "creationdate", nullable = false)
  protected Long creationDate;
  @Column(name = "updateid")
  protected String updaterId;
  @Column(name = "updatedate", nullable = true)
  protected Long updateDate = null;
  @Column(name = "enddate", nullable = true)
  protected Long endDate = null;
  @Column(name = "nbaccessmax")
  protected int nbAccessMax;
  @Column(name = "nbaccess")
  protected int nbAccess;
  @AttributeOverride(name = "uuid", column =
  @Column(name = "keyfile", columnDefinition = "varchar(255)", length = 255))
  @EmbeddedId
  protected UuidPk token;
  @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "ticket",
  cascade = CascadeType.REMOVE)
  protected List<DownloadDetail> downloads = new ArrayList<DownloadDetail>();

  protected Ticket() {
  }

  protected Ticket(int sharedObjectId, String componentId, UserDetail creator,
          Date creationDate, Date endDate, int nbAccessMax) {
    this(sharedObjectId, componentId, creator.getId(), creationDate, endDate, nbAccessMax);
  }

  protected Ticket(int sharedObjectId, String componentId, String creatorId,
          Date creationDate, Date endDate, int nbAccessMax) {
    this.token = new UuidPk();
    this.sharedObjectId = sharedObjectId;
    this.componentId = componentId;
    this.creatorId = creatorId;
    this.creationDate = creationDate.getTime();
    if (endDate != null) {
      this.endDate = endDate.getTime();
    }
    this.nbAccessMax = nbAccessMax;
  }

  public long getSharedObjectId() {
    return sharedObjectId;
  }

  public String getComponentId() {
    return componentId;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreator(final UserDetail creator) {
    this.creatorId = creator.getId();
  }

  public Date getCreationDate() {
    return new Date(creationDate);
  }

  public void setLastModifier(UserDetail modifier) {
    this.updaterId = modifier.getId();
  }

  public String getLastModifier() {
    return this.updaterId;
  }

  public Date getUpdateDate() {
    if (updateDate != null) {
      return new Date(updateDate);
    }
    return null;
  }

  public void setUpdateDate(Date updateDate) {
    if (updateDate != null) {
      this.updateDate = updateDate.getTime();
    } else {
      this.updateDate = null;
    }
  }

  public Date getEndDate() {
    Date date = null;
    if (endDate != null) {
      date = new Date(endDate);
    }
    return date;
  }

  public void setEndDate(Date endDate) {
    if (endDate != null) {
      this.endDate = endDate.getTime();
    } else {
      this.endDate = null;
    }
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

  public String getToken() {
    return token.getUuid();
  }

  public void setToken(String uuid) {
    this.token = new UuidPk(uuid);
  }

  public List<DownloadDetail> getDownloads() {
    return Collections.unmodifiableList(downloads);
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
   *
   * @return the relative path of the URL of this ticket.
   */
  private String getRelativeUrl() {
    return "/Ticket?Key=" + getToken();
  }

  public boolean isValid() {
    if (StringUtil.isDefined(getToken())) {
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
   *
   * @return true if this ticket was modified, false otherwise.
   */
  public boolean isModified() {
    return this.updateDate != null && StringUtil.isDefined(updaterId);
  }

  /**
   * Is this ticket a continuous one, that is with no limitation in time and in quantity.
   *
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

  public String getSharedObjectType() {
    return sharedObjectType;
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
    final Ticket other = (Ticket) obj;
    if (this.token != other.token && (this.token == null || !this.token.equals(other.token))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Ticket{" + "sharedObjectType=" + sharedObjectType + ", sharedObjectId=" + sharedObjectId + ", componentId=" + componentId + ", creatorId=" + creatorId + ", creationDate=" + creationDate + ", updaterId=" + updaterId + ", updateDate=" + updateDate + ", endDate=" + endDate + ", nbAccessMax=" + nbAccessMax + ", nbAccess=" + nbAccess + ", token=" + token + ", downloads=" + downloads + '}';
  }

  public abstract ShareableAccessControl getAccessControl();

  public abstract ShareableResource getResource();
}
