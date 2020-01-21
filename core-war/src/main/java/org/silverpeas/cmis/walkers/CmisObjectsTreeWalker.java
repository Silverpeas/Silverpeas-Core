/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.cmis.walkers;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.MutableProperties;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.CmisObjectFactory;
import org.silverpeas.core.cmis.model.Folding;
import org.silverpeas.core.cmis.model.TypeId;
import org.silverpeas.cmis.security.AccessControllerRegister;
import org.silverpeas.core.Identifiable;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A walker of a CMIS objects tree whose each leaf is mapped to a given Silverpeas organizational
 * resource or to a given user contribution. The root of the tree is a virtual container of all of
 * the Silverpeas root spaces. The walker provides a functional view to access the CMIS objects but
 * behind de scene it walks across the organizational schema of Silverpeas by using its different
 * services. Because the way to access an object in Silverpeas (and hence to browse the CMIS objects
 * tree) depends on the types of the Silverpeas objects implied in a walk of the CMIS tree, the
 * implementation of the walker is left to more concrete walkers specifically designed to handle a
 * peculiar type of a Silverpeas object.
 * @author mmoquillon
 */
public abstract class CmisObjectsTreeWalker {

  private static final String NOTHING = null;
  private static final List<String> NOTHINGS = null;

  @Inject
  private CmisObjectFactory objectFactory;
  @Inject
  private OrganizationController controller;

  /**
   * Gets an instance of a {@link CmisObjectsTreeWalker}.
   * @return a {@link CmisObjectsTreeWalker} instance
   */
  public static CmisObjectsTreeWalker getInstance() {
    return ServiceProvider.getSingleton(TreeWalkerSelector.class);
  }

  /**
   * Selects an instance of {@link CmisObjectsTreeWalker} that knows how to browse for and to handle
   * the type of Silverpeas objects the format of the specified unique identifier is related to.
   * @param objectId a unique identifier of a Silverpeas object.
   * <p>
   * This method is dedicated to be used by the concrete type of the abstract
   * {@link CmisObjectsTreeWalker} class to select the correct next tree walker when walking the
   * CMIS objects tree.
   * </p>
   * @return an instance of a concrete type of {@link CmisObjectsTreeWalker} that has the knowledge
   * to handle the type of Silverpeas objects referred by the format of the given identifier.
   */
  protected static CmisObjectsTreeWalker selectInstance(final String objectId) {
    return ServiceProvider.getSingleton(TreeWalkerSelector.class).selectByObjectId(objectId);
  }

  /**
   * Gets in Silverpeas the object identified by the specified unique identifier. The way how to get
   * such an object depends on its type and it is then delegated to the walker that handles such a
   * type of Silverpeas objects.
   * @param objectId the unique identifier of an object in Silverpeas.
   * @param <T> the concrete type of the object to return.
   * @return a Silverpeas object or null if no such object exists.
   */
  protected abstract <T extends Identifiable> T getSilverpeasObjectById(final String objectId);

  /**
   * Gets in Silverpeas all the children objects of the specified parent and that are accessible
   * to the given user. The way how to get such an object depends on its type and it is then
   * delegated to the walker that handles such a type of Silverpeas objects.
   * @param parentId the unique identifier of the parent in Silverpeas.
   * @param user the user for which the children are get.
   * @param <T> the concrete type of the children.
   * @return a stream over all the allowed children of the specified parent in Silverpeas.
   */
  protected abstract <T extends AbstractI18NBean & Identifiable> Stream<T> getAllowedChildrenOfSilverpeasObject(
      final String parentId, final User user);

  /**
   * Creates the a CMIS object representation of the specified Silverpeas object. The representation
   * depends on the concrete type of the Silverpeas object. The way how create such a CMIS object
   * depends on the type of the Silverpeas object and it is then delegated to the walker that
   * handles such a type of Silverpeas objects.
   * @param silverpeasObject an object in Silverpeas.
   * @param language the language to use in the localization of the CMIS object.
   * @param <T> the concrete type of the Silverpeas object.
   * @return a {@link CmisObject} innstance.
   */
  protected abstract <T extends CmisObject> T createCmisObject(final Object silverpeasObject,
      final String language);

  public final boolean isSupported(final String objectId) {
    return selectInstance(objectId) != null;
  }

