/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.service.cache.TreeCache;
import org.silverpeas.core.admin.space.SpaceI18N;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.space.SpaceProfileInstManager;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.space.dao.SpaceDAO;
import org.silverpeas.core.admin.persistence.AdminPersistenceException;
import org.silverpeas.core.admin.persistence.SpaceI18NRow;
import org.silverpeas.core.admin.persistence.SpaceRow;
import org.silverpeas.core.admin.space.notification.SpaceEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.i18n.I18NHelper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Singleton
public class SpaceInstManager {

  @Inject
  private ComponentInstManager componentInstManager;
  @Inject
  private SpaceProfileInstManager spaceProfileInstManager;
  @Inject
  private SpaceEventNotifier notifier;

  public SpaceInstManager() {
  }

  /**
   * Return a copy of space instance
   */
  public SpaceInst copy(SpaceInst spaceInstToCopy) {
    if (spaceInstToCopy == null) {
      return null;
    }

    SpaceInst spaceInst = new SpaceInst();
    spaceInst.setLevel(spaceInstToCopy.getLevel());
    spaceInst.setLocalId(spaceInstToCopy.getLocalId());
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
    spaceInst.setSubSpaces(spaceInstToCopy.getSubSpaces());

    // Create a copy of components
    for (int nI = 0; nI < spaceInstToCopy.getNumComponentInst(); nI++) {
      spaceInst.addComponentInst(componentInstManager.copy(spaceInstToCopy.getComponentInst(nI)));
    }

    // Create a copy of space profiles
    for (int nI = 0; nI < spaceInstToCopy.getNumSpaceProfileInst(); nI++) {
      spaceInst.addSpaceProfileInst(spaceInstToCopy.getSpaceProfileInst(nI));
    }

    spaceInst.setLanguage(spaceInstToCopy.getLanguage());

    // Create a copy of space translations
    for (SpaceI18N translation : spaceInstToCopy.getTranslations().values()) {
      spaceInst.addTranslation(translation);
    }
    spaceInst.setDisplaySpaceFirst(spaceInstToCopy.isDisplaySpaceFirst());
    spaceInst.setPersonalSpace(spaceInstToCopy.isPersonalSpace());

    return spaceInst;
  }

