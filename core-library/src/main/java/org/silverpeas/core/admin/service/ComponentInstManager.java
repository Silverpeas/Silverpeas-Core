/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.component.dao.ComponentDAO;
import org.silverpeas.core.admin.component.model.ComponentI18N;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.component.model.SilverpeasSharedComponentInstance;
import org.silverpeas.core.admin.component.notification.ComponentInstanceEventNotifier;
import org.silverpeas.core.admin.persistence.ComponentInstanceI18NRow;
import org.silverpeas.core.admin.persistence.ComponentInstanceRow;
import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.user.ProfileInstManager;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;
import static org.silverpeas.core.SilverpeasExceptionMessages.*;
import static org.silverpeas.core.notification.system.ResourceEvent.Type.DELETION;
import static org.silverpeas.core.notification.system.ResourceEvent.Type.UPDATE;

@Service
@Singleton
@Transactional(value = Transactional.TxType.MANDATORY)
public class ComponentInstManager {

  public static final String COMPONENT = "component";
  @Inject
  private ProfileInstManager profileInstManager;
  @Inject
  private ComponentInstanceEventNotifier notifier;
  @Inject
  private OrganizationSchema organizationSchema;

  public static ComponentInstManager get() {
    return ServiceProvider.getSingleton(ComponentInstManager.class);
  }