  /**
   * Gets the CMIS data of the Silverpeas object uniquely identified by the specified identifier.
   * Only the data satisfying the given filtering rules are returned.
   * @param objectId the unique identifier of a Silverpeas object.
   * @param filtering the filtering rules to apply on the data.
   * @return an {@link ObjectData} instance that provides the CMIS data of the specified Silverpeas
   * object.
   */
  public ObjectData getObjectData(final String objectId, final Filtering filtering) {
    return checkAndDo(objectId, filtering, (o, f) -> {
      final CmisObject cmisObject = createCmisObject(o, f.getLanguage());
      return buildObjectData(cmisObject, f);
    });
  }

  /**
   * Gets the CMIS data of the Silverpeas object that is located at the specified path in the CMIS
   * objects tree. Only the data satisfying the given filtering rules are returned.
   * @param path the path of the object in Silverpeas to get.
   * @param filtering the filtering rules to apply on the data.
   * @return an {@link ObjectData} instance that provides the CMIS data of the specified Silverpeas
   * object.
   */
  public ObjectData getObjectDataByPath(final String path, final Filtering filtering) {
    if (StringUtil.isNotDefined(path) || !path.startsWith(CmisFolder.PATH_SEPARATOR)) {
      throw new IllegalArgumentException("Invalid path: " + path);
    }
    final String[] pathSegments = path.substring(1).split(CmisFolder.PATH_SEPARATOR);
    return walkDownPathForChildData("", 0, pathSegments, filtering);
  }

  /**
   * Gets a list of the CMIS data of the Silverpeas objects that are the direct parents of the
   * CMIS object uniquely identified by the specified identifier. Only the data satisfying the given
   * filtering rules are returned. A file-able CMIS object can have one or more parents but in
   * Silverpeas, only publications in an EDM can have more than one parent (through the aliasing
   * mechanism), so unless the object referred by the specified identifier is an aliased
   * publication, this method returns usually a list with one element.
   * @param objectId the unique identifier of a Silverpeas resource or contribution.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @return a list of {@link ObjectParentData} elements, each of them being a wrapper of an
   * {@link ObjectData} instance with the path of the specified object relative to this parent. The
   * CMIS data are carried by the {@link ObjectData} object. If the specified object isn't
   * file-able or it is the root folder (the virtual root space in Silverpeas), then an empty list
   * is returned.
   */
  public List<ObjectParentData> getParentsData(final String objectId, final Filtering filtering) {
    return checkAndDo(objectId, filtering, this::browseParentsOfObject);
  }

  /**
   * Gets a list of CMIS data of the Silverpeas objects that are children of the CMIS folder
   * uniquely identified by the specified identifier. The CMIS folder represents a Silverpeas
   * resource that is a container of others Silverpeas resources (space, component instance, ...).
   * @param folderId the unique identifier of a Silverpeas resource.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @param paging the paging to apply on the elements of the list.
   * @return an {@link ObjectInFolderList} instance that is a list of {@link ObjectInFolderData}
   * elements, each of them being a decorator of an {@link ObjectData} instance with its path in
   * the CMIS repository tree (if asked by the filtering). The CMIS data are carried by the
   * {@link ObjectData} object.
   */
  public ObjectInFolderList getChildrenData(final String folderId, final Filtering filtering,
      final Paging paging) {
    return checkAndDo(folderId, filtering, (o, f) -> browseObjectsInFolder(o, f, paging));
  }

  /**
   * Gets the CMIS objects subtree rooted to the CMIS folder uniquely identified by the specified
   * identifier. The CMIS folder represents a Silverpeas resource that is a container of others
   * Silverpeas resources (space, component instance, ...). A list of the direct children of the
   * folder ({@link ObjectInFolderContainer} instances) is returned with, for each of them,
   * if any, their own direct children and so on. Each child is described by their CMIS data
   * (a decorator {@link ObjectInFolderData} of an {@link ObjectData} instance) filtered with the
   * given filtering rules.
   * @param folderId the unique identifier of a Silverpeas resource.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @param depth the maximum depth of the subtree to return from the specified folder.
   * @return a list of {@link ObjectInFolderContainer} elements (the direct children), each of them
   * being a container of others {@link ObjectInFolderContainer} objects (recursive walk of
   * children) and described by an {@link ObjectInFolderData} instance that is a decorator of an
   * {@link ObjectData} instance (the CMIS data) with its path in the CMIS repository tree (if asked
   * by the filtering). The CMIS data are carried by the {@link ObjectData} object.
   */
  public List<ObjectInFolderContainer> getSubTreeData(final String folderId,
      final Filtering filtering, final long depth) {
    return checkAndDo(folderId, filtering, (o, f) -> browseObjectsInFolderTree(o, f, depth));
  }

