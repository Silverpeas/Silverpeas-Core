/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.persistence.ComponentInstanceRow;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import javax.persistence.Transient;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.Manager;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * The class ComponentInstLight is the representation in memory of a component instance
 */
public class ComponentInstLight extends AbstractI18NBean<ComponentI18N>
    implements SilverpeasSharedComponentInstance {

  private static final long serialVersionUID = 4859368422448142768L;

  /* Unique identifier of the instance */
  private String id;

  /* Unique identifier of the father of the space */
  private String domainFatherId;

  /* instance Type */
  private String name;
  private Date createDate = null;
  private Date updateDate = null;
  private Date removeDate = null;
  private String status = null;
  private int createdBy = -1;
  private int updatedBy = -1;
  private int removedBy = -1;
  private int orderNum = -1;
  private String creatorName = null;
  private String updaterName = null;
  private String removerName = null;
  private List<SpaceInstLight> path = null;
  private boolean isInheritanceBlocked = false;
  private boolean hidden = false;
  private boolean publicApp = false;

  /** Used only in the aim to improve performances */
  @Transient
  private ComponentInst cachedComponentInst;

  /**
   * Constructor
   */
  public ComponentInstLight() {
    id = "";
    domainFatherId = "";
    name = "";
  }

  /**
   * Constructor
   */
  public ComponentInstLight(ComponentInstanceRow compo) {
    id = Integer.toString(compo.id);
    domainFatherId = Integer.toString(compo.spaceId);
    setLabel(compo.name);
    setDescription(compo.description);
    name = compo.componentName;

    if (compo.createTime != null) {
      createDate = new Date(Long.parseLong(compo.createTime));
    }
    if (compo.updateTime != null) {
      updateDate = new Date(Long.parseLong(compo.updateTime));
    }
    if (compo.removeTime != null) {
      removeDate = new Date(Long.parseLong(compo.removeTime));
    }
    status = compo.status;

    createdBy = compo.createdBy;
    updatedBy = compo.updatedBy;
    removedBy = compo.removedBy;

    orderNum = compo.orderNum;

    isInheritanceBlocked = compo.inheritanceBlocked == 1;
    hidden = compo.hidden == 1;
    publicApp = compo.publicAccess == 1;
  }

  @Override
  public String getId() {
    return name + id;
  }

  @Override
  public String getSpaceId() {
    return domainFatherId;
  }

  public int getLocalId() {
    return Integer.parseInt(id);
  }

  public void setLocalId(int id) {
    this.id = String.valueOf(id);
  }

  /**
   * Set the domain father id
   */
  public void setDomainFatherId(String sDomainFatherId) {
    this.domainFatherId = sDomainFatherId;
  }

  /**
   * Get the domain father id
   * @return the component instance father id. If the component instance has no father,
   * returns an empty string.
   */
  public String getDomainFatherId() {
    return domainFatherId;
  }

  /**
   * Has this component instance a domain father? In the case of a component instance, a domain
   * father is a space. For example, a component instance has no domain father when it belongs to
   * the user's personal space.
   * @return true if this component has a domain father, false otherwise.
   */
  public boolean hasDomainFather() {
    return StringUtil.isDefined(domainFatherId);
  }

  /**
   * Get the component name
   * This method is a hack (technical debt)
   * @return the component name
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Get the component type
   * @return the component type
   */
  @Override
  public String getLabel() {
    return super.getName();
  }

  public void setLabel(String label) {
    super.setName(label);
  }

  public Date getCreateDate() {
    return createDate;
  }

  public Date getRemoveDate() {
    return removeDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Date getUpdateDate() {
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

  public String getPath(String separator) {
    StringBuilder sPath = new StringBuilder();
    if (path != null) {
      SpaceInstLight space = null;
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

  /**
   * I18N
   */
  public String getLabel(String language) {
    return super.getName(language);
  }

  /**
   * This method is a hack (technical debt)
   * @param name
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  public void setOrderNum(int orderNum) {
    this.orderNum = orderNum;
  }

  public int getOrderNum() {
    return orderNum;
  }

  public boolean isInheritanceBlocked() {
    return isInheritanceBlocked;
  }

  public void setInheritanceBlocked(boolean isInheritanceBlocked) {
    this.isInheritanceBlocked = isInheritanceBlocked;
  }

  @Override
  public boolean isHidden() {
    return hidden;
  }

  @Override
  public boolean isPublic() {
    return publicApp;
  }

  @Override
  public boolean isWorkflow() {
    final Optional<WAComponent> component = WAComponent.getByName(getName());
    return component.isPresent() && component.get().isWorkflow();
  }

  @Override
  public boolean isTopicTracker() {
    final Optional<WAComponent> component = WAComponent.getByName(getName());
    return component.isPresent() && component.get().isTopicTracker();
  }

  public String getIcon(boolean bigOne) {
    String app = getName();
    if (isWorkflow()) {
      app = "processManager";
    }
    String size = bigOne ? "Big.png" : "Small.gif";
    return URLUtil.getApplicationURL() + "/util/icons/component/" + app + size;
  }

  @Override
  public Collection<SilverpeasRole> getSilverpeasRolesFor(final User user) {
    Set<SilverpeasRole> silverpeasRoles =
        SilverpeasRole.from(OrganizationController.get().getUserProfiles(user.getId(), getId()));
    silverpeasRoles.remove(Manager);
    return silverpeasRoles;
  }

  @Override
  public String getParameterValue(final String parameterName) {
    return getCachedComponentInst().getParameterValue(parameterName);
  }

  public boolean isRemoved() {
    return ComponentInst.STATUS_REMOVED.equals(getStatus());
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
    ComponentInstLight other = (ComponentInstLight) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  /**
   * Gets the linked {@link ComponentInst}.
   * @return the linked {@link ComponentInst}.
   */
  private ComponentInst getCachedComponentInst() {
    if (cachedComponentInst == null && isDefined(getId())) {
      cachedComponentInst = OrganizationController.get().getComponentInst(getId());
    }
    return cachedComponentInst;
  }
}