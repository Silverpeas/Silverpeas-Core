/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.cmis.util.CmisProperties;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cmis.model.CmisFile;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.CmisObjectFactory;
import org.silverpeas.core.cmis.model.DocumentFile;
import org.silverpeas.core.cmis.model.Folding;
import org.silverpeas.core.cmis.model.Space;
import org.silverpeas.core.cmis.model.TypeId;
import org.silverpeas.core.contribution.model.Attachment;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.cmis.model.CmisFile.PATH_SEPARATOR;

/**
 * Abstract class providing default behaviour of the different methods defined in the {@link
 * CmisObjectsTreeWalker} interface. The more concrete walkers, each of them working on a specific
 * type of a Silverpeas object (and hence on the CMIS object), have to extend this class and
 * implement the methods required by the default behaviour in this class.
 */
public abstract class AbstractCmisObjectsTreeWalker implements CmisObjectsTreeWalker {

  @Inject
  private CmisObjectFactory objectFactory;
  @Inject
  private OrganizationController controller;

  @Inject
  private TreeWalkerSelector selector;

  /**
   * Creates a new {@link CmisObject} instance from the specified CMIS data properties and in the
   * given language. This method is to be called by the {@link #createChildData(String,
   * CmisProperties, ContentStream, String)} method.
   * <p>
   * @param properties the CMIS properties of the object to create.
   * @param contentStream a stream on a content from which a document has to be created.
   * @param language the ISO 639-1 code of the language in which the textual properties of the
   * object as well as the content (if any) are expressed.
   * @return the created {@link CmisObject} object corresponding to the given CMIS properties.
   * @see #createChildData(String, CmisProperties, ContentStream, String)
   */
  protected abstract CmisObject createObjectData(CmisProperties properties,
      ContentStream contentStream, String language);

  /**
   * Encodes the specified Silverpeas object into its CMIS counterpart. The CMIS representation
   * depends on the concrete type of the Silverpeas object. The way how create such a CMIS object
   * depends on the type of the Silverpeas object and it is then delegated to the walker that
   * handles such a type of Silverpeas objects.
   *
   * @param <T>      the concrete type of the CMIS object.
   * @param resource a localized resource in Silverpeas.
   * @param language the language to use in the localization of the CMIS object.
   * @return a {@link CmisObject} instance.
   */
  protected abstract <T extends CmisObject> T encodeToCmisObject(final LocalizedResource resource,
      final String language);

  /**
   * Is the object with the specified identifier supported by this walker?
   * @param objectId the unique identifier of an object.
   * @return true if the walker knows how to work with objects with such an identifier pattern.
   * False otherwise.
   */
  protected abstract boolean isObjectSupported(final String objectId);

  /**
   * Is the specified CMIS type supported by this walker?
   * @param typeId the unique identifier of a CMIS type.
   * @return true if the walker knows how to work with objects of the specified CMIS type. False
   * otherwise.
   */
  protected abstract boolean isTypeSupported(final TypeId typeId);

  @Override
  public CmisObject getObjectData(final String objectId, final Filtering filtering) {
    return checkAndDo(objectId, filtering, (o, f) -> {
      final CmisObject cmisObject = encodeToCmisObject(o, f.getLanguage());
      setObjectDataFields(cmisObject, f);
      return cmisObject;
    });
  }

  @Override
  public CmisFile getObjectDataByPath(final String path, final Filtering filtering) {
    if (StringUtil.isNotDefined(path) || !path.startsWith(PATH_SEPARATOR)) {
      throw new IllegalArgumentException("Invalid path: " + path);
    }
    final String[] pathSegments = path.substring(1).split(PATH_SEPARATOR);
    return walkDownPathForChildData(Space.ROOT_ID, 0, pathSegments, filtering);
  }

  @Override
  public List<ObjectParentData> getParentsData(final String objectId, final Filtering filtering) {
    return checkAndDo(objectId, filtering, this::browseParentsOfObject);
  }

