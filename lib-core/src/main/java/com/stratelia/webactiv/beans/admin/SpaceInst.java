/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
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

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.AbstractI18NBean;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * The class SpaceInst is the representation in memory of a space
 */
public class SpaceInst extends AbstractI18NBean implements Serializable, Comparable<SpaceInst> {

  private static final long serialVersionUID = 4695928610067045964L;
  // First page possible types
  final public static int FP_TYPE_STANDARD = 0; // Page d'acueil standard
  final public static int FP_TYPE_COMPONENT_INST = 1; // Composant (dans ce cas
  // : firstPageExtraParam =
  // composant instance ID)
  final public static int FP_TYPE_PORTLET = 2; // Portlets
  final public static int FP_TYPE_HTML_PAGE = 3; // Page HTML

  final public static String STATUS_REMOVED = "R";

  /* Unique identifier of the space */
  private String m_sId;

  /* Unique identifier of the father of the space */
  private String m_sDomainFatherId;

  /* Unique identifier of the space */
  private String m_sName;

  /* Describe the space */
  private String m_sDescription;

  /* User Id of the creator of the space */
  private String m_sCreatorUserId;

  /* First page type of the space */
  private int m_iFirstPageType;

  /* First page extra param of the space */
  private String m_sFirstPageExtraParam;

  /* Space relative order */
  private int m_iOrderNum;

  private Date createDate = null;
  private Date updateDate = null;
  private Date removeDate = null;
  private String status = null;

  private UserDetail creator;
  private UserDetail updater;
  private UserDetail remover;
  private String updaterUserId;
  private String removerUserId;

  private boolean isInheritanceBlocked = false;

  private String look = null;

  /* Collection of components Instances */
  private ArrayList<ComponentInst> m_alComponentInst;

  /* Collection of space profiles Instances */
  private ArrayList<SpaceProfileInst> m_alSpaceProfileInst;

  /* Array of space ids that are children of this space */
  private String[] m_asSubSpaceIds;

  private int level = 0;

  private boolean displaySpaceFirst = true;
  private boolean isPersonalSpace = false;

  /**
   * Constructor
   */
  public SpaceInst() {
    m_sId = "";
    m_sDomainFatherId = "";
    m_sName = "";
    m_sDescription = "";
    m_sCreatorUserId = "";
    m_iFirstPageType = 0;
    m_sFirstPageExtraParam = "";
    m_iOrderNum = 0;
    m_alComponentInst = new ArrayList<ComponentInst>();
    m_alSpaceProfileInst = new ArrayList<SpaceProfileInst>();
    m_asSubSpaceIds = new String[0];
    level = 0;
    displaySpaceFirst = true;
    setPersonalSpace(false);
  }

  public int compareTo(SpaceInst o) {
    return m_iOrderNum - o.m_iOrderNum;
  }

  /**
   * Set the space id
   * @param sId new space id
   */
  public void setId(String sId) {
    m_sId = sId;
  }

  /**
   * Get the space id
   * @return the requested space id
   */
  public String getId() {
    return m_sId;
  }

  /**
   * Set the space father id
   * @param sDomainFatherId The space father id
   */
  public void setDomainFatherId(String sDomainFatherId) {
    m_sDomainFatherId = sDomainFatherId;
  }

  /**
   * Get the domain father id
   * @return the space father id. If space has no father, returns an empty string.
   */
  public String getDomainFatherId() {
    return m_sDomainFatherId;
  }

  /**
   * Set the space name
   * @param sName The new space name
   */
  public void setName(String sName) {
    m_sName = sName;
  }

  /**
   * Get the space name
   * @return the space name
   */
  public String getName() {
    return m_sName;
  }

  public String getName(String language) {

    if (isPersonalSpace) {
      return GeneralPropertiesManager.getGeneralMultilang(language).getString("GML.personalSpace",
          "Mon espace");
    } else {
      if (!I18NHelper.isI18N)
        return getName();

      SpaceI18N s = (SpaceI18N) getTranslations().get(language);
      if (s == null)
        s = (SpaceI18N) getNextTranslation();

      return s.getName();
    }
  }

