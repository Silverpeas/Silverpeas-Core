/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.sharing.model;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.OrderBy;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.sharing.security.ShareableAccessControl;
import org.silverpeas.core.sharing.security.ShareableResource;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Date;

import static org.silverpeas.core.persistence.OrderBy.asc;
import static org.silverpeas.core.persistence.OrderBy.desc;

@Entity
@Table(name = "sb_filesharing_ticket")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "shared_object_type")
@AttributeOverride(name = "id",
    column = @Column(name = "keyfile", columnDefinition = "varchar(40)", length = 40))
@NamedQueries({@NamedQuery(name = "Ticket.findAllTicketForSharedObjectId",
    query = "SELECT t FROM Ticket t WHERE t.sharedObjectId = :sharedObjectId AND t" +
        ".sharedObjectType = :ticketType")})
public abstract class Ticket extends BasicJpaEntity<Ticket, UuidIdentifier>
    implements Serializable {

  private static final long serialVersionUID = -612174156104966079L;

  public static final String FILE_TYPE = "Attachment";
  public static final String VERSION_TYPE = "Versionned";
  public static final String NODE_TYPE = "Node";
  public static final String PUBLICATION_TYPE = "Publication";

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

  protected Ticket() {
  }

  protected Ticket(int sharedObjectId, String componentId, UserDetail creator, Date creationDate,
      Date endDate, int nbAccessMax) {
    this(sharedObjectId, componentId, creator.getId(), creationDate, endDate, nbAccessMax);
  }

  protected Ticket(int sharedObjectId, String componentId, String creatorId, Date creationDate,
      Date endDate, int nbAccessMax) {
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
    return getId();
  }

  public void setToken(String uuid) {
    setId(uuid);
  }

  public String getUrl(HttpServletRequest request) {
    return URLUtil.getFullApplicationURL(request) + getRelativeUrl();
  }

  /**
   * Gets the URL of this ticket relative to the web context it belongs to.
   * @return the relative path of the URL of this ticket.
   */
  private String getRelativeUrl() {
    return "/Ticket?Key=" + getToken();
  }

  public boolean isValid() {
    if (StringUtil.isDefined(getToken())) {
      boolean isValid = true;
      if (getEndDate() != null) {
        isValid = getEndDate().after(new Date());
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

  public String getSharedObjectType() {
    return sharedObjectType;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + (this.getId() != null ? this.getId().hashCode() : 0);
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
    return this.getId() == other.getId() ||
        (this.getId() != null && this.getId().equals(other.getId()));
  }

  @Override
  public String toString() {
    return "Ticket{" + "sharedObjectType=" + sharedObjectType + ", sharedObjectId=" +
        sharedObjectId + ", componentId=" + componentId + ", creatorId=" + creatorId +
        ", creationDate=" + creationDate + ", updaterId=" + updaterId + ", updateDate=" +
        updateDate + ", endDate=" + endDate + ", nbAccessMax=" + nbAccessMax + ", nbAccess=" +
        nbAccess + ", token=" + getId() + '}';
  }

  public void addDownload() {
    this.nbAccess = this.nbAccess + 1;
  }

  public abstract ShareableAccessControl getAccessControl();

  public abstract ShareableResource getResource();

  public enum QUERY_ORDER_BY {

    CREATION_DATE_ASC(asc("creationDate")), CREATION_DATE_DESC(desc("creationDate")),
    END_DATE_ASC(asc("enddate")), END_DATE_DESC(desc("enddate")),
    NB_ACCESS_DATE_ASC(asc("nbaccess")), NB_ACCESS_DATE_DESC(desc("nbaccess"));

    private final OrderBy orderBy;

    QUERY_ORDER_BY(OrderBy orderBy) {
      this.orderBy = orderBy;
    }

    public OrderBy getOrderBy() {
      return orderBy;
    }
  }
}
