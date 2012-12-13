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

package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.silverpeas.admin.space.SpaceServiceFactory;
import org.silverpeas.admin.space.quota.ComponentSpaceQuotaKey;
import org.silverpeas.admin.space.quota.DataStorageSpaceQuotaKey;
import org.silverpeas.quota.constant.QuotaType;
import org.silverpeas.quota.exception.QuotaException;
import org.silverpeas.quota.exception.QuotaRuntimeException;
import org.silverpeas.quota.model.Quota;
import org.silverpeas.util.UnitUtil;

import com.google.common.base.Objects;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.AbstractI18NBean;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * The class SpaceInst is the representation in memory of a space
 */
public class SpaceInst extends AbstractI18NBean implements Serializable, Comparable<SpaceInst>,
    Cloneable {

  public static final String PERSONAL_SPACE_ID = "-10";
  public static final String DEFAULT_SPACE_ID = "-20";

  private static final long serialVersionUID = 4695928610067045964L;
  // First page possible types
  final public static int FP_TYPE_STANDARD = 0; // Page d'acueil standard
  final public static int FP_TYPE_COMPONENT_INST = 1; // Composant (dans ce cas
  // : firstPageExtraParam = composant instance ID)
  final public static int FP_TYPE_PORTLET = 2; // Portlets
  final public static int FP_TYPE_HTML_PAGE = 3; // Page HTML
  final public static String STATUS_REMOVED = "R";

  /* Unique identifier of the space */
  private String id;

  /* Unique identifier of the father of the space */
  private String domainFatherId;

  /* Unique identifier of the space */
  private String name;

  /* Describe the space */
  private String description;

  /* User Id of the creator of the space */
  private String creatorUserId;

  /* First page type of the space */
  private int firstPageType;

  /* First page extra param of the space */
  private String firstPageExtraParam;

  /* Space relative order */
  private int orderNum;
  private Date createDate = null;
  private Date updateDate = null;
  private Date removeDate = null;
  private String status = null;
  private UserDetail creator;
  private UserDetail updater;
  private UserDetail remover;
  private String updaterUserId;
  private String removerUserId;
  private boolean inheritanceBlocked = false;
  private String look = null;

  /* Collection of components Instances */
  private ArrayList<ComponentInst> components;

  /* Collection of space profiles Instances */
  private ArrayList<SpaceProfileInst> spaceProfiles;

  /* Array of space ids that are children of this space */
  private String[] subSpaceIds;
  private int level = 0;
  private boolean displaySpaceFirst = true;
  private boolean isPersonalSpace = false;

  /**
   * This data is not used in equals and hashcode process as it is an extra information.
   */
  private Quota componentSpaceQuota = null;
  private Quota componentSpaceQuotaReached = null;

  /**
   * This data is not used in equals and hashcode process as it is an extra information.
   */
  private Quota dataStorageQuota = null;
  private Quota dataStorageQuotaReached = null;

  /**
   * Constructor
   */
  public SpaceInst() {
    id = "";
    domainFatherId = "";
    name = "";
    description = "";
    creatorUserId = "";
    firstPageType = 0;
    firstPageExtraParam = "";
    orderNum = 0;
    components = new ArrayList<ComponentInst>();
    spaceProfiles = new ArrayList<SpaceProfileInst>();
    subSpaceIds = new String[0];
    level = 0;
    displaySpaceFirst = true;
    isPersonalSpace = false;
  }

  public int compareTo(SpaceInst o) {
    return orderNum - o.orderNum;
  }

  /**
   * Set the space id
   * @param sId new space id
   */
  public void setId(String sId) {
    id = sId;
  }

  /**
   * Get the space id
   * @return the requested space id
   */
  public String getId() {
    return id;
  }

  /**
   * Set the space father id
   * @param sDomainFatherId The space father id
   */
  public void setDomainFatherId(String sDomainFatherId) {
    domainFatherId = sDomainFatherId;
  }

  /**
   * Get the domain father id
   * @return the space father id. If space has no father, returns an empty string.
   */
  public String getDomainFatherId() {
    return domainFatherId;
  }

  /**
   * Set the space name
   * @param sName The new space name
   */
  public void setName(String sName) {
    name = sName;
  }

  /**
   * Get the space name
   * @return the space name
   */
  public String getName() {
    return name;
  }

  public String getName(String language) {

    if (isPersonalSpace) {
      return GeneralPropertiesManager.getGeneralMultilang(language).getString("GML.personalSpace",
          "Mon espace");
    } else {
      if (!I18NHelper.isI18N) {
        return getName();
      }
      SpaceI18N s = (SpaceI18N) getTranslations().get(language);
      if (s != null) {
        return s.getName();
      } else {
        return getName();
      }
    }
  }

  /**
   * Set the space description
   * @param sDescription The new space description
   */
  public void setDescription(String sDescription) {
    description = sDescription;
  }

  /**
   * Get the space description
   * @return The space description
   */
  public String getDescription() {
    return description;
  }

  public String getDescription(String language) {
    if (!I18NHelper.isI18N) {
      return getDescription();
    }

    SpaceI18N s = (SpaceI18N) getTranslations().get(language);
    if (s != null) {
      return s.getDescription();
    } else {
      return getDescription();
    }
  }

  /**
   * Set the space creator id
   * @param sCreatorUserId The user id of person who created the space
   */
  public void setCreatorUserId(String sCreatorUserId) {
    creatorUserId = sCreatorUserId;
  }

  /**
   * Get the space creator id
   * @return The user id of person who created the space
   */
  public String getCreatorUserId() {
    return creatorUserId;
  }

  /**
   * Set the space first page type
   * @param iFirstPageType
   */
  public void setFirstPageType(int iFirstPageType) {
    firstPageType = iFirstPageType;
  }

  /**
   * Get the space first page type
   * @return The space first page type
   */
  public int getFirstPageType() {
    return firstPageType;
  }

  /**
   * Set the space relative order num
   * @param iOrderNum
   */
  public void setOrderNum(int iOrderNum) {
    orderNum = iOrderNum;
  }

  /**
   * Get the space relative order num
   * @return
   */
  public int getOrderNum() {
    return orderNum;
  }

  /**
   * Set the space first page extra parameter
   * @param sFirstPageExtraParam
   */
  public void setFirstPageExtraParam(String sFirstPageExtraParam) {
    firstPageExtraParam = sFirstPageExtraParam;
  }

  /**
   * Get the space first page extra parameter
   * @return The space first page extra parameter
   */
  public String getFirstPageExtraParam() {
    return firstPageExtraParam;
  }

  /**
   * Set the list of children space ids
   * @param asSubSpaceIds Array of String containing all the children space ids
   */
  public void setSubSpaceIds(String[] asSubSpaceIds) {
    if (asSubSpaceIds == null) {
      subSpaceIds = new String[0];
    } else {
      subSpaceIds = asSubSpaceIds;
    }
  }

  /**
   * Get the list of children space ids
   * @return Array of String containing all the children space ids
   */
  public String[] getSubSpaceIds() {
    return subSpaceIds;
  }

  /**
   * Get the number of components in that space
   * @return The number of components in that space
   */
  public int getNumComponentInst() {
    return components.size();
  }

  /**
   * Add a component in component list (WARNING : component will not be added in database, only in
   * that spaceInst object !!!)
   * @param componentInst component instance to be added
   */
  public void addComponentInst(ComponentInst componentInst) {
    components.add(componentInst);
  }

  /**
   * Remove a component from component list (WARNING : component will not be removed from database,
   * only in that spaceInst object !!!)
   * @param componentInst component instance to be removed
   */
  public void deleteComponentInst(ComponentInst componentInst) {
    for (int nI = 0; nI < components.size(); nI++) {
      if (components.get(nI).getName().equals(componentInst.getName())) {
        components.remove(nI);
        return;
      }
    }
  }

  /**
   * Get all the components in that space
   * @return The components in that space
   */
  public ArrayList<ComponentInst> getAllComponentsInst() {
    return components;
  }

  /**
   * Remove all components from component list (WARNING : components will not be removed from
   * database, only in that spaceInst object !!!)
   */
  public void removeAllComponentsInst() {
    components = new ArrayList<ComponentInst>();
  }

  /**
   * Add a component in component list (WARNING : component will not be added in database, only in
   * that spaceInst object !!!)
   * @param componentName component instance to be added
   */
  public ComponentInst getComponentInst(String componentName) {
    if (!components.isEmpty()) {
      for (ComponentInst component : components) {
        if (component.getName().equals(componentName)) {
          return component;
        }
      }
    }
    return null;
  }

  /**
   * Get a component from component list, given its name (WARNING : if more than one component
   * instance match the given name, the first one will be returned)
   * @param nIndex
   * @return
   */
  public ComponentInst getComponentInst(int nIndex) {
    return components.get(nIndex);
  }

  /**
   * Get the number of space profiles in that space
   * @return The number of space profiles in that space
   */
  public int getNumSpaceProfileInst() {
    return spaceProfiles.size();
  }

  /**
   * Add a space profile in space profile list (WARNING : space profile will not be added in
   * database, only in that spaceInst object !!!)
   * @param spaceProfileInst space profile to be added
   */
  public void addSpaceProfileInst(SpaceProfileInst spaceProfileInst) {
    spaceProfileInst.setSpaceFatherId(getId());
    if (spaceProfiles.contains(spaceProfileInst)) {
      spaceProfiles.remove(spaceProfileInst);
    }
    spaceProfiles.add(spaceProfileInst);
  }

  /**
   * Remove a space profile from space profile list (WARNING : space profile will not be removed
   * from database, only from that spaceInst object !!!)
   * @param spaceProfileInst space profile to be removed
   */
  public void deleteSpaceProfileInst(SpaceProfileInst spaceProfileInst) {
    for (int nI = 0; nI < spaceProfiles.size(); nI++) {
      SpaceProfileInst profile = spaceProfiles.get(nI);
      if (profile.getId().equals(spaceProfileInst.getId())) {
        spaceProfiles.remove(nI);
      }
    }
  }

  /**
   * Get all the space profiles from space profile list
   * @return The space profiles of that space
   */
  public ArrayList<SpaceProfileInst> getAllSpaceProfilesInst() {
    return spaceProfiles;
  }

  /**
   * Remove all space profiles from space profiles list (WARNING : space profiles will not be
   * removed from database, only from that spaceInst object !!!)
   */
  public void removeAllSpaceProfilesInst() {
    spaceProfiles = new ArrayList<SpaceProfileInst>();
  }

  /**
   * Get a space profile from space profiles list, given its name (WARNING : if more than one space
   * profile match the given name, the first one will be returned)
   * @param sSpaceProfileName name of requested space profile
   */
  public SpaceProfileInst getSpaceProfileInst(String sSpaceProfileName) {
    return getSpaceProfileInst(sSpaceProfileName, false);
  }

  public SpaceProfileInst getInheritedSpaceProfileInst(String sSpaceProfileName) {
    return getSpaceProfileInst(sSpaceProfileName, true);
  }

  private SpaceProfileInst getSpaceProfileInst(String spaceProfileName, boolean inherited) {
    for (SpaceProfileInst profile : spaceProfiles) {
      if (profile.isInherited() == inherited && profile.getName().equals(spaceProfileName)) {
        return profile;
      }
    }
    return null;
  }

  /**
   * Get a space profile from space profiles list, given its name (WARNING : if more than one space
   * profile match the given name, the first one will be returned)
   * @param nIndex position of requested space profile in space profile list
   */
  public SpaceProfileInst getSpaceProfileInst(int nIndex) {
    return spaceProfiles.get(nIndex);
  }

  public List<SpaceProfileInst> getInheritedProfiles() {
    List<SpaceProfileInst> profiles = new ArrayList<SpaceProfileInst>();
    for (SpaceProfileInst profile : spaceProfiles) {
      if (profile.isInherited()) {
        profiles.add(profile);
      }
    }

    return profiles;
  }

  public List<SpaceProfileInst> getProfiles() {
    List<SpaceProfileInst> profiles = new ArrayList<SpaceProfileInst>();
    for (SpaceProfileInst profile : spaceProfiles) {
      if (!profile.isInherited()) {
        profiles.add(profile);
      }
    }

    return profiles;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getLevel() {
    return this.level;
  }

  public boolean isRoot() {
    return (!StringUtil.isDefined(getDomainFatherId()) || "0".equals(getDomainFatherId()) || "-1"
        .equals(getDomainFatherId()));
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

  public boolean isInheritanceBlocked() {
    return inheritanceBlocked;
  }

  public void setInheritanceBlocked(boolean isInheritanceBlocked) {
    this.inheritanceBlocked = isInheritanceBlocked;
  }

  public String getLook() {
    return look;
  }

  public void setLook(String look) {
    this.look = look;
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

  public boolean isDisplaySpaceFirst() {
    return displaySpaceFirst;
  }

  public void setDisplaySpaceFirst(boolean isDisplaySpaceFirst) {
    this.displaySpaceFirst = isDisplaySpaceFirst;
  }

  public void setPersonalSpace(boolean isPersonalSpace) {
    this.isPersonalSpace = isPersonalSpace;
  }

  public boolean isPersonalSpace() {
    return isPersonalSpace;
  }

  /**
   * @return the componentSpaceQuota
   */
  public Quota getComponentSpaceQuota() {
    if (componentSpaceQuota == null) {
      loadComponentSpaceQuota();
    }
    return componentSpaceQuota;
  }

  /**
   * Sets the max count of component space of the space
   */
  public void setComponentSpaceQuotaMaxCount(final long componentSpaceQuotaMaxCount)
      throws QuotaException {
    loadComponentSpaceQuota();
    componentSpaceQuota.setMaxCount(componentSpaceQuotaMaxCount);
    componentSpaceQuota.validateBounds();
  }

  /**
   * Indicates if the quota of the space or of a parent space is reached.
   * @return
   */
  public boolean isComponentSpaceQuotaReached() {
    componentSpaceQuotaReached = SpaceServiceFactory.getComponentSpaceQuotaService()
        .getQuotaReachedFromSpacePath(ComponentSpaceQuotaKey.from(this));
    return componentSpaceQuotaReached.isReached();
  }

  /**
   * Gets the error message about component space quota reached.
   * @param language
   * @return
   */
  public String getComponentSpaceQuotaReachedErrorMessage(final String language) {
    return getQuotaReachedErrorMessage(componentSpaceQuotaReached, language,
        "componentSpaceQuotaReached");
  }

  /**
   * Centralizes the component space quota loading
   */
  private void loadComponentSpaceQuota() {
    try {
      componentSpaceQuota =
          SpaceServiceFactory.getComponentSpaceQuotaService()
              .get(ComponentSpaceQuotaKey.from(this));
    } catch (final QuotaException qe) {
      throw new QuotaRuntimeException("Space", SilverpeasException.ERROR,
          "root.EX_CANT_GET_COMPONENT_SPACE_QUOTA", qe);
    }
  }

  /**
   * @return the dataStorageQuota
   */
  public Quota getDataStorageQuota() {
    if (dataStorageQuota == null) {
      loadDataStorageQuota();
    }
    return dataStorageQuota;
  }

  /**
   * Sets the max count of data storage of the space
   */
  public void setDataStorageQuotaMaxCount(final long dataStorageQuotaMaxCount)
      throws QuotaException {
    loadDataStorageQuota();
    dataStorageQuota.setMaxCount(dataStorageQuotaMaxCount);
    dataStorageQuota.validateBounds();
  }

  /**
   * Indicates if the quota of the space or of a parent space is reached.
   * @return
   */
  public boolean isDataStorageQuotaReached() {
    dataStorageQuotaReached = SpaceServiceFactory.getDataStorageSpaceQuotaService()
        .getQuotaReachedFromSpacePath(DataStorageSpaceQuotaKey.from(this));
    return dataStorageQuotaReached.isReached();
  }

  /**
   * Gets the error message about data storage space quota reached.
   * @param language
   * @return
   */
  public String getDataStorageQuotaReachedErrorMessage(final String language) {
    return getQuotaReachedErrorMessage(dataStorageQuotaReached, language, "dataStorageQuotaReached");
  }

  /**
   * Centralizes the data storage quota loading
   */
  private void loadDataStorageQuota() {
    try {
      dataStorageQuota =
          SpaceServiceFactory.getDataStorageSpaceQuotaService().get(
              DataStorageSpaceQuotaKey.from(this));
    } catch (final QuotaException qe) {
      throw new QuotaRuntimeException("Space", SilverpeasException.ERROR,
          "root.EX_CANT_GET_DATA_STORAGE_QUOTA", qe);
    }
  }

  /**
   * Centralized the error message about reached quota.
   * @param quotaReached
   * @param language
   * @param stringTemplateFile
   * @return
   */
  private String getQuotaReachedErrorMessage(Quota quotaReached, String language,
      final String stringTemplateFile) {
    if (!QuotaType.COMPONENTS_IN_SPACE.equals(quotaReached.getType())) {
      quotaReached = quotaReached.clone();
      quotaReached.setMinCount(UnitUtil.convertTo(quotaReached.getMinCount(), UnitUtil.memUnit.B,
          UnitUtil.memUnit.MB));
      quotaReached.setMaxCount(UnitUtil.convertTo(quotaReached.getMaxCount(), UnitUtil.memUnit.B,
          UnitUtil.memUnit.MB));
      quotaReached.setCount(UnitUtil.convertTo(quotaReached.getCount(), UnitUtil.memUnit.B,
          UnitUtil.memUnit.MB));
    }
    SpaceInstLight space =
        OrganizationControllerFactory.getFactory().getOrganizationController()
            .getSpaceInstLightById(quotaReached.getResourceId());
    final SilverpeasTemplate template =
        SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("admin/space/quota");
    template.setAttribute("quota", quotaReached);
    if (!space.getShortId().equals(new SpaceInstLight(this).getShortId())) {
      template.setAttribute("fromSpaceId", space.getShortId());
      template.setAttribute("fromSpaceName", space.getName());
    }
    if (!StringUtil.isDefined(language)) {
      language = I18NHelper.defaultLanguage;
    }
    return template.applyFileTemplate(stringTemplateFile + "_" + language);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj instanceof SpaceInst) {
      SpaceInst other = (SpaceInst) obj;
      return Objects.equal(other.createDate, createDate)
          && Objects.equal(other.id, id)
          && Objects.equal(other.level, level)
          && Objects.equal(other.look, look)
          && Objects.equal(other.firstPageType, firstPageType)
          && Objects.equal(other.orderNum, orderNum)
          && Objects.equal(other.creatorUserId, creatorUserId)
          && Objects.equal(other.description, description)
          && Objects.equal(other.domainFatherId, domainFatherId)
          && Objects.equal(other.firstPageExtraParam, firstPageExtraParam)
          && Objects.equal(other.name, name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(createDate, id, level, look, firstPageType, orderNum,
        creatorUserId, description, domainFatherId, firstPageExtraParam, name);
  }

  @Override
  public SpaceInst clone() {
    SpaceInst clone = new SpaceInst();

    // clone basic information
    clone.setDescription(description);
    clone.setDisplaySpaceFirst(displaySpaceFirst);
    clone.setFirstPageExtraParam(firstPageExtraParam);
    clone.setFirstPageType(firstPageType);
    clone.setInheritanceBlocked(inheritanceBlocked);
    clone.setLook(look);
    clone.setName(name);
    clone.setPersonalSpace(isPersonalSpace);

    // clone profiles
    List<SpaceProfileInst> profiles = getProfiles();
    for (SpaceProfileInst profile : profiles) {
      clone.addSpaceProfileInst(profile.clone());
    }

    // clone components
    List<ComponentInst> components = getAllComponentsInst();
    for (ComponentInst component : components) {
      clone.addComponentInst((ComponentInst) component.clone());
    }

    // clone subspace ids
    clone.setSubSpaceIds(getSubSpaceIds().clone());

    return clone;
  }

  public void removeInheritedProfiles() {
    ArrayList<SpaceProfileInst> newProfiles = new ArrayList<SpaceProfileInst>();
    for (SpaceProfileInst profile : spaceProfiles) {
      if (!profile.isInherited()) {
        newProfiles.add(profile);
      }
    }
    spaceProfiles = newProfiles;
  }
}