  protected ComponentInstManager() {
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

    componentInst.setCreationDate(componentInstToCopy.getCreationDate());
    componentInst.setLastUpdateDate(componentInstToCopy.getLastUpdateDate());
    componentInst.setRemovalDate(componentInstToCopy.getRemovalDate());
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
   * @param spaceLocalId
   * @return
   * @throws AdminException
   */
  public void createComponentInst(ComponentInst componentInst, int spaceLocalId)
      throws AdminException {
    try {
      // Create the component node
      ComponentInstanceRow newInstance = makeComponentInstanceRow(componentInst);
      newInstance.spaceId = spaceLocalId;
      organizationSchema.instance().createComponentInstance(newInstance);
      componentInst.setLocalId(newInstance.id);

      // duplicates existing translations
      Map<String, ComponentI18N> translations = componentInst.getTranslations();
      for (Map.Entry<String, ComponentI18N> i18n : translations.entrySet()) {
        if (!i18n.getKey().equals(newInstance.lang)) {
          // default language stored in main table must not be stored in i18n table
          ComponentI18N translation = i18n.getValue();
          ComponentInstanceI18NRow row =
              new ComponentInstanceI18NRow(newInstance.id, i18n.getKey(), translation.getName(),
              translation.getDescription());
          organizationSchema.instanceI18N().createTranslation(row);
        }
      }

      // Add the parameters if necessary
      List<Parameter> parameters = componentInst.getParameters();

      for (Parameter parameter : parameters) {
        organizationSchema.instanceData().createInstanceData(
            componentInst.getLocalId(), parameter);
      }

      // Create the profile nodes
      for (ProfileInst profile : componentInst.getProfiles()) {
        profileInstManager.createProfileInst(profile, componentInst.getLocalId());
      }

    } catch (Exception e) {
      throw new AdminException(failureOnAdding(COMPONENT, componentInst.getName()), e);
    }
  }

  public void sendComponentToBasket(ComponentInst componentInst, String userId)
      throws AdminException {
    // Find a name which is not in concurrency with a previous deleted component
    boolean nameOK = false;
    int retry = 0;
    String deletedComponentName = null;
    while (!nameOK) {
      String componentName = componentInst.getLabel() + Administration.Constants.BASKET_SUFFIX;
      if (retry > 0) {
        componentName += " " + retry;
      }
      boolean spaceAlreadyExists;
      try {
        spaceAlreadyExists = organizationSchema.instance()
            .isComponentIntoBasket(idAsInt(componentInst.getDomainFatherId()),
                componentName);
      } catch (SQLException e) {
        throw new AdminException(e.getMessage(), e);
      }
      nameOK = !spaceAlreadyExists;
      deletedComponentName = componentName;
      retry++;
    }

    // Set component into basket with a unique name
    try {
      organizationSchema.instance()
          .sendComponentToBasket(componentInst.getLocalId(), deletedComponentName, userId);
    } catch (Exception e) {
      throw new AdminException(
          failureOnMoving(COMPONENT, componentInst.getLocalId(), "bin", ""), e);
    }
  }

  public void restoreComponentFromBasket(int localComponentId) throws AdminException {
    try {
      organizationSchema.instance().restoreComponentFromBasket(localComponentId);
    } catch (Exception e) {
      throw new AdminException(failureOnRestoring(COMPONENT, localComponentId), e);
    }
  }

  /**
   * Get component instance with the given id.
   * @param componentId the unique component instance id.
   * @return {@link ComponentInst} instance.
   * @throws AdminException in case of technical error.
   */
  public ComponentInst getComponentInst(int componentId) throws AdminException {
    final ComponentInst componentInst = new ComponentInst();
    componentInst.removeAllProfilesInst();
    try {
      // Load the component detail
      final ComponentInstanceRow instance = organizationSchema.instance()
          .getComponentInstance(componentId);
      if (instance != null) {
        // Set the attributes of the component Inst
        componentInst.setLocalId(instance.id);
        componentInst.setName(instance.componentName);
        componentInst.setLabel(instance.name);
        componentInst.setDescription(instance.description);
        componentInst.setDomainFatherId(idAsString(instance.spaceId));
        componentInst.setOrderNum(instance.orderNum);
        if (instance.createTime != null) {
          componentInst.setCreationDate(new Date(Long.parseLong(instance.createTime)));
        }
        if (instance.updateTime != null) {
          componentInst.setLastUpdateDate(new Date(Long.parseLong(instance.updateTime)));
        }
        if (instance.removeTime != null) {
          componentInst.setRemovalDate(new Date(Long.parseLong(instance.removeTime)));
        }
        componentInst.setCreatorUserId(idAsString(instance.createdBy));
        componentInst.setUpdaterUserId(idAsString(instance.updatedBy));
        componentInst.setRemoverUserId(idAsString(instance.removedBy));
        componentInst.setStatus(instance.status);
        // Get the parameters if any
        final List<Parameter> parameters = organizationSchema.instanceData()
            .getAllParametersInComponent(componentId);
        componentInst.setParameters(parameters);
        // Get the profiles
        final String[] asProfileIds = organizationSchema.userRole()
            .getAllUserRoleIdsOfInstance(componentInst.getLocalId());
        // Insert the profileInst in the componentInst
        for (int nI = 0; asProfileIds != null && nI < asProfileIds.length; nI++) {
          final ProfileInst profileInst = profileInstManager.getProfileInst(asProfileIds[nI], false);
          componentInst.addProfileInst(profileInst);
        }
        componentInst.setLanguage(instance.lang);
        // translations
        loadTranslations(componentInst, instance);
        componentInst.setPublic(instance.publicAccess == 1);
        componentInst.setHidden(instance.hidden == 1);
        componentInst.setInheritanceBlocked(instance.inheritanceBlocked == 1);
      } else {
        SilverLogger.getLogger(this).error("Component instance " + componentId + " not found!");
      }
    } catch (SQLException e) {
      throw new AdminException(failureOnUpdate(COMPONENT, componentId), e);
    }
    return componentInst;
  }

  /**
   * Return the all the root spaces ids available in Silverpeas
   *
   * @return
   * @throws AdminException
   */
  public List<ComponentInstLight> getRemovedComponents() throws AdminException {
    try {
      ComponentInstanceRow[] componentRows = organizationSchema.instance()
          .getRemovedComponents();

      return componentInstanceRows2ComponentInstLights(componentRows);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("removed components", ""), e);
    }
  }