  /**
   * Browses for the objects in the CMIS tree rooted at the specified object in Silverpeas. The
   * browsing depends on the type of the specified object in Silverpeas and it is then delegated
   * to the walker that knows how to browse a tree rooted at such a tree.
   * @param object an identifiable object in Silverpeas.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @param depth the depth of the browsing of the tree.
   * @return a list of {@link ObjectInFolderContainer} elements (the direct children), each of them
   * being a container of others {@link ObjectInFolderContainer} objects (recursive walk of
   * the different tree's nodes) and described by an {@link ObjectInFolderData} instance that
   * is a decorator of an {@link ObjectData} instance (the CMIS data) with its path in the CMIS
   * repository tree (if asked by the filtering). The CMIS data are carried by the
   * {@link ObjectData} object.
   */
  protected abstract List<ObjectInFolderContainer> browseObjectsInFolderTree(
      final Identifiable object, final Filtering filtering, final long depth);

  /**
   * Browses for the direct children in the CMIS folder represented by the specified Silverpeas
   * object. the browsing of children depends on the type of the specified object in Silverpeas and
   * it is then delegated to the walker that knows how to browse for children such an object.
   * @param object an identifiable object in Silverpeas.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @param paging the paging to apply on the elements of the list.
   * @return an {@link ObjectInFolderList} instance that is a list of {@link ObjectInFolderData}
   * elements, each of them being a decorator of an {@link ObjectData} instance with its path in
   * the CMIS repository tree (if asked by the filtering). The CMIS data are carried by the
   * {@link ObjectData} object.
   */
  protected abstract ObjectInFolderList browseObjectsInFolder(final Identifiable object,
      final Filtering filtering, final Paging paging);

  /**
   * Browses for the parents in the CMIS objects tree of the specified Silverpeas object. In a
   * CMIS objects tree, a child can have direct one or several parents. This method returns all of
   * them. However, as the browsing of the CMIS objects tree depends on the type of its node and
   * hence on the type of the objects in Silverpeas, the browsing is delegated to the walker that
   * knows how to handle such a Silverpeas object.
   * @param object an identifiable object in Silverpeas.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @return a list of {@link ObjectParentData} elements, each of them being a wrapper of an
   * {@link ObjectData} instance with the path of the specified object relative to this parent. The
   * CMIS data are carried by the {@link ObjectData} object. If the specified object isn't
   * file-able or it is the root folder (the virtual root space in Silverpeas), then an empty list
   * is returned.
   */
  protected abstract List<ObjectParentData> browseParentsOfObject(final Identifiable object,
      final Filtering filtering);

  /**
   * Builds the CMIS data corresponding to the specified CMIS folder and by taking into account the
   * filtering that indicates the properties to return.
   * @param folder a CMIS folder representing a Silverpeas object.
   * @param filtering the filtering rules to apply on the data to build.
   * @return an {@link ObjectInFolderData} object representing the CMIS data of the folder plus its
   * path segment in the CMIS objects tree (if not excluded by the filtering rules).
   */
  protected final ObjectInFolderData buildObjectInFolderData(final CmisFolder folder,
      final Filtering filtering) {
    ObjectInFolderDataImpl objectInFolderData = new ObjectInFolderDataImpl();
    final ObjectData objData = buildObjectData(folder, filtering);
    objectInFolderData.setObject(objData);
    if (filtering.isPathSegmentToBeIncluded()) {
      objectInFolderData.setPathSegment(folder.getPathSegment());
    }
    return objectInFolderData;
  }

  /**
   * Builds the CMIS data both for the specified CMIS folder and recursively for all of its children
   * by taking into account the filtering that indicates the properties to return.
   * @param folder a CMIS folder representing a Silverpeas object.
   * @param filtering the filtering rules to apply on the data to build.
   * @return an {@link ObjectInFolderContainerImpl} instance representing the CMIS data of the
   * folder plus the CMIS data of its direct children, and so on for each of its children.
   */
  protected final ObjectInFolderContainerImpl buildObjectInFolderContainer(final CmisFolder folder,
      final Filtering filtering) {
    ObjectInFolderData objectInFolder = buildObjectInFolderData(folder, filtering);
    ObjectInFolderContainerImpl container = new ObjectInFolderContainerImpl();
    container.setObject(objectInFolder);
    return container;
  }

