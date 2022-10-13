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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.server.support.TypeDefinitionFactory;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.model.Application;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.ContributionFolder;
import org.silverpeas.core.cmis.model.DocumentFile;
import org.silverpeas.core.cmis.model.Publication;
import org.silverpeas.core.cmis.model.Space;
import org.silverpeas.core.cmis.model.TypeId;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        new CMISFolderSubTypeBuilder(typeDefinitionFactory).setTypeId(TypeId.SILVERPEAS_SPACE)
            .setObjectType(Space.class)
            .setName("Collaborative Space")
            .setDescription(
                "An hierarchical tree of both spaces and applications to organize your " +
                    "contributions")
            .setAllowedChildrenTypes(Space.getAllAllowedChildrenTypes()
                .stream()
                .map(TypeId::value)
                .collect(Collectors.toList()))
            .setCreatable(false)
            .build();
    typeDefinitions.put(spaceType.getId(), spaceType);

    // add Silverpeas application type
    MutableFolderTypeDefinition appType =
        new CMISFolderSubTypeBuilder(typeDefinitionFactory).setTypeId(TypeId.SILVERPEAS_APPLICATION)
            .setObjectType(Application.class)
            .setName("Silverpeas Application")
            .setDescription("An application to manage some kinds of your contributions")
            .setAllowedChildrenTypes(Application.getAllAllowedChildrenTypes()
                .stream()
                .map(TypeId::value)
                .collect(Collectors.toList()))
            .setCreatable(false)
            .build();
    typeDefinitions.put(appType.getId(), appType);

    // add Silverpeas contribution folder type (NodeDetail)
    MutableFolderTypeDefinition folderType =
        new CMISFolderSubTypeBuilder(typeDefinitionFactory).setTypeId(TypeId.SILVERPEAS_FOLDER)
            .setObjectType(ContributionFolder.class)
            .setName("Silverpeas Folder")
            .setDescription(
                "A folder to categorize and organize your contributions according to thematics")
            .setAllowedChildrenTypes(ContributionFolder.getAllAllowedChildrenTypes()
                .stream()
                .map(TypeId::value)
                .collect(Collectors.toList()))
            .setCreatable(false)
            .build();
    typeDefinitions.put(folderType.getId(), folderType);

    // add publication type
    MutableFolderTypeDefinition pubType =
        new CMISFolderSubTypeBuilder(typeDefinitionFactory).setTypeId(TypeId.SILVERPEAS_PUBLICATION)
            .setObjectType(Publication.class)
            .setName("Publication")
            .setDescription(
                "A publication to gathers in a single contribution about a given subject one or " +
                    "more contents")
            .setAllowedChildrenTypes(Publication.getAllAllowedChildrenTypes()
                .stream()
                .map(TypeId::value)
                .collect(Collectors.toList()))
            .setCreatable(true)
            .build();
    typeDefinitions.put(pubType.getId(), pubType);

    // add Silverpeas document type (attachments to publications)
    MutableDocumentTypeDefinition docType =
        typeDefinitionFactory.createDocumentTypeDefinition(CmisVersion.CMIS_1_1,
            BaseTypeId.CMIS_DOCUMENT.value());
    docType.setId(TypeId.SILVERPEAS_DOCUMENT.value());
    docType.setLocalName(DocumentFile.class.getSimpleName());
    docType.setQueryName(TypeId.SILVERPEAS_DOCUMENT.value());
    docType.setDisplayName("Document files");
    docType.setDescription("A document attached to a given publication");
    docType.setIsFulltextIndexed(true);
    docType.setIsCreatable(true);
    docType.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);
    docType.setIsVersionable(false);
    docType.setTypeMutability(typeDefinitionFactory.createTypeMutability(false, false, false));
    removeQueryableAndOrderableFlags(docType);
    typeDefinitions.put(docType.getId(), docType);
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
        (MutablePropertyIdDefinition) typeDefinition.getPropertyDefinitions()
            .computeIfAbsent(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS,
                p -> new PropertyIdDefinitionImpl());
    return allowedChildTypes.getDefaultValue();
  }

  private static class CMISFolderSubTypeBuilder {

    private final TypeDefinitionFactory typeDefinitionFactory;
    private TypeId typeId;
    private Class<? extends CmisObject> objectType;
    private List<String> childrenTypes;
    private boolean isCreatable;
    private String name;
    private String description;

    private CMISFolderSubTypeBuilder(final TypeDefinitionFactory typeDefinitionFactory) {
      this.typeDefinitionFactory = typeDefinitionFactory;
    }

    public CMISFolderSubTypeBuilder setTypeId(final TypeId typeId) {
      this.typeId = typeId;
      return this;
    }

    public CMISFolderSubTypeBuilder setObjectType(final Class<? extends CmisObject> objectType) {
      this.objectType = objectType;
      return this;
    }

    public CMISFolderSubTypeBuilder setAllowedChildrenTypes(
        final List<String> allowedChildrenTypes) {
      this.childrenTypes = allowedChildrenTypes;
      return this;
    }

    public CMISFolderSubTypeBuilder setCreatable(final boolean creatable) {
      isCreatable = creatable;
      return this;
    }

    public CMISFolderSubTypeBuilder setName(final String name) {
      this.name = name;
      return this;
    }

    public CMISFolderSubTypeBuilder setDescription(final String description) {
      this.description = description;
      return this;
    }

    public MutableFolderTypeDefinition build() {
      Objects.requireNonNull(name);
      Objects.requireNonNull(description);
      Objects.requireNonNull(typeId);
      Objects.requireNonNull(objectType);
      MutableFolderTypeDefinition folderType =
          typeDefinitionFactory.createFolderTypeDefinition(CmisVersion.CMIS_1_1,
              BaseTypeId.CMIS_FOLDER.value());
      folderType.setId(typeId.value());
      folderType.setLocalName(objectType.getSimpleName());
      folderType.setQueryName(typeId.value());
      folderType.setDisplayName(name);
      folderType.setDescription(description);
      folderType.setIsCreatable(isCreatable);
      folderType.setTypeMutability(typeDefinitionFactory.createTypeMutability(false, false, false));
      final MutablePropertyIdDefinition allowedChildrenTypes =
          (MutablePropertyIdDefinition) folderType.getPropertyDefinitions()
              .get(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS);
      allowedChildrenTypes.setDefaultValue(
          childrenTypes == null ? Collections.emptyList() : childrenTypes);
      for (PropertyDefinition<?> propDef : folderType.getPropertyDefinitions().values()) {
        MutablePropertyDefinition<?> mutablePropDef = (MutablePropertyDefinition<?>) propDef;
        mutablePropDef.setIsQueryable(false);
        mutablePropDef.setIsOrderable(false);
      }
      return folderType;
    }
  }
}
  