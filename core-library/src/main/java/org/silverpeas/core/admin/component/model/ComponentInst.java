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

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.Manager;
import static org.silverpeas.core.util.StringUtil.isDefined;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ComponentInst extends AbstractI18NBean<ComponentI18N>
    implements Cloneable, SilverpeasSharedComponentInstance {

  private static final long serialVersionUID = 1L;
  private static final Pattern COMPONENT_INSTANCE_IDENTIFIER =
      Pattern.compile("^([a-zA-Z-_]+)([0-9]+)$");

  public static final String STATUS_REMOVED = "R";
  @XmlAttribute
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
  private boolean isPublic;
  private boolean isHidden;
  private boolean isInheritanceBlocked = false;
  private List<ProfileInst> profiles;
  private transient List<Parameter> parameters = null;

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

  /**
   * Gets the name of the multi-user component from which the specified instance was spawn. By
   * convention, the identifiers of the component instances are made up of the name of the
   * component followed by a number. This method is a way to get directly the component name
   * from an instance identifier.
   * @param componentInstanceId the unique identifier of a component instance.
   * @return the name of the multi-user component or null if the specified identifier doesn't match
   * the rule of a shared component instance identifier.
   */
  public static String getComponentName(final String componentInstanceId) {
    String componentName = null;
    Matcher matcher = COMPONENT_INSTANCE_IDENTIFIER.matcher(componentInstanceId);
    if (matcher.matches()) {
      componentName = matcher.group(1);
    }
    return componentName;
  }

  /**
   * Gets the local identifier of the multi-user component from which the specified instance was
   * spawn. By convention, the identifiers of the component instances are made up of the name of the
   * component followed by a number, the local identifier. This method is a way to get directly
   * the component local identifier from an instance identifier.
   * @param componentInstanceId the unique identifier of a component instance.
   * @return the local identifier of the multi-user component or -1 if the specified identifier
   * doesn't match the rule of a shared component instance identifier.
   */
  public static int getComponentLocalId(final String componentInstanceId) {
    int componentId = -1;
    if (StringUtil.isDefined(componentInstanceId)) {
      Matcher matcher = COMPONENT_INSTANCE_IDENTIFIER.matcher(componentInstanceId);
      if (matcher.matches()) {
        componentId = Integer.parseInt(matcher.group(2));
      }
    }
    return componentId;
  }

  @Override
  public Object clone() {
    ComponentInst ci;
    try {
      ci = (ComponentInst) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new SilverpeasRuntimeException(e);
    }
    if (profiles == null) {
      ci.profiles = null;
    } else {
      ci.profiles = new ArrayList<>(profiles.size());
      for (ProfileInst profile : profiles) {
        ci.addProfileInst(profile.copy());
      }
    }
    ci.parameters = new ArrayList<>(this.parameters.size());
    for (Parameter param : this.parameters) {
      ci.parameters.add(param.clone());
    }
    ci.setTranslations(getClonedTranslations());
    return ci;
  }

  @Override
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

  @Override
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
    remover = null;
  }

  public String getUpdaterUserId() {
    return updaterUserId;
  }

  public void setUpdaterUserId(String updaterUserId) {
    this.updaterUserId = updaterUserId;
    updater = null;
  }

  public int getNumProfileInst() {
    return profiles.size();
  }

  public void addProfileInst(ProfileInst profileInst) {
    profiles.add(profileInst);
  }

  @Override
  public String getSpaceId() {
    return getDomainFatherId();
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
      parameters = new ArrayList<>();
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

  @Override
  public String getParameterValue(String parameterName) {
    Parameter param = getParameter(parameterName);
    if (param != null) {
      return param.getValue();
    }
    return "";
  }

  @Override
  public String getLabel(String language) {
    return super.getName(language);
  }

  @Override
  public boolean isHidden() {
    return isHidden;
  }

  public void setHidden(boolean isHidden) {
    this.isHidden = isHidden;
  }

  @Override
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
    creator = null;
  }

  public UserDetail getCreator() {
    if (creator == null && isDefined(creatorUserId)) {
      creator = UserDetail.getById(creatorUserId);
    }
    return creator;
  }

  public UserDetail getUpdater() {
    if (updater == null && isDefined(updaterUserId)) {
      updater = UserDetail.getById(updaterUserId);
    }
    return updater;
  }

  public UserDetail getRemover() {
    if (remover == null && isDefined(removerUserId)) {
      remover = UserDetail.getById(removerUserId);
    }
    return remover;
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

  @Override
  public boolean isWorkflow() {
    return WAComponent.getByName(getName())
        .orElseThrow(() -> new SilverpeasRuntimeException("No Such WAComponent " + getName()))
        .isWorkflow();
  }

  @Override
  public boolean isTopicTracker() {
    return WAComponent.getByName(getName())
        .orElseThrow(() -> new SilverpeasRuntimeException("No Such WAComponent " + getName()))
        .isTopicTracker();
  }

  public String getPermalink() {
    return URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, getId());
  }

  public String getInternalLink() {
    return URLUtil.getURL(getName(), "", getId()) + "Main";
  }

  @Override
  public Collection<SilverpeasRole> getSilverpeasRolesFor(final User user) {
    Set<SilverpeasRole> silverpeasRoles =
        SilverpeasRole.from(OrganizationController.get().getUserProfiles(user.getId(), getId()));
    silverpeasRoles.remove(Manager);
    return silverpeasRoles;
  }
}