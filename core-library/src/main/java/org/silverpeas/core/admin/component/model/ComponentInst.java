/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.BasicIdentifier;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.model.WithPermanentLink;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.synchronizedList;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.MANAGER;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ComponentInst extends AbstractI18NBean<ComponentI18N>
    implements SilverpeasSharedComponentInstance, WithPermanentLink {

  private static final long serialVersionUID = 1L;
  private static final Pattern COMPONENT_INSTANCE_IDENTIFIER =
      Pattern.compile("^([a-zA-Z-_]+)(\\d+)$");

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
  private final List<ProfileInst> profiles = synchronizedList(new ArrayList<>());
  private transient List<Parameter> parameters = null;

  @Override
  protected Class<ComponentI18N> getTranslationType() {
    return ComponentI18N.class;
  }

  /**
   * Creates new ComponentInst
   */
  public ComponentInst() {
    id = "";
    name = "";
    domainFatherId = "";
    order = 0;
    isPublic = false;
    isHidden = false;
  }

  /**
   * Creates a new Component instance as a copy of the specified one. Only the identifier isn't
   * copied as it should be unique.
   *
   * @param ci a component instance to copy.
   */
  @SuppressWarnings("IncompleteCopyConstructor")
  public ComponentInst(final ComponentInst ci) {
    super(ci);
    id = "";
    name = ci.name;
    domainFatherId = ci.domainFatherId;
    order = ci.order;
    createDate = ci.createDate;
    updateDate = ci.updateDate;
    removeDate = ci.removeDate;
    status = ci.status;
    creatorUserId = ci.creatorUserId;
    creator = ci.creator;
    updaterUserId = ci.updaterUserId;
    updater = ci.updater;
    removerUserId = ci.removerUserId;
    remover = ci.remover;
    isPublic = ci.isPublic;
    isHidden = ci.isHidden;
    isInheritanceBlocked = ci.isInheritanceBlocked;
    ci.profiles.stream().map(ProfileInst::new).forEach(this::addProfileInst);
    if (ci.parameters != null) {
      parameters = ci.parameters.stream().map(Parameter::new).collect(Collectors.toList());
    }
    setTranslations(ci.getClonedTranslations());
  }

  /**
   * Gets the name of the multi-user component from which the specified instance was spawn. By
   * convention, the identifiers of the component instances are made up of the name of the component
   * followed by a number. This method is a way to get directly the component name from an instance
   * identifier.
   *
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
   * component followed by a number, the local identifier. This method is a way to get directly the
   * component local identifier from an instance identifier.
   *
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
  public BasicIdentifier getIdentifier() {
    return new BasicIdentifier(getLocalId(), getId());
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

  /**
   * This method is a hack (technical debt)
   *
   * @param sName new name of the application.
   */
  @Override
  public void setName(String sName) {
    name = sName;
  }

  /**
   * This method is a hack (technical debt)
   *
   * @return the name of the application.
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

  @Override
  public int getOrderPosition() {
    return getOrderNum();
  }

  public Date getCreationDate() {
    return createDate;
  }

  public void setCreationDate(Date createDate) {
    this.createDate = createDate;
  }

  public Date getRemovalDate() {
    return removeDate;
  }

  public void setRemovalDate(Date removeDate) {
    this.removeDate = removeDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Date getLastUpdateDate() {
    return updateDate;
  }

  public void setLastUpdateDate(Date updateDate) {
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
    profiles.removeIf(p -> p.getName().equals(profileInst.getName()));
  }

  public List<ProfileInst> getAllProfilesInst() {
    return List.copyOf(profiles);
  }

  public List<ProfileInst> getInheritedProfiles() {
    return profiles.stream()
        .filter(ProfileInst::isInherited)
        .collect(Collectors.toList());
  }

  /**
   * Gets the specific right profiles of this component instance (that is to say all the
   * non-inherited profiles)
   * @return the specific right profiles of this component instance;
   */
  public List<ProfileInst> getProfiles() {
    return profiles.stream()
        .filter(Predicate.not(ProfileInst::isInherited))
        .collect(Collectors.toList());
  }

  public void removeAllProfilesInst() {
    profiles.clear();
  }

  public ProfileInst getProfileInst(String profileName) {
    return profiles.stream()
        .filter(Predicate.not(ProfileInst::isInherited))
        .filter(p -> p.getName().equals(profileName))
        .findFirst()
        .orElse(null);
  }

  public ProfileInst getInheritedProfileInst(String profileName) {
    return profiles.stream()
        .filter(ProfileInst::isInherited)
        .filter(p -> p.getName().equals(profileName))
        .findFirst()
        .orElse(null);
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
    return parameters.stream()
        .filter(p -> p.getName().equalsIgnoreCase(parameterName))
        .findFirst()
        .orElse(null);
  }

  @Override
  public String getParameterValue(String parameterName) {
    Parameter param = getParameter(parameterName);
    return param != null ? param.getValue() : "";
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

  public User getCreator() {
    if (creator == null && isDefined(creatorUserId)) {
      creator = UserDetail.getById(creatorUserId);
    }
    return creator;
  }

  public User getLastUpdater() {
    if (updater == null && isDefined(updaterUserId)) {
      updater = UserDetail.getById(updaterUserId);
    }
    return updater;
  }

  public void removeInheritedProfiles() {
    profiles.removeIf(ProfileInst::isInherited);
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

  @Override
  public String getPermalink() {
    return URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, getId());
  }

  @Override
  public Collection<SilverpeasRole> getSilverpeasRolesFor(final User user) {
    Set<SilverpeasRole> silverpeasRoles = SilverpeasRole.fromStrings(
        OrganizationController.get().getUserProfiles(user.getId(), getId()));
    silverpeasRoles.remove(MANAGER);
    return silverpeasRoles;
  }

  @Override
  public boolean isRemoved() {
    return STATUS_REMOVED.equals(getStatus());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ComponentInst that = (ComponentInst) o;
    return id.equals(that.id) && name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }
}