  /**
   * Gets the CMIS representation of the specified localized resource in Silverpeas. This is a
   * shortcut of the following code:
   * <blockquote><pre>
   *  selectInstance(object.getIdentifier().asString()).createCmisObject(object, language);
   * </pre></blockquote>
   *
   * @param resource a localized resource in Silverpeas exposed in the CMIS objects tree
   * @param language the language in which the resource has to be exposed.
   * @param <T>      the concrete type of the CMIS object
   * @return @return a {@link CmisObject} instance.
   */
  protected final <T extends CmisObject> T getCmisObject(final LocalizedResource resource, final String language) {
    return getTreeWalkerSelector().selectByObjectIdOrFail(resource.getIdentifier().asString()).encodeToCmisObject(resource, language);
  }

  /**
   * Browses for the parents in the CMIS objects tree of the specified Silverpeas object. In a CMIS
   * objects tree, a child can have direct one or several parents. This method returns all of them.
   * However, as the browsing of the CMIS objects tree depends on the type of its node and hence on
   * the type of the objects in Silverpeas, the browsing is delegated to the walker that knows how
   * to handle such a Silverpeas object.
   *
   * @param object    an identifiable object in Silverpeas.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @return a list of {@link ObjectParentData} elements, each of them being a wrapper of an {@link
   * ObjectData} instance with the path of the specified object relative to this parent. The CMIS
   * data are carried by the {@link ObjectData} object. If the specified object isn't file-able or
   * it is the root folder (the virtual root space in Silverpeas), then an empty list is returned.
   */
  protected abstract List<ObjectParentData> browseParentsOfObject(final LocalizedResource object,
      final Filtering filtering);

  /**
   * Gets in Silverpeas the object identified by the specified unique identifier. The way how to get
   * such an object depends on its type and it is then delegated to the walker that handles such a
   * type of Silverpeas objects.
   *
   * @param <T>      the concrete type of the object to return.
   * @param objectId the unique identifier of an object in Silverpeas.
   * @return a Silverpeas object or null if no such object exists.
   */
  protected abstract <T extends LocalizedResource & Securable> T getSilverpeasObjectById(final String objectId);