  /**
   * Set the space description
   * @param sDescription The new space description
   */
  public void setDescription(String sDescription) {
    m_sDescription = sDescription;
  }

  /**
   * Get the space description
   * @return The space description
   */
  public String getDescription() {
    return m_sDescription;
  }

  public String getDescription(String language) {
    if (!I18NHelper.isI18N)
      return getDescription();

    SpaceI18N s = (SpaceI18N) getTranslations().get(language);
    if (s == null)
      s = (SpaceI18N) getNextTranslation();

    return s.getDescription();
  }

  /**
   * Set the space creator id
   * @param sCreatorUserId The user id of person who created the space
   */
  public void setCreatorUserId(String sCreatorUserId) {
    m_sCreatorUserId = sCreatorUserId;
  }

  /**
   * Get the space creator id
   * @return The user id of person who created the space
   */
  public String getCreatorUserId() {
    return m_sCreatorUserId;
  }

  /**
   * Set the space first page type
   * @param iFirstPageType
   */
  public void setFirstPageType(int iFirstPageType) {
    m_iFirstPageType = iFirstPageType;
  }

  /**
   * Get the space first page type
   * @return The space first page type
   */
  public int getFirstPageType() {
    return m_iFirstPageType;
  }

  /**
   * Set the space relative order num
   * @param iOrderNum
   */
  public void setOrderNum(int iOrderNum) {
    m_iOrderNum = iOrderNum;
  }

  /**
   * Get the space relative order num
   * @return
   */
  public int getOrderNum() {
    return m_iOrderNum;
  }

  /**
   * Set the space first page extra parameter
   * @param sFirstPageExtraParam
   */
  public void setFirstPageExtraParam(String sFirstPageExtraParam) {
    m_sFirstPageExtraParam = sFirstPageExtraParam;
  }

  /**
   * Get the space first page extra parameter
   * @return The space first page extra parameter
   */
  public String getFirstPageExtraParam() {
    return m_sFirstPageExtraParam;
  }

  /**
   * Set the list of children space ids
   * @param asSubSpaceIds Array of String containing all the children space ids
   */
  public void setSubSpaceIds(String[] asSubSpaceIds) {
    if (asSubSpaceIds == null)
      m_asSubSpaceIds = new String[0];
    else
      m_asSubSpaceIds = asSubSpaceIds;
  }

  /**
   * Get the list of children space ids
   * @return Array of String containing all the children space ids
   */
  public String[] getSubSpaceIds() {
    return m_asSubSpaceIds;
  }

  /**
   * Get the number of components in that space
   * @return The number of components in that space
   */
  public int getNumComponentInst() {
    return m_alComponentInst.size();
  }

  /**
   * Add a component in component list (WARNING : component will not be added in database, only in
   * that spaceInst object !!!)
   * @param componentInst component instance to be added
   */
  public void addComponentInst(ComponentInst componentInst) {
    m_alComponentInst.add(componentInst);
  }

  /**
   * Remove a component from component list (WARNING : component will not be removed from database,
   * only in that spaceInst object !!!)
   * @param componentInst component instance to be removed
   */
  public void deleteComponentInst(ComponentInst componentInst) {
    for (int nI = 0; nI < m_alComponentInst.size(); nI++)
      if (m_alComponentInst.get(nI).getName().equals(componentInst.getName())) {
        m_alComponentInst.remove(nI);
        return;
      }
  }

  /**
   * Get all the components in that space
   * @return The components in that space
   */
  public ArrayList<ComponentInst> getAllComponentsInst() {
    return m_alComponentInst;
  }

  /**
   * Remove all components from component list (WARNING : components will not be removed from
   * database, only in that spaceInst object !!!)
   */
  public void removeAllComponentsInst() {
    m_alComponentInst = new ArrayList<ComponentInst>();
  }