  /**
   * Builds the CMIS data corresponding to the given parent of the specified folder and by taking
   * into account the filtering that indicates the properties to return.
   * @param parent a CMIS folder parent of the below other CMIS folder. It represents a
   * Silverpeas object, itself parent of the Silverpeas object represented by the below CMIS
   * folder.
   * @param folder a CMIS folder representing a Silverpeas object.
   * @param filtering the filtering rules to apply on the data to build.
   * @return an {@link ObjectParentData} object representing the CMIS data of the parent plus the
   * path segment of the child folder relative to the parent in the CMIS objects tree (if not
   * excluded by the filtering rules).
   */
  protected final ObjectParentData buildObjectParentData(final CmisFolder parent,
      final CmisFolder folder, final Filtering filtering) {
    ObjectData objectData = buildObjectData(parent, filtering);
    ObjectParentDataImpl parentData = new ObjectParentDataImpl(objectData);
    if (filtering.isPathSegmentToBeIncluded()) {
      parentData.setRelativePathSegment(folder.getPathSegment());
    }
    return parentData;
  }

  /**
   * Builds the CMIS data corresponding to the specified CMIS object and by taking into account the
   * filtering that indicates the properties to return.
   * @param object a CMIS object.
   * @param filtering the filtering rules to apply on the data to build.
   * @return an {@link ObjectData} object gathering both the CMIS attributes and properties of the
   * specified CMIS object.
   */
  protected final ObjectData buildObjectData(final CmisObject object, final Filtering filtering) {
    final ObjectDataImpl objData = new ObjectDataImpl();

    if (filtering.isACLToBeIncluded()) {
      AccessControlListImpl acl = new AccessControlListImpl();
      acl.setAces(object.getAcl());
      objData.setAcl(acl);
      objData.setIsExactAcl(true);
    }

    if (filtering.areAllowedActionsToBeIncluded()) {
      // should depend on the type of the object and on the access right of the current user
      AllowableActionsImpl allowableActions = new AllowableActionsImpl();
      allowableActions.setAllowableActions(object.getAllowedActions());
      objData.setAllowableActions(allowableActions);
    }

    final Properties objProps = setProperties(object, filtering);
    objData.setProperties(objProps);

    return objData;
  }

  private ObjectData walkDownPathForChildData(final String parentId, final int idx,
      final String[] pathSegments, final Filtering filtering) {
    final User user = filtering.getCurrentUser();
    final String language = user.getUserPreferences().getLanguage();
    final Identifiable object = getAllowedChildrenOfSilverpeasObject(parentId, user)
        .filter(s -> s.getName(language).equals(pathSegments[idx]))
        .findFirst()
        .orElseThrow(() -> new CmisObjectNotFoundException(
            "No such object with name '" + pathSegments[idx] + "' in the path " +
                String.join(CmisFolder.PATH_SEPARATOR, pathSegments)));
    final CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.selectInstance(object.getId());
    if (idx >= pathSegments.length - 1) {
      CmisObject cmisObject = walker.createCmisObject(object, filtering.getLanguage());
      return buildObjectData(cmisObject, filtering);
    } else {
      return walker.walkDownPathForChildData(object.getId(), idx + 1, pathSegments, filtering);
    }
  }

  private <T> T checkAndDo(final String objectId, final Filtering filtering,
      BiFunction<Identifiable, Filtering, T> action) {
    final Identifiable object = getSilverpeasObjectById(objectId);
    checkObjectExists(objectId, object);
    checkUserPermissions(filtering.getCurrentUser(), object.getClass(), objectId);
    return action.apply(object, filtering);
  }

