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

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.i18n.Translation;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.dao.ComponentDAO;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameter;
import com.stratelia.webactiv.beans.admin.instance.control.SPParameters;
import com.stratelia.webactiv.organization.ComponentInstanceI18NRow;
import com.stratelia.webactiv.organization.ComponentInstanceRow;
import com.stratelia.webactiv.organization.SpaceRow;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ComponentInstManager {
  static ProfileInstManager m_ProfileInstManager = new ProfileInstManager();

  /**
   * Constructor
   */
  public ComponentInstManager() {
  }

  /** Return a copy of the given componentInst */
  public ComponentInst copy(ComponentInst componentInstToCopy) {
    ComponentInst componentInst = new ComponentInst();
    componentInst.setId(componentInstToCopy.getId());
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

    for (int nI = 0; nI < componentInstToCopy.getNumProfileInst(); nI++)
      componentInst.addProfileInst(componentInstToCopy.getProfileInst(nI));

    SPParameters parameters = componentInstToCopy.getSPParameters();
    componentInst.setSPParameters(parameters);

    componentInst.setLanguage(componentInstToCopy.getLanguage());

    // Create a copy of component translations
    Iterator<Translation> translations = componentInstToCopy.getTranslations().values()
        .iterator();
    while (translations.hasNext()) {
      componentInst.addTranslation(translations.next());
    }

    componentInst.setPublic(componentInstToCopy.isPublic());
    componentInst.setHidden(componentInstToCopy.isHidden());
    componentInst.setInheritanceBlocked(componentInstToCopy
        .isInheritanceBlocked());

    return componentInst;
  }

  /**
   * Creates a component instance in database
   */
  public String createComponentInst(ComponentInst componentInst,
      DomainDriverManager ddManager, String sFatherId) throws AdminException {
    try {
      // Create the component node
      ComponentInstanceRow newInstance = makeComponentInstanceRow(componentInst);
      newInstance.spaceId = idAsInt(sFatherId);
      ddManager.organization.instance.createComponentInstance(newInstance);
      String sComponentNodeId = idAsString(newInstance.id);

      // Add the parameter if necessary
      List<SPParameter> parameters = componentInst.getParameters();

      SilverTrace.info("admin", "ComponentInstManager.createComponentInst",
          "root.MSG_GEN_PARAM_VALUE", "nb parameters = " + parameters.size());

      SPParameter parameter = null;
      for (int nI = 0; nI < parameters.size(); nI++) {
        parameter = parameters.get(nI);
        ddManager.organization.instanceData.createInstanceData(
            idAsInt(sComponentNodeId), parameter);
      }

      // Create the profile nodes
      for (int nI = 0; nI < componentInst.getNumProfileInst(); nI++)
        m_ProfileInstManager.createProfileInst(
            componentInst.getProfileInst(nI), ddManager, sComponentNodeId);

      return sComponentNodeId;
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.createComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_COMPONENT",
          "component name: '" + componentInst.getName() + "'", e);
    }
  }

  public void sendComponentToBasket(DomainDriverManager ddManager,
      String componentId, String tempLabel, String userId)
      throws AdminException {
    try {
      ddManager.organization.instance.sendComponentToBasket(
          idAsInt(componentId), tempLabel, userId);
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.sendComponentToBasket",
          SilverpeasException.ERROR, "admin.EX_ERR_SEND_COMPONENT_TO_BASKET",
          "componentId = " + componentId, e);
    }
  }

  public void restoreComponentFromBasket(DomainDriverManager ddManager,
      String componentId) throws AdminException {
    try {
      ddManager.organization.instance
          .restoreComponentFromBasket(idAsInt(componentId));
    } catch (Exception e) {
      throw new AdminException(
          "ComponentInstManager.restoreComponentFromBasket",
          SilverpeasException.ERROR,
          "admin.EX_ERR_RESTORE_COMPONENT_FROM_BASKET", "componentId = "
              + componentId, e);
    }
  }

  /**
   * Get component instance with the given id
   */
  public ComponentInst getComponentInst(DomainDriverManager ddManager,
      String sComponentId, String sFatherId) throws AdminException {
    if (sFatherId == null) {
      try {
        ddManager.getOrganizationSchema();
        SpaceRow space = ddManager.organization.space
            .getSpaceOfInstance(idAsInt(sComponentId));
        if (space == null)
          space = new SpaceRow();
        sFatherId = idAsString(space.id);
      } catch (Exception e) {
        throw new AdminException("ComponentInstManager.getComponentInst",
            SilverpeasException.ERROR, "admin.EX_ERR_GET_COMPONENT",
            "component id: '" + sComponentId + "'", e);
      } finally {
        ddManager.releaseOrganizationSchema();
      }
    }

    ComponentInst componentInst = new ComponentInst();
    componentInst.removeAllProfilesInst();
    this.setComponentInst(componentInst, ddManager, sComponentId, sFatherId);

    return componentInst;
  }

  /**
   * Return the all the root spaces ids available in Silverpeas
   */
  public List<ComponentInstLight> getRemovedComponents(DomainDriverManager ddManager)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ComponentInstanceRow[] componentRows = ddManager.organization.instance
          .getRemovedComponents();

      return componentInstanceRows2ComponentInstLights(componentRows);
    } catch (Exception e) {
      throw new AdminException("SpaceInstManager.getRemovedSpaces",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_REMOVED_SPACES", e);
    }
  }

  /**
   * Get component instance name with the given id
   */
  public String getComponentInstName(DomainDriverManager ddManager,
      String sComponentId) throws AdminException {
    String compoName = null;
    try {
      ddManager.getOrganizationSchema();
      ComponentInstanceRow compo = ddManager.organization.instance
          .getComponentInstance(idAsInt(sComponentId));
      if (compo != null)
        compoName = compo.componentName;
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.getComponentInstName",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_COMPONENT_NAME",
          "component id: '" + sComponentId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
    return compoName;
  }

  /**
   * Get component instance light with the given id
   */
  public ComponentInstLight getComponentInstLight(
      DomainDriverManager ddManager, String sComponentId) throws AdminException {
    ComponentInstLight compoLight = null;
    try {
      ddManager.getOrganizationSchema();
      ComponentInstanceRow compo = ddManager.organization.instance
          .getComponentInstance(idAsInt(sComponentId));
      if (compo != null) {
        compoLight = new ComponentInstLight(compo);
        compoLight.setId(compoLight.getName() + sComponentId);
        compoLight.setDomainFatherId("WA" + compoLight.getDomainFatherId());

        // Add default translation
        ComponentI18N translation = new ComponentI18N(compo.lang, compo.name,
            compo.description);
        compoLight.addTranslation((Translation) translation);

        List<ComponentInstanceI18NRow> translations = ddManager.organization.instanceI18N
            .getTranslations(compo.id);
        for (int t = 0; translations != null && t < translations.size(); t++) {
          ComponentInstanceI18NRow row = translations.get(t);
          compoLight.addTranslation((Translation) new ComponentI18N(row));
        }
      }
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.getComponentInstName",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_COMPONENT_NAME",
          "component id: '" + sComponentId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
    return compoLight;
  }

  /*
   * Get component instance information with given component id
   */
  public void setComponentInst(ComponentInst componentInst,
      DomainDriverManager ddManager, String sComponentId, String sFatherId)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Load the component detail
      ComponentInstanceRow instance = ddManager.organization.instance
          .getComponentInstance(idAsInt(sComponentId));

      if (instance != null) {
        // Set the attributes of the component Inst
        componentInst.setId(idAsString(instance.id));
        componentInst.setName(instance.componentName);
        componentInst.setLabel(instance.name);
        componentInst.setDescription(instance.description);
        componentInst.setDomainFatherId(sFatherId);
        componentInst.setOrderNum(instance.orderNum);

        if (instance.createTime != null)
          componentInst.setCreateDate(new Date(Long
              .parseLong(instance.createTime)));
        if (instance.updateTime != null)
          componentInst.setUpdateDate(new Date(Long
              .parseLong(instance.updateTime)));
        if (instance.removeTime != null)
          componentInst.setRemoveDate(new Date(Long
              .parseLong(instance.removeTime)));

        componentInst.setCreatorUserId(idAsString(instance.createdBy));
        componentInst.setUpdaterUserId(idAsString(instance.updatedBy));
        componentInst.setRemoverUserId(idAsString(instance.removedBy));

        componentInst.setStatus(instance.status);

        // Get the parameters if any
        SPParameters parameters = ddManager.organization.instanceData
            .getAllParametersInComponent(idAsInt(sComponentId));
        componentInst.setSPParameters(parameters);

        // Get the profiles
        String[] asProfileIds = ddManager.organization.userRole
            .getAllUserRoleIdsOfInstance(idAsInt(componentInst.getId()));

        // Insert the profileInst in the componentInst
        for (int nI = 0; asProfileIds != null && nI < asProfileIds.length; nI++) {
          ProfileInst profileInst = m_ProfileInstManager.getProfileInst(
              ddManager, asProfileIds[nI], sComponentId);
          componentInst.addProfileInst(profileInst);
        }

        componentInst.setLanguage(instance.lang);

        // Add default translation
        ComponentI18N translation = new ComponentI18N(instance.lang,
            instance.name, instance.description);
        componentInst.addTranslation((Translation) translation);

        List<ComponentInstanceI18NRow> translations =
            ddManager.organization.instanceI18N.getTranslations(instance.id);
        for (int t = 0; translations != null && t < translations.size(); t++) {
          ComponentInstanceI18NRow row = translations.get(t);
          componentInst.addTranslation((Translation) new ComponentI18N(row));
        }

        componentInst.setPublic((instance.publicAccess == 1));
        componentInst.setHidden((instance.hidden == 1));
        componentInst.setInheritanceBlocked((instance.inheritanceBlocked == 1));
      } else {
        SilverTrace.error("admin", "ComponentInstManager.setComponentInst",
            "root.EX_RECORD_NOT_FOUND", "instanceId = " + sComponentId);
      }
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.setComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_SET_COMPONENT",
          "component id: '" + sComponentId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Deletes component instance from Silverpeas
   */
  public void deleteComponentInst(ComponentInst componentInst,
      DomainDriverManager ddManager) throws AdminException {
    try {
      // delete translations
      ddManager.organization.instanceI18N
          .removeTranslations(idAsInt(componentInst.getId()));

      // delete the component node
      ddManager.organization.instance
          .removeComponentInstance(idAsInt(componentInst.getId()));
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.deleteComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_COMPONENT",
          "component id: '" + componentInst.getId() + "'", e);
    }
  }

  /*
   * Updates component in Silverpeas
   */
  public void updateComponentOrder(DomainDriverManager ddManager,
      String sComponentId, int orderNum) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.organization.instance.updateComponentOrder(
          idAsInt(sComponentId), orderNum);
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.updateComponentOrder",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT",
          "Component Id : '" + sComponentId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /*
   * Updates component in Silverpeas
   */
  public void updateComponentInheritance(DomainDriverManager ddManager,
      String sComponentId, boolean inheritanceBlocked) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.organization.instance.updateComponentInheritance(
          idAsInt(sComponentId), inheritanceBlocked);
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.updateComponentOrder",
          SilverpeasException.ERROR,
          "admin.EX_ERR_UPDATE_COMPONENT_INHERITANCE", "Component Id : '"
              + sComponentId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /*
   * Updates component instance and recursively sub-elements in Silverpeas
   */
  public void updateComponentInstRecur(ComponentInst componentInst,
      DomainDriverManager ddManager, ComponentInst compoInstNew)
      throws AdminException {
    ArrayList<String> alOldCompoProfile = new ArrayList<String>();
    ArrayList<String> alNewCompoProfile = new ArrayList<String>();
    ArrayList<String> alAddProfile = new ArrayList<String>();
    ArrayList<String> alRemProfile = new ArrayList<String>();
    ArrayList<String> alStayProfile = new ArrayList<String>();

    try {
      // Compute the Old component profile list
      ArrayList<ProfileInst> alProfileInst = componentInst.getAllProfilesInst();
      for (int nI = 0; nI < alProfileInst.size(); nI++)
        alOldCompoProfile.add(alProfileInst.get(nI).getName());

      // Compute the New component profile list
      alProfileInst = compoInstNew.getAllProfilesInst();
      for (int nI = 0; nI < alProfileInst.size(); nI++)
        alNewCompoProfile.add(alProfileInst.get(nI).getName());

      // Compute the remove Profile list
      for (int nI = 0; nI < alOldCompoProfile.size(); nI++)
        if (alNewCompoProfile.indexOf(alOldCompoProfile.get(nI)) == -1)
          alRemProfile.add(alOldCompoProfile.get(nI));

      // Compute the add and stay Profile list
      for (int nI = 0; nI < alNewCompoProfile.size(); nI++)
        if (alOldCompoProfile.indexOf(alNewCompoProfile.get(nI)) == -1)
          alAddProfile.add(alNewCompoProfile.get(nI));
        else
          alStayProfile.add(alNewCompoProfile.get(nI));

      // Add the new Profiles
      for (int nI = 0; nI < alAddProfile.size(); nI++)
        m_ProfileInstManager.createProfileInst(compoInstNew
            .getProfileInst((String) alAddProfile.get(nI)), ddManager,
            componentInst.getId());

      // Remove the removed profiles
      for (int nI = 0; nI < alRemProfile.size(); nI++)
        m_ProfileInstManager.deleteProfileInst(componentInst
            .getProfileInst((String) alRemProfile.get(nI)), ddManager);

      // Update the stayed profile
      for (int nI = 0; nI < alStayProfile.size(); nI++)
        m_ProfileInstManager.updateProfileInst(componentInst
            .getProfileInst((String) alStayProfile.get(nI)), ddManager,
            compoInstNew.getProfileInst((String) alStayProfile.get(nI)));
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.updateComponentInstRecur",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT",
          "component id: '" + componentInst.getId() + "'", e);
    }
  }

  /*
   * Updates component instance in Silverpeas
   */
  public String updateComponentInst(DomainDriverManager ddManager,
      ComponentInst compoInstNew) throws AdminException {
    try {
      List<SPParameter> parameters = compoInstNew.getParameters();
      for (int nI = 0; nI < parameters.size(); nI++) {
        ddManager.organization.instanceData.updateInstanceData(
            idAsInt(compoInstNew.getId()), parameters.get(nI));
      }

      // Create the component node
      ComponentInstanceRow changedInstance = makeComponentInstanceRow(compoInstNew);
      changedInstance.id = idAsInt(compoInstNew.getId());

      ComponentInstanceRow old = ddManager.organization.instance
          .getComponentInstance(changedInstance.id);

      SilverTrace.debug("admin", this.getClass().getName()
          + ".updateComponentInst", "root.MSG_GEN_PARAM_VALUE", "remove = "
          + compoInstNew.isRemoveTranslation() + ", translationId = "
          + compoInstNew.getTranslationId());

      if (compoInstNew.isRemoveTranslation()) {
        // Remove of a translation is required
        if (old.lang.equalsIgnoreCase(compoInstNew.getLanguage())) {
          // Default language = translation
          List<ComponentInstanceI18NRow> translations = ddManager.organization.instanceI18N
              .getTranslations(changedInstance.id);

          if (translations != null && translations.size() > 0) {
            ComponentInstanceI18NRow translation = translations.get(0);

            changedInstance.lang = translation.lang;
            changedInstance.name = translation.name;
            changedInstance.description = translation.description;

            ddManager.organization.instance
                .updateComponentInstance(changedInstance);

            ddManager.organization.instanceI18N
                .removeTranslation(translation.id);
          }
        } else {
          ddManager.organization.instanceI18N.removeTranslation(Integer
              .parseInt(compoInstNew.getTranslationId()));
        }
      } else {
        // Add or update a translation
        if (changedInstance.lang != null) {
          if (old.lang == null) {
            // translation for the first time
            old.lang = I18NHelper.defaultLanguage;
          }

          if (!old.lang.equalsIgnoreCase(changedInstance.lang)) {
            ComponentInstanceI18NRow row = new ComponentInstanceI18NRow(
                changedInstance);
            String translationId = compoInstNew.getTranslationId();
            if (translationId != null && !translationId.equals("-1")) {
              // update translation
              row.id = Integer.parseInt(compoInstNew.getTranslationId());

              ddManager.organization.instanceI18N.updateTranslation(row);
            } else {
              ddManager.organization.instanceI18N.createTranslation(row);
            }

            changedInstance.lang = old.lang;
            changedInstance.name = old.name;
            changedInstance.description = old.description;
          }
        }

        ddManager.organization.instance
            .updateComponentInstance(changedInstance);
      }

      return idAsString(changedInstance.id);
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.updateComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT",
          "component id: '" + compoInstNew.getId() + "'", e);
    }
  }

  /*
   * Move component instance in Silverpeas
   */
  public void moveComponentInst(DomainDriverManager ddManager, String spaceId,
      String componentId) throws AdminException {
    try {
      // Create the component node
      ddManager.organization.instance.moveComponentInstance(idAsInt(spaceId),
          idAsInt(componentId));
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.moveComponentInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_COMPONENT",
          "spaceId= " + spaceId + " componentId=" + componentId, e);
    }
  }

  /**
   * Get the component ids with the given component name
   */
  public String[] getAllCompoIdsByComponentName(DomainDriverManager ddManager,
      String sComponentName) throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // Initialize a ComponentInstanceRow for search
      ComponentInstanceRow cir = new ComponentInstanceRow();
      cir.name = null;
      cir.description = null;
      cir.orderNum = -1;
      cir.componentName = sComponentName;

      // Search for components instance with given component name
      ComponentInstanceRow[] cirs = ddManager.organization.instance
          .getAllMatchingComponentInstances(cir);
      if (cirs == null)
        return new String[0];

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
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

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
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

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

  public List<String> getAllowedComponentIds(int userId, List<String> groupIds, String spaceId)
      throws AdminException {
    return getAllowedComponentIds(userId, groupIds, spaceId, null);
  }

  public List<String> getAllowedComponentIds(int userId, List<String> groupIds, String spaceId,
      String componentName) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ADMIN_DATASOURCE);

      if (StringUtil.isDefined(spaceId)) {
        // getting componentIds available for user in given space (not subspaces)
        return ComponentDAO.getAvailableComponentIdsInSpace(con, groupIds, userId, spaceId,
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

  /**
   * Converts ComponentInst to ComponentInstanceRow
   */
  private ComponentInstanceRow makeComponentInstanceRow(
      ComponentInst componentInst) {
    ComponentInstanceRow instance = new ComponentInstanceRow();

    instance.id = idAsInt(componentInst.getId());
    instance.componentName = componentInst.getName();
    instance.name = componentInst.getLabel();
    instance.description = componentInst.getDescription();
    instance.orderNum = componentInst.getOrderNum();
    instance.lang = componentInst.getLanguage();
    instance.createdBy = idAsInt(componentInst.getCreatorUserId());
    instance.updatedBy = idAsInt(componentInst.getUpdaterUserId());

    if (componentInst.isPublic())
      instance.publicAccess = 1;
    else
      instance.publicAccess = 0;

    if (componentInst.isHidden())
      instance.hidden = 1;
    else
      instance.hidden = 0;

    if (componentInst.isInheritanceBlocked())
      instance.inheritanceBlocked = 1;
    else
      instance.inheritanceBlocked = 0;

    return instance;
  }

  private List<ComponentInstLight> componentInstanceRows2ComponentInstLights(
      ComponentInstanceRow[] rows) {
    List<ComponentInstLight> components = new ArrayList<ComponentInstLight>();
    ComponentInstLight componentLight = null;
    for (int s = 0; rows != null && s < rows.length; s++) {
      componentLight = new ComponentInstLight((ComponentInstanceRow) rows[s]);
      componentLight.setId(componentLight.getName() + componentLight.getId());
      componentLight.setDomainFatherId("WA"
          + componentLight.getDomainFatherId());
      components.add(componentLight);
    }
    return components;
  }

  /**
   * Convert String Id to int Id
   */
  private int idAsInt(String id) {
    if (id == null || id.length() == 0)
      return -1; // the null id.

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
    return Integer.toString(id);
  }
}