  /**
   * Builds the CMIS data corresponding to all of the specified Silverpeas object by taking into
   * account the filtering that indicates the properties to return and the paging that indicates
   * both from which object the build is started and the number of objects to build (and return).
   *
   * @param objects   the objects in Silverpeas to expose and therefore for which a CMIS object
   *                  representation has to be built.
   * @param filtering the filtering rules to apply on the data to build.
   * @param paging    the paging to apply on the elements of the list.
   * @return an {@link ObjectInFolderList} instance that is a list of {@link ObjectInFolderData}
   * elements, each of them being a decorator of an {@link ObjectData} instance with its path in the
   * CMIS repository tree (if asked by the filtering). The CMIS data are carried by the {@link
   * ObjectData} object. The size and the content of the list is conditioned by the paging rules.
   */
  protected final ObjectInFolderList buildObjectInFolderList(final List<LocalizedResource> objects,
      final Filtering filtering, final Paging paging) {
    final List<ObjectInFolderData> children = objects.stream()
        .skip(paging.getSkipCount().longValue())
        .limit(paging.getMaxItems().longValue())
        .map(o -> {
          CmisFile cmisObject = getCmisObject(o, filtering.getLanguage());
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
   * Gets in Silverpeas all the children objects of the specified parent and that are accessible to
   * the given user. The way how to get such an object depends on its type and it is then delegated
   * to the walker that handles such a type of Silverpeas objects.
   *
   * @param parentId the unique identifier of the parent in Silverpeas.
   * @param user     the user for which the children are get.
   * @return a stream over all the allowed children of the specified parent in Silverpeas.
   */
  protected abstract Stream<LocalizedResource> getAllowedChildrenOfSilverpeasObject(final ResourceIdentifier parentId, final User user);

  /**
   * Builds the CMIS data both for the specified CMIS folder and recursively for all of its children
   * by taking into account the filtering that indicates the properties to return.
   *
   * @param file      a CMIS file representing a Silverpeas object.
   * @param filtering the filtering rules to apply on the data to build.
   * @return an {@link ObjectInFolderContainerImpl} instance representing the CMIS data of the
   * folder plus the CMIS data of its direct children, and so on for each of its children.
   */
  protected final ObjectInFolderContainerImpl buildObjectInFolderContainer(final CmisFile file,
      final Filtering filtering) {
    ObjectInFolderData objectInFolder = buildObjectInFolderData(file, filtering);
    ObjectInFolderContainerImpl container = new ObjectInFolderContainerImpl();
    container.setObject(objectInFolder);
    return container;
  }

  @Override
  public ObjectInFolderList getChildrenData(final String folderId, final Filtering filtering, final Paging paging) {
    return checkAndDo(folderId, filtering, (o, f) -> browseObjectsInFolder(o, f, paging));
  }

  /**
   * The specified CMIS objects in Silverpeas implement the {@link ObjectData} interface that
   * defines the specific CMIS attributes and properties a CMIS object should have. Sets these data
   * by taking into account the specified filtering rules.
   *
   * @param object    a CMIS object.
   * @param filtering the filtering rules to apply on the data to set.
   */
  protected final void setObjectDataFields(final CmisObject object, final Filtering filtering) {
    if (filtering.isACLToBeIncluded()) {
      object.addACEs(filtering.getCurrentUser());
    }

    if (filtering.areAllowedActionsToBeIncluded()) {
      // should depend on the type of the object
      object.setAllowableActions();
    }

    setProperties(object, filtering);
  }

  @Override
  public List<ObjectInFolderContainer> getSubTreeData(final String folderId, final Filtering filtering, final long depth) {
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

  private String getObjectName(final LocalizedResource object, final String language) {
    if (object instanceof Attachment) {
      return ((Attachment) object).getFilename();
    } else {
      return object.getTranslation(language).getName();
    }
  }

  /**
   * Browses for the objects in the CMIS tree rooted at the specified object in Silverpeas. The
   * browsing depends on the type of the specified object in Silverpeas and it is then delegated to
   * the walker that knows how to browse a tree rooted at such a tree.
   *
   * @param object    an identifiable object in Silverpeas.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @param depth     the depth of the browsing of the tree.
   * @return a list of {@link ObjectInFolderContainer} elements (the direct children), each of them
   * being a container of others {@link ObjectInFolderContainer} objects (recursive walk of the
   * different tree's nodes) and described by an {@link ObjectInFolderData} instance that is a
   * decorator of an {@link ObjectData} instance (the CMIS data) with its path in the CMIS
   * repository tree (if asked by the filtering). The CMIS data are carried by the {@link
   * ObjectData} object.
   */
  protected abstract List<ObjectInFolderContainer> browseObjectsInFolderTree(final LocalizedResource object, final Filtering filtering, final long depth);

  /**
   * Browses for the direct children in the CMIS folder represented by the specified Silverpeas
   * object. the browsing of children depends on the type of the specified object in Silverpeas and
   * it is then delegated to the walker that knows how to browse for children such an object.
   *
   * @param object    an identifiable object in Silverpeas.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @param paging    the paging to apply on the elements of the list.
   * @return an {@link ObjectInFolderList} instance that is a list of {@link ObjectInFolderData}
   * elements, each of them being a decorator of an {@link ObjectData} instance with its path in the
   * CMIS repository tree (if asked by the filtering). The CMIS data are carried by the {@link
   * ObjectData} object.
   */
  protected abstract ObjectInFolderList browseObjectsInFolder(final LocalizedResource object, final Filtering filtering, final Paging paging);

  /**
   * Browses the different subtrees rooted at the specified objects for all the children accessible
   * by the user in the given filter up to the specified level depth of the trees. The subtrees are
   * all part of a same CMIS objects tree and therefore share a common parent.
   *
   * @param objects   the objects in Silverpeas as root of the subtrees to browse.
   * @param filtering the filtering rules to apply on the CMIS data to return.
   * @param depth     the depth of the browsing of the tree.
   * @return a list of {@link ObjectInFolderContainer} elements (the direct children), each of them
   * being a container of others {@link ObjectInFolderContainer} objects (recursive walk of the
   * different tree's nodes) and described by an {@link ObjectInFolderData} instance that is a
   * decorator of an {@link ObjectData} instance (the CMIS data) with its path in the CMIS
   * repository tree (if asked by the filtering). The CMIS data are carried by the {@link
   * ObjectData} object.
   */
  protected final List<ObjectInFolderContainer> browseObjectsInFolderSubTrees(final List<LocalizedResource> objects, final Filtering filtering, final long depth) {
    List<ObjectInFolderContainer> tree = new ArrayList<>();
    for (LocalizedResource object : objects) {
      final AbstractCmisObjectsTreeWalker walker = getTreeWalkerSelector().selectByObjectIdOrFail(object.getIdentifier().asString());
      final CmisFile child = walker.encodeToCmisObject(object, filtering.getLanguage());
      final ObjectInFolderContainerImpl objectInFolder = buildObjectInFolderContainer(child, filtering);
      tree.add(objectInFolder);

      if (depth != 1 && child.isFolding()) {
        final List<ObjectInFolderContainer> children = walker.browseObjectsInFolderTree(object, filtering, depth == -1 ? -1 : depth - 1);
        objectInFolder.setChildren(children);
      }
    }
    return tree;
  }

  /**
   * Builds the CMIS data corresponding to the specified CMIS folder and by taking into account the
   * filtering that indicates the properties to return.
   * @param file a CMIS file representing a Silverpeas object.
   * @param filtering the filtering rules to apply on the data to build.
   * @return an {@link ObjectInFolderData} object representing the CMIS data of the folder plus its
   * path segment in the CMIS objects tree (if not excluded by the filtering rules).
   */
  protected final ObjectInFolderData buildObjectInFolderData(final CmisFile file,
      final Filtering filtering) {
    setObjectDataFields(file, filtering);
    ObjectInFolderDataImpl objectInFolderData = new ObjectInFolderDataImpl();
    objectInFolderData.setObject(file);
    if (filtering.isPathSegmentToBeIncluded()) {
      objectInFolderData.setPathSegment(file.getPathSegment());
    }
    return objectInFolderData;
  }

  /**
   * Builds the CMIS data corresponding to the given parent of the specified folder and by taking
   * into account the filtering that indicates the properties to return.
   *
   * @param parent    a CMIS folder parent of the below other CMIS folder. It represents a
   *                  Silverpeas object, itself parent of the Silverpeas object represented by the
   *                  below CMIS folder.
   * @param folder    a CMIS folder representing a Silverpeas object.
   * @param filtering the filtering rules to apply on the data to build.
   * @return an {@link ObjectParentData} object representing the CMIS data of the parent plus the
   * path segment of the child folder relative to the parent in the CMIS objects tree (if not
   * excluded by the filtering rules).
   */
  protected final ObjectParentData buildObjectParentData(final CmisFolder parent, final CmisObject folder, final Filtering filtering) {
    setObjectDataFields(parent, filtering);
    ObjectParentDataImpl parentData = new ObjectParentDataImpl(parent);
    if (filtering.isPathSegmentToBeIncluded()) {
      parentData.setRelativePathSegment(folder.getName());
    }
    return parentData;
  }

  private CmisFile walkDownPathForChildData(final ResourceIdentifier parentId, final int idx, final String[] pathSegments, final Filtering filtering) {
    final User user = filtering.getCurrentUser();
    final String language = user.getUserPreferences()
        .getLanguage();
    final LocalizedResource object = getAllowedChildrenOfSilverpeasObject(parentId, user).filter(
            s -> getObjectName(s, language).equals(pathSegments[idx]))
        .findFirst()
        .orElseThrow(() -> new CmisObjectNotFoundException(
            "No such object with name '" + pathSegments[idx] + "' in the path " + PATH_SEPARATOR +
                String.join(PATH_SEPARATOR, pathSegments)));
    ResourceIdentifier objectId = object.getIdentifier();
    final AbstractCmisObjectsTreeWalker walker =
        getTreeWalkerSelector().selectByObjectIdOrFail(objectId.asString());
    if (idx >= pathSegments.length - 1) {
      CmisFile cmisFile = walker.encodeToCmisObject(object, filtering.getLanguage());
      setObjectDataFields(cmisFile, filtering);
      return cmisFile;
    } else {
      return walker.walkDownPathForChildData(objectId, idx + 1, pathSegments, filtering);
    }
  }

  private <T extends LocalizedResource & Securable, R> R checkAndDo(final String objectId, final Filtering filtering, BiFunction<T, Filtering, R> action) {
    final T object = getSilverpeasObjectById(objectId);
    checkObjectExists(objectId, object);
    checkUserPermissions(filtering.getCurrentUser(), object);
    return action.apply(object, filtering);
  }

  private void setProperties(final CmisObject object, final Filtering filtering) {
    Set<String> filter = filtering.getPropertiesFilter();
    CmisProperties properties = new CmisProperties(new PropertiesImpl(), filter);
    properties.setObjectId(object.getId())
        .setObjectTypeId(object.getTypeId())
        .setDefaultProperties()
        .setName(object.getName())
        .setDescription(object.getDescription())
        .setCreationData(object.getCreator(), object.getCreationDate())
        .setLastModificationData(object.getLastModifier(), object.getLastModificationDate());

    if (object.isFileable()) {
      CmisFile file = (CmisFile) object;
      if (file.isFolding()) {
        // folder properties
        final Folding folder = (Folding) file;
        List<String> types = folder.getAllowedChildrenTypes()
            .stream()
            .map(TypeId::value)
            .collect(Collectors.toList());
        properties.setAllowedChildObjectTypeIds(types).setParentObjectId(folder.getParentId())
            .setPath(folder.getPath());
      } else {
        // document properties
        DocumentFile document = (DocumentFile) file;
        String versionSeriesId = document.getId() + ":versions";
        String streamContentId = document.getName() == null ? null : document.getId() + ":content";
        properties.setImmutability(false)
            .setVersioningData(versionSeriesId, null, document.getLastComment(), true, true, true)
            .setPWCData(false, false, null, null)
            .setContent(streamContentId, document.getMimeType(), document.getSize(),
                document.getName());
      }
    }
    object.setProperties(properties.getProperties());
  }

  private static void checkObjectExists(final String objId, final Object obj) {
    if (obj == null) {
      throw new CmisObjectNotFoundException("The object '" + objId + "' doesn't exist");
    }
  }

  private void checkUserPermissions(final User user, final Securable object) {
    if (!object.canBeAccessedBy(user)) {
      throw new CmisPermissionDeniedException(
          "The user '" + user.getDisplayedName() + "' (id=" + user.getId() + " isn't authorized");
    }
  }

  /**
   * Gets an {@link OrganizationController} object to access, create and modify an object in
   * Silverpeas.
   *
   * @return an {@link OrganizationController} instance.
   */
  protected final OrganizationController getController() {
    return controller;
  }

  /**
   * Gets a {@link CmisObjectFactory} object to create a CMIS object representation of some
   * Silverpeas objects.
   *
   * @return a {@link CmisObjectFactory} instance.
   */
  protected final CmisObjectFactory getObjectFactory() {
    return objectFactory;
  }

  /**
   * Gets a {@link TreeWalkerSelector} object to select the correct {@link CmisObjectsTreeWalker}
   * object able to work on given CMIS objects.
   * @return a {@link TreeWalkerSelector} instance.
   */
  protected final TreeWalkerSelector getTreeWalkerSelector() {
    return selector;
  }
}
  