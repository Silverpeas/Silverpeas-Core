/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.component.constant.ComponentInstanceParameterName;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.AbstractI18NBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComponentInst extends AbstractI18NBean<ComponentI18N>
    implements Serializable, Cloneable, Comparable<ComponentInst> {

  private static final long serialVersionUID = 1L;
  public final static String STATUS_REMOVED = "R";
  private String id;
  private String name;
  private String domainFatherId;
  private int order;
  private Date createDate = null;
  private Date updateDate = null;
  private Date removeDate = null;
  private String status = null;
  private String creatorUserId;
  private UserDetail creator;
  private String updaterUserId;
  private UserDetail updater;
  private String removerUserId;
  private UserDetail remover;
  private boolean isPublic = false;
  private boolean isHidden = false;
  private boolean isInheritanceBlocked = false;
  private List<ProfileInst> profiles;
  private List<Parameter> parameters = null;

  /**
   * Creates new ComponentInst
   */
  public ComponentInst() {
    id = "";
    name = "";
    domainFatherId = "";
    order = 0;
    profiles = new ArrayList<>();
    isPublic = false;
    isHidden = false;
  }

  @Override
  public int compareTo(ComponentInst o) {
    return order - o.getOrderNum();
  }

  @Override
  public Object clone() {
    ComponentInst ci = new ComponentInst();
    ci.setLocalId(getLocalId());
    ci.setName(name);
    ci.setLabel(getLabel());
    ci.setDescription(getDescription());
    ci.setDomainFatherId(domainFatherId);
    ci.setOrderNum(order);
    ci.setPublic(isPublic);
    ci.setHidden(isHidden);
    ci.setInheritanceBlocked(isInheritanceBlocked);
    if (profiles == null) {
      ci.profiles = null;
    } else {
      for (ProfileInst profile : profiles) {
        ci.addProfileInst((ProfileInst) profile.clone());
      }
    }
    ci.parameters = new ArrayList<>(this.parameters.size());
    for (Parameter param : this.parameters) {
      ci.parameters.add(param.clone());
    }
    ci.setTranslations(getClonedTranslations());
    return ci;
  }

  protected String[] cloneStringArray(String[] src) {
    if (src == null) {
      return null;
    }
    String[] clonedArray = new String[src.length];
    System.arraycopy(src, 0, clonedArray, 0, src.length);
    return clonedArray;
  }

  public String getId() {
    return name + id;
  }

  public void setLocalId(int id) {
    this.id = String.valueOf(id);
  }

  public int getLocalId() {
    return StringUtil.isDefined(id) ? Integer.parseInt(id) : -1;
  }

  public String getLocalIdAsString() {
    return id;
  }

  /**
   * This method is a hack (technical debt)
   * @param sName
   */
  @Override
  public void setName(String sName) {
    name = sName;
  }

  /**
   * This method is a hack (technical debt)
   * @return
   */
  @Override
  public String getName() {
    return name;
  }

  public void setLabel(String sLabel) {
    super.setName(sLabel);
  }

  public String getLabel() {
    return super.getName();
  }

  public void setDomainFatherId(String sDomainFatherId) {
    domainFatherId = sDomainFatherId;
  }

  public String getDomainFatherId() {
    return domainFatherId;
  }

  public void setOrderNum(int iOrderNum) {
    order = iOrderNum;
  }

  public int getOrderNum() {
    return order;
  }

  public Date getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  public Date getRemoveDate() {
    return removeDate;
  }

  public void setRemoveDate(Date removeDate) {
    this.removeDate = removeDate;
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

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public String getRemoverUserId() {
    return removerUserId;
  }

  public void setRemoverUserId(String removerUserId) {
    this.removerUserId = removerUserId;
  }

  public String getUpdaterUserId() {
    return updaterUserId;
  }

  public void setUpdaterUserId(String updaterUserId) {
    this.updaterUserId = updaterUserId;
  }

  public int getNumProfileInst() {
    return profiles.size();
  }

  public void addProfileInst(ProfileInst profileInst) {
    profiles.add(profileInst);
  }

  public String getSpaceId() {
    return domainFatherId;
  }

  public void deleteProfileInst(ProfileInst profileInst) {
    for (int nI = 0; nI < profiles.size(); nI++) {
      if (profiles.get(nI).getName().equals(profileInst.getName())) {
        profiles.remove(nI);
      }
    }
  }

  public List<ProfileInst> getAllProfilesInst() {
    return profiles;
  }

  public List<ProfileInst> getInheritedProfiles() {
    List<ProfileInst> inheritedProfiles = new ArrayList<>();
    for (ProfileInst profile : profiles) {
      if (profile.isInherited()) {
        inheritedProfiles.add(profile);
      }
    }

    return inheritedProfiles;
  }

  public List<ProfileInst> getProfiles() {
    List<ProfileInst> specificProfiles = new ArrayList<>();
    for (ProfileInst profile : profiles) {
      if (!profile.isInherited()) {
        specificProfiles.add(profile);
      }
    }

    return specificProfiles;
  }

  public void removeAllProfilesInst() {
    profiles.clear();
  }

  public ProfileInst getProfileInst(String profileName) {
    for (ProfileInst profile : profiles) {
      if (!profile.isInherited() && profile.getName().equals(profileName)) {
        return profile;
      }
    }
    return null;
  }

  public ProfileInst getInheritedProfileInst(String profileName) {
    for (ProfileInst profile : profiles) {
      if (profile.isInherited() && profile.getName().equals(profileName)) {
        return profile;
      }
    }
    return null;
  }

  public ProfileInst getProfileInst(int nIndex) {
    return profiles.get(nIndex);
  }

  public List<Parameter> getParameters() {
    if (parameters == null) {
      parameters = new ArrayList<Parameter>();
    }
    return parameters;
  }

  public void setParameters(List<Parameter> parameters) {
    this.parameters = parameters;
  }

  public Parameter getParameter(String parameterName) {
    for (Parameter parameter : parameters) {
      if (parameter.getName().equalsIgnoreCase(parameterName)) {
        return parameter;
      }
    }
    return null;
  }

  public String getParameterValue(String parameterName) {
    Parameter param = getParameter(parameterName);
    if (param != null) {
      return param.getValue();
    }
    return "";
  }

  /**
   * Gets a component instance parameter from a centralized parameter name.
   * @param parameterName the parameter name
   * @return a component instance parameter from a centralized parameter name.
   */
  public String getParameterValue(ComponentInstanceParameterName parameterName) {
    return getParameterValue(parameterName.name());
  }

  public String getLabel(String language) {
    return super.getName(language);
  }

  public boolean isHidden() {
    return isHidden;
  }

  public void setHidden(boolean isHidden) {
    this.isHidden = isHidden;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  public boolean isInheritanceBlocked() {
    return isInheritanceBlocked;
  }

  public void setInheritanceBlocked(boolean isInheritanceBlocked) {
    this.isInheritanceBlocked = isInheritanceBlocked;
  }

  public String getCreatorUserId() {
    return creatorUserId;
  }

  public void setCreatorUserId(String creatorUserId) {
    this.creatorUserId = creatorUserId;
  }

  public UserDetail getCreator() {
    return creator;
  }

  public void setCreator(UserDetail creator) {
    this.creator = creator;
  }

  public UserDetail getUpdater() {
    return updater;
  }

  public void setUpdater(UserDetail updater) {
    this.updater = updater;
  }

  public UserDetail getRemover() {
    return remover;
  }

  public void setRemover(UserDetail remover) {
    this.remover = remover;
  }

  public void removeInheritedProfiles() {
    ArrayList<ProfileInst> newProfiles = new ArrayList<>();
    for (ProfileInst profile : profiles) {
      if (!profile.isInherited()) {
        newProfiles.add(profile);
      }
    }
    profiles = newProfiles;
  }

  public boolean isWorkflow() {
    return WAComponent.get(getName()).get().isWorkflow();
  }

  public boolean isTopicTracker() {
    return WAComponent.get(getName()).get().isTopicTracker();
  }

  public String getPermalink() {
    return URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, getId());
  }
}