  private Properties setProperties(final CmisObject object, final Filtering filtering) {
    Set<String> filter = filtering.getPropertiesFilter();
    PropertiesImpl props = new PropertiesImpl();
    props.addProperty(new PropertyStringImpl(PropertyIds.NAME, object.getName()));
    props.addProperty(new PropertyStringImpl(PropertyIds.DESCRIPTION, object.getDescription()));
    props.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, NOTHINGS));
    props.addProperty(new PropertyStringImpl(PropertyIds.CHANGE_TOKEN, NOTHING));

    setPropertyId(props, PropertyIds.OBJECT_ID, object.getId(), filter);
    setPropertyId(props, PropertyIds.BASE_TYPE_ID, object.getBaseCmisType().value(), filter);
    setPropertyId(props, PropertyIds.OBJECT_TYPE_ID, object.getCmisType().value(), filter);
    setPropertyString(props, PropertyIds.CREATED_BY, object.getCreator(), filter);
    setPropertyDateTime(props, PropertyIds.CREATION_DATE, object.getCreationDate(), filter);
    setPropertyString(props, PropertyIds.LAST_MODIFIED_BY, object.getLastModifier(), filter);
    setPropertyDateTime(props, PropertyIds.LAST_MODIFICATION_DATE, object.getLastModificationDate(),
        filter);

    if (object instanceof Folding) {
      final Folding folder = (Folding) object;
      List<String> types = folder.getAllowedChildrenTypes()
          .stream()
          .map(TypeId::value)
          .collect(Collectors.toList());
      props.addProperty(new PropertyIdImpl(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, types));
      setPropertyId(props, PropertyIds.PARENT_ID, folder.getParentId(), filter);
      setPropertyString(props, PropertyIds.PATH, folder.getPath(), filter);
    }
    return props;
  }

  private static void setPropertyString(final MutableProperties props, final String propertyName,
      final String propertyValue, final Set<String> filter) {
    applyFilter(filter, propertyName,
        () -> props.addProperty(new PropertyStringImpl(propertyName, propertyValue)));
  }

  private static void setPropertyId(final MutableProperties props, final String propertyName,
      final String propertyValue, final Set<String> filter) {
    applyFilter(filter, propertyName,
        () -> props.addProperty(new PropertyIdImpl(propertyName, propertyValue)));
  }

  private static void setPropertyIds(final MutableProperties props, final String propertyName,
      final List<String> propertyValue, final Set<String> filter) {
    applyFilter(filter, propertyName,
        () -> props.addProperty(new PropertyIdImpl(propertyName, propertyValue)));
  }

  private static void setPropertyDateTime(final MutableProperties props, final String propertyName,
      final long propertyValue, final Set<String> filter) {
    applyFilter(filter, propertyName, () -> props.addProperty(
        new PropertyDateTimeImpl(propertyName, millisToCalendar(propertyValue))));
  }

  private static void applyFilter(final Set<String> filter, final String propertyName,
      Runnable function) {
    if (filter.isEmpty() || (filter.size() == 1 && filter.contains("*")) ||
        filter.contains(propertyName)) {
      function.run();
    }
  }

  /**
   * Converts milliseconds into a {@link GregorianCalendar} object, setting
   * the timezone to GMT and cutting milliseconds off.
   */
  static GregorianCalendar millisToCalendar(long millis) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
    calendar.setTimeInMillis((long) (Math.ceil((double) millis / 1000) * 1000));
    return calendar;
  }

  private static void checkObjectExists(final String objId, final Object obj) {
    if (obj == null) {
      throw new CmisObjectNotFoundException("The object '" + objId + "' doesn't exist");
    }
  }

  private void checkUserPermissions(final User user, final Class<?> objectClass, final String id) {
    AccessControllerRegister accessControllerRegister =
        ServiceProvider.getSingleton(AccessControllerRegister.class);
    final AccessController<String> accessController =
        accessControllerRegister.getAccessController(objectClass);
    if (!accessController.isUserAuthorized(user.getId(), id)) {
      throw new CmisPermissionDeniedException(
          "The user '" + user.getDisplayedName() + "' (id=" + user.getId() + " isn't authorized");
    }
  }

  /**
   * Gets an {@link OrganizationController} object to access, create and modify an object in
   * Silverpeas.
   * @return an {@link OrganizationController} instance.
   */
  protected final OrganizationController getController() {
    return controller;
  }

  /**
   * Gets a {@link CmisObjectFactory} object to create a CMIS object representation of some
   * Silverpeas objects.
   * @return a {@link CmisObjectFactory} instance.
   */
  protected final CmisObjectFactory getObjectFactory() {
    return objectFactory;
  }
}
  