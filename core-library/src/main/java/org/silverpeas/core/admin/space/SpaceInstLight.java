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
package org.silverpeas.core.admin.space;

import org.silverpeas.core.BasicIdentifier;
import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.persistence.SpaceRow;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.security.authorization.SpaceAccessControl;
import org.silverpeas.core.util.ResourceLocator;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author neysseri
 */
public class SpaceInstLight extends AbstractI18NBean<SpaceI18N>
    implements Serializable, LocalizedResource, Identifiable, Securable, Comparable<SpaceInstLight> {

  private static final long serialVersionUID = 8772050454345960478L;
  private String id = null;
  private int orderNum = 0;
  private int level = -1;
  private String fatherId = null;
  private Date createDate = null;
  private Date updateDate = null;
  private Date removeDate = null;
  private String status = null;
  private int createdBy = -1;
  private int updatedBy = -1;
  private int removedBy = -1;
  private String creatorName = null;
  private String updaterName = null;
  private String removerName = null;
  private String look = null;
  private List<SpaceInstLight> path = null;
  private boolean displaySpaceFirst = true;
  private boolean isPersonalSpace = false;
  private boolean inheritanceBlocked = false;

  @Override
  protected Class<SpaceI18N> getTranslationType() {
    return SpaceI18N.class;
  }

  public SpaceInstLight() {
    id = "";
    fatherId = "";
    orderNum = 0;
    level = -1;
    displaySpaceFirst = true;
    isPersonalSpace = false;
    inheritanceBlocked = false;
  }

  public SpaceInstLight(SpaceRow spaceRow) {
    if (spaceRow != null) {
      setLocalId(spaceRow.id);
      setName(spaceRow.name);
      setFatherId(spaceRow.domainFatherId);
      setDescription(spaceRow.description);
      orderNum = spaceRow.orderNum;
      if (spaceRow.createTime != null) {
        createDate = new Date(Long.parseLong(spaceRow.createTime));
      }
      if (spaceRow.updateTime != null) {
        updateDate = new Date(Long.parseLong(spaceRow.updateTime));
      }
      if (spaceRow.removeTime != null) {
        removeDate = new Date(Long.parseLong(spaceRow.removeTime));
      }
      status = spaceRow.status;

      createdBy = spaceRow.createdBy;
      updatedBy = spaceRow.updatedBy;
      removedBy = spaceRow.removedBy;

      look = spaceRow.look;
      displaySpaceFirst = (spaceRow.displaySpaceFirst == 1);
      isPersonalSpace = spaceRow.isPersonalSpace == 1;
      inheritanceBlocked = spaceRow.inheritanceBlocked == 1;
    }
  }

  public SpaceInstLight(SpaceInst spaceInst) {
    if (spaceInst != null) {
      setLocalId(spaceInst.getLocalId());
      setName(spaceInst.getName());
      setFatherId(spaceInst.getDomainFatherId());
      setDescription(spaceInst.getDescription());
      orderNum = spaceInst.getOrderNum();
      setLevel(spaceInst.getLevel());
      createDate = spaceInst.getCreationDate();
      updateDate = spaceInst.getLastUpdateDate();
      removeDate = spaceInst.getRemovalDate();
      status = spaceInst.getStatus();
      look = spaceInst.getLook();

      setTranslations(spaceInst.getTranslations());
      displaySpaceFirst = spaceInst.isDisplaySpaceFirst();
      isPersonalSpace = spaceInst.isPersonalSpace();
      inheritanceBlocked = spaceInst.isInheritanceBlocked();
    }
  }

  public String getId() {
    return SpaceInst.SPACE_KEY_PREFIX + getLocalId();
  }

  @Override
  @SuppressWarnings("unchecked")
  public BasicIdentifier getIdentifier() {
    return new BasicIdentifier(getLocalId(), getId());
  }

  public String getFatherId() {
    return fatherId;
  }

  public int getLevel() {
    return level;
  }

  @Override
  public String getName(String language) {
    if (isPersonalSpace) {
      return ResourceLocator.getGeneralLocalizationBundle(language)
          .getString("GML.personalSpace");
    }

    return super.getName(language);
  }

  public void setFatherId(int fatherId) {
    this.fatherId = Integer.toString(fatherId);
  }

  public void setFatherId(String fatherId) {
    this.fatherId = fatherId;
  }

  public void setLocalId(int id) {
    this.id = Integer.toString(id);
  }

  public int getLocalId() {
    return Integer.parseInt(id);
  }

  public void setLevel(int i) {
    level = i;
  }

  public boolean isRoot() {
    return isRoot(getFatherId());
  }

  public static boolean isRoot(String spaceId) {
    return "0".equals(spaceId);
  }

  public int getOrderNum() {
    return orderNum;
  }

  public void setOrderNum(int orderNum) {
    this.orderNum = orderNum;
  }

  public Date getCreationDate() {
    return createDate;
  }

  public Date getRemovalDate() {
    return removeDate;
  }

  public String getStatus() {
    return status;
  }

  public Date getLastUpdateDate() {
    return updateDate;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  public String getRemoverName() {
    return removerName;
  }

  public void setRemoverName(String removerName) {
    this.removerName = removerName;
  }

  public String getUpdaterName() {
    return updaterName;
  }

  public void setUpdaterName(String updaterName) {
    this.updaterName = updaterName;
  }

  public int getCreatedBy() {
    return createdBy;
  }

  public int getRemovedBy() {
    return removedBy;
  }

  public int getUpdatedBy() {
    return updatedBy;
  }

  @Override
  public User getCreator() {
    return User.getById(String.valueOf((getCreatedBy())));
  }

  @Override
  public User getLastUpdater() {
    if (updatedBy < 0) {
      return getCreator();
    }
    return User.getById(String.valueOf((getUpdatedBy())));
  }

  public int compareTo(SpaceInstLight o) {
    return getOrderNum() - o.getOrderNum();
  }

  public String getPath(String separator) {
    StringBuilder sPath = new StringBuilder();
    if (path != null) {
      SpaceInstLight space;
      for (int i = 0; i < path.size(); i++) {
        if (i > 0) {
          sPath.append(separator);
        }
        space = path.get(i);
        sPath.append(space.getName());
      }
    }
    return sPath.toString();
  }

  public void setPath(List<SpaceInstLight> path) {
    this.path = path;
  }

  public String getLook() {
    return look;
  }

  public void setLook(String look) {
    this.look = look;
  }

  public boolean isDisplaySpaceFirst() {
    return displaySpaceFirst;
  }

  public void setDisplaySpaceFirst(boolean displaySpaceFirst) {
    this.displaySpaceFirst = displaySpaceFirst;
  }

  public boolean isPersonalSpace() {
    return isPersonalSpace;
  }

  public void setPersonalSpace(boolean isPersonalSpace) {
    this.isPersonalSpace = isPersonalSpace;
  }

  public boolean isInheritanceBlocked() {
    return inheritanceBlocked;
  }

  public void setInheritanceBlocked(boolean isInheritanceBlocked) {
    this.inheritanceBlocked = isInheritanceBlocked;
  }

  public boolean isRemoved() {
    return SpaceInst.STATUS_REMOVED.equals(getStatus());
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return SpaceAccessControl.get().isUserAuthorized(user.getId(), getId());
  }

  /**
   * Is the user can modify this collaboration space?
   * @param user a user in Silverpeas.
   * @return true if the user can both access this collaboration space and has management privilege
   * on this space (by being either an administrator or a space manager)
   */
  @Override
  public boolean canBeModifiedBy(final User user) {
    return SpaceAccessControl.get().isUserAuthorized(user.getId(), getId())
        && (user.isAccessAdmin() || user.isAccessSpaceManager());
  }

  @Override
  public boolean canBeFiledInBy(final User user) {
    return canBeModifiedBy(user);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SpaceInstLight other = (SpaceInstLight) obj;
    return Objects.equals(getId(), other.getId());
  }
}