  /**
   * Create a new space in database
   */
  public void createSpaceInst(SpaceInst spaceInst,
      DomainDriverManager ddManager) throws AdminException {
    try {
      // Check if the new space to add is valid
      this.isValidSpace(spaceInst);

      // Convert space instance to SpaceRow
      SpaceRow newSpaceRow = this.makeSpaceRow(spaceInst);

      // Create the space node
      ddManager.getOrganization().space.createSpace(newSpaceRow);
      spaceInst.setLocalId(newSpaceRow.id);
      notifier.notifyEventOn(ResourceEvent.Type.CREATION, spaceInst);

      // duplicates existing translations
      Map<String, SpaceI18N> translations = spaceInst.getTranslations();
      for (String lang : translations.keySet()) {
        if (!lang.equals(newSpaceRow.lang)) {
          // default language stored in main table must not be stored in i18n table
          SpaceI18N translation = (SpaceI18N) translations.get(lang);
          SpaceI18NRow row =
              new SpaceI18NRow(newSpaceRow.id, lang, translation.getName(),
                  translation.getDescription());
          ddManager.getOrganization().spaceI18N.createTranslation(row);
        }
      }

      // Create the SpaceProfile nodes
      for (int nI = 0; nI < spaceInst.getNumSpaceProfileInst(); nI++) {
        spaceProfileInstManager.createSpaceProfileInst(spaceInst.getSpaceProfileInst(nI),
            ddManager, spaceInst.getLocalId());
      }
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.createSpaceInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_SPACE", "space name : '"
          + spaceInst.getName() + "'", e);
    }
  }

  /**
   * Get the space instance with the given space id
   *
   * @param spaceInstLocalId driver space id
   * @return Space information as SpaceInst object
   */
  public SpaceInst getSpaceInstById(DomainDriverManager ddManager, int spaceInstLocalId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      // Load the space detail
      SpaceRow space = ddManager.getOrganization().space.getSpace(spaceInstLocalId);

      if (space == null) {
        SilverTrace.error("admin", "SpaceInstManager.getSpaceInstById",
            "admin.EX_ERR_SPACE_NOT_FOUND", "spaceId = " + spaceInstLocalId);
        return null;
      }

      // Set the attributes of the space Inst
      SpaceInst spaceInst = spaceRow2SpaceInst(space);
      // Get the sub spaces
      List<SpaceRow> asSubSpaces = ddManager.getOrganization().space.getDirectSubSpaces(
          spaceInstLocalId);
      List<SpaceInst> spaceInsts = new ArrayList<>(asSubSpaces.size());
      for (SpaceRow spaceRow: asSubSpaces) {
        spaceInsts.add(spaceRow2SpaceInst(spaceRow));
      }
      spaceInst.setSubSpaces(spaceInsts);

      // Get the components
      String[] asCompoIds =
          ddManager.getOrganization().instance.getAllComponentInstanceIdsInSpace(
              spaceInstLocalId);

      // Insert the componentsInst in the spaceInst
      if (asCompoIds != null) {
        for (String componentId : asCompoIds) {
          ComponentInst componentInst = componentInstManager.getComponentInst(
              ddManager, idAsInt(componentId), spaceInstLocalId);
          WAComponent.get(componentInst.getName())
              .ifPresent(waComponent -> spaceInst.addComponentInst(componentInst));
        }
      }

      // Get the space profiles
      String[] asProfIds =
          ddManager.getOrganization().spaceUserRole.getAllSpaceUserRoleIdsOfSpace(
              spaceInstLocalId);

      // Insert the spaceProfilesInst in the spaceInst
      if (asProfIds != null) {
        for (String profileId : asProfIds) {
          SpaceProfileInst spaceProfileInst =
              spaceProfileInstManager.getSpaceProfileInst(ddManager, profileId, spaceInstLocalId);
          spaceInst.addSpaceProfileInst(spaceProfileInst);
        }
      }

      spaceInst.setLanguage(space.lang);

      // Add default translation
      SpaceI18N translation = new SpaceI18N(space.lang, space.name,
          space.description);
      spaceInst.addTranslation(translation);

      List<SpaceI18NRow> translations =
          ddManager.getOrganization().spaceI18N.getTranslations(spaceInstLocalId);
      for (int t = 0; translations != null && t < translations.size(); t++) {
        SpaceI18NRow row = translations.get(t);
        spaceInst.addTranslation(new SpaceI18N(row));
      }

      return spaceInst;
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getSpaceInstById",
          SilverpeasException.ERROR, "admin.EX_ERR_SET_SPACE", "spaceId : '"
          + spaceInstLocalId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public SpaceInst getPersonalSpace(DomainDriverManager ddManager, String userId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      // Load the space detail
      SpaceRow space = ddManager.getOrganization().space.getPersonalSpace(userId);

      if (space != null) {
        SpaceInst spaceInst = spaceRow2SpaceInst(space);

        // Get the components
        String[] asCompoIds =
            ddManager.getOrganization().instance
            .getAllComponentInstanceIdsInSpace(spaceInst.getLocalId());

        // Insert the componentsInst in the spaceInst
        for (int nI = 0; asCompoIds != null && nI < asCompoIds.length; nI++) {
          ComponentInst componentInst = componentInstManager.getComponentInst(
              ddManager, idAsInt(asCompoIds[nI]), spaceInst.getLocalId());
          WAComponent.get(componentInst.getName())
              .ifPresent(waComponent -> spaceInst.addComponentInst(componentInst));
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

  private SpaceInst spaceRow2SpaceInst(SpaceRow space) {
    // Set the attributes of the space Inst
    SpaceInst spaceInst = new SpaceInst();
    spaceInst.setLocalId(space.id);
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

    return spaceInst;
  }

  /**
   * Get the space instance with the given space id
   *
   * @param spaceLocalId driver space id
   * @return Space information as SpaceInst object
   */
  public SpaceInstLight getSpaceInstLightById(DomainDriverManager ddManager,
      int spaceLocalId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Load the space detail
      SpaceRow spaceRow = ddManager.getOrganization().space.getSpace(spaceLocalId);
      if (spaceRow == null) {
        return null;
      }

      SpaceInstLight spaceInstLight = new SpaceInstLight(spaceRow);

      // Add level
      spaceInstLight.setLevel(getSpaceLevel(spaceRow.id));

      // Add translations
      setTranslations(ddManager, spaceInstLight, spaceRow);

      return spaceInstLight;
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getSpaceInstLightById",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE", "space Id = "
          + spaceLocalId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /*
   * Updates space in Silverpeas
   */
  public void updateSpaceOrder(DomainDriverManager ddManager, int spaceLocalId,
      int orderNum) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.getOrganization().space.updateSpaceOrder(spaceLocalId, orderNum);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.updateSpaceOrder",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACE",
          "space Id : '" + spaceLocalId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  private int getSpaceLevel(int spaceId) {
    return TreeCache.getSpaceLevel(spaceId);
  }

  /**
   * Return the all the root spaces ids available in Silverpeas
   */
  public String[] getAllRootSpaceIds(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asSpaceIds = ddManager.getOrganization().space.getAllRootSpaceIds();
      if (asSpaceIds != null) {
        return asSpaceIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
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
      String[] asSpaceIds = ddManager.getOrganization().space.getAllSpaceIds();
      if (asSpaceIds != null) {
        return asSpaceIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
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
      SpaceRow[] spaceRows = ddManager.getOrganization().space.getRemovedSpaces();

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
   *
   * @return a List of SpaceInstLight
   */
  public List<SpaceInstLight> getSubSpaces(DomainDriverManager ddManager, int spaceLocalId) throws
      AdminException {
    try {
      ddManager.getOrganizationSchema();
      List<SpaceRow> rows = ddManager.getOrganization().space.getDirectSubSpaces(spaceLocalId);

      return spaceRows2SpaceInstLights(ddManager, rows.toArray(new SpaceRow[0]));

    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getSubSpaces",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SUBSPACES", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public List<Integer> getRootSpaceIds() throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      return SpaceDAO.getRootSpaceIds(con);

    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getRootSpaceIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ROOTSPACEIDS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private void setTranslations(DomainDriverManager ddManager, SpaceInstLight space, SpaceRow row) {
    try {
      space.setLanguage(row.lang);
      // Add default translation
      SpaceI18N translation = new SpaceI18N(row.lang, row.name, row.description);
      space.addTranslation(translation);

      if (I18NHelper.isI18nContentActivated) {
        List<SpaceI18NRow> translations =
            ddManager.getOrganization().spaceI18N.getTranslations(row.id);
        for (int t = 0; translations != null && t < translations.size(); t++) {
          SpaceI18NRow i18nRow = translations.get(t);
          space.addTranslation(new SpaceI18N(i18nRow));
        }
      }
    } catch (AdminPersistenceException e) {
      e.printStackTrace();
    }
  }

  private List<SpaceInstLight> spaceRows2SpaceInstLights(DomainDriverManager ddManager,
      SpaceRow[] spaceRows) {
    List<SpaceInstLight> spaces = new ArrayList<SpaceInstLight>();
    if (spaceRows == null) {
      return spaces;
    }
    for (SpaceRow row : spaceRows) {
      SpaceInstLight spaceLight = new SpaceInstLight(row);
      setTranslations(ddManager, spaceLight, row);
      spaces.add(spaceLight);
    }
    return spaces;
  }

  /**
   * Get all the space profiles of a space
   */
  public String[] getAllSpaceProfileIds(DomainDriverManager ddManager,
      int spaceLocalId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asSpaceProfileIds = ddManager.getOrganization().spaceUserRole.
          getAllSpaceUserRoleIdsOfSpace(spaceLocalId);
      if (asSpaceProfileIds != null) {
        return asSpaceProfileIds;
      } else {
        return ArrayUtil.EMPTY_STRING_ARRAY;
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
      int sDomainFatherId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asSpaceIds = ddManager.getOrganization().space.getDirectSubSpaceIds(sDomainFatherId);
      if (asSpaceIds != null) {
        return asSpaceIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
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
      ddManager.getOrganization().spaceI18N.removeTranslations(spaceInst.getLocalId());

      // delete the space node
      ddManager.getOrganization().space.removeSpace(spaceInst.getLocalId());
      notifier.notifyEventOn(ResourceEvent.Type.DELETION, spaceInst);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.deleteSpaceInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_SPACE",
          "space Id : '" + spaceInst.getLocalId() + "'", e);
    }
  }

  /*
   * Delete space from Silverpeas
   */
  public void sendSpaceToBasket(DomainDriverManager ddManager, SpaceInst spaceInst, String userId)
      throws AdminException {
    // Find a name which is not in concurrency with a previous deleted space
    boolean nameOK = false;
    int retry = 0;
    String deletedSpaceName = null;
    while (!nameOK) {
      deletedSpaceName = spaceInst.getName() + Admin.basketSuffix;
      if (retry > 0) {
        deletedSpaceName += " " + retry;
      }
      boolean spaceAlreadyExists = ddManager.getOrganization().space
          .isSpaceIntoBasket(idAsInt(spaceInst.getDomainFatherId()), deletedSpaceName);
      nameOK = !spaceAlreadyExists;
      retry++;
    }

    // Set space into basket with a unique name
    try {
      ddManager.getOrganization().space
          .sendSpaceToBasket(spaceInst.getLocalId(), deletedSpaceName, userId);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.sendSpaceToBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_SEND_SPACE_INTO_BASKET",
          "spaceId = " + spaceInst.getId(), e);
    }
  }

  public void removeSpaceFromBasket(DomainDriverManager ddManager,
      int spaceLocalId) throws AdminException {
    try {
      ddManager.getOrganization().space.removeSpaceFromBasket(spaceLocalId);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.removeSpaceFromBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_RESTORE_SPACE_FROM_BASKET",
          "spaceId = " + spaceLocalId, e);
    }
  }

  /*
   * Move space from current location to space defined by fatherId
   */
  public void moveSpace(DomainDriverManager ddManager, int spaceId, int fatherId)
      throws AdminException {
    try {
      ddManager.getOrganization().space.moveSpace(spaceId, fatherId);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.moveSpace",
          SilverpeasException.ERROR, "admin.EX_ERR_MOVING_SPACE",
          "spaceId= " + spaceId + ", fatherId=" + fatherId, e);
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
      changedSpace.id = spaceInstNew.getLocalId();

      SpaceRow oldSpace = ddManager.getOrganization().space.getSpace(changedSpace.id);
      if (spaceInstNew.isRemoveTranslation()) {
        if (oldSpace.lang.equalsIgnoreCase(spaceInstNew.getLanguage())) {
          List<SpaceI18NRow> translations = ddManager.getOrganization().spaceI18N.getTranslations(
              changedSpace.id);

          if (translations != null && translations.size() > 0) {
            SpaceI18NRow translation = translations.get(0);

            changedSpace.lang = translation.lang;
            changedSpace.name = translation.name;
            changedSpace.description = translation.description;

            ddManager.getOrganization().space.updateSpace(changedSpace);

            ddManager.getOrganization().spaceI18N.removeTranslation(translation.id);
          }
        } else {
          ddManager.getOrganization().spaceI18N.removeTranslation(Integer.parseInt(spaceInstNew.
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

              ddManager.getOrganization().spaceI18N.updateTranslation(row);
            } else {
              ddManager.getOrganization().spaceI18N.createTranslation(row);
            }

            changedSpace.lang = oldSpace.lang;
            changedSpace.name = oldSpace.name;
            changedSpace.description = oldSpace.description;
          }
        }

        ddManager.getOrganization().space.updateSpace(changedSpace);
      }
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.updateSpaceInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACE",
          "space Id : '" + spaceInstNew.getLocalId() + "'", e);
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
          "space Id : '" + spaceInst.getLocalId() + "'");
    }
  }

  /**
   * Tests if a space with given space id exists
   *
   * @param ddManager
   * @param spaceLocalId
   * @return true if the given space instance name is an existing space.
   * @throws AdminException
   */
  public boolean isSpaceInstExist(DomainDriverManager ddManager, int spaceLocalId) throws
      AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.getOrganization().space.isSpaceInstExist(spaceLocalId);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.isSpaceInstExist",
          SilverpeasException.ERROR, "admin.EX_ERR_IS_SPACE_EXIST",
          "space Id : '" + spaceLocalId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public List<SpaceInstLight> getAllSpaces(DomainDriverManager ddManager) throws AdminException {
    List<SpaceInstLight> spaces = new ArrayList<SpaceInstLight>();
    try {
      ddManager.getOrganizationSchema();
      SpaceRow[] spaceRows = ddManager.getOrganization().space.getAllSpaces();
      spaces.addAll(spaceRows2SpaceInstLights(ddManager, spaceRows));
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getAllSpaces",
          SilverpeasException.ERROR, "admin.EX_GETTING_ALL_SPACES", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
    return spaces;
  }

  public List<Integer> getManageableSpaceIds(String userId, List<String> groupIds)
      throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
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
    space.id = spaceInst.getLocalId();
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
