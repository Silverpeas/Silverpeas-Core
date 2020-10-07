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
import org.apache.chemistry.opencmis.commons.definitions.MutableDocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableFolderTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutablePropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutablePropertyIdDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableRelationshipTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.server.support.TypeDefinitionFactory;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.model.Application;
import org.silverpeas.core.cmis.model.ContributionFolder;
import org.silverpeas.core.cmis.model.Publication;
import org.silverpeas.core.cmis.model.Space;
import org.silverpeas.core.cmis.model.TypeId;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type manager is responsible to maintain a mapping between the business object types in
 * Silverpeas and those of the CMIS object domain's.
 * @author mmoquillon
 */
@Service
public class SilverpeasCmisTypeManager {

  private static final String NAMESPACE = "https://www.silverpeas.org/cmis";

  private final TypeDefinitionFactory typeDefinitionFactory = TypeDefinitionFactory.newInstance();
  private final Map<String, TypeDefinition> typeDefinitions = new HashMap<>();

  @PostConstruct
  private void defineTypes() {
    typeDefinitionFactory.setDefaultNamespace(NAMESPACE);
    typeDefinitionFactory.setDefaultControllableAcl(false);
    typeDefinitionFactory.setDefaultControllablePolicy(false);
    typeDefinitionFactory.setDefaultQueryable(false);
    typeDefinitionFactory.setDefaultFulltextIndexed(false);
    typeDefinitionFactory.setDefaultTypeMutability(
        typeDefinitionFactory.createTypeMutability(false, false, false));

    addBaseCMISObjectTypes();
    addSilverpeasCmisObjectTypes();
  }

  private void addSilverpeasCmisObjectTypes() {
    // add Silverpeas collaborative space type
    MutableFolderTypeDefinition spaceType =
        typeDefinitionFactory.createFolderTypeDefinition(CmisVersion.CMIS_1_1,
            BaseTypeId.CMIS_FOLDER.value());
    spaceType.setId(TypeId.SILVERPEAS_SPACE.value());
    spaceType.setLocalName(Space.class.getSimpleName());
    spaceType.setQueryName(TypeId.SILVERPEAS_SPACE.value());
    spaceType.setDisplayName("Collaborative Space");
    spaceType.setDescription(
        "An hierarchical tree of both spaces and applications to organize your contributions");
    spaceType.setTypeMutability(typeDefinitionFactory.createTypeMutability(true, false, false));
    final MutablePropertyIdDefinition spaceAllowedChildTypes =
        (MutablePropertyIdDefinition) spaceType.getPropertyDefinitions().get(
           PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS);
    spaceAllowedChildTypes.setDefaultValue(
        Space.getAllAllowedChildrenTypes().stream().map(TypeId::value).collect(Collectors.toList()));
    removeQueryableAndOrderableFlags(spaceType);
    typeDefinitions.put(spaceType.getId(), spaceType);

    // add Silverpeas application type
    MutableFolderTypeDefinition appType =
        typeDefinitionFactory.createFolderTypeDefinition(CmisVersion.CMIS_1_1,
            BaseTypeId.CMIS_FOLDER.value());
    appType.setId(TypeId.SILVERPEAS_APPLICATION.value());
    appType.setLocalName(Application.class.getSimpleName());
    appType.setQueryName(TypeId.SILVERPEAS_APPLICATION.value());
    appType.setDisplayName("Silverpeas Application");
    appType.setDescription("An application to manage some kinds of your contributions");
    appType.setTypeMutability(typeDefinitionFactory.createTypeMutability(true, false, false));
    final MutablePropertyIdDefinition appAllowedChildTypes =
        (MutablePropertyIdDefinition) appType.getPropertyDefinitions()
            .get(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS);
    appAllowedChildTypes.setDefaultValue(Application.getAllAllowedChildrenTypes()
        .stream()
        .map(TypeId::value)
        .collect(Collectors.toList()));
    removeQueryableAndOrderableFlags(appType);
    typeDefinitions.put(appType.getId(), appType);

    // add Silverpeas contribution folder type (Node)
    MutableFolderTypeDefinition folderType =
        typeDefinitionFactory.createFolderTypeDefinition(CmisVersion.CMIS_1_1,
            BaseTypeId.CMIS_FOLDER.value());
    folderType.setId(TypeId.SILVERPEAS_FOLDER.value());
    folderType.setLocalName(ContributionFolder.class.getSimpleName());
    folderType.setQueryName(TypeId.SILVERPEAS_FOLDER.value());
    folderType.setDisplayName("Silverpeas Folder");
    folderType.setDescription(
        "A folder to categorize and organize your contributions according to thematics");
    folderType.setTypeMutability(typeDefinitionFactory.createTypeMutability(true, false, false));
    final MutablePropertyIdDefinition folderAllowedChildTypes =
        (MutablePropertyIdDefinition) folderType.getPropertyDefinitions()
            .get(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS);
    folderAllowedChildTypes.setDefaultValue(ContributionFolder.getAllAllowedChildrenTypes()
        .stream()
        .map(TypeId::value)
        .collect(Collectors.toList()));
    removeQueryableAndOrderableFlags(folderType);
    typeDefinitions.put(folderType.getId(), folderType);

    // add publications
    MutableFolderTypeDefinition publiType =
        typeDefinitionFactory.createFolderTypeDefinition(CmisVersion.CMIS_1_1,
            BaseTypeId.CMIS_FOLDER.value());
    publiType.setId(TypeId.SILVERPEAS_PUBLICATION.value());
    publiType.setLocalName(Publication.class.getSimpleName());
    publiType.setQueryName(TypeId.SILVERPEAS_PUBLICATION.value());
    publiType.setDisplayName("Publication");
    publiType.setDescription(
        "A publication to gathers in a single contribution about a given subject one or more contents");
    publiType.setTypeMutability(typeDefinitionFactory.createTypeMutability(true, false, false));
    final MutablePropertyIdDefinition publiAllowedChildTypes =
        (MutablePropertyIdDefinition) publiType.getPropertyDefinitions()
            .get(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS);
    publiAllowedChildTypes.setDefaultValue(Publication.getAllAllowedChildrenTypes()
        .stream()
        .map(TypeId::value)
        .collect(Collectors.toList()));
    removeQueryableAndOrderableFlags(publiType);
    typeDefinitions.put(publiType.getId(), publiType);
  }

