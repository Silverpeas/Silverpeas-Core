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
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.cmis.CMISEnvForTests;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.cmis.TreeNode;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.CmisObjectFactory;
import org.silverpeas.core.cmis.model.Space;
import org.silverpeas.core.cmis.model.TypeId;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.StringUtil;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.chemistry.opencmis.commons.enums.Action.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.silverpeas.cmis.SilverpeasObjectsTree.LANGUAGE;
import static org.silverpeas.cmis.util.CmisDateConverter.millisToCalendar;

/**
 * Unit tests about the walking of the CMIS objects tree whose each node is mapped to a given
 * object in Silverpeas and whose the tree is mapped itself to the organizational schema of the
 * Silverpeas resources (spaces, component instances, ...). The tests checks both the CMIS objects
 * tree is correctly mapped to an organizational schema of Silverpeas resources and the CMIS data
 * of the CMIS objects matches correctly the underlying represented Silverpeas objects.
 * @author mmoquillon
 */
@DisplayName(
    "Test the mapping between the CMIS objects tree and an organizational schema of Silverpeas " +
        "resources")
class CmisObjectsTreeWalkerTest extends CMISEnvForTests {

  @Test
  @DisplayName("Getting the CMIS data of a Silverpeas object not exposed through the CMIS should " +
      "throw an exception")
  void getObjectDataOfANonExposedResource() {
    final String id = "Toto2";
    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisObjectNotFoundException.class, () -> walker.getObjectData(id, filtering));
  }

  @Test
  @DisplayName("Getting the CMIS data of an unknown Silverpeas object should throw an exception")
  void getObjectDataOfAnUnknownResource() {
    final String id = "kmelia42";
    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisObjectNotFoundException.class, () -> walker.getObjectData(id, filtering));
  }

  @Test
  @DisplayName(
      "The CMIS tree root should be mapped to the virtual container of all of the Silverpeas's " +
          "root spaces")
  void getObjectDataOfRootSpace() {
    final String spaceId = "WA0";
    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    ObjectData data = CmisObjectsTreeWalker.getInstance().getObjectData(spaceId, filtering);
    assertThat(data, notNullValue());
    assertThat(data.getAcl(), nullValue());
    assertThat(data.getBaseTypeId(), is(BaseTypeId.CMIS_FOLDER));
    assertThat(data.getId(), is(spaceId));
    assertThat(data.getChangeEventInfo(), nullValue());
    assertThat(data.getPolicyIds(), nullValue());
    assertThat(data.getRelationships(), empty());
    assertThat(data.getRenditions(), empty());
    assertThat(data.getAllowableActions(), notNullValue());
    assertThat(data.getProperties(), notNullValue());

    AllowableActions actions = data.getAllowableActions();
    assertThat(actions.getAllowableActions(),
        containsInAnyOrder(CAN_GET_FOLDER_TREE, CAN_GET_PROPERTIES, CAN_GET_DESCENDANTS,
            CAN_GET_CHILDREN, CAN_GET_ACL));

    final Space root = new CmisObjectFactory().createRootSpace();
    User admin = User.getById("0");
    Map<String, PropertyData<?>> props = data.getProperties().getProperties();
    assertThat(props.get(PropertyIds.OBJECT_ID).getFirstValue(), is(Space.ROOT_ID.asString()));
    assertThat(props.get(PropertyIds.NAME).getFirstValue(), is(root.getName()));
    assertThat(props.get(PropertyIds.DESCRIPTION).getFirstValue(), is(root.getDescription()));
    assertThat(props.get(PropertyIds.CREATED_BY).getFirstValue(), is(admin.getDisplayedName()));
    assertThat(props.get(PropertyIds.LAST_MODIFIED_BY).getFirstValue(),
        is(admin.getDisplayedName()));
    assertThat(props.get(PropertyIds.CREATION_DATE).getFirstValue(),
        is(millisToCalendar(root.getCreationDate())));
    assertThat(props.get(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue(),
        is(millisToCalendar(root.getLastModificationDate())));
    assertThat(props.get(PropertyIds.BASE_TYPE_ID).getFirstValue(),
        is(BaseTypeId.CMIS_FOLDER.value()));
    assertThat(props.get(PropertyIds.OBJECT_TYPE_ID).getFirstValue(),
        is(TypeId.SILVERPEAS_SPACE.value()));

    assertThat(props.get(PropertyIds.PARENT_ID).getFirstValue(), nullValue());
    assertThat(props.get(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS).getValues(),
        contains(TypeId.SILVERPEAS_SPACE.value()));
  }

  @Test
  @DisplayName("The CMIS data of a space in Silverpeas should match the properties of that space")
  void getObjectDataOfASpace() {
    final String spaceId = "WA2";
    final SpaceInstLight space = organizationController.getSpaceInstLightById(spaceId);

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    ObjectData data = CmisObjectsTreeWalker.getInstance().getObjectData(spaceId, filtering);
    assertCMISObjectMatchesSpace(data, space);
  }

  @Test
  @DisplayName(
      "The CMIS data of an application in Silverpeas should match the properties of that " +
          "application")
  void getObjectDataOfAnApplication() {
    final String appId = "kmelia3";
    final ComponentInstLight app = organizationController.getComponentInstLight(appId);

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    ObjectData data = CmisObjectsTreeWalker.getInstance().getObjectData(appId, filtering);
    assertCMISObjectMatchesApplication(data, app);
  }

  @Test
  @DisplayName("The CMIS data of a node (topic, album, ...) in an application of Silverpeas " +
      "should match the properties of that node")
  void getObjectDataOfANode() {
    final NodePK nodePK = new NodePK("3", "kmelia2");
    final NodeDetail node = nodeService.getDetail(nodePK);
    assertThat(node, notNullValue());

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    String folderId = node.getIdentifier().asString();
    ObjectData data = CmisObjectsTreeWalker.getInstance().getObjectData(folderId, filtering);
    assertCMISObjectMatchesNode(data, node);
  }

  @Test
  @DisplayName("The CMIS data of a publication in an application of Silverpeas should match the " +
      "properties of that publication")
  void getObjectDataOfAPublication() {
    final PublicationPK pk = new PublicationPK("1", "kmelia1");
    final PublicationDetail publi = publicationService.getDetail(pk);
    assertThat(publi, notNullValue());

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    String publiId = publi.getIdentifier().asString();
    ObjectData data = CmisObjectsTreeWalker.getInstance().getObjectData(publiId, filtering);
    assertCMISObjectMatchesPublication(data, publi);
  }

  @Test
  @DisplayName("The CMIS data of an attachemnt of a publication in an application of Silverpeas " +
      "should match the properties of that publication")
  void getObjectDataOfAnAttachment() {
    final SimpleDocumentPK pk = new SimpleDocumentPK("1", "kmelia1");
    final SimpleDocument doc = attachmentService.searchDocumentById(pk, LANGUAGE);
    assertThat(doc, notNullValue());

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    String docId = doc.getIdentifier().asString();
    ObjectData data = CmisObjectsTreeWalker.getInstance().getObjectData(docId, filtering);
    assertCMISObjectMatchesDocument(data, doc);
  }

  @Test
  @DisplayName(
      "Getting the CMIS data of an unknown Silverpeas object by its path should throw an exception")
  void getObjectDataByPathOfAnUnknownResource() {
    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    String path = pathToNode(organization.findTreeNodeById("WA2"), filtering.getLanguage()) +
        CmisFolder.PATH_SEPARATOR + "TOTO";
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisObjectNotFoundException.class, () ->
        walker.getObjectDataByPath(path, filtering));
  }

  @Test
  @DisplayName(
      "Getting the CMIS data of a Silverpeas object by a bad path should throw an exception")
  void getObjectDataByANonExistingPath() {
    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    String path = CmisFolder.PATH_SEPARATOR + "TOTO" + CmisFolder.PATH_SEPARATOR +
        pathToNode(organization.findTreeNodeById("kmelia3"), filtering.getLanguage());
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisObjectNotFoundException.class,
        () -> walker.getObjectDataByPath(path, filtering));
  }

  @Test
  @DisplayName("An object child of the CMIS object tree root should match a space in Silverpeas")
  void getRootChildDataByPath() {
    final String spaceId = "WA2";
    final TreeNode node = organization.findTreeNodeById(spaceId);

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    final String path = pathToNode(node, filtering.getLanguage());
    ObjectData data = CmisObjectsTreeWalker.getInstance().getObjectDataByPath(path, filtering);
    assertCMISObjectMatchesSpace(data, (SpaceInstLight) node.getObject());
  }

  @Test
  @DisplayName(
      "The walk of the CMIS objects tree should match the browsing of the organizational schema " +
          "of Silverpeas resources")
  void getObjectDataOfAppByPath() {
    final String appId = "kmelia3";
    final TreeNode node = organization.findTreeNodeById(appId);

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    final String path = pathToNode(node, filtering.getLanguage());
    ObjectData data = CmisObjectsTreeWalker.getInstance().getObjectDataByPath(path, filtering);
    assertCMISObjectMatchesApplication(data, (ComponentInstLight) node.getObject());
  }

  @Test
  @DisplayName("The path of a node in an application should be connected to the CMIS objects tree")
  void getObjectDataOfFolderByPath() {
    final String nodeId = ContributionIdentifier.from("kmelia2", "3", NodeDetail.TYPE).asString();
    final TreeNode node = organization.findTreeNodeById(nodeId);

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    final String path = pathToNode(node, filtering.getLanguage());
    ObjectData data = CmisObjectsTreeWalker.getInstance().getObjectDataByPath(path, filtering);
    assertCMISObjectMatchesNode(data, (NodeDetail) node.getObject());
  }

  @Test
  @DisplayName("The path of a publication in an application should be connected to the CMIS " +
      "objects tree")
  void getObjectDataOfPublicationByPath() {
    final String publiId =
        ContributionIdentifier.from("kmelia1", "2", PublicationDetail.TYPE).asString();
    final TreeNode node = organization.findTreeNodeById(publiId);

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    final String path = pathToNode(node, filtering.getLanguage());
    ObjectData data = CmisObjectsTreeWalker.getInstance().getObjectDataByPath(path, filtering);
    assertCMISObjectMatchesPublication(data, (PublicationDetail) node.getObject());
  }

  @Test
  @DisplayName(
      "The path of a document of a publication should be connected to the CMIS objects tree")
  void getObjectDataOfADocumentByPath() {
    final String docId =
        ContributionIdentifier.from("kmelia1", "2", DocumentType.attachment.getName()).asString();
    final TreeNode node = organization.findTreeNodeById(docId);

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    final String path = pathToNode(node, filtering.getLanguage());
    ObjectData data = CmisObjectsTreeWalker.getInstance().getObjectDataByPath(path, filtering);
    assertCMISObjectMatchesDocument(data, (SimpleDocument) node.getObject());
  }

  @Test
  @DisplayName(
      "Getting the children of an non existing Silverpeas resource should throw an exception")
  void getChildrenOfANonExistingSilverpeasObject() {
    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisObjectNotFoundException.class,
        () -> walker.getChildrenData("kmelia42", filtering, Paging.NO_PAGING));
  }

  @Test
  @DisplayName(
      "Getting the children of a Silverpeas resource not exposed through CMIS should throw an " +
          "exception")
  void getChildrenOfANonExposedSilverpeasObject() {
    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisObjectNotFoundException.class, () ->
        walker.getChildrenData("TITI", filtering, Paging.NO_PAGING));
  }

  @Test
  @DisplayName(
      "The children of a CMIS object should match the children of the Silverpeas resource related" +
          " by the CMIS object")
  void getChildrenWithoutPaging() {
    final String spaceId = "WA1";
    List<LocalizedResource> spaceChildren = organization.findTreeNodeById(spaceId)
        .getChildren()
        .stream()
        .map(TreeNode::getObject)
        .collect(Collectors.toList());

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludePathSegment(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    Paging paging = Paging.NO_PAGING;
    ObjectInFolderList children =
        CmisObjectsTreeWalker.getInstance().getChildrenData(spaceId, filtering, paging);
    assertThat(children, notNullValue());
    assertThat(children.getNumItems().intValue(), is(spaceChildren.size()));
    assertThat(children.hasMoreItems(), is(false));

    final String language = filtering.getLanguage();
    List<ObjectInFolderData> dataList = children.getObjects();
    assertThat(dataList.size(), is(spaceChildren.size()));
    dataList.forEach(data -> {
      Optional<LocalizedResource> child =
          spaceChildren.stream()
              .filter(c -> c.getIdentifier().asString().equals(data.getObject().getId()))
              .findFirst();
      assertThat(child.isPresent(), is(true));
      child.ifPresent(c -> {
        assertThat(((AbstractI18NBean<?>) c).getName(language), is(data.getPathSegment()));
        assertCMISObjectMatchesSilverpeasObject(data.getObject(), c);
      });
    });
  }

  @Test
  @DisplayName("The paging on the children of a CMIS object should return only the asked objects")
  void getChildrenWithPaging() {
    final String spaceId = "WA2";
    List<LocalizedResource> spaceChildren = organization.findTreeNodeById(spaceId)
        .getChildren()
        .stream()
        .map(TreeNode::getObject)
        .collect(Collectors.toList());

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludePathSegment(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    Paging paging = new Paging(BigInteger.ZERO, BigInteger.ONE);
    ObjectInFolderList children =
        CmisObjectsTreeWalker.getInstance().getChildrenData(spaceId, filtering, paging);
    assertThat(children, notNullValue());
    assertThat(children.getNumItems().intValue(), is(spaceChildren.size()));
    assertThat(children.hasMoreItems(), is(true));

    final String language = filtering.getLanguage();
    List<ObjectInFolderData> dataList = children.getObjects();
    assertThat(dataList.size(), is(1));
    ObjectData data = dataList.get(0).getObject();
    Optional<LocalizedResource> child =
        spaceChildren.stream()
            .filter(c -> c.getIdentifier().asString().equals(data.getId()))
            .findFirst();
    assertThat(child.isPresent(), is(true));
    child.ifPresent(c -> {
      assertThat(((AbstractI18NBean<?>) c).getName(language), is(dataList.get(0).getPathSegment()));
      assertCMISObjectMatchesSilverpeasObject(data, c);
    });
  }

  @Test
  @DisplayName("No CMIS data are returned for a Silverpeas object without any children")
  void getNoChildren() {
    final String appId = "kmelia3";
    final String nodeId =
        ContributionIdentifier.from(appId, NodePK.ROOT_NODE_ID, NodeDetail.TYPE).asString();
    final TreeNode catNode = organization.findTreeNodeById(nodeId);
    final Set<TreeNode> childrenNodes = catNode.getChildren();
    assertThat(childrenNodes.isEmpty(), is(true));

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludePathSegment(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    Paging paging = Paging.NO_PAGING;
    ObjectInFolderList children =
        CmisObjectsTreeWalker.getInstance().getChildrenData(nodeId, filtering, paging);
    assertThat(children, notNullValue());
    assertThat(children.getNumItems().intValue(), is(0));
    assertThat(children.hasMoreItems(), is(false));
    assertThat(children.getObjects().isEmpty(), is(true));
  }

  @Test
  @DisplayName(
      "Getting the subtree of children of an non existing Silverpeas resource should throw an " +
          "exception")
  void getTheSubtreeOfANonExistentObject() {
    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisObjectNotFoundException.class,
        () -> walker.getSubTreeData("kmelia42", filtering, 0));
  }

  @Test
  @DisplayName(
      "Getting the subtree of children of a Silverpeas resource not exposed through CMIS should " +
          "throw an exception")
  void getTheSubtreeOfANonExposedObject() {
    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisObjectNotFoundException.class,
        () -> walker.getSubTreeData("TOTO", filtering, 0));
  }

  @Test
  @DisplayName(
      "Getting the subtree of children of a Silverpeas resource with a depth lesser thant -1 " +
          "should throw an exception")
  void getTheSubtreeWithAnInvalidDepthValue() {
    final String spaceId = "WA1";
    final TreeNode spaceNode = organization.findTreeNodeById(spaceId);
    assertThat(spaceNode.getChildren().isEmpty(), is(false));

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludePathSegment(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    int depth = -2; // no depth
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(IllegalArgumentException.class,
        () -> walker.getSubTreeData(spaceId, filtering, depth));
  }

  @Test
  @DisplayName(
      "No CMIS data are returned for a Silverpeas object rooted at a subtree with a 0-depth level")
  void getTheSubtreeWithoutAnyDepth() {
    final String spaceId = "WA1";
    final TreeNode spaceNode = organization.findTreeNodeById(spaceId);
    assertThat(spaceNode.getChildren().isEmpty(), is(false));

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludePathSegment(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    int depth = 0; // no depth
    List<ObjectInFolderContainer> subtree =
        CmisObjectsTreeWalker.getInstance().getSubTreeData(spaceId, filtering, depth);
    assertThat(subtree, notNullValue());
    assertThat(subtree.isEmpty(), is(true));
  }

  @Test
  @DisplayName("No CMIS data are returned for a Silverpeas object without subtree")
  void getTheSubTreeOfAnObjectWithoutAnyChildren() {
    final String appId = "kmelia3";
    final String nodeId =
        ContributionIdentifier.from(appId, NodePK.ROOT_NODE_ID, NodeDetail.TYPE).asString();
    final TreeNode catNode = organization.findTreeNodeById(nodeId);
    final Set<TreeNode> childrenNodes = catNode.getChildren();
    assertThat(childrenNodes.isEmpty(), is(true));

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludePathSegment(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    int depth = -1; // the whole subtree
    List<ObjectInFolderContainer> subtree =
        CmisObjectsTreeWalker.getInstance().getSubTreeData(nodeId, filtering, depth);
    assertThat(subtree.isEmpty(), is(true));
  }

  @Test
  @DisplayName(
      "The CMIS data of a subtree should match the children of the Silverpeas object " +
          "corresponding to the root of that subtree")
  void getTheWholeSubtreeOfASpace() {
    final String spaceId = "WA1";
    final TreeNode spaceNode = organization.findTreeNodeById(spaceId);

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludePathSegment(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    int depth = -1; // the whole subtree
    List<ObjectInFolderContainer> subtree =
        CmisObjectsTreeWalker.getInstance().getSubTreeData(spaceId, filtering, depth);
    assertThat(subtree, notNullValue());
    assertThat(subtree.isEmpty(), is(false));

    Set<TreeNode> childrenNodes = spaceNode.getChildren();
    assertChildrenMatches(childrenNodes, subtree, depth, filtering.getLanguage());
  }

  @Test
  @DisplayName(
      "Asking for only folders for a subtree should satisfies that request")
  void getFoldersOnlySubtreeOfASpace() {
    final String spaceId = "WA1";
    final TreeNode spaceNode = organization.findTreeNodeById(spaceId);

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludePathSegment(true)
        .setIncludeRelationships(IncludeRelationships.NONE)
        .setIncludeCmisObjectTypes(Filtering.IncludeCmisObjectTypes.ONLY_FOLDERS);
    int depth = -1; // the whole subtree
    List<ObjectInFolderContainer> subtree =
        CmisObjectsTreeWalker.getInstance().getSubTreeData(spaceId, filtering, depth);
    assertThat(subtree, notNullValue());
    assertThat(subtree.isEmpty(), is(false));

    assertChildrenTypeMatchesOnlyFolders(subtree);
  }

  @Test
  @DisplayName(
      "The CMIS data of the first level of a subtree should match the children of the Silverpeas " +
          "object corresponding to the root of that subtree")
  void getAOneLevelSubtreeOfASpace() {
    final String spaceId = "WA1";
    final TreeNode spaceNode = organization.findTreeNodeById(spaceId);

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludePathSegment(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    int depth = 1; // the direct children
    List<ObjectInFolderContainer> subtree =
        CmisObjectsTreeWalker.getInstance().getSubTreeData(spaceId, filtering, depth);
    assertThat(subtree, notNullValue());
    assertThat(subtree.isEmpty(), is(false));

    Set<TreeNode> childrenNodes = spaceNode.getChildren();
    assertChildrenMatches(childrenNodes, subtree, depth, filtering.getLanguage());
  }

  @Test
  @DisplayName(
      "The CMIS data of a part of a subtree should match the children of the Silverpeas " +
          "object corresponding to the root of that subtree")
  void getAPartOfASubtreeOfASpace() {
    final String spaceId = "WA1";
    final TreeNode spaceNode = organization.findTreeNodeById(spaceId);

    Filtering filtering = new Filtering().setIncludeAllowableActions(true)
        .setIncludePathSegment(true)
        .setIncludeRelationships(IncludeRelationships.NONE);
    int depth = 3;
    List<ObjectInFolderContainer> subtree =
        CmisObjectsTreeWalker.getInstance().getSubTreeData(spaceId, filtering, depth);
    assertThat(subtree, notNullValue());
    assertThat(subtree.isEmpty(), is(false));

    Set<TreeNode> childrenNodes = spaceNode.getChildren();
    assertChildrenMatches(childrenNodes, subtree, depth, filtering.getLanguage());
  }

  private void assertChildrenMatches(final Set<TreeNode> nodes,
      final List<ObjectInFolderContainer> containers, final int depth, final String language) {
    assertThat(nodes.size(), is(containers.size()));
    containers.forEach(c -> {
      ObjectData data = c.getObject().getObject();
      String pathSegment = c.getObject().getPathSegment();
      Optional<TreeNode> optNode =
          nodes.stream().filter(n -> n.getId().equals(data.getId())).findFirst();
      assertThat(optNode.isPresent(), is(true));
      optNode.ifPresent(n -> {
        assertThat(getObjectName(n.getObject(), language), is(pathSegment));
        assertCMISObjectMatchesSilverpeasObject(data, n.getObject());
        if (depth > 1 || depth == -1) {
          int newDepth = depth == -1 ? -1 : depth - 1;
          Set<TreeNode> children = getChildrenForCMIS(n);
          assertChildrenMatches(children, c.getChildren(), newDepth, language);
        }
      });
    });
  }

  private void assertChildrenTypeMatchesOnlyFolders(final List<ObjectInFolderContainer> subtree) {
    subtree.forEach(c -> {
      ObjectData data = c.getObject().getObject();
      assertThat(data, instanceOf(CmisFolder.class));
      assertChildrenTypeMatchesOnlyFolders(c.getChildren());
    });
  }

  // the root node shouldn't be exposed in the CMIS tree: it represents the application itself as
  // a node
  private Set<TreeNode> getChildrenForCMIS(final TreeNode node) {
    Set<TreeNode> children = node.getChildren();
    if (children.size() == 1 && children.stream()
        .anyMatch(tn -> tn.getObject() instanceof NodeDetail &&
            ((NodeDetail)tn.getObject()).isRoot())) {
      children = children.iterator().next().getChildren();
    }
    return children;
  }

  private void assertCMISObjectMatchesSilverpeasObject(final ObjectData data,
      final LocalizedResource resource) {
    if (resource instanceof SpaceInstLight) {
      assertCMISObjectMatchesSpace(data, (SpaceInstLight) resource);
    } else if (resource instanceof ComponentInstLight) {
      assertCMISObjectMatchesApplication(data, (ComponentInstLight) resource);
    } else if (resource instanceof NodeDetail) {
      assertCMISObjectMatchesNode(data, (NodeDetail) resource);
    } else if (resource instanceof PublicationDetail) {
      assertCMISObjectMatchesPublication(data, (PublicationDetail) resource);
    } else if (resource instanceof SimpleDocument) {
      assertCMISObjectMatchesDocument(data, (SimpleDocument) resource);
    } else {
      fail("Non CMIS matching for the resource: " + resource);
    }
  }

  private void assertCMISObjectMatchesSpace(final ObjectData data, final SpaceInstLight space) {
    assertThat(data, notNullValue());
    assertThat(data.getAcl(), nullValue());
    assertThat(data.getBaseTypeId(), is(BaseTypeId.CMIS_FOLDER));
    assertThat(data.getId(), is(space.getId()));
    assertThat(data.getChangeEventInfo(), nullValue());
    assertThat(data.getPolicyIds(), nullValue());
    assertThat(data.getRelationships(), empty());
    assertThat(data.getRenditions(), empty());
    assertThat(data.getAllowableActions(), notNullValue());
    assertThat(data.getProperties(), notNullValue());

    AllowableActions actions = data.getAllowableActions();
    assertThat(actions.getAllowableActions(),
        containsInAnyOrder(CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS, CAN_GET_FOLDER_PARENT,
            CAN_GET_PROPERTIES, CAN_GET_DESCENDANTS, CAN_GET_CHILDREN, CAN_GET_ACL));

    assertProperties(data.getProperties(), space);
  }

  private void assertCMISObjectMatchesApplication(final ObjectData data,
      final ComponentInstLight application) {
    assertThat(data, notNullValue());
    assertThat(data.getAcl(), nullValue());
    assertThat(data.getBaseTypeId(), is(BaseTypeId.CMIS_FOLDER));
    assertThat(data.getId(), is(application.getId()));
    assertThat(data.getChangeEventInfo(), nullValue());
    assertThat(data.getPolicyIds(), nullValue());
    assertThat(data.getRelationships(), empty());
    assertThat(data.getRenditions(), empty());
    assertThat(data.getAllowableActions(), notNullValue());
    assertThat(data.getProperties(), notNullValue());

    AllowableActions actions = data.getAllowableActions();
    assertThat(actions.getAllowableActions(),
        containsInAnyOrder(CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS, CAN_GET_FOLDER_PARENT,
            CAN_GET_PROPERTIES, CAN_GET_DESCENDANTS, CAN_GET_CHILDREN, CAN_GET_ACL));

    assertProperties(data.getProperties(), application);
  }

  private void assertCMISObjectMatchesNode(final ObjectData data, final NodeDetail node) {
    assertThat(data, notNullValue());
    assertThat(data.getAcl(), nullValue());
    assertThat(data.getBaseTypeId(), is(BaseTypeId.CMIS_FOLDER));
    assertThat(data.getId(), is(node.getIdentifier().asString()));
    assertThat(data.getChangeEventInfo(), nullValue());
    assertThat(data.getPolicyIds(), nullValue());
    assertThat(data.getRelationships(), empty());
    assertThat(data.getRenditions(), empty());
    assertThat(data.getAllowableActions(), notNullValue());
    assertThat(data.getProperties(), notNullValue());

    AllowableActions actions = data.getAllowableActions();
    assertThat(actions.getAllowableActions(),
        containsInAnyOrder(CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS, CAN_GET_FOLDER_PARENT,
            CAN_GET_PROPERTIES, CAN_GET_DESCENDANTS, CAN_GET_CHILDREN, CAN_GET_ACL));

    assertProperties(data.getProperties(), node);
  }

  private void assertCMISObjectMatchesPublication(final ObjectData data,
      final PublicationDetail publication) {
    assertThat(data, notNullValue());
    assertThat(data.getAcl(), nullValue());
    assertThat(data.getBaseTypeId(), is(BaseTypeId.CMIS_FOLDER));
    assertThat(data.getId(), is(publication.getIdentifier().asString()));
    assertThat(data.getChangeEventInfo(), nullValue());
    assertThat(data.getPolicyIds(), nullValue());
    assertThat(data.getRelationships(), empty());
    assertThat(data.getRenditions(), empty());
    assertThat(data.getAllowableActions(), notNullValue());
    assertThat(data.getProperties(), notNullValue());

    AllowableActions actions = data.getAllowableActions();
    assertThat(actions.getAllowableActions(),
        containsInAnyOrder(CAN_GET_FOLDER_TREE, CAN_GET_OBJECT_PARENTS, CAN_GET_FOLDER_PARENT,
            CAN_GET_PROPERTIES, CAN_GET_DESCENDANTS, CAN_GET_CHILDREN, CAN_GET_ACL));

    assertProperties(data.getProperties(), publication);
  }

  private void assertCMISObjectMatchesDocument(final ObjectData data, final SimpleDocument doc) {
    assertThat(data, notNullValue());
    assertThat(data.getAcl(), nullValue());
    assertThat(data.getBaseTypeId(), is(BaseTypeId.CMIS_DOCUMENT));
    assertThat(data.getId(), is(doc.getIdentifier().asString()));
    assertThat(data.getChangeEventInfo(), nullValue());
    assertThat(data.getPolicyIds(), nullValue());
    assertThat(data.getRelationships(), empty());
    assertThat(data.getRenditions(), empty());
    assertThat(data.getAllowableActions(), notNullValue());
    assertThat(data.getProperties(), notNullValue());

    AllowableActions actions = data.getAllowableActions();
    assertThat(actions.getAllowableActions(),
        containsInAnyOrder(CAN_GET_ALL_VERSIONS, CAN_GET_PROPERTIES, CAN_GET_ACL,
            CAN_GET_OBJECT_PARENTS, CAN_GET_CONTENT_STREAM));

    assertProperties(data.getProperties(), doc);
  }

  /**
   * Asserts the specified properties of a CMIS object matches the expected Silverpeas business
   * object.
   * @param properties the CMIS properties of a CMIS object.
   * @param space a {@link SpaceInstLight} instance in Silverpeas.
   */
  private void assertProperties(final Properties properties, final SpaceInstLight space) {
    User requester = User.getCurrentRequester();
    String language = requester.getUserPreferences().getLanguage();
    User creator = User.getById(String.valueOf(space.getCreatedBy()));
    User lastUpdater = User.getById(String.valueOf(space.getUpdatedBy()));

    Map<String, PropertyData<?>> props = properties.getProperties();
    assertThat(props.get(PropertyIds.OBJECT_ID).getFirstValue(), is(space.getId()));
    assertThat(props.get(PropertyIds.NAME).getFirstValue(), is(space.getName(language)));
    assertThat(props.get(PropertyIds.DESCRIPTION).getFirstValue(),
        is(space.getDescription(language)));
    assertThat(props.get(PropertyIds.CREATED_BY).getFirstValue(), is(creator.getDisplayedName()));
    assertThat(props.get(PropertyIds.LAST_MODIFIED_BY).getFirstValue(),
        is(lastUpdater.getDisplayedName()));
    assertThat(props.get(PropertyIds.CREATION_DATE).getFirstValue(),
        is(millisToCalendar(space.getCreationDate().getTime())));
    assertThat(props.get(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue(),
        is(millisToCalendar(space.getLastUpdateDate().getTime())));
    assertThat(props.get(PropertyIds.BASE_TYPE_ID).getFirstValue(),
        is(BaseTypeId.CMIS_FOLDER.value()));
    assertThat(props.get(PropertyIds.OBJECT_TYPE_ID).getFirstValue(),
        is(TypeId.SILVERPEAS_SPACE.value()));

    assertThat(props.get(PropertyIds.PARENT_ID).getFirstValue(),
        is(SpaceInst.SPACE_KEY_PREFIX + space.getFatherId()));
    assertThat(props.get(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS).getValues(),
        containsInAnyOrder(TypeId.SILVERPEAS_SPACE.value(), TypeId.SILVERPEAS_APPLICATION.value()));

    TreeNode spaceNode = organization.findTreeNodeById(space.getId());
    String path = pathToNode(spaceNode, language);
    assertThat(props.get(PropertyIds.PATH).getFirstValue(), is(path));
  }

  /**
   * Asserts the specified properties of a CMIS object matches the expected Silverpeas business
   * object.
   * @param properties the CMIS properties of a CMIS object.
   * @param application a {@link ComponentInstLight} instance in Silverpeas.
   */
  private void assertProperties(final Properties properties, final ComponentInstLight application) {
    User requester = User.getCurrentRequester();
    String language = requester.getUserPreferences().getLanguage();
    User creator = User.getById(String.valueOf(application.getCreatedBy()));
    User lastUpdater = User.getById(String.valueOf(application.getUpdatedBy()));

    Map<String, PropertyData<?>> props = properties.getProperties();
    assertThat(props.get(PropertyIds.OBJECT_ID).getFirstValue(), is(application.getId()));
    assertThat(props.get(PropertyIds.NAME).getFirstValue(), is(application.getName(language)));
    assertThat(props.get(PropertyIds.DESCRIPTION).getFirstValue(),
        is(application.getDescription(language)));
    assertThat(props.get(PropertyIds.CREATED_BY).getFirstValue(), is(creator.getDisplayedName()));
    assertThat(props.get(PropertyIds.LAST_MODIFIED_BY).getFirstValue(),
        is(lastUpdater.getDisplayedName()));
    assertThat(props.get(PropertyIds.CREATION_DATE).getFirstValue(),
        is(millisToCalendar(application.getCreationDate().getTime())));
    assertThat(props.get(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue(),
        is(millisToCalendar(application.getLastUpdateDate().getTime())));
    assertThat(props.get(PropertyIds.BASE_TYPE_ID).getFirstValue(),
        is(BaseTypeId.CMIS_FOLDER.value()));
    assertThat(props.get(PropertyIds.OBJECT_TYPE_ID).getFirstValue(),
        is(TypeId.SILVERPEAS_APPLICATION.value()));

    assertThat(props.get(PropertyIds.PARENT_ID).getFirstValue(),
        is(application.getDomainFatherId()));
    assertThat(props.get(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS).getValues(),
        is(Arrays.asList(TypeId.SILVERPEAS_FOLDER.value(), TypeId.SILVERPEAS_PUBLICATION.value())));

    TreeNode appNode = organization.findTreeNodeById(application.getId());
    String path = pathToNode(appNode, language);
    assertThat(props.get(PropertyIds.PATH).getFirstValue(), is(path));
  }

  /**
   * Asserts the specified properties of a CMIS object matches the expected Silverpeas business
   * object.
   * @param properties the CMIS properties of a CMIS object.
   * @param node a {@link NodeDetail} instance in Silverpeas.
   */
  private void assertProperties(final Properties properties, final NodeDetail node) {
    User requester = User.getCurrentRequester();
    String language = requester.getUserPreferences().getLanguage();
    String appId = node.getIdentifier().getComponentInstanceId();

    Map<String, PropertyData<?>> props = properties.getProperties();
    assertThat(props.get(PropertyIds.OBJECT_ID).getFirstValue(),
        is(node.getIdentifier().asString()));
    assertThat(props.get(PropertyIds.NAME).getFirstValue(), is(node.getName(language)));
    assertThat(props.get(PropertyIds.DESCRIPTION).getFirstValue(),
        is(node.getDescription(language)));
    assertThat(props.get(PropertyIds.CREATED_BY).getFirstValue(),
        is(node.getCreator().getDisplayedName()));
    assertThat(props.get(PropertyIds.LAST_MODIFIED_BY).getFirstValue(),
        is(node.getLastUpdater().getDisplayedName()));
    assertThat(props.get(PropertyIds.CREATION_DATE).getFirstValue(),
        is(millisToCalendar(node.getCreationDate().getTime())));
    assertThat(props.get(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue(),
        is(millisToCalendar(node.getLastUpdateDate().getTime())));
    assertThat(props.get(PropertyIds.BASE_TYPE_ID).getFirstValue(),
        is(BaseTypeId.CMIS_FOLDER.value()));
    assertThat(props.get(PropertyIds.OBJECT_TYPE_ID).getFirstValue(),
        is(TypeId.SILVERPEAS_FOLDER.value()));

    String parentId = node.getFatherPK().isRoot() ? appId :
        ContributionIdentifier.from(node.getFatherPK(), NodeDetail.TYPE).asString();
    assertThat(props.get(PropertyIds.PARENT_ID).getFirstValue(), is(parentId));
    assertThat(props.get(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS).getValues(),
        containsInAnyOrder(TypeId.SILVERPEAS_FOLDER.value(),
            TypeId.SILVERPEAS_PUBLICATION.value()));

    TreeNode folderNode = organization.findTreeNodeById(node.getIdentifier().asString());
    String path = pathToNode(folderNode, language);
    assertThat(props.get(PropertyIds.PATH).getFirstValue(), is(path));
  }

  /**
   * Asserts the specified properties of a CMIS object matches the expected Silverpeas business
   * object.
   * @param properties the CMIS properties of a CMIS object.
   * @param pub a {@link PublicationDetail} instance in Silverpeas.
   */
  private void assertProperties(final Properties properties, final PublicationDetail pub) {
    User requester = User.getCurrentRequester();
    String language = requester.getUserPreferences().getLanguage();

    Map<String, PropertyData<?>> props = properties.getProperties();
    assertThat(props.get(PropertyIds.OBJECT_ID).getFirstValue(),
        is(pub.getIdentifier().asString()));
    assertThat(props.get(PropertyIds.NAME).getFirstValue(), is(pub.getName(language)));
    assertThat(props.get(PropertyIds.DESCRIPTION).getFirstValue(),
        is(pub.getDescription(language)));
    assertThat(props.get(PropertyIds.CREATED_BY).getFirstValue(),
        is(pub.getCreator().getDisplayedName()));
    assertThat(props.get(PropertyIds.LAST_MODIFIED_BY).getFirstValue(),
        is(pub.getLastUpdater().getDisplayedName()));
    assertThat(props.get(PropertyIds.CREATION_DATE).getFirstValue(),
        is(millisToCalendar(pub.getCreationDate().getTime())));
    assertThat(props.get(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue(),
        is(millisToCalendar(pub.getLastUpdateDate().getTime())));
    assertThat(props.get(PropertyIds.BASE_TYPE_ID).getFirstValue(),
        is(BaseTypeId.CMIS_FOLDER.value()));
    assertThat(props.get(PropertyIds.OBJECT_TYPE_ID).getFirstValue(),
        is(TypeId.SILVERPEAS_PUBLICATION.value()));

    Optional<Location> location = publicationService.getMainLocation(pub.getPK());
    assertThat(location.isPresent(), is(true));
    Location folder = location.get();
    String folderId = folder.isRoot() ? pub.getInstanceId() :
        ContributionIdentifier.from(folder, NodeDetail.TYPE).asString();
    assertThat(props.get(PropertyIds.PARENT_ID).getFirstValue(), is(folderId));
    assertThat(props.get(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS).getValues(),
        containsInAnyOrder(TypeId.SILVERPEAS_DOCUMENT.value()));

    TreeNode pubNode = organization.findTreeNodeById(pub.getIdentifier().asString());
    String path = pathToNode(pubNode, language);
    assertThat(props.get(PropertyIds.PATH).getFirstValue(), is(path));
  }

  /**
   * Asserts the specified properties of a CMIS object matches the expected Silverpeas business
   * object.
   * @param properties the CMIS properties of a CMIS object.
   * @param doc a {@link SimpleDocument} instance in Silverpeas.
   */
  private void assertProperties(final Properties properties, final SimpleDocument doc) {
    Map<String, PropertyData<?>> props = properties.getProperties();
    String comment = StringUtil.isDefined(doc.getComment()) ? doc.getComment() : "";
    assertThat(props.get(PropertyIds.OBJECT_ID).getFirstValue(),
        is(doc.getIdentifier().asString()));
    assertThat(props.get(PropertyIds.NAME).getFirstValue(), is(doc.getFilename()));
    assertThat(props.get(PropertyIds.DESCRIPTION).getFirstValue(), is(doc.getDescription()));
    assertThat(props.get(PropertyIds.CREATED_BY).getFirstValue(),
        is(doc.getCreator().getDisplayedName()));
    assertThat(props.get(PropertyIds.LAST_MODIFIED_BY).getFirstValue(),
        is(doc.getLastUpdater().getDisplayedName()));
    assertThat(props.get(PropertyIds.CREATION_DATE).getFirstValue(),
        is(millisToCalendar(doc.getCreationDate().getTime())));
    assertThat(props.get(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue(),
        is(millisToCalendar(doc.getLastUpdateDate().getTime())));
    assertThat(props.get(PropertyIds.BASE_TYPE_ID).getFirstValue(),
        is(BaseTypeId.CMIS_DOCUMENT.value()));
    assertThat(props.get(PropertyIds.OBJECT_TYPE_ID).getFirstValue(),
        is(TypeId.SILVERPEAS_DOCUMENT.value()));
    assertThat(props.get(PropertyIds.IS_IMMUTABLE).getFirstValue(), is(false));
    assertThat(props.get(PropertyIds.IS_LATEST_VERSION).getFirstValue(), is(true));
    assertThat(props.get(PropertyIds.IS_MAJOR_VERSION).getFirstValue(), is(true));
    assertThat(props.get(PropertyIds.IS_LATEST_MAJOR_VERSION).getFirstValue(), is(true));
    assertThat(props.get(PropertyIds.VERSION_LABEL).getFirstValue(), is(doc.getTitle()));
    assertThat(props.get(PropertyIds.VERSION_SERIES_ID).getFirstValue(),
        is(doc.getIdentifier().asString()));
    assertThat(props.get(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT).getFirstValue(), is(false));
    assertThat(props.get(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY).getFirstValue(), nullValue());
    assertThat(props.get(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID).getFirstValue(), nullValue());
    assertThat(props.get(PropertyIds.CHECKIN_COMMENT).getFirstValue(), is(comment));
    assertThat(props.get(PropertyIds.IS_PRIVATE_WORKING_COPY).getFirstValue(), is(false));
    assertThat(
        ((BigInteger) props.get(PropertyIds.CONTENT_STREAM_LENGTH).getFirstValue()).longValue(),
        is(doc.getSize()));
    assertThat(props.get(PropertyIds.CONTENT_STREAM_MIME_TYPE).getFirstValue(),
        is(doc.getContentType()));
    assertThat(props.get(PropertyIds.CONTENT_STREAM_FILE_NAME).getFirstValue(),
        is(doc.getFilename()));
    assertThat(props.get(PropertyIds.CONTENT_STREAM_ID).getFirstValue(), nullValue());
  }
}
  