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

import org.silverpeas.core.admin.component.model.ComponentI18N;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.ProfileInstManager;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.component.dao.ComponentDAO;
import org.silverpeas.core.admin.persistence.ComponentInstanceI18NRow;
import org.silverpeas.core.admin.persistence.ComponentInstanceRow;
import org.silverpeas.core.admin.persistence.SpaceRow;
import org.silverpeas.core.admin.component.notification.ComponentInstanceEventNotifier;
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

import static org.silverpeas.core.notification.system.ResourceEvent.Type.UPDATE;

@Singleton
public class ComponentInstManager {

  @Inject
  private ProfileInstManager profileInstManager;
  @Inject
  private ComponentInstanceEventNotifier notifier;

  public ComponentInstManager() {
  }

  /**
   * Return a copy of the given componentInst
   *
   * @param componentInstToCopy
   * @return
   */
  public ComponentInst copy(ComponentInst componentInstToCopy) {
    ComponentInst componentInst = new ComponentInst();
    componentInst.setLocalId(componentInstToCopy.getLocalId());
    componentInst.setName(componentInstToCopy.getName());
    componentInst.setLabel(componentInstToCopy.getLabel());
    componentInst.setDescription(componentInstToCopy.getDescription());
    componentInst.setDomainFatherId(componentInstToCopy.getDomainFatherId());
    componentInst.setOrderNum(componentInstToCopy.getOrderNum());

    componentInst.setCreateDate(componentInstToCopy.getCreateDate());
    componentInst.setUpdateDate(componentInstToCopy.getUpdateDate());
    componentInst.setRemoveDate(componentInstToCopy.getRemoveDate());
    componentInst.setStatus(componentInstToCopy.getStatus());
    componentInst.setCreatorUserId(componentInstToCopy.getCreatorUserId());
    componentInst.setUpdaterUserId(componentInstToCopy.getUpdaterUserId());
    componentInst.setRemoverUserId(componentInstToCopy.getRemoverUserId());

    for (ProfileInst profile : componentInstToCopy.getAllProfilesInst()) {
      componentInst.addProfileInst(profile);
    }

    List<Parameter> parameters = componentInstToCopy.getParameters();
    componentInst.setParameters(parameters);

    componentInst.setLanguage(componentInstToCopy.getLanguage());

    // Create a copy of component translations
    for (ComponentI18N translation : componentInstToCopy.getTranslations().values()) {
      componentInst.addTranslation(translation);
    }

    componentInst.setPublic(componentInstToCopy.isPublic());
    componentInst.setHidden(componentInstToCopy.isHidden());
    componentInst.setInheritanceBlocked(componentInstToCopy.isInheritanceBlocked());

    return componentInst;
  }