  /**
   * Get component instance light with the given id
   *
   * @param compLocalId
   * @return
   * @throws AdminException
   */
  public ComponentInstLight getComponentInstLight(int compLocalId) throws AdminException {
    ComponentInstLight compoLight = null;
    try {
      ComponentInstanceRow compo = organizationSchema.instance().getComponentInstance(
          compLocalId);
      if (compo != null) {
        compoLight = new ComponentInstLight(compo);
        compoLight.setLocalId(compLocalId);
        compoLight.setDomainFatherId("WA" + compoLight.getDomainFatherId());
        loadTranslations(compoLight, compo);
      }
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(COMPONENT, compLocalId), e);
    }
    return compoLight;
  }

  /**
   * Deletes component instance from Silverpeas
   *
   * @param componentInst
   * @throws AdminException
   */
  public void deleteComponentInst(ComponentInst componentInst) throws AdminException {
    try {
      // delete translations
      organizationSchema.instanceI18N().removeTranslations(componentInst.getLocalId());

      // delete the component node
      organizationSchema.instance().removeComponentInstance(componentInst.getLocalId());

      // notify about the deletion
      notifier.notifyEventOn(DELETION, componentInst);
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(COMPONENT, componentInst.getLocalId()), e);
    }
  }

  /*
   * Updates component in Silverpeas
   */
  public void updateComponentOrder(int compLocalId, int orderNum) throws AdminException {
    try {
      organizationSchema.instance().updateComponentOrder(compLocalId, orderNum);
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("order of component", compLocalId), e);
    }
  }

  /*
   * Updates component in Silverpeas
   */
  public void updateComponentInheritance(int compLocalId, boolean inheritanceBlocked)
      throws AdminException {
    try {
      organizationSchema.instance().updateComponentInheritance(compLocalId,
          inheritanceBlocked);
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(COMPONENT, compLocalId), e);
    }
  }

  /*
   * Updates component instance in Silverpeas
   */
  public void updateComponentInst(ComponentInst compoInstOld, ComponentInst compoInstNew)
      throws AdminException {
    try {
      List<Parameter> parameters = compoInstNew.getParameters();
      for (Parameter parameter : parameters) {
        organizationSchema.instanceData().updateInstanceData(compoInstNew.getLocalId(),
            parameter);
      }
      // Create the component node
      ComponentInstanceRow changedInstance = makeComponentInstanceRow(compoInstNew);
      changedInstance.id = compoInstNew.getLocalId();
      ComponentInstanceRow old = organizationSchema.instance().getComponentInstance(
          changedInstance.id);

      if (compoInstNew.isRemoveTranslation()) {
        removeTranslation(compoInstNew, changedInstance, old);
      } else {
        updateTranslation(compoInstNew, changedInstance, old);
        organizationSchema.instance().updateComponentInstance(changedInstance);
        notifier.notifyEventOn(UPDATE, compoInstOld, compoInstNew);
      }

    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(COMPONENT, compoInstNew.getLocalId()), e);
    }
  }

  private void updateTranslation(final ComponentInst compoInstNew,
      final ComponentInstanceRow changedInstance, final ComponentInstanceRow old)
      throws SQLException {
    // Add or update a translation
    if (changedInstance.lang != null) {
      old.lang = I18NHelper.checkLanguage(old.lang);
      if (!old.lang.equalsIgnoreCase(changedInstance.lang)) {
        ComponentInstanceI18NRow row = new ComponentInstanceI18NRow(changedInstance);
        String translationId = compoInstNew.getTranslationId();
        if (translationId != null && !"-1".equals(translationId)) {
          // update translation
          row.id = Integer.parseInt(compoInstNew.getTranslationId());
          organizationSchema.instanceI18N().updateTranslation(row);
        } else {
          organizationSchema.instanceI18N().createTranslation(row);
        }
        changedInstance.lang = old.lang;
        changedInstance.name = old.name;
        changedInstance.description = old.description;
      }
    }
  }

  private void removeTranslation(final ComponentInst compoInstNew,
      final ComponentInstanceRow changedInstance, final ComponentInstanceRow old)
      throws SQLException {
    // Remove of a translation is required
    if (old.lang.equalsIgnoreCase(compoInstNew.getLanguage())) {
      // Default language = translation
      List<ComponentInstanceI18NRow> translations = organizationSchema.instanceI18N().
          getTranslations(changedInstance.id);

      if (translations != null && !translations.isEmpty()) {
        ComponentInstanceI18NRow translation = translations.get(0);
        changedInstance.lang = translation.lang;
        changedInstance.name = translation.name;
        changedInstance.description = translation.description;
        organizationSchema.instance().updateComponentInstance(changedInstance);
        organizationSchema.instanceI18N().removeTranslation(translation.id);
      }
    } else {
      organizationSchema.instanceI18N().removeTranslation(Integer.parseInt(compoInstNew.
          getTranslationId()));
    }
  }

  /*
   * Move component instance in Silverpeas
   */
  public void moveComponentInst(int spaceLocalId, int componentLocalId) throws AdminException {
    try {
      // Create the component node
      organizationSchema.instance().moveComponentInstance(spaceLocalId, componentLocalId);
    } catch (Exception e) {
      throw new AdminException(
          failureOnMoving(COMPONENT, componentLocalId, "space", spaceLocalId), e);
    }
  }

  /**
   * Get the component ids with the given component name
   *
   * @param sComponentName
   * @return
   * @throws AdminException
   */
  public String[] getAllCompoIdsByComponentName(String sComponentName) throws AdminException {
    try {
      // Initialize a ComponentInstanceRow for search
      ComponentInstanceRow cir = new ComponentInstanceRow();
      cir.name = null;
      cir.description = null;
      cir.orderNum = -1;
      cir.componentName = sComponentName;

      // Search for components instance with given component name
      ComponentInstanceRow[] cirs = organizationSchema.instance()
          .getAllMatchingComponentInstances(
          cir);
      if (cirs == null) {
        return ArrayUtil.emptyStringArray();
      }

      String[] compoIds = new String[cirs.length];
      for (int nI = 0; nI < cirs.length; nI++) {
        compoIds[nI] = idAsString(cirs[nI].id);
      }
      return compoIds;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("instances of component", sComponentName), e);
    }
  }

  public String[] getComponentIdsInSpace(int spaceId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      // getting all componentIds available for user
      List<String> componentIds = ComponentDAO.getComponentIdsInSpace(con, spaceId);
      return componentIds.toArray(new String[0]);

    } catch (Exception e) {
      throw new AdminException(failureOnGetting("component instances in space", spaceId), e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<String> getAllActiveComponentIds() throws AdminException {
    try {
      return ComponentDAO.getAllActiveComponentIds();
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all components", "of platform!!!"), e);
    }
  }

  public List<ComponentInstLight> getComponentsInSpace(int spaceId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      // getting all components in given space
      final List<ComponentInstLight> componentsInSpace = ComponentDAO.getComponentsInSpace(con, spaceId);
      setTranslations(con, componentsInSpace);
      return componentsInSpace;

    } catch (Exception e) {
      throw new AdminException(failureOnGetting("component instances in space", spaceId), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @SuppressWarnings({"unchecked"})
  private void loadTranslations(final SilverpeasSharedComponentInstance instance,
      final ComponentInstanceRow compo) throws SQLException {
    // default translation
    final ComponentI18N translation = new ComponentI18N(compo.lang, compo.name, compo.description);
    final AbstractI18NBean<ComponentI18N> i18nInstance = (AbstractI18NBean<ComponentI18N>) instance;
    i18nInstance.addTranslation(translation);
    // other ones
    final List<ComponentInstanceI18NRow> translations = organizationSchema.instanceI18N().
        getTranslations(compo.id);
    for (int i = 0; translations != null && i < translations.size(); i++) {
      final ComponentInstanceI18NRow row = translations.get(i);
      i18nInstance.addTranslation(new ComponentI18N(row));
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends SilverpeasSharedComponentInstance> void setTranslations(final Connection con,
      Collection<T> instances) throws AdminException {
    if (!instances.isEmpty()) {
      long startTime = System.currentTimeMillis();
      final Map<Integer, T> instancesByLocalIds = new HashMap<>(instances.size());
      final Map<Integer, AbstractI18NBean<ComponentI18N>> i18nInstancesByLocalIds = instances.stream()
          .map(i -> {
            final int localId;
            if (i instanceof ComponentInstLight) {
              localId = ((ComponentInstLight) i).getLocalId();
            } else {
              localId = ((ComponentInst) i).getLocalId();
            }
            instancesByLocalIds.put(localId, i);
            return Pair.of(localId, (AbstractI18NBean<ComponentI18N>) i);
          })
          .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
      try {
        final Map<Integer, List<ComponentInstanceI18NRow>> allTranslations = organizationSchema
            .instanceI18N().getIndexedTranslations(con, i18nInstancesByLocalIds.keySet());
        i18nInstancesByLocalIds.forEach((localId, i18nInstance) -> {
          final T instance = instancesByLocalIds.get(localId);
          // default translation
          final ComponentI18N translation = new ComponentI18N(i18nInstance.getLanguage(),
              instance.getLabel(), instance.getDescription());
          i18nInstance.addTranslation(translation);
          // other ones
          allTranslations.getOrDefault(localId, emptyList())
              .forEach(r -> i18nInstance.addTranslation(new ComponentI18N(r)));
        });
      } catch (SQLException e) {
        throw new AdminException(e);
      } finally {
        long endTime = System.currentTimeMillis();
        SilverLogger.getLogger(this).debug(() -> MessageFormat
            .format(" search translations in {0} for {1} component instances",
                formatDurationHMS(endTime - startTime), instances.size()));
      }
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
      throw new AdminException(failureOnGetting("component instances of component " + componentName,
          " accessible to user " + userId), e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<Parameter> getParameters(int compLocalId) throws AdminException {
    try {
      // Get the parameters if any
      return organizationSchema.instanceData().getAllParametersInComponent(compLocalId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("parameters of component", compLocalId), e);
    }
  }

  /**
   * Get the value of given parameter and about given component.
   * @param componentId component identifier.
   * @param paramName parameter name.
   * @param ignoreCase true to ignore case on parameter name.
   * @return return the value as string, or {@link StringUtil#EMPTY} if parameter has not been
   * found.
   * @throws AdminException on database error.
   */
  public String getParameterValueByComponentAndParamName(final Integer componentId,
      final String paramName, final boolean ignoreCase) throws AdminException {
    try {
      return organizationSchema.instanceData()
          .getParameterValueByComponentAndParamName(componentId, paramName, ignoreCase);
    } catch (Exception e) {
      throw new AdminException(MessageFormat
          .format("Can''t get parameter value of parameter {0} on instance {1}",
              String.valueOf(componentId), paramName), e);
    }
  }

  /**
   * Gets all parameters values by component and by parameter name.
   * @param componentIds list of component identifier.
   * @param paramNames optional list of parameter name. All parameters are retrieved if it is not
   * filled or null
   * @return a map filled with couples of parameter name / value per component instance identifier.
   * @throws AdminException on database error.
   */
  public Map<Integer, Map<String, String>> getParameterValuesByComponentIdThenByParamName(
      final Collection<Integer> componentIds, final Collection<String> paramNames)
      throws AdminException {
    try {
      return organizationSchema.instanceData()
          .getParameterValuesByComponentAndByParamName(componentIds, paramNames);
    } catch (Exception e) {
      throw new AdminException("Can't get parameter values...", e);
    }
  }

  public List<Integer> getComponentIds(Parameter parameter) throws AdminException {
    try {
      return organizationSchema.instanceData().getComponentIdsWithParameterValue(parameter);
    } catch (Exception e) {
      throw new AdminException(
          "Can't get components with parameter '" + parameter.getName() + "' = '" +
              parameter.getValue() + "'", e);
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
    List<ComponentInstLight> components = new ArrayList<>();
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
    return StringUtil.asInt(id, -1);
  }

  /**
   * Convert int Id to String Id
   */
  private static String idAsString(int id) {
    return java.lang.Integer.toString(id);
  }
}