  private void addBaseCMISObjectTypes() {
    // add base folder type
    MutableFolderTypeDefinition folderType =
        typeDefinitionFactory.createBaseFolderTypeDefinition(CmisVersion.CMIS_1_1);
    folderType.setTypeMutability(typeDefinitionFactory.createTypeMutability(true, false, false));
    removeQueryableAndOrderableFlags(folderType);
    typeDefinitions.put(folderType.getId(), folderType);

    // add base document type
    MutableDocumentTypeDefinition documentType =
        typeDefinitionFactory.createBaseDocumentTypeDefinition(CmisVersion.CMIS_1_1);
    removeQueryableAndOrderableFlags(documentType);
    typeDefinitions.put(documentType.getId(), documentType);

    // add relationship type
    MutableRelationshipTypeDefinition relationshipType =
        typeDefinitionFactory.createBaseRelationshipTypeDefinition(CmisVersion.CMIS_1_1);
    removeQueryableAndOrderableFlags(relationshipType);
    typeDefinitions.put(relationshipType.getId(), relationshipType);
  }

  /**
   * Removes the queryable and sortable flags from the property definitions
   * of a type definition because this implementations does neither support
   * queries nor can order objects.
   */
  private void removeQueryableAndOrderableFlags(MutableTypeDefinition type) {
    for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
      MutablePropertyDefinition<?> mutablePropDef = (MutablePropertyDefinition<?>) propDef;
      mutablePropDef.setIsQueryable(false);
      mutablePropDef.setIsOrderable(false);
    }
  }

  /**
   * Gets the definition of the subtypes of the specified type. If the specified type is null
   * then gets the definition of all of the base types managed by Silverpeas.
   * @param typeId the unique identifier of a type.
   * @param includePropertyDefinitions a boolean indicating if the types' properties have to be
   * included.
   * @param paging paging statements on the results to return.
   * @return a list of type definitions or an empty list if either the specified type has no
   * subtypes or no more remaining subtypes after the skipCount items.
   */
  public TypeDefinitionList getSubTypeDefinitionsOf(final String typeId,
      final boolean includePropertyDefinitions, final Paging paging) {
    return typeDefinitionFactory.createTypeDefinitionList(typeDefinitions, typeId,
        includePropertyDefinitions, paging.getMaxItems(), paging.getSkipCount());
  }

  /**
   * Gets the definition of the specified type.
   * @param typeId the unique identifier of a type.
   * @return a type definition
   * @throws CmisObjectNotFoundException if no such type is supported in the Silverpeas CMIS
   * implementation.
   */
  public TypeDefinition getTypeDefinition(final String typeId) {
    final TypeDefinition type = typeDefinitions.get(typeId);
    if (type == null) {
      throw new CmisObjectNotFoundException("Type '" + typeId + "' isn't supported!");
    }
    return typeDefinitionFactory.copy(type, true, CmisVersion.CMIS_1_1);
  }

  /**
   * Gets the types that are allowed for the objects to be a child of the objects whose the type is
   * given.
   * @param typeId the unique identifier of a type.
   * @return a list of types identifiers or an empty list if either the specified type isn't a
   * folder as base type or if there is no constrains against the supported types for children.
   */
  public List<String> getAllowedChildObjectTypeIds(final String typeId) {
    final TypeDefinition typeDefinition = getTypeDefinition(typeId);
    final MutablePropertyIdDefinition allowedChildTypes =
        (MutablePropertyIdDefinition) typeDefinition.getPropertyDefinitions().computeIfAbsent(
            PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, p -> new PropertyIdDefinitionImpl());
    return allowedChildTypes.getDefaultValue();
  }
}
  