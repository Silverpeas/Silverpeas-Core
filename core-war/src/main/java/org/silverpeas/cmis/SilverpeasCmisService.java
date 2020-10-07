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

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService;
import org.silverpeas.core.annotation.WebService;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.Collections;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * A web service implementing all the services defined in the CMIS specification and that a
 * CMIS-compliant web application must satisfy as well as some additional services of optional
 * CMIS capabilities. It requires the user to be authenticated; it validates then its credentials.
 * <p>
 * This implementation of the CMIS services is returned to the OpenCMIS framework to perform the
 * CMIS operations requested by a client through one of the supported protocol binding
 * (SOAP-based Web Service, AtomPub or web browser specific). Behind the scene, the processing of
 * the operation is delegated to the CMIS repository targeted by the request. In the current
 * implementation for Silverpeas, only one repository is defined and that repository is the entry
 * point to the whole resources handled and stored in Silverpeas. If the targeted CMIS repository
 * doesn't match this single repository, then a {@link CmisObjectNotFoundException} exception
 * is thrown.
 * </p>
 * @author mmoquillon
 */
@WebService
public class SilverpeasCmisService extends AbstractCmisService
    implements CallContextAwareCmisService {

  private static final Map<BaseTypeId, BiConsumer<ObjectInfoImpl, ObjectData>> objectInfoSetters =
      new EnumMap<>(BaseTypeId.class);

  private CmisRequestContext context;

  static {
    objectInfoSetters.put(BaseTypeId.CMIS_FOLDER, (i, d) -> {
      final Map<String, PropertyData<?>> properties = d.getProperties().getProperties();
      i.setHasParent(properties.get(PropertyIds.PARENT_ID).getFirstValue() != null);
      i.setContentType(null);
      i.setFileName(null);
      i.setHasContent(false);
      i.setSupportsDescendants(true);
      i.setSupportsFolderTree(true);
    });

    objectInfoSetters.put(BaseTypeId.CMIS_DOCUMENT, (i, d) -> {
      i.setHasParent(true);
      i.setSupportsDescendants(false);
      i.setSupportsFolderTree(false);
      // for instance. Later taken into account its content
      i.setHasContent(false);
      i.setContentType(null);
      i.setFileName(null);
    });
  }

  @Inject
  private SilverpeasCmisRepository cmisRepository;

  @Override
  public CmisRequestContext getCallContext() {
    return this.context;
  }

  /**
   * Sets the current CMIS call context.
   * @param callContext a {@link CmisRequestContext} instance.
   */
  @Override
  public void setCallContext(final CallContext callContext) {
    this.context = (CmisRequestContext) callContext;
  }

  @Override
  public List<RepositoryInfo> getRepositoryInfos(final ExtensionsData extension) {
    return Collections.singletonList(cmisRepository.getRepositoryInfo());
  }

  @Override
  public TypeDefinitionList getTypeChildren(final String repositoryId, final String typeId,
      final Boolean includePropertyDefinitions, final BigInteger maxItems,
      final BigInteger skipCount, final ExtensionsData extension) {
    final SilverpeasCmisRepository repository = getRepository(repositoryId);
    return repository.getTypeChildren(typeId, includePropertyDefinitions, maxItems, skipCount);
  }

  @Override
  public TypeDefinition getTypeDefinition(final String repositoryId, final String typeId,
      final ExtensionsData extension) {
    final SilverpeasCmisRepository repository = getRepository(repositoryId);
    return repository.getTypeDefinition(typeId);
  }

  @Override
  public ObjectInFolderList getChildren(final String repositoryId, final String folderId,
      final String filter, final String orderBy, final Boolean includeAllowableActions,
      final IncludeRelationships includeRelationships, final String renditionFilter,
      final Boolean includePathSegment, final BigInteger maxItems, final BigInteger skipCount,
      final ExtensionsData extension) {
    final SilverpeasCmisRepository repository = getRepository(repositoryId);
    final ObjectInFolderList children =
        repository.getChildren(folderId, filter, includeAllowableActions, includeRelationships,
            includePathSegment, maxItems, skipCount);
    buildObjectInfo(repository, folderId,
        children.getObjects().stream().map(ObjectInFolderData::getObject));

    return children;
  }

  @Override
  public List<ObjectInFolderContainer> getDescendants(final String repositoryId,
      final String folderId, final BigInteger depth, final String filter,
      final Boolean includeAllowableActions, final IncludeRelationships includeRelationships,
      final String renditionFilter, final Boolean includePathSegment,
      final ExtensionsData extension) {
    final SilverpeasCmisRepository repository = getRepository(repositoryId);
    final List<ObjectInFolderContainer> descendants =
        repository.getDescendants(folderId, depth, filter, includeAllowableActions,
            includeRelationships, includePathSegment);
    buildObjectInfo(repository, folderId, descendants.stream().map(c -> c.getObject().getObject()));
    return descendants;
  }

  @Override
  public List<ObjectInFolderContainer> getFolderTree(final String repositoryId,
      final String folderId, final BigInteger depth, final String filter,
      final Boolean includeAllowableActions, final IncludeRelationships includeRelationships,
      final String renditionFilter, final Boolean includePathSegment,
      final ExtensionsData extension) {
    final SilverpeasCmisRepository repository = getRepository(repositoryId);
    final List<ObjectInFolderContainer> descendants =
        repository.getFolderTree(folderId, depth, filter, includeAllowableActions,
            includeRelationships, includePathSegment);
    buildObjectInfo(repository, folderId, descendants.stream().map(c -> c.getObject().getObject()));
    return descendants;
  }

  @Override
  public ObjectData getFolderParent(final String repositoryId, final String folderId,
      final String filter, final ExtensionsData extension) {
    final SilverpeasCmisRepository repository = getRepository(repositoryId);
    final List<ObjectParentData> parents =
        repository.getObjectParents(folderId, filter, false, false);
    if (parents.isEmpty()) {
      throw new CmisInvalidArgumentException("The root folder has no parent!");
    }
    final ObjectData parent = parents.get(0).getObject();
    final CallContext callContext = getCallContext();
    if (callContext.isObjectInfoRequired()) {
      setObjectInfo(parent);
    }
    return parent;
  }

  @Override
  public List<ObjectParentData> getObjectParents(final String repositoryId, final String objectId,
      final String filter, final Boolean includeAllowableActions,
      final IncludeRelationships includeRelationships, final String renditionFilter,
      final Boolean includeRelativePathSegment, final ExtensionsData extension) {
    final SilverpeasCmisRepository repository = getRepository(repositoryId);
    final List<ObjectParentData> parents =
        repository.getObjectParents(objectId, filter, includeAllowableActions,
            includeRelativePathSegment);
    buildObjectInfo(repository, objectId, parents.stream().map(ObjectParentData::getObject));
    return parents;
  }

  @Override
  public ObjectData getObject(final String repositoryId, final String objectId, final String filter,
      final Boolean includeAllowableActions, final IncludeRelationships includeRelationships,
      final String renditionFilter, final Boolean includePolicyIds, final Boolean includeAcl,
      final ExtensionsData extension) {
    final SilverpeasCmisRepository repository = getRepository(repositoryId);
    final ObjectData object =
        repository.getObject(objectId, filter, includeAllowableActions, includeRelationships,
            includeAcl);
    final CallContext callContext = getCallContext();
    if (callContext.isObjectInfoRequired()) {
      setObjectInfo(object);
    }
    return object;
  }

  @Override
  public ObjectData getObjectByPath(final String repositoryId, final String path,
      final String filter, final Boolean includeAllowableActions,
      final IncludeRelationships includeRelationships, final String renditionFilter,
      final Boolean includePolicyIds, final Boolean includeAcl, final ExtensionsData extension) {
    final SilverpeasCmisRepository repository = getRepository(repositoryId);
    final ObjectData objData =
        repository.getObjectByPath(path, filter, includeAllowableActions, includeRelationships,
            includeAcl);
    final CallContext callContext = getCallContext();
    if (callContext.isObjectInfoRequired()) {
      setObjectInfo(objData);
    }

    return objData;
  }

  private void buildObjectInfo(final SilverpeasCmisRepository repository, final String objectId,
      final Stream<ObjectData> relatives) {
    final CallContext callContext = getCallContext();
    if (callContext.isObjectInfoRequired()) {
      final ObjectData object =
          repository.getObject(objectId, null, false, IncludeRelationships.NONE, false);
      setObjectInfo(object);
      setObjectInfo(relatives);
    }
  }

  private void setObjectInfo(final Stream<ObjectData> objectDataStream) {
    objectDataStream.forEach(this::setObjectInfo);
  }

  private void setObjectInfo(final ObjectData object) {
    // should be adapted to the type of the object
    final Map<String, PropertyData<?>> properties = object.getProperties().getProperties();
    final String objectTypeId = (String) properties.get(PropertyIds.OBJECT_TYPE_ID).getFirstValue();

    final ObjectInfoImpl objectInfo = new ObjectInfoImpl();
    objectInfo.setId(object.getId());
    objectInfo.setName(properties.get(PropertyIds.NAME).getId());
    objectInfo.setBaseType(object.getBaseTypeId());
    objectInfo.setTypeId(objectTypeId);
    objectInfo.setCreatedBy((String) properties.get(PropertyIds.CREATED_BY).getFirstValue());
    objectInfo.setCreationDate(
        (GregorianCalendar) properties.get(PropertyIds.CREATION_DATE).getFirstValue());
    objectInfo.setLastModificationDate(
        (GregorianCalendar) properties.get(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue());
    objectInfo.setHasAcl(true);
    objectInfo.setVersionSeriesId(null);
    objectInfo.setIsCurrentVersion(true);
    objectInfo.setRenditionInfos(null);
    objectInfo.setSupportsPolicies(false);
    objectInfo.setRelationshipSourceIds(null);
    objectInfo.setRelationshipTargetIds(null);
    objectInfo.setSupportsRelationships(false);
    objectInfo.setWorkingCopyId(null);
    objectInfo.setWorkingCopyOriginalId(null);
    objectInfo.setObject(object);

    BiConsumer<ObjectInfoImpl, ObjectData> setter = objectInfoSetters.get(object.getBaseTypeId());
    if (setter != null) {
      setter.accept(objectInfo, object);
    }

    addObjectInfo(objectInfo);
  }

  private SilverpeasCmisRepository getRepository(final String repoId) {
    if (!cmisRepository.getId().equals(repoId)) {
      throw new CmisObjectNotFoundException("Unknown repository '" + repoId + "'!");
    }
    return cmisRepository;
  }
}
