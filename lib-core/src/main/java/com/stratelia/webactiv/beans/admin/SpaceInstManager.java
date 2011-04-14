/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.i18n.Translation;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.cache.TreeCache;
import com.stratelia.webactiv.beans.admin.dao.SpaceDAO;
import com.stratelia.webactiv.organization.AdminPersistenceException;
import com.stratelia.webactiv.organization.SpaceI18NRow;
import com.stratelia.webactiv.organization.SpaceRow;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class SpaceInstManager {

  static ComponentInstManager m_ComponentInstManager = new ComponentInstManager();
  static SpaceProfileInstManager m_SpaceProfileInstManager = new SpaceProfileInstManager();

  /**
   * Constructor
   */
  public SpaceInstManager() {
  }

  /**
   * Return a copy of space instance
   */
  public SpaceInst copy(SpaceInst spaceInstToCopy) {
    SpaceInst spaceInst = new SpaceInst();
    spaceInst.setLevel(spaceInstToCopy.getLevel());
    spaceInst.setId(spaceInstToCopy.getId());
    spaceInst.setDomainFatherId(spaceInstToCopy.getDomainFatherId());
    spaceInst.setName(spaceInstToCopy.getName());
    spaceInst.setDescription(spaceInstToCopy.getDescription());
    spaceInst.setCreatorUserId(spaceInstToCopy.getCreatorUserId());
    spaceInst.setFirstPageType(spaceInstToCopy.getFirstPageType());
    spaceInst.setFirstPageExtraParam(spaceInstToCopy.getFirstPageExtraParam());
    spaceInst.setOrderNum(spaceInstToCopy.getOrderNum());
    spaceInst.setCreateDate(spaceInstToCopy.getCreateDate());
    spaceInst.setUpdateDate(spaceInstToCopy.getUpdateDate());
    spaceInst.setRemoveDate(spaceInstToCopy.getRemoveDate());
    spaceInst.setStatus(spaceInstToCopy.getStatus());
    spaceInst.setUpdaterUserId(spaceInstToCopy.getUpdaterUserId());
    spaceInst.setRemoverUserId(spaceInstToCopy.getRemoverUserId());
    spaceInst.setInheritanceBlocked(spaceInstToCopy.isInheritanceBlocked());
    spaceInst.setLook(spaceInstToCopy.getLook());

    // Create a copy of array of subspaces ids
    String[] asSubSpaceIdsToCopy = spaceInstToCopy.getSubSpaceIds();
    String[] asSubSpaceIds = new String[asSubSpaceIdsToCopy.length];
    System.arraycopy(asSubSpaceIdsToCopy, 0, asSubSpaceIds, 0, asSubSpaceIdsToCopy.length);

    spaceInst.setSubSpaceIds(asSubSpaceIds);

    // Create a copy of components
    for (int nI = 0; nI < spaceInstToCopy.getNumComponentInst(); nI++) {
      spaceInst.addComponentInst(m_ComponentInstManager.copy(spaceInstToCopy.getComponentInst(nI)));
    }

    // Create a copy of space profiles
    for (int nI = 0; nI < spaceInstToCopy.getNumSpaceProfileInst(); nI++) {
      spaceInst.addSpaceProfileInst(spaceInstToCopy.getSpaceProfileInst(nI));
    }

    spaceInst.setLanguage(spaceInstToCopy.getLanguage());

    // Create a copy of space translations
    Iterator<Translation> translations = spaceInstToCopy.getTranslations().values().iterator();
    while (translations.hasNext()) {
      spaceInst.addTranslation(translations.next());
    }
    spaceInst.setDisplaySpaceFirst(spaceInstToCopy.isDisplaySpaceFirst());
    spaceInst.setPersonalSpace(spaceInstToCopy.isPersonalSpace());

    return spaceInst;
  }

  /**
   * Create a new space in database
   */
  public String createSpaceInst(SpaceInst spaceInst,
      DomainDriverManager ddManager) throws AdminException {
    try {
      // Check if the new space to add is valid
      this.isValidSpace(spaceInst);

      // Convert space instance to SpaceRow
      SpaceRow newSpaceRow = this.makeSpaceRow(spaceInst);

      // Create the space node
      String sSpaceNodeId = "";
      ddManager.organization.space.createSpace(newSpaceRow);
      sSpaceNodeId = idAsString(newSpaceRow.id);

      // Create the SpaceProfile nodes
      for (int nI = 0; nI < spaceInst.getNumSpaceProfileInst(); nI++) {
        m_SpaceProfileInstManager.createSpaceProfileInst(spaceInst.getSpaceProfileInst(nI),
            ddManager, sSpaceNodeId);
      }

      return sSpaceNodeId;
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.createSpaceInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_SPACE", "space name : '"
          + spaceInst.getName() + "'", e);
    }
  }

  /**
   * Get the space instance with the given space id
   * @param sSpaceInstId driver space id
   * @return Space information as SpaceInst object
   */
  public SpaceInst getSpaceInstById(DomainDriverManager ddManager,
      String sSpaceInstId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      SpaceInst spaceInst = new SpaceInst();
      spaceInst.removeAllComponentsInst();
      this.setSpaceInstById(ddManager, spaceInst, sSpaceInstId);
      return spaceInst;
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getSpaceInstById",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE", "space Id : '"
          + sSpaceInstId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public SpaceInst getPersonalSpace(DomainDriverManager ddManager, String userId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Load the space detail
      SpaceRow space = ddManager.organization.space.getPersonalSpace(userId);

      if (space != null) {
        SpaceInst spaceInst = new SpaceInst();

        // Set the attributes of the space Inst
        spaceInst.setId(idAsString(space.id));
        spaceInst.setDomainFatherId(idAsString(space.domainFatherId));
        spaceInst.setLevel(getSpaceLevel(space.id));
        spaceInst.setName(space.name);
        spaceInst.setDescription(space.description);
        spaceInst.setCreatorUserId(idAsString(space.createdBy));
        spaceInst.setFirstPageType(space.firstPageType);
        spaceInst.setFirstPageExtraParam(space.firstPageExtraParam);
        spaceInst.setOrderNum(space.orderNum);

        if (space.createTime != null) {
          spaceInst.setCreateDate(new Date(Long.parseLong(space.createTime)));
        }
        if (space.updateTime != null) {
          spaceInst.setUpdateDate(new Date(Long.parseLong(space.updateTime)));
        }
        if (space.removeTime != null) {
          spaceInst.setRemoveDate(new Date(Long.parseLong(space.removeTime)));
        }

        spaceInst.setUpdaterUserId(idAsString(space.updatedBy));
        spaceInst.setRemoverUserId(idAsString(space.removedBy));

        spaceInst.setStatus(space.status);

        spaceInst.setInheritanceBlocked(space.inheritanceBlocked == 1);
        spaceInst.setLook(space.look);

        spaceInst.setDisplaySpaceFirst(space.displaySpaceFirst == 1);
        spaceInst.setPersonalSpace(space.isPersonalSpace == 1);

        // Get the components
        String[] asCompoIds = ddManager.organization.instance.getAllComponentInstanceIdsInSpace(idAsInt(spaceInst.
            getId()));

        // Insert the componentsInst in the spaceInst
        for (int nI = 0; asCompoIds != null && nI < asCompoIds.length; nI++) {
          ComponentInst componentInst = m_ComponentInstManager.getComponentInst(
              ddManager, asCompoIds[nI], spaceInst.getId());
          spaceInst.addComponentInst(componentInst);
        }

        spaceInst.setLanguage(space.lang);
        return spaceInst;
      }
      return null;
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getPersonalSpace",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PERSONAL_SPACE", "userId = " + userId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the space instance with the given space id
   * @param sSpaceInstId driver space id
   * @return Space information as SpaceInst object
   */
  public SpaceInstLight getSpaceInstLightById(DomainDriverManager ddManager,
      String spaceId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Load the space detail
      SpaceRow spaceRow = ddManager.organization.space.getSpace(idAsInt(spaceId));

      SpaceInstLight spaceInstLight = new SpaceInstLight(spaceRow);

      // Add level
      spaceInstLight.setLevel(getSpaceLevel(spaceRow.id));

      // Add translations
      setTranslations(ddManager, spaceInstLight, spaceRow);

      return spaceInstLight;
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getSpaceInstLightById",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE", "space Id = "
          + spaceId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /*
   * Updates space in Silverpeas
   */
  public void updateSpaceOrder(DomainDriverManager ddManager, String sSpaceId,
      int orderNum) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.organization.space.updateSpaceOrder(idAsInt(sSpaceId), orderNum);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.updateSpaceOrder",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACE",
          "space Id : '" + sSpaceId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public void updateSpaceInheritance(DomainDriverManager ddManager,
      String sSpaceId, boolean inheritanceBlocked) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.organization.space.updateSpaceInheritance(idAsInt(sSpaceId),
          inheritanceBlocked);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.updateSpaceInheritance",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACE_INHERITANCE",
          "Component Id : '" + sSpaceId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  private int getSpaceLevel(int spaceId)
      throws AdminException {
    return TreeCache.getSpaceLevel(Integer.toString(spaceId));
  }

  /**
   * Set the space instance associated to the given space inst id
   */
  private void setSpaceInstById(DomainDriverManager ddManager,
      SpaceInst spaceInst, String sSpaceInstId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Load the space detail
      SpaceRow space = ddManager.organization.space.getSpace(idAsInt(sSpaceInstId));

      // Set the attributes of the space Inst
      spaceInst.setId(idAsString(space.id));
      spaceInst.setDomainFatherId(idAsString(space.domainFatherId));
      int spaceLevel = -1;
      if (spaceInst.isRoot()) {
        spaceLevel = 0;
      } else {
        spaceLevel = getSpaceLevel(space.domainFatherId) + 1;
      }
      spaceInst.setLevel(spaceLevel);
      spaceInst.setName(space.name);
      spaceInst.setDescription(space.description);
      spaceInst.setCreatorUserId(idAsString(space.createdBy));
      spaceInst.setFirstPageType(space.firstPageType);
      spaceInst.setFirstPageExtraParam(space.firstPageExtraParam);
      spaceInst.setOrderNum(space.orderNum);

      if (space.createTime != null) {
        spaceInst.setCreateDate(new Date(Long.parseLong(space.createTime)));
      }
      if (space.updateTime != null) {
        spaceInst.setUpdateDate(new Date(Long.parseLong(space.updateTime)));
      }
      if (space.removeTime != null) {
        spaceInst.setRemoveDate(new Date(Long.parseLong(space.removeTime)));
      }

      spaceInst.setUpdaterUserId(idAsString(space.updatedBy));
      spaceInst.setRemoverUserId(idAsString(space.removedBy));

      spaceInst.setStatus(space.status);

      spaceInst.setInheritanceBlocked(space.inheritanceBlocked == 1);
      spaceInst.setLook(space.look);

      spaceInst.setDisplaySpaceFirst(space.displaySpaceFirst == 1);
      spaceInst.setPersonalSpace(space.isPersonalSpace == 1);

      // Get the sub spaces
      String[] asSubSpaceIds = ddManager.organization.space.getDirectSubSpaceIds(idAsInt(
          sSpaceInstId));
      spaceInst.setSubSpaceIds(asSubSpaceIds);

      // Get the components
      String[] asCompoIds = ddManager.organization.instance.getAllComponentInstanceIdsInSpace(idAsInt(
          sSpaceInstId));

      // Insert the componentsInst in the spaceInst
      for (int nI = 0; asCompoIds != null && nI < asCompoIds.length; nI++) {
        ComponentInst componentInst = m_ComponentInstManager.getComponentInst(
            ddManager, asCompoIds[nI], sSpaceInstId);
        spaceInst.addComponentInst(componentInst);
      }

      // Get the space profiles
      String[] asProfIds = ddManager.organization.spaceUserRole.getAllSpaceUserRoleIdsOfSpace(idAsInt(
          sSpaceInstId));

      // Insert the spaceProfilesInst in the spaceInst
      for (int nI = 0; asProfIds != null && nI < asProfIds.length; nI++) {
        SpaceProfileInst spaceProfileInst = m_SpaceProfileInstManager.getSpaceProfileInst(ddManager,
            asProfIds[nI], sSpaceInstId);
        spaceInst.addSpaceProfileInst(spaceProfileInst);
      }

      spaceInst.setLanguage(space.lang);

      // Add default translation
      SpaceI18N translation = new SpaceI18N(space.lang, space.name,
          space.description);
      spaceInst.addTranslation(translation);

      List<SpaceI18NRow> translations = ddManager.organization.spaceI18N.getTranslations(idAsInt(
          sSpaceInstId));
      for (int t = 0; translations != null && t < translations.size(); t++) {
        SpaceI18NRow row = translations.get(t);
        spaceInst.addTranslation(new SpaceI18N(row));
      }
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.setSpaceInstById",
          SilverpeasException.ERROR, "admin.EX_ERR_SET_SPACE", "space Id : '"
          + sSpaceInstId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Return the all the root spaces ids available in Silverpeas
   */
  public String[] getAllRootSpaceIds(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asSpaceIds = ddManager.organization.space.getAllRootSpaceIds();
      if (asSpaceIds != null) {
        return asSpaceIds;
      }
      return new String[0];
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getAllSpaceIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_SPACE_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Return the all the spaces ids available in Silverpeas
   */
  public String[] getAllSpaceIds(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asSpaceIds = ddManager.organization.space.getAllSpaceIds();
      if (asSpaceIds != null) {
        return asSpaceIds;
      }
      return new String[0];
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getAllSpaceIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_SPACE_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Returns all components which has been removed but not definitely deleted
   */
  public List<SpaceInstLight> getRemovedSpaces(DomainDriverManager ddManager)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      SpaceRow[] spaceRows = ddManager.organization.space.getRemovedSpaces();

      return spaceRows2SpaceInstLights(ddManager, spaceRows);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getRemovedSpaces",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_REMOVED_SPACES", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Return subspaces of a space
   * @return a List of SpaceInstLight
   */
  public List<SpaceInstLight> getSubSpaces(String spaceId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      return SpaceDAO.getSubSpaces(con, Integer.parseInt(spaceId));

    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getSubSpaces",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SUBSPACES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<String> getRootSpaceIds() throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      return SpaceDAO.getRootSpaceIds(con);

    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getRootSpaceIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ROOTSPACEIDS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private void setTranslations(DomainDriverManager ddManager,
      SpaceInstLight space, SpaceRow row) {
    try {
      space.setLanguage(row.lang);

      // Add default translation
      SpaceI18N translation = new SpaceI18N(row.lang, row.name, row.description);
      space.addTranslation(translation);

      if (I18NHelper.isI18N) {
        List<SpaceI18NRow> translations = ddManager.organization.spaceI18N.getTranslations(row.id);
        for (int t = 0; translations != null && t < translations.size(); t++) {
          SpaceI18NRow i18nRow = translations.get(t);
          space.addTranslation(new SpaceI18N(i18nRow));
        }
      }
    } catch (AdminPersistenceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private List<SpaceInstLight> spaceRows2SpaceInstLights(DomainDriverManager ddManager,
      SpaceRow[] spaceRows) {
    List<SpaceInstLight> spaces = new ArrayList<SpaceInstLight>();
    SpaceInstLight spaceLight = null;
    for (int s = 0; spaceRows != null && s < spaceRows.length; s++) {
      SpaceRow row = spaceRows[s];
      spaceLight = new SpaceInstLight(row);

      setTranslations(ddManager, spaceLight, row);

      spaces.add(spaceLight);
    }
    return spaces;
  }

  /**
   * Get all the space profiles of a space
   */
  public String[] getAllSpaceProfileIds(DomainDriverManager ddManager,
      String sSpaceId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asSpaceProfileIds = ddManager.organization.spaceUserRole.
          getAllSpaceUserRoleIdsOfSpace(idAsInt(sSpaceId));
      if (asSpaceProfileIds != null) {
        return asSpaceProfileIds;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getAllSpaceProfileIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_SPACE_PROFILE_IDS",
          e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /*
   * Get all the spaces ids available in Silverpeas
   */
  public String[] getAllSubSpaceIds(DomainDriverManager ddManager,
      String sDomainFatherId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asSpaceIds = ddManager.organization.space.getDirectSubSpaceIds(idAsInt(
          sDomainFatherId));
      if (asSpaceIds != null) {
        return asSpaceIds;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getAllSubSpaceIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_SUBSPACE_IDS",
          " father space Id : '" + sDomainFatherId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /*
   * Delete space from Silverpeas
   */
  public void deleteSpaceInst(SpaceInst spaceInst, DomainDriverManager ddManager)
      throws AdminException {
    try {
      // delete translations
      ddManager.organization.spaceI18N.removeTranslations(idAsInt(spaceInst.getId()));

      // delete the space node
      ddManager.organization.space.removeSpace(idAsInt(spaceInst.getId()));
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.deleteSpaceInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_SPACE",
          "space Id : '" + spaceInst.getId() + "'", e);
    }
  }

  /*
   * Delete space from Silverpeas
   */
  public void sendSpaceToBasket(DomainDriverManager ddManager, String spaceId,
      String newSpaceName, String userId) throws AdminException {
    try {
      ddManager.organization.space.sendSpaceToBasket(idAsInt(spaceId),
          newSpaceName, userId);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.sendSpaceToBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_SEND_SPACE_TO_BASKET",
          "spaceId = " + spaceId, e);
    }
  }

  public void removeSpaceFromBasket(DomainDriverManager ddManager,
      String spaceId) throws AdminException {
    try {
      ddManager.organization.space.removeSpaceFromBasket(idAsInt(spaceId));
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.removeSpaceFromBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_RESTORE_SPACE_FROM_BASKET",
          "spaceId = " + spaceId, e);
    }
  }

  /*
   * Updates space in Silverpeas
   */
  public void updateSpaceInst(DomainDriverManager ddManager,
      SpaceInst spaceInstNew) throws AdminException {
    try {
      // Check that the given space is valid
      this.isValidSpace(spaceInstNew);

      SpaceRow changedSpace = makeSpaceRow(spaceInstNew);
      changedSpace.id = idAsInt(spaceInstNew.getId());

      SpaceRow oldSpace = ddManager.organization.space.getSpace(changedSpace.id);

      SilverTrace.debug("admin",
          this.getClass().getName() + ".updateSpaceInst",
          "root.MSG_GEN_PARAM_VALUE", "remove = "
          + spaceInstNew.isRemoveTranslation() + ", translationId = "
          + spaceInstNew.getTranslationId());

      if (spaceInstNew.isRemoveTranslation()) {
        if (oldSpace.lang.equalsIgnoreCase(spaceInstNew.getLanguage())) {
          List<SpaceI18NRow> translations = ddManager.organization.spaceI18N.getTranslations(
              changedSpace.id);

          if (translations != null && translations.size() > 0) {
            SpaceI18NRow translation = translations.get(0);

            changedSpace.lang = translation.lang;
            changedSpace.name = translation.name;
            changedSpace.description = translation.description;

            ddManager.organization.space.updateSpace(changedSpace);

            ddManager.organization.spaceI18N.removeTranslation(translation.id);
          }
        } else {
          ddManager.organization.spaceI18N.removeTranslation(Integer.parseInt(spaceInstNew.
              getTranslationId()));
        }
      } else {
        if (changedSpace.lang != null) {
          if (oldSpace.lang == null) {
            // translation for the first time
            oldSpace.lang = I18NHelper.defaultLanguage;
          }
          if (!oldSpace.lang.equalsIgnoreCase(changedSpace.lang)) {
            SpaceI18NRow row = new SpaceI18NRow(changedSpace);
            String translationId = spaceInstNew.getTranslationId();
            if (translationId != null && !translationId.equals("-1")) {
              // update translation
              row.id = Integer.parseInt(spaceInstNew.getTranslationId());

              ddManager.organization.spaceI18N.updateTranslation(row);
            } else {
              ddManager.organization.spaceI18N.createTranslation(row);
            }

            changedSpace.lang = oldSpace.lang;
            changedSpace.name = oldSpace.name;
            changedSpace.description = oldSpace.description;
          }
        }

        ddManager.organization.space.updateSpace(changedSpace);
      }
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.updateSpaceInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACE",
          "space Id : '" + spaceInstNew.getId() + "'", e);
    }
  }

  /**
   * Check if the given space to add is valid
   */
  private void isValidSpace(SpaceInst spaceInst) throws AdminException {
    String sError = "";

    // Check the minimum configuration (no requirements on description)
    if (spaceInst.getName().length() < 1) {
      sError += "The space name is empty";
    }

    if (sError.length() != 0) {
      throw new AdminException("SpaceInstManager.isValidSpace",
          SilverpeasException.ERROR, "admin.EX_ERR_INVALID_SPACE",
          "space Id : '" + spaceInst.getId() + "'");
    }
  }

  /**
   * Tests if a space with given space id exists
   * @param ddManager
   * @param sSpaceInstId
   * @return true if the given space instance name is an existing space.
   * @throws AdminException
   */
  public boolean isSpaceInstExist(DomainDriverManager ddManager, String sSpaceInstId) throws
      AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.space.isSpaceInstExist(idAsInt(sSpaceInstId));
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.isSpaceInstExist",
          SilverpeasException.ERROR, "admin.EX_ERR_IS_SPACE_EXIST",
          "space Id : '" + sSpaceInstId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public List<SpaceInstLight> getAllSpaces(DomainDriverManager ddManager) throws AdminException {
    List<SpaceInstLight> spaces = new ArrayList<SpaceInstLight>();
    try {
      ddManager.getOrganizationSchema();
      SpaceRow[] spaceRows = ddManager.organization.space.getAllSpaces();
      spaces.addAll(spaceRows2SpaceInstLights(ddManager, spaceRows));
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getAllSpaces",
          SilverpeasException.ERROR, "admin.EX_GETTING_ALL_SPACES", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
    return spaces;
  }

  public List<String> getManageableSpaceIds(String userId, List<String> groupIds)
      throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);
      return SpaceDAO.getManageableSpaceIds(con, userId, groupIds);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getManageableSpaceIds",
          SilverpeasException.ERROR, "admin.EX_GETTING_MANAGEABLE_SPACEIDS", e);
    } finally {
      DBUtil.close(con);
    }

  }

  /**
   * Convert SpaceInst to SpaceRow
   */
  private SpaceRow makeSpaceRow(SpaceInst spaceInst) {
    SpaceRow space = new SpaceRow();
    space.id = idAsInt(spaceInst.getId());
    space.domainFatherId = idAsInt(spaceInst.getDomainFatherId());
    space.name = spaceInst.getName();
    space.description = spaceInst.getDescription();
    space.createdBy = idAsInt(spaceInst.getCreatorUserId());
    space.firstPageType = spaceInst.getFirstPageType();
    space.firstPageExtraParam = spaceInst.getFirstPageExtraParam();
    space.orderNum = spaceInst.getOrderNum();
    space.updatedBy = idAsInt(spaceInst.getUpdaterUserId());
    space.lang = spaceInst.getLanguage();
    space.look = spaceInst.getLook();

    if (spaceInst.isInheritanceBlocked()) {
      space.inheritanceBlocked = 1;
    } else {
      space.inheritanceBlocked = 0;
    }

    if (spaceInst.isDisplaySpaceFirst()) {
      space.displaySpaceFirst = 1;
    } else {
      space.displaySpaceFirst = 0;
    }

    space.isPersonalSpace = 0;
    if (spaceInst.isPersonalSpace()) {
      space.isPersonalSpace = 1;
    }

    return space;
  }

  /**
   * Convert String Id to int Id
   */
  private int idAsInt(String id) {
    if (id == null || id.length() == 0) {
      return -1; // the null id.
    }
    try {
      return Integer.parseInt(id);
    } catch (NumberFormatException e) {
      return -1; // the null id.
    }
  }

  /**
   * Convert int Id to String Id
   */
  static private String idAsString(int id) {
    return String.valueOf(id);
  }
}
