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
import org.silverpeas.cmis.security.AccessControllerRegister;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cmis.CmisContributionsProvider;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.CmisObjectFactory;
import org.silverpeas.core.cmis.model.Folding;
import org.silverpeas.core.cmis.model.Space;
import org.silverpeas.core.cmis.model.TypeId;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract class providing default behaviour of the different methods defined in the
 * {@link CmisObjectsTreeWalker} interface. The more concrete walkers, each of them working on a
 * specific type of a Silverpeas object (and hence on the CMIS object), have to extend this class
 * and implement the methods required by the default behaviour in this class.
 */
public abstract class AbstractCmisObjectsTreeWalker implements CmisObjectsTreeWalker {

  private static final String NOTHING = null;
  private static final List<String> NOTHINGS = null;

  @Inject
  private CmisObjectFactory objectFactory;
  @Inject
  private OrganizationController controller;

  /**
   * Selects an instance of {@link AbstractCmisObjectsTreeWalker} that knows how to browse for
   * and to handle
   * the type of Silverpeas objects the format of the specified unique identifier is related to.
   * @param objectId a unique identifier of a Silverpeas object.
   * <p>
   * This method is dedicated to be used by the concrete type of the abstract
   * {@link AbstractCmisObjectsTreeWalker} class to select the correct next tree walker when
   * walking the
   * CMIS objects tree.
   * </p>
   * @return an instance of a concrete type of {@link AbstractCmisObjectsTreeWalker} that has the
   * knowledge
   * to handle the type of Silverpeas objects referred by the format of the given identifier.
   */
  protected static AbstractCmisObjectsTreeWalker selectInstance(final String objectId) {
    return (AbstractCmisObjectsTreeWalker) ServiceProvider.getSingleton(TreeWalkerSelector.class)
        .selectByObjectId(objectId);
  }

  /**
   * Gets the CMIS representation of the specified localized resource in Silverpeas. This is
   * a shortcut of the following code:
   * <blockquote><pre>
   *  selectInstance(object.getIdentifier().asString()).createCmisObject(object, language);
   * </pre></blockquote>
   * @param resource a localized resource in Silverpeas exposed in the CMIS objects tree
   * @param language the language in which the resource has to be exposed.
   * @param <T> the concrete type of the CMIS object
   * @return @return a {@link CmisObject} instance.
   */
  protected static <T extends CmisObject> T getCmisObject(final LocalizedResource resource,
      final String language) {
    return selectInstance(resource.getIdentifier().asString()).createCmisObject(resource, language);
  }