  /**
   * Add a component in component list (WARNING : component will not be added in database, only in
   * that spaceInst object !!!)
   * @param componentInst component instance to be added
   */
  public ComponentInst getComponentInst(String sComponentName) {
    if (m_alComponentInst.size() != 0)
      for (int nI = 0; nI < m_alComponentInst.size(); nI++)
        if (m_alComponentInst.get(nI).getName().equals(sComponentName))
          return m_alComponentInst.get(nI);

    return null;
  }

  /**
   * Get a component from component list, given its name (WARNING : if more than one component
   * instance match the given name, the first one will be returned)
   * @param componentInst component instance to be added
   */
  public ComponentInst getComponentInst(int nIndex) {
    return m_alComponentInst.get(nIndex);
  }

  /**
   * Get the number of space profiles in that space
   * @return The number of space profiles in that space
   */
  public int getNumSpaceProfileInst() {
    return m_alSpaceProfileInst.size();
  }

  /**
   * Add a space profile in space profile list (WARNING : space profile will not be added in
   * database, only in that spaceInst object !!!)
   * @param spaceProfileInst space profile to be added
   */
  public void addSpaceProfileInst(SpaceProfileInst spaceProfileInst) {
    spaceProfileInst.setSpaceFatherId(getId());
    m_alSpaceProfileInst.add(spaceProfileInst);
  }

  /**
   * Remove a space profile from space profile list (WARNING : space profile will not be removed
   * from database, only from that spaceInst object !!!)
   * @param spaceProfileInst space profile to be removed
   */
  public void deleteSpaceProfileInst(SpaceProfileInst spaceProfileInst) {
    for (int nI = 0; nI < m_alSpaceProfileInst.size(); nI++) {
      SpaceProfileInst profile = m_alSpaceProfileInst.get(nI);
      if (profile.getId().equals(spaceProfileInst.getId()))
        m_alSpaceProfileInst.remove(nI);
    }
  }

  /**
   * Get all the space profiles from space profile list
   * @return The space profiles of that space
   */
  public ArrayList<SpaceProfileInst> getAllSpaceProfilesInst() {
    return m_alSpaceProfileInst;
  }

  /**
   * Remove all space profiles from space profiles list (WARNING : space profiles will not be
   * removed from database, only from that spaceInst object !!!)
   */
  public void removeAllSpaceProfilesInst() {
    m_alSpaceProfileInst = new ArrayList<SpaceProfileInst>();
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

  private SpaceProfileInst getSpaceProfileInst(String sSpaceProfileName,
      boolean inherited) {
    for (int nI = 0; nI < m_alSpaceProfileInst.size(); nI++) {
      SpaceProfileInst profile = m_alSpaceProfileInst.get(nI);
      if (profile.isInherited() == inherited
          && profile.getName().equals(sSpaceProfileName))
        return profile;
    }
    return null;
  }

  /**
   * Get a space profile from space profiles list, given its name (WARNING : if more than one space
   * profile match the given name, the first one will be returned)
   * @param nIndex position of requested space profile in space profile list
   */
  public SpaceProfileInst getSpaceProfileInst(int nIndex) {
    return m_alSpaceProfileInst.get(nIndex);
  }

  public List<SpaceProfileInst> getInheritedProfiles() {
    List<SpaceProfileInst> profiles = new ArrayList<SpaceProfileInst>();
    for (int nI = 0; nI < m_alSpaceProfileInst.size(); nI++) {
      SpaceProfileInst profile = m_alSpaceProfileInst.get(nI);
      if (profile.isInherited())
        profiles.add(profile);
    }

    return profiles;
  }

  public List<SpaceProfileInst> getProfiles() {
    List<SpaceProfileInst> profiles = new ArrayList<SpaceProfileInst>();
    for (int nI = 0; nI < m_alSpaceProfileInst.size(); nI++) {
      SpaceProfileInst profile = m_alSpaceProfileInst.get(nI);
      if (!profile.isInherited())
        profiles.add(profile);
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
    return (!StringUtil.isDefined(getDomainFatherId()) || "0"
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
    return isInheritanceBlocked;
  }

  public void setInheritanceBlocked(boolean isInheritanceBlocked) {
    this.isInheritanceBlocked = isInheritanceBlocked;
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
}