  /**
   * Creates a component instance in database
   *
   * @param componentInst
   * @param ddManager
   * @param spaceLocalId
   * @return
   * @throws AdminException
   */
  public void createComponentInst(ComponentInst componentInst, DomainDriverManager ddManager,
      int spaceLocalId) throws AdminException {
    try {
      // Create the component node
      ComponentInstanceRow newInstance = makeComponentInstanceRow(componentInst);
      newInstance.spaceId = spaceLocalId;
      ddManager.getOrganization().instance.createComponentInstance(newInstance);
      componentInst.setLocalId(newInstance.id);

      // duplicates existing translations
      Map<String, ComponentI18N> translations = componentInst.getTranslations();
      for (String lang : translations.keySet()) {
        if (!lang.equals(newInstance.lang)) {
          // default language stored in main table must not be stored in i18n table
          ComponentI18N translation = translations.get(lang);
          ComponentInstanceI18NRow row =
              new ComponentInstanceI18NRow(newInstance.id, lang, translation.getName(),
              translation.getDescription());
          ddManager.getOrganization().instanceI18N.createTranslation(row);
        }
      }

      // Add the parameters if necessary
      List<Parameter> parameters = componentInst.getParameters();

      for (Parameter parameter : parameters) {
        ddManager.getOrganization().instanceData.createInstanceData(
            componentInst.getLocalId(), parameter);
      }

      // Create the profile nodes
      for (ProfileInst profile : componentInst.getProfiles()) {
        profileInstManager.createProfileInst(profile, ddManager, componentInst.getLocalId());
      }

    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.createComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_COMPONENT",
          "component name: '" + componentInst.getName() + "'", e);
    }
  }

  public void sendComponentToBasket(DomainDriverManager ddManager, ComponentInst componentInst,
      String userId) throws AdminException {
    // Find a name which is not in concurrency with a previous deleted component
    boolean nameOK = false;
    int retry = 0;
    String deletedComponentName = null;
    while (!nameOK) {
      deletedComponentName = componentInst.getLabel() + Admin.basketSuffix;
      if (retry > 0) {
        deletedComponentName += " " + retry;
      }
      boolean spaceAlreadyExists = ddManager.getOrganization().instance
          .isComponentIntoBasket(idAsInt(componentInst.getDomainFatherId()), deletedComponentName);
      nameOK = !spaceAlreadyExists;
      retry++;
    }

    // Set component into basket with a unique name
    try {
      ddManager.getOrganization().instance
          .sendComponentToBasket(componentInst.getLocalId(), deletedComponentName, userId);
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.sendComponentToBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_REMOVE_COMPONENT",
          "component name: '" + componentInst.getName() + "'", e);
    }
  }

  public void restoreComponentFromBasket(DomainDriverManager ddManager, int localComponentId) throws
      AdminException {
    try {
      ddManager.getOrganization().instance.restoreComponentFromBasket(localComponentId);
    } catch (Exception e) {
      throw new AdminException(
          "ComponentInstManager.restoreComponentFromBasket",
          SilverpeasException.ERROR,
          "admin.EX_ERR_RESTORE_COMPONENT_FROM_BASKET", "componentId = " + localComponentId, e);
    }
  }

  /**
   * Get component instance with the given id
   *
   * @param ddManager
   * @param localComponentId
   * @param spaceLocalId
   * @return
   * @throws AdminException
   */
  public ComponentInst getComponentInst(DomainDriverManager ddManager, int localComponentId,
      Integer spaceLocalId) throws AdminException {
    Integer fatherLocalId = spaceLocalId;
    if (fatherLocalId == null) {
      try {
        ddManager.getOrganizationSchema();
        SpaceRow space = ddManager.getOrganization().space.getSpaceOfInstance(localComponentId);
        if (space == null) {
          space = new SpaceRow();
        }
        fatherLocalId = space.id;
      } catch (Exception e) {
        throw new AdminException("ComponentInstManager.getComponentInst",
            SilverpeasException.ERROR, "admin.EX_ERR_GET_COMPONENT",
            "component id: '" + localComponentId + "'", e);
      } finally {
        ddManager.releaseOrganizationSchema();
      }
    }

    ComponentInst componentInst = new ComponentInst();
    componentInst.removeAllProfilesInst();
    this.setComponentInst(componentInst, ddManager, localComponentId, fatherLocalId);

    return componentInst;
  }

  /**
   * Return the all the root spaces ids available in Silverpeas
   *
   * @param ddManager
   * @return
   * @throws AdminException
   */
  public List<ComponentInstLight> getRemovedComponents(DomainDriverManager ddManager)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ComponentInstanceRow[] componentRows = ddManager.getOrganization().instance
          .getRemovedComponents();

      return componentInstanceRows2ComponentInstLights(componentRows);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getRemovedSpaces",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_REMOVED_SPACES", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get component instance light with the given id
   *
   * @param ddManager
   * @param compLocalId
   * @return
   * @throws AdminException
   */
  public ComponentInstLight getComponentInstLight(DomainDriverManager ddManager, int compLocalId)
      throws AdminException {
    ComponentInstLight compoLight = null;
    try {
      ddManager.getOrganizationSchema();
      ComponentInstanceRow compo = ddManager.getOrganization().instance.getComponentInstance(
          compLocalId);
      if (compo != null) {
        compoLight = new ComponentInstLight(compo);
        compoLight.setLocalId(compLocalId);
        compoLight.setDomainFatherId("WA" + compoLight.getDomainFatherId());

        // Add default translation
        ComponentI18N translation = new ComponentI18N(compo.lang, compo.name, compo.description);
        compoLight.addTranslation(translation);

        List<ComponentInstanceI18NRow> translations = ddManager.getOrganization().instanceI18N.
            getTranslations(compo.id);
        for (int t = 0; translations != null && t < translations.size(); t++) {
          ComponentInstanceI18NRow row = translations.get(t);
          compoLight.addTranslation(new ComponentI18N(row));
        }
      }
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.getComponentInstName",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_COMPONENT_NAME",
          "component id: '" + compLocalId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
    return compoLight;
  }

  /*
   * Get component instance information with given component id
   */
  public void setComponentInst(ComponentInst componentInst, DomainDriverManager ddManager,
      int compLocalId, int fatherId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Load the component detail
      ComponentInstanceRow instance = ddManager.getOrganization().instance.getComponentInstance(
          compLocalId);

      if (instance != null) {
        // Set the attributes of the component Inst
        componentInst.setLocalId(instance.id);
        componentInst.setName(instance.componentName);
        componentInst.setLabel(instance.name);
        componentInst.setDescription(instance.description);
        componentInst.setDomainFatherId(idAsString(fatherId));
        componentInst.setOrderNum(instance.orderNum);

        if (instance.createTime != null) {
          componentInst.setCreateDate(new Date(Long.parseLong(instance.createTime)));
        }
        if (instance.updateTime != null) {
          componentInst.setUpdateDate(new Date(Long.parseLong(instance.updateTime)));
        }
        if (instance.removeTime != null) {
          componentInst.setRemoveDate(new Date(Long.parseLong(instance.removeTime)));
        }

        componentInst.setCreatorUserId(idAsString(instance.createdBy));
        componentInst.setUpdaterUserId(idAsString(instance.updatedBy));
        componentInst.setRemoverUserId(idAsString(instance.removedBy));

        componentInst.setStatus(instance.status);

        // Get the parameters if any
        List<Parameter> parameters = ddManager.getOrganization().instanceData
            .getAllParametersInComponent(compLocalId);
        componentInst.setParameters(parameters);

        // Get the profiles
        String[] asProfileIds = ddManager.getOrganization().userRole.getAllUserRoleIdsOfInstance(
            componentInst.getLocalId());

        // Insert the profileInst in the componentInst
        for (int nI = 0; asProfileIds != null && nI < asProfileIds.length; nI++) {
          ProfileInst profileInst = profileInstManager.getProfileInst(
              ddManager, asProfileIds[nI]);
          componentInst.addProfileInst(profileInst);
        }

        componentInst.setLanguage(instance.lang);

        // Add default translation
        ComponentI18N translation = new ComponentI18N(instance.lang,
            instance.name, instance.description);
        componentInst.addTranslation(translation);

        List<ComponentInstanceI18NRow> translations = ddManager.getOrganization().instanceI18N
            .getTranslations(instance.id);
        for (int t = 0; translations != null && t < translations.size(); t++) {
          ComponentInstanceI18NRow row = translations.get(t);
          componentInst.addTranslation(new ComponentI18N(row));
        }

        componentInst.setPublic((instance.publicAccess == 1));
        componentInst.setHidden((instance.hidden == 1));
        componentInst.setInheritanceBlocked((instance.inheritanceBlocked == 1));
      } else {
        SilverTrace.error("admin", "ComponentInstManager.setComponentInst",
            "root.EX_RECORD_NOT_FOUND", "instanceId = " + compLocalId);
      }
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.setComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_SET_COMPONENT",
          "component id: '" + compLocalId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Deletes component instance from Silverpeas
   *
   * @param componentInst
   * @param ddManager
   * @throws AdminException
   */
  public void deleteComponentInst(ComponentInst componentInst, DomainDriverManager ddManager) throws
      AdminException {
    try {
      // delete translations
      ddManager.getOrganization().instanceI18N.removeTranslations(componentInst.getLocalId());

      // delete the component node
      ddManager.getOrganization().instance.removeComponentInstance(componentInst.getLocalId());
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.deleteComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_COMPONENT",
          "component id: '" + componentInst.getLocalId() + "'", e);
    }
  }

  /*
   * Updates component in Silverpeas
   */
  public void updateComponentOrder(DomainDriverManager ddManager, int compLocalId, int orderNum)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.getOrganization().instance.updateComponentOrder(compLocalId, orderNum);
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.updateComponentOrder",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT",
          "Component Id : '" + compLocalId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /*
   * Updates component in Silverpeas
   */
  public void updateComponentInheritance(DomainDriverManager ddManager,
      int compLocalId, boolean inheritanceBlocked) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.getOrganization().instance.updateComponentInheritance(compLocalId,
          inheritanceBlocked);
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.updateComponentOrder",
          SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_COMPONENT_INHERITANCE", "Component Id : '" + compLocalId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /*
   * Updates component instance in Silverpeas
   */
  public void updateComponentInst(DomainDriverManager ddManager, ComponentInst compoInstOld,
      ComponentInst compoInstNew)
      throws AdminException {
    try {
      List<Parameter> parameters = compoInstNew.getParameters();
      for (Parameter parameter : parameters) {
        ddManager.getOrganization().instanceData.updateInstanceData(compoInstNew.getLocalId(),
            parameter);
      }
      // Create the component node
      ComponentInstanceRow changedInstance = makeComponentInstanceRow(compoInstNew);
      changedInstance.id = compoInstNew.getLocalId();
      ComponentInstanceRow old = ddManager.getOrganization().instance.getComponentInstance(
          changedInstance.id);

      if (compoInstNew.isRemoveTranslation()) {
        // Remove of a translation is required
        if (old.lang.equalsIgnoreCase(compoInstNew.getLanguage())) {
          // Default language = translation
          List<ComponentInstanceI18NRow> translations = ddManager.getOrganization().instanceI18N.
              getTranslations(changedInstance.id);

          if (translations != null && !translations.isEmpty()) {
            ComponentInstanceI18NRow translation = translations.get(0);
            changedInstance.lang = translation.lang;
            changedInstance.name = translation.name;
            changedInstance.description = translation.description;
            ddManager.getOrganization().instance.updateComponentInstance(changedInstance);
            ddManager.getOrganization().instanceI18N.removeTranslation(translation.id);
          }
        } else {
          ddManager.getOrganization().instanceI18N.removeTranslation(Integer.parseInt(compoInstNew.
              getTranslationId()));
        }
      } else {
        // Add or update a translation
        if (changedInstance.lang != null) {
          old.lang = I18NHelper.checkLanguage(old.lang);
          if (!old.lang.equalsIgnoreCase(changedInstance.lang)) {
            ComponentInstanceI18NRow row = new ComponentInstanceI18NRow(changedInstance);
            String translationId = compoInstNew.getTranslationId();
            if (translationId != null && !"-1".equals(translationId)) {
              // update translation
              row.id = Integer.parseInt(compoInstNew.getTranslationId());
              ddManager.getOrganization().instanceI18N.updateTranslation(row);
            } else {
              ddManager.getOrganization().instanceI18N.createTranslation(row);
            }
            changedInstance.lang = old.lang;
            changedInstance.name = old.name;
            changedInstance.description = old.description;
          }
        }
        ddManager.getOrganization().instance.updateComponentInstance(changedInstance);
        notifier.notifyEventOn(UPDATE, compoInstOld, compoInstNew);
      }

    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.updateComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT",
          "component id: '" + compoInstNew.getLocalId() + "'", e);
    }
  }

  /*
   * Move component instance in Silverpeas
   */
  public void moveComponentInst(DomainDriverManager ddManager, int spaceLocalId,
      int componentLocalId) throws AdminException {
    try {
      // Create the component node
      ddManager.getOrganization().instance.moveComponentInstance(spaceLocalId, componentLocalId);
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.moveComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT",
          "spaceId= " + spaceLocalId + " componentId=" + componentLocalId, e);
    }
  }

  /**
   * Get the component ids with the given component name
   *
   * @param ddManager
   * @param sComponentName
   * @return
   * @throws AdminException
   */
  public String[] getAllCompoIdsByComponentName(DomainDriverManager ddManager, String sComponentName)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Initialize a ComponentInstanceRow for search
      ComponentInstanceRow cir = new ComponentInstanceRow();
      cir.name = null;
      cir.description = null;
      cir.orderNum = -1;
      cir.componentName = sComponentName;

      // Search for components instance with given component name
      ComponentInstanceRow[] cirs = ddManager.getOrganization().instance
          .getAllMatchingComponentInstances(
          cir);
      if (cirs == null) {
        return ArrayUtil.EMPTY_STRING_ARRAY;
      }

      String[] compoIds = new String[cirs.length];
      for (int nI = 0; nI < cirs.length; nI++) {
        compoIds[nI] = idAsString(cirs[nI].id);
      }
      return compoIds;
    } catch (Exception e) {
      throw new AdminException(
          "ComponentInstManager.getAllCompoIdsByComponentName",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_INSTANCES_OF_COMPONENT",
          "component name: '" + sComponentName + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public String[] getComponentIdsInSpace(int spaceId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      // getting all componentIds available for user
      List<String> componentIds = ComponentDAO.getComponentIdsInSpace(con, spaceId);
      return componentIds.toArray(new String[componentIds.size()]);

    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.getComponentIdsInSpace",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE_COMPONENTIDS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<ComponentInstLight> getComponentsInSpace(int spaceId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      // getting all components in given space
      return ComponentDAO.getComponentsInSpace(con, spaceId);

    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.getComponentIdsInSpace",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE_COMPONENTIDS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<String> getAllowedComponentIds(int userId, List<String> groupIds)
      throws AdminException {
    return getAllowedComponentIds(userId, groupIds, null);
  }

  public List<String> getAllowedComponentIds(int userId, List<String> groupIds,
      Integer spaceLocalId) throws AdminException {
    return getAllowedComponentIds(userId, groupIds, spaceLocalId, null);
  }

  public List<String> getAllowedComponentIds(int userId, List<String> groupIds,
      Integer spaceLocalId, String componentName) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      if (spaceLocalId != null) {
        // getting componentIds available for user in given space (not subspaces)
        return ComponentDAO.getAvailableComponentIdsInSpace(con, groupIds, userId, spaceLocalId,
            componentName);
      } else {
        // getting all componentIds available for user
        return ComponentDAO.getAllAvailableComponentIds(con, groupIds, userId, componentName);
      }

    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getAllowedRootSpaceIds",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_ALLOWED_SPACEIDS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<Parameter> getParameters(DomainDriverManager ddManager, int compLocalId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Get the parameters if any
      List<Parameter> parameters = ddManager.getOrganization().instanceData
          .getAllParametersInComponent(compLocalId);
      return (parameters);
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.getParameters", SilverpeasException.ERROR,
          "admin.EX_ERR_GET_COMPONENT_PARAMETERS", "componentId = " + compLocalId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Converts ComponentInst to ComponentInstanceRow
   */
  private ComponentInstanceRow makeComponentInstanceRow(ComponentInst componentInst) {
    ComponentInstanceRow instance = new ComponentInstanceRow();
    instance.id = componentInst.getLocalId();
    instance.componentName = componentInst.getName();
    instance.name = componentInst.getLabel();
    instance.description = componentInst.getDescription();
    instance.orderNum = componentInst.getOrderNum();
    instance.lang = componentInst.getLanguage();
    instance.createdBy = idAsInt(componentInst.getCreatorUserId());
    instance.updatedBy = idAsInt(componentInst.getUpdaterUserId());
    if (componentInst.isPublic()) {
      instance.publicAccess = 1;
    } else {
      instance.publicAccess = 0;
    }

    if (componentInst.isHidden()) {
      instance.hidden = 1;
    } else {
      instance.hidden = 0;
    }

    if (componentInst.isInheritanceBlocked()) {
      instance.inheritanceBlocked = 1;
    } else {
      instance.inheritanceBlocked = 0;
    }

    return instance;
  }

  private List<ComponentInstLight> componentInstanceRows2ComponentInstLights(
      ComponentInstanceRow[] rows) {
    List<ComponentInstLight> components = new ArrayList<ComponentInstLight>();
    for (int s = 0; rows != null && s < rows.length; s++) {
      ComponentInstLight componentLight = new ComponentInstLight(rows[s]);
      componentLight.setDomainFatherId("WA" + componentLight.getDomainFatherId());
      components.add(componentLight);
    }
    return components;
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
    return java.lang.Integer.toString(id);
  }
}
