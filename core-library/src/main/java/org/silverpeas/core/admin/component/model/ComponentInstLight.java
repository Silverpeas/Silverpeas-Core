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

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.persistence.ComponentInstanceRow;
import org.silverpeas.core.i18n.AbstractI18NBean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * The class ComponentInstLight is the representation in memory of a component instance
 */
public class ComponentInstLight extends AbstractI18NBean<ComponentI18N> implements Serializable {

  private static final long serialVersionUID = 4859368422448142768L;

  /* Unique identifier of the instance */
  private String m_sId;

  /* Unique identifier of the father of the space */
  private String m_sDomainFatherId;

  /* instance Type */
  private String m_sName;
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

  /**
   * Constructor
   */
  public ComponentInstLight() {
    m_sId = "";
    m_sDomainFatherId = "";
    m_sName = "";
  }

  /**
   * Constructor
   */
  public ComponentInstLight(ComponentInstanceRow compo) {
    m_sId = Integer.toString(compo.id);
    m_sDomainFatherId = Integer.toString(compo.spaceId);
    setLabel(compo.name);
    setDescription(compo.description);
    m_sName = compo.componentName;

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

  public String getId() {
    return m_sName + m_sId;
  }

  public int getLocalId() {
    return Integer.parseInt(m_sId);
  }

  public void setLocalId(int id) {
    this.m_sId = String.valueOf(id);
  }

  /**
   * Set the domain father id
   */
  public void setDomainFatherId(String sDomainFatherId) {
    this.m_sDomainFatherId = sDomainFatherId;
  }

  /**
   * Get the domain father id
   * @return the component instance father id. If the component instance has no father,
   * returns an empty string.
   */
  public String getDomainFatherId() {
    return m_sDomainFatherId;
  }

  /**
   * Has this component instance a domain father? In the case of a component instance, a domain
   * father is a space. For example, a component instance has no domain father when it belongs to
   * the user's personal space.
   * @return true if this component has a domain father, false otherwise.
   */
  public boolean hasDomainFather() {
    return StringUtil.isDefined(m_sDomainFatherId) &&
        m_sDomainFatherId.startsWith(SpaceInst.SPACE_KEY_PREFIX);
  }

  /**
   * Get the component name
   * This method is a hack (technical debt)
   * @return the component name
   */
  @Override
  public String getName() {
    return m_sName;
  }

  /**
   * Get the component type
   * @return the component type
   */
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
    String sPath = "";
    if (path != null) {
      SpaceInstLight space = null;
      for (int i = 0; i < path.size(); i++) {
        if (i > 0) {
          sPath += separator;
        }

        space = path.get(i);
        sPath += space.getName();
      }
    }
    return sPath;
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
    m_sName = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_sId == null) ? 0 : m_sId.hashCode());
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

  public boolean isHidden() {
    return hidden;
  }

  public boolean isPublic() {
    return publicApp;
  }

  public boolean isWorkflow() {
    return WAComponent.get(getName()).get().isWorkflow();
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
    if (m_sId == null) {
      if (other.m_sId != null) {
        return false;
      }
    } else if (!m_sId.equals(other.m_sId)) {
      return false;
    }
    return true;
  }
}