  /**
   * Is there any {@link AbstractCmisObjectsTreeWalker} implementation that knows how to handle
   * the subtree rooted to the object with the specified unique identifier?
   * @param objectId the unique identifier of a Silverpeas object.
   * @return true if there is a CMIS tree walker knowing how to handle the object related by the
   * specified identifier. False otherwise.
   */
  protected static boolean supports(final String objectId) {
    try {
      AbstractCmisObjectsTreeWalker walker = selectInstance(objectId);
      return walker.isSupported(objectId);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Gets in Silverpeas the object identified by the specified unique identifier. The way how to get
   * such an object depends on its type and it is then delegated to the walker that handles such a
   * type of Silverpeas objects.
   * @param <T> the concrete type of the object to return.
   * @param objectId the unique identifier of an object in Silverpeas.
   * @return a Silverpeas object or null if no such object exists.
   */
  protected abstract <T extends LocalizedResource> T getSilverpeasObjectById(final String objectId);

  /**
   * Gets in Silverpeas all the children objects of the specified parent and that are accessible
   * to the given user. The way how to get such an object depends on its type and it is then
   * delegated to the walker that handles such a type of Silverpeas objects.
   * @param parentId the unique identifier of the parent in Silverpeas.
   * @param user the user for which the children are get.
   * @return a stream over all the allowed children of the specified parent in Silverpeas.
   */
  protected abstract Stream<LocalizedResource> getAllowedChildrenOfSilverpeasObject(
      final ResourceIdentifier parentId, final User user);

  /**
   * Creates the a CMIS object representation of the specified Silverpeas object. The representation
   * depends on the concrete type of the Silverpeas object. The way how create such a CMIS object
   * depends on the type of the Silverpeas object and it is then delegated to the walker that
   * handles such a type of Silverpeas objects.
   * @param <T> the concrete type of the CMIS object.
   * @param resource a localized resource in Silverpeas.
   * @param language the language to use in the localization of the CMIS object.
   * @return a {@link CmisObject} instance.
   */
  protected abstract <T extends CmisObject> T createCmisObject(final LocalizedResource resource,
      final String language);

  /**
   * Is the object with the specified identifier supported by this walker?
   * @param objectId the unique identifier of an object.
   * @return true if the walker knows how to work with objects with such an identifier pattern.
   * False otherwise.
   */
  protected abstract boolean isSupported(final String objectId);

  @Override
  public ObjectData getObjectData(final String objectId, final Filtering filtering) {
    return checkAndDo(objectId, filtering, (o, f) -> {
      final CmisObject cmisObject = createCmisObject(o, f.getLanguage());
      return buildObjectData(cmisObject, f);
    });
  }

  @Override
  public ObjectData getObjectDataByPath(final String path, final Filtering filtering) {
    if (StringUtil.isNotDefined(path) || !path.startsWith(CmisFolder.PATH_SEPARATOR)) {
      throw new IllegalArgumentException("Invalid path: " + path);
    }
    final String[] pathSegments = path.substring(1).split(CmisFolder.PATH_SEPARATOR);
    return walkDownPathForChildData(Space.ROOT_ID, 0, pathSegments, filtering);
  }

  @Override
  public List<ObjectParentData> getParentsData(final String objectId, final Filtering filtering) {
    return checkAndDo(objectId, filtering, this::browseParentsOfObject);
  }

  @Override
  public ObjectInFolderList getChildrenData(final String folderId, final Filtering filtering,
      final Paging paging) {
    return checkAndDo(folderId, filtering, (o, f) -> browseObjectsInFolder(o, f, paging));
  }

  @Override
  public List<ObjectInFolderContainer> getSubTreeData(final String folderId,
      final Filtering filtering, final long depth) {
    if (depth < -1) {
      throw new IllegalArgumentException("Invalid subtree depth level: " + depth);
    }
    return checkAndDo(folderId, filtering, (o, f) -> {
      if (depth == 0) {
        return Collections.emptyList();
      }
      return browseObjectsInFolderTree(o, f, depth);
    });
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
      final LocalizedResource object, final Filtering filtering, final long depth);

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
  protected abstract ObjectInFolderList browseObjectsInFolder(final LocalizedResource object,
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
  protected abstract List<ObjectParentData> browseParentsOfObject(final LocalizedResource object,
      final Filtering filtering);

  /**
   * Browses the different subtrees rooted at the specified objects for all the children accessible
   * by the user in the given filter up to the specified level depth of the trees. The subtrees are
   * all part of a same CMIS objects tree and therefore share a common parent.
   * @param objects the objects in Silverpeas as root of the subtrees to browse.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @param depth the depth of the browsing of the tree.
   * @return a list of {@link ObjectInFolderContainer} elements (the direct children), each
   * of them being a container of others {@link ObjectInFolderContainer} objects (recursive walk of
   * the different tree's nodes) and described by an {@link ObjectInFolderData} instance that
   * is a decorator of an {@link ObjectData} instance (the CMIS data) with its path in the CMIS
   * repository tree (if asked by the filtering). The CMIS data are carried by the
   * {@link ObjectData} object.
   */
  protected final List<ObjectInFolderContainer> browseObjectsInFolderSubTrees(
      final List<LocalizedResource> objects, final Filtering filtering, final long depth) {
    List<ObjectInFolderContainer> tree = new ArrayList<>();
    for (LocalizedResource object : objects) {
      final AbstractCmisObjectsTreeWalker walker =
          AbstractCmisObjectsTreeWalker.selectInstance(object.getIdentifier().asString());
      final CmisFolder child = walker.createCmisObject(object, filtering.getLanguage());
      final ObjectInFolderContainerImpl objectInFolder =
          buildObjectInFolderContainer(child, filtering);
      tree.add(objectInFolder);

      if (depth != 1) {
        final List<ObjectInFolderContainer> children =
            walker.browseObjectsInFolderTree(object, filtering, depth == -1 ? -1 : depth - 1);
        objectInFolder.setChildren(children);
      }
    }
    return tree;
  }

  /**
   * Builds the CMIS data corresponding to all of the specified Silverpeas object by taking into
   * account the filtering that indicates the properties to return and the paging that indicates
   * both from which object the build is started and the number of objects to build (and return).
   * @param objects the objects in Silverpeas to expose and therefore for which a CMIS object
   * representation has to be built.
   * @param filtering the filtering rules to apply on the data to build.
   * @param paging the paging to apply on the elements of the list.
   * @return an {@link ObjectInFolderList} instance that is a list of {@link ObjectInFolderData}
   * elements, each of them being a decorator of an {@link ObjectData} instance with its
   * path in the CMIS repository tree (if asked by the filtering). The CMIS data are carried by the
   * {@link ObjectData} object. The size and the content of the list is conditioned by the paging
   * rules.
   */
  protected final ObjectInFolderList buildObjectInFolderList(final List<LocalizedResource> objects,
      final Filtering filtering, final Paging paging) {
    final List<ObjectInFolderData> children = objects.stream()
        .skip(paging.getSkipCount().longValue())
        .limit(paging.getMaxItems().longValue())
        .map(o -> {
          CmisFolder cmisObject =
              AbstractCmisObjectsTreeWalker.getCmisObject(o, filtering.getLanguage());
          return buildObjectInFolderData(cmisObject, filtering);
        })
        .collect(Collectors.toList());

    final ObjectInFolderListImpl childrenInList = new ObjectInFolderListImpl();
    childrenInList.setHasMoreItems(
        paging.getSkipCount().longValue() + children.size() < objects.size());
    childrenInList.setObjects(children);
    childrenInList.setNumItems(BigInteger.valueOf(objects.size()));
    return childrenInList;
  }

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

  private ObjectData walkDownPathForChildData(final ResourceIdentifier parentId, final int idx,
      final String[] pathSegments, final Filtering filtering) {
    final User user = filtering.getCurrentUser();
    final String language = user.getUserPreferences().getLanguage();
    final LocalizedResource object = getAllowedChildrenOfSilverpeasObject(parentId, user).filter(
        s -> s.getTranslation(language).getName().equals(pathSegments[idx]))
        .findFirst()
        .orElseThrow(() -> new CmisObjectNotFoundException(
            "No such object with name '" + pathSegments[idx] + "' in the path " +
                String.join(CmisFolder.PATH_SEPARATOR, pathSegments)));
    ResourceIdentifier objectId = object.getIdentifier();
    final AbstractCmisObjectsTreeWalker walker =
        AbstractCmisObjectsTreeWalker.selectInstance(objectId.asString());
    if (idx >= pathSegments.length - 1) {
      CmisObject cmisObject = walker.createCmisObject(object, filtering.getLanguage());
      return buildObjectData(cmisObject, filtering);
    } else {
      return walker.walkDownPathForChildData(objectId, idx + 1, pathSegments, filtering);
    }
  }

  private <T> T checkAndDo(final String objectId, final Filtering filtering,
      BiFunction<LocalizedResource, Filtering, T> action) {
    final LocalizedResource object = getSilverpeasObjectById(objectId);
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
      List<String> types =
          folder.getAllowedChildrenTypes().stream().map(TypeId::value).collect(Collectors.toList());
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

  /**
   * Gets the provider of the user contributions managed by the specified application.
   * @param appId the unique identifier of a component instance, id est of an application in
   * Silverpeas.
   * @return a {@link CmisContributionsProvider} instance.
   */
  protected CmisContributionsProvider getContributionsProvider(final String appId) {
    return ServiceProvider.getServiceByComponentInstanceAndNameSuffix(appId,
        CmisContributionsProvider.Constants.NAME_SUFFIX);
  }
}
  