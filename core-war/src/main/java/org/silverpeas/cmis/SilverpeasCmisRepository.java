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

package org.silverpeas.cmis;

import org.apache.chemistry.opencmis.commons.BasicPermissions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.*;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CreatablePropertyTypesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.NewTypeSettableAttributesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.cmis.model.CmisFile;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.Space;
import org.silverpeas.core.util.URLUtil;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A CMIS repository implementation for Silverpeas. Its responsibility is to provide an access
 * to the data in Silverpeas that is conform to the CMIS 1.1 specification and by taking into
 * account of the CMIS service capabilities that are supported as well as of its constrained support
 * of those capabilities.
 * <p>
 * Behind the scene, it defines the capabilities actually supported and
 * delegates the knowledge of how the CMIS objects and the Silverpeas objects are mapped to a
 * manager. The access to the Silverpeas objects is done through this manager and it is
 * controlled by the repository itself according to the supported CMIS service capabilities.
 * </p>
 * @author mmoquillon
 */
@Repository
public class SilverpeasCmisRepository {

  private String repoId;

  @Inject
  private SilverpeasCmisTypeManager typeManager;
  @Inject
  private SilverpeasCmisObjectManager objectManager;
  @Inject
  private SilverpeasCmisSettings settings;

  @PostConstruct
  protected void init() {
    repoId = settings.getRepositoryId();
  }

  /**
   * Gets the unique identifier of this repository.
   * @return the repository's unique identifier.
   */
  public String getId() {
    return this.repoId;
  }

  /**
   * Gets information about the CMIS repository, the optional capabilities
   * it supports and its access control information if applicable.
   * @return the repository info
   */
  public RepositoryInfo getRepositoryInfo() {
    RepositoryInfoImpl repositoryInfo = new RepositoryInfoImpl();

    repositoryInfo.setId(repoId);
    repositoryInfo.setName(settings.getRepositoryName());
    repositoryInfo.setDescription(settings.getRepositoryDescription());
    repositoryInfo.setCmisVersionSupported(CmisVersion.CMIS_1_1.value());
    repositoryInfo.setProductName("Silverpeas Collaborative Portal");
    repositoryInfo.setProductVersion(URLUtil.getSilverpeasVersion());
    repositoryInfo.setVendorName("Silverpeas");
    repositoryInfo.setRootFolder(Space.ROOT_ID.asString());
    repositoryInfo.setThinClientUri("");
    repositoryInfo.setChangesIncomplete(true);

    final RepositoryCapabilitiesImpl capabilities = new RepositoryCapabilitiesImpl();
    setNavigationCapabilities(capabilities);
    setObjectCapabilities(capabilities);
    setFilingCapabilities(capabilities);
    setVersioningCapabilities(capabilities);
    setQueryCapabilities(capabilities);
    setTypeCapabilities(capabilities);
    setAclCapabilities(capabilities);
    repositoryInfo.setCapabilities(capabilities);

    final AclCapabilitiesDataImpl aclCapability = new AclCapabilitiesDataImpl();
    setAccessControl(aclCapability);
    repositoryInfo.setAclCapabilities(aclCapability);

    return repositoryInfo;
  }

  /**
   * Gets the list of object types defined for the repository that are
   * children of the specified type.
   *
   * @param typeId
   *            <em>(optional)</em> the typeId of an object type specified in
   *            the repository (if not specified the repository MUST return
   *            all base object types)
   * @param includePropertyDefinitions
   *            <em>(optional)</em> if <code>true</code> the repository MUST
   *            return the property definitions for each object type returned
   *            (default is <code>false</code>)
   * @param maxItems
   *            <em>(optional)</em> the maximum number of items to return in a
   *            response (default is repository specific)
   * @param skipCount
   *            <em>(optional)</em> number of potential results that the
   *            repository MUST skip/page over before returning any results
   *            (default is 0)
   * @return the list of type children
   */
  public TypeDefinitionList getTypeChildren(final String typeId,
      final Boolean includePropertyDefinitions, final BigInteger maxItems,
      final BigInteger skipCount) {
    return typeManager.getSubTypeDefinitionsOf(typeId, includePropertyDefinitions,
        new Paging(skipCount, maxItems));
  }

  /**
   * Gets the definition of the specified object type.
   * @param typeId
   *            typeId of an object type specified in the repository
   * @return the type definition
   */
  public TypeDefinition getTypeDefinition(final String typeId) {
    return typeManager.getTypeDefinition(typeId);
  }

  /**
   * Gets the list of child objects contained in the specified folder.
   *
   * @param folderId
   *            the identifier for the folder
   * @param filter
   *            <em>(optional)</em> a comma-separated list of query names that
   *            defines which properties must be returned by the repository
   *            (default is repository specific)
   * @param includeAllowableActions
   *            <em>(optional)</em> if {@code true}, then the repository must
   *            return the available actions for each object in the result set
   *            (default is {@code false})
   * @param includeRelationships
   *            <em>(optional)</em> indicates what relationships in which the
   *            objects participate must be returned (default is
   *            {@link IncludeRelationships#NONE})
   * @param includePathSegment
   *            <em>(optional)</em> if {@code true}, returns a path segment
   *            for each child object for use in constructing that object's
   *            path (default is {@code false})
   * @param maxItems
   *            <em>(optional)</em> the maximum number of items to return in a
   *            response (default is repository specific)
   * @param skipCount
   *            <em>(optional)</em> number of potential results that the
   *            repository MUST skip/page over before returning any results
   *            (default is 0)
   * @return the list of children. Each child is a {@link org.silverpeas.core.cmis.model.CmisFile}
   * instance.
   */
  public ObjectInFolderList getChildren(final String folderId, final String filter,
      final Boolean includeAllowableActions, final IncludeRelationships includeRelationships,
      final Boolean includePathSegment, final BigInteger maxItems, final BigInteger skipCount) {
    final Filtering filtering = new Filtering().setPropertiesFilter(filter)
        .setIncludeAllowableActions(includeAllowableActions)
        .setIncludePathSegment(includePathSegment)
        .setIncludeRelationships(includeRelationships);
    return objectManager.getChildren(folderId, filtering, new Paging(skipCount, maxItems));
  }

  /**
   * Gets the set of descendant objects contained in the specified folder or
   * any of its child folders. The difference with {
   * @link #getFolderTree(String, BigInteger, String, Boolean, IncludeRelationships, Boolean)} is
   * that this method returns both folders and documents.
   *
   * @param folderId
   *            the identifier for the folder
   * @param depth
   *            the number of levels of depth in the folder hierarchy from
   *            which to return results. If null, by default set at -1 meaning returning only the
   *            direct children of the folder.
   * @param filter
   *            <em>(optional)</em> a comma-separated list of query names that
   *            defines which properties must be returned by the repository
   *            (default is repository specific)
   * @param includeAllowableActions
   *            <em>(optional)</em> if {@code true}, then the repository must
   *            return the available actions for each object in the result set
   *            (default is {@code false})
   * @param includeRelationships
   *            <em>(optional)</em> indicates what relationships in which the
   *            objects participate must be returned (default is
   *            {@link IncludeRelationships#NONE})
   * @param includePathSegment
   *            <em>(optional)</em> if {@code true}, returns a path segment
   *            for each child object for use in constructing that object's
   *            path (default is {@code false})
   * @return the tree of descendants. Each descendent carried by the
   * {@link org.apache.chemistry.opencmis.commons.data.ObjectInFolderData} objects in the listed
   * containers is a {@link org.silverpeas.core.cmis.model.CmisFile} instance.
   **/
  public List<ObjectInFolderContainer> getDescendants(final String folderId, final BigInteger depth,
      final String filter, final Boolean includeAllowableActions,
      final IncludeRelationships includeRelationships, final Boolean includePathSegment) {
    return getObjectInFolderContainers(folderId, depth, filter, includeAllowableActions,
        includeRelationships, includePathSegment, Filtering.IncludeCmisObjectTypes.ALL);
  }

  /**
   * Gets the set of descendant folder objects contained in the specified
   * folder. The difference with
   * {@link #getDescendants(String, BigInteger, String, Boolean, IncludeRelationships, Boolean)} is
   * that this method returns only folders and not documents.
   *
   * @param folderId
   *            the identifier for the folder
   * @param depth
   *            the number of levels of depth in the folder hierarchy from
   *            which to return results
   * @param filter
   *            <em>(optional)</em> a comma-separated list of query names that
   *            defines which properties must be returned by the repository
   *            (default is repository specific)
   * @param includeAllowableActions
   *            <em>(optional)</em> if {@code true}, then the repository must
   *            return the available actions for each object in the result set
   *            (default is {@code false})
   * @param includeRelationships
   *            <em>(optional)</em> indicates what relationships in which the
   *            objects participate must be returned (default is
   *            {@link IncludeRelationships#NONE})
   * @param includePathSegment
   *            <em>(optional)</em> if {@code true}, returns a path segment
   *            for each child object for use in constructing that object's
   *            path (default is {@code false})
   * @return the folder tree. Each descendent carried by the
   * {@link org.apache.chemistry.opencmis.commons.data.ObjectInFolderData} objects in the
   * listed containers is a {@link org.silverpeas.core.cmis.model.CmisFolder} instance.
   **/
  public List<ObjectInFolderContainer> getFolderTree(String folderId, BigInteger depth, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      Boolean includePathSegment) {
    return getObjectInFolderContainers(folderId, depth, filter, includeAllowableActions,
        includeRelationships, includePathSegment, Filtering.IncludeCmisObjectTypes.ONLY_FOLDERS);
  }

  /**
   * Gets the parent folder(s) for the specified fileable object.
   *
   * @param objectId
   *            the identifier for the object
   * @param filter
   *            <em>(optional)</em> a comma-separated list of query names that
   *            defines which properties must be returned by the repository
   *            (default is repository specific)
   * @param includeAllowableActions
   *            <em>(optional)</em> if {@code true}, then the repository must
   *            return the available actions for each object in the result set
   *            (default is {@code false})
   * @param includeRelativePathSegment
   *            <em>(optional)</em> if {@code true}, returns a relative path
   *            segment for each parent object for use in constructing that
   *            object's path (default is {@code false})
   * @return the list of parents. Each parent is a {@link org.silverpeas.core.cmis.model.CmisFolder}
   * instance.
   */
  public List<ObjectParentData> getObjectParents(final String objectId, final String filter,
      final Boolean includeAllowableActions, final Boolean includeRelativePathSegment) {
    final Filtering filtering = new Filtering().setPropertiesFilter(filter)
        .setIncludeAllowableActions(includeAllowableActions)
        .setIncludePathSegment(includeRelativePathSegment);
    return objectManager.getParents(objectId, filtering);
  }

  /**
   * Gets the specified information for the object specified by its unique identifier.
   *
   * @param objectId
   *            the identifier for the object
   * @param filter
   *            <em>(optional)</em> a comma-separated list of query names that
   *            defines which properties must be returned by the repository
   *            (default is repository specific)
   * @param includeAllowableActions
   *            <em>(optional)</em> if {@code true}, then the repository must
   *            return the allowable actions for the object (default is
   *            {@code false})
   * @param includeRelationships
   *            <em>(optional)</em> indicates what relationships in which the
   *            object participates must be returned (default is
   *            {@link IncludeRelationships#NONE})
   * @param includeAcl
   *            <em>(optional)</em> if {@code true}, then the repository must
   *            return the ACL for the object (default is {@code false})
   * @return the CMIS object.
   */
  public CmisObject getObject(final String objectId, final String filter,
      final Boolean includeAllowableActions, final IncludeRelationships includeRelationships,
      final Boolean includeAcl) {
    final Filtering filtering = new Filtering().setPropertiesFilter(filter)
        .setIncludeAllowableActions(includeAllowableActions)
        .setIncludeAcl(includeAcl)
        .setIncludeRelationships(includeRelationships);
    return objectManager.getObject(objectId, filtering);
  }

  /**
   * Gets the specified information for the object specified by path.
   *
   * @param path
   *            the path to the object
   * @param filter
   *            <em>(optional)</em> a comma-separated list of query names that
   *            defines which properties must be returned by the repository
   *            (default is repository specific)
   * @param includeAllowableActions
   *            <em>(optional)</em> if {@code true}, then the repository must
   *            return the allowable actions for the object (default is
   *            {@code false})
   * @param includeRelationships
   *            <em>(optional)</em> indicates what relationships in which the
   *            object participates must be returned (default is
   *            {@link IncludeRelationships#NONE})
   * @param includeAcl
   *            <em>(optional)</em> if {@code true}, then the repository must
   *            return the ACL for the object (default is {@code false})
   * @return the CMIS file-able object located at the given path.
   */
  public CmisFile getObjectByPath(final String path, final String filter,
      final Boolean includeAllowableActions, final IncludeRelationships includeRelationships,
      final Boolean includeAcl) {
    final Filtering filtering = new Filtering().setPropertiesFilter(filter)
        .setIncludeAllowableActions(includeAllowableActions)
        .setIncludeAcl(includeAcl)
        .setIncludeRelationships(includeRelationships);
    return objectManager.getObjectByPath(path, filtering);
  }

  /**
   * Gets the content stream for the specified document object.
   *
   * @param objectId
   *            the identifier for the object
   * @param offset
   *            the position in bytes in the document content from which the stream to return
   *            should start.
   * @param length the length in bytes of the stream to return.
   * @return the content stream
   */
  public ContentStream getContentStream(final String objectId, final BigInteger offset,
      final BigInteger length) {
    long start = offset == null ? 0 : offset.longValue();
    long size = length == null ? -1 : length.longValue();
    User currentUser = User.getCurrentRequester();
    String language = currentUser.getUserPreferences().getLanguage();
    return objectManager.getContentStream(objectId, language, start, size);
  }

  private List<ObjectInFolderContainer> getObjectInFolderContainers(final String folderId,
      final BigInteger depth, final String filter, final Boolean includeAllowableActions,
      final IncludeRelationships includeRelationships, final Boolean includePathSegment,
      final Filtering.IncludeCmisObjectTypes includeCmisObjectTypes) {
    final long actualDepth = depth == null ? 1 : depth.longValue();
    if (actualDepth == 0 || actualDepth < -1) {
      throw new CmisInvalidArgumentException("The depth value is incorrect: " + actualDepth);
    }
    final Filtering filtering = new Filtering().setPropertiesFilter(filter)
        .setIncludeAllowableActions(includeAllowableActions)
        .setIncludePathSegment(includePathSegment)
        .setIncludeRelationships(includeRelationships)
        .setIncludeCmisObjectTypes(includeCmisObjectTypes);
    return objectManager.getDescendants(folderId, filtering, actualDepth);
  }

  private static void setAccessControl(final AclCapabilitiesDataImpl aclCapability) {
    aclCapability.setSupportedPermissions(SupportedPermissions.BOTH);
    aclCapability.setAclPropagation(AclPropagation.PROPAGATE);

    // permissions
    final List<PermissionDefinition> permissions = new ArrayList<>();
    permissions.add(aPermission(BasicPermissions.READ, "Read"));
    permissions.add(aPermission(BasicPermissions.WRITE, "Write"));
    permissions.add(aPermission(BasicPermissions.ALL, "All"));
    aclCapability.setPermissionDefinitionData(permissions);

    // mappings table
    final List<PermissionMapping> mappings = new ArrayList<>();
    mappings.add(aMapping(PermissionMapping.CAN_GET_DESCENDENTS_FOLDER, BasicPermissions.READ));
    mappings.add(aMapping(PermissionMapping.CAN_GET_CHILDREN_FOLDER, BasicPermissions.READ));
    mappings.add(aMapping(PermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT, BasicPermissions.READ));
    mappings.add(aMapping(PermissionMapping.CAN_GET_PARENTS_FOLDER, BasicPermissions.READ));
    mappings.add(aMapping(PermissionMapping.CAN_GET_PROPERTIES_OBJECT, BasicPermissions.READ));
    mappings.add(aMapping(PermissionMapping.CAN_VIEW_CONTENT_OBJECT, BasicPermissions.READ));
    mappings.add(
        aMapping(PermissionMapping.CAN_GET_OBJECT_RELATIONSHIPS_OBJECT, BasicPermissions.READ));
    mappings.add(aMapping(PermissionMapping.CAN_GET_ACL_OBJECT, BasicPermissions.READ));
    final Map<String, PermissionMapping> mappingTable =
        mappings.stream().collect(Collectors.toMap(PermissionMapping::getKey, m -> m));
    aclCapability.setPermissionMappingData(mappingTable);
  }

  private static void setAclCapabilities(final RepositoryCapabilitiesImpl capabilities) {
    // acl capabilities
    capabilities.setCapabilityAcl(CapabilityAcl.DISCOVER);
  }

  private static void setTypeCapabilities(final RepositoryCapabilitiesImpl capabilities) {
    // type capabilities
    final CreatablePropertyTypesImpl creatablePropertyTypes = new CreatablePropertyTypesImpl();
    capabilities.setCreatablePropertyTypes(creatablePropertyTypes);
    final NewTypeSettableAttributesImpl typeSetAttributes = new NewTypeSettableAttributesImpl();
    typeSetAttributes.setCanSetControllableAcl(false);
    typeSetAttributes.setCanSetControllablePolicy(false);
    typeSetAttributes.setCanSetCreatable(false);
    typeSetAttributes.setCanSetDescription(false);
    typeSetAttributes.setCanSetDisplayName(false);
    typeSetAttributes.setCanSetFileable(false);
    typeSetAttributes.setCanSetFulltextIndexed(false);
    typeSetAttributes.setCanSetId(false);
    typeSetAttributes.setCanSetIncludedInSupertypeQuery(false);
    typeSetAttributes.setCanSetLocalName(false);
    typeSetAttributes.setCanSetLocalNamespace(false);
    typeSetAttributes.setCanSetQueryable(false);
    typeSetAttributes.setCanSetQueryName(false);
    capabilities.setNewTypeSettableAttributes(typeSetAttributes);
  }

  private static void setQueryCapabilities(final RepositoryCapabilitiesImpl capabilities) {
    // query capabilities
    capabilities.setCapabilityQuery(CapabilityQuery.NONE);
    capabilities.setCapabilityJoin(CapabilityJoin.NONE);
  }

  private static void setVersioningCapabilities(final RepositoryCapabilitiesImpl capabilities) {
    // versioning capabilities
    capabilities.setIsPwcSearchable(false);
    capabilities.setIsPwcUpdatable(false);
    capabilities.setAllVersionsSearchable(false);
  }

  private static void setFilingCapabilities(final RepositoryCapabilitiesImpl capabilities) {
    // filing capabilities
    capabilities.setSupportsMultifiling(false);
    capabilities.setSupportsUnfiling(false);
    capabilities.setSupportsVersionSpecificFiling(false);
  }

  private static void setObjectCapabilities(final RepositoryCapabilitiesImpl capabilities) {
    // object capabilities
    capabilities.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.NONE);
    capabilities.setCapabilityChanges(CapabilityChanges.NONE);
    capabilities.setCapabilityRendition(CapabilityRenditions.NONE);
  }

  private static void setNavigationCapabilities(final RepositoryCapabilitiesImpl capabilities) {
    // navigation capabilities
    capabilities.setSupportsGetDescendants(true);
    capabilities.setSupportsGetFolderTree(true);
    capabilities.setCapabilityOrderBy(CapabilityOrderBy.NONE);
  }

  private static PermissionDefinition aPermission(String permission, String description) {
    PermissionDefinitionDataImpl pd = new PermissionDefinitionDataImpl();
    pd.setId(permission);
    pd.setDescription(description);
    return pd;
  }

  private static PermissionMapping aMapping(String key, String permission) {
    PermissionMappingDataImpl pm = new PermissionMappingDataImpl();
    pm.setKey(key);
    pm.setPermissions(Collections.singletonList(permission));

    return pm;
  }
}
  