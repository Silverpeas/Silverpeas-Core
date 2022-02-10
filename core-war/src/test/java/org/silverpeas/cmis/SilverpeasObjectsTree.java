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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.cmis;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.persistence.ComponentInstanceRow;
import org.silverpeas.core.admin.persistence.SpaceRow;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.StringUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A tree of Silverpeas objects that can be exposed by our CMIS implementation.
 * @author mmoquillon
 */
public class SilverpeasObjectsTree {

  public static final String LANGUAGE = "en";

  private final Set<TreeNode> rootSpaces = new HashSet<>();
  private final Map<String, TreeNode> cache = new HashMap<>();

  private int customLocalIdCount = 100;

  public void clear() {
    cache.forEach((id, treeNode) -> {
      treeNode.getChildren().clear();
    });
    rootSpaces.clear();
    cache.clear();
  }

  public TreeNode addRootSpace(final SpaceInstLight space) {
    space.setFatherId("0");
    TreeNode node = new TreeNode(cache, space, null);
    rootSpaces.add(node);
    cache.put(space.getId(), node);
    return node;
  }

  public Set<TreeNode> getRootNodes() {
    return rootSpaces;
  }

  public TreeNode findTreeNodeById(final String nodeId) {
    return cache.get(nodeId);
  }

  public TreeNode addSpace(int localId, String fatherId, int order, String name,
      String description) {
    boolean isRoot = StringUtil.isNotDefined(fatherId);
    SpaceRow row = new SpaceRow();
    row.id = localId;
    row.lang = LANGUAGE;
    row.name = name;
    row.description = description;
    row.domainFatherId = isRoot ? 0 : Integer.parseInt(fatherId.substring(2));
    row.createdBy = 0;
    row.createTime = String.valueOf(System.currentTimeMillis());
    row.updatedBy = row.createdBy;
    row.updateTime = row.createTime;
    row.orderNum = order;
    row.displaySpaceFirst = 1;
    row.inheritanceBlocked = 0;
    row.firstPageType = 0;
    final SpaceInstLight space = new SpaceInstLight(row);

    TreeNode node;
    if (isRoot) {
      node = addRootSpace(space);
    } else {
      TreeNode parentNode = cache.get(fatherId);
      node = parentNode.addChild(space);
    }

    return node;
  }

  public TreeNode addApplication(String type, int localId, String fatherId, int order, String name,
      String description) {
    ComponentInstanceRow row = new ComponentInstanceRow();
    row.id = localId;
    row.lang = LANGUAGE;
    row.name = name;
    row.description = description;
    row.spaceId = Integer.parseInt(fatherId.substring(2));
    row.createdBy = 0;
    row.createTime = String.valueOf(System.currentTimeMillis());
    row.updatedBy = row.createdBy;
    row.updateTime = row.createTime;
    row.orderNum = order;
    row.inheritanceBlocked = 0;
    row.componentName = type.toLowerCase();
    row.hidden = 0;
    row.publicAccess = 0;
    final ComponentInstLight application = new ComponentInstLight(row);

    TreeNode parentNode = cache.get(fatherId);
    return parentNode.addChild(application);
  }

  public TreeNode addFolder(int localId, String fatherId, int depth, String name,
      String description) {
    String appId;
    String fatherNodeId;
    TreeNode parentNode;
    try {
      ContributionIdentifier fatherFolderId = ContributionIdentifier.decode(fatherId);
      appId = fatherFolderId.getComponentInstanceId();
      fatherNodeId = fatherFolderId.getLocalId();
      parentNode = cache.get(fatherId);
    } catch (IllegalArgumentException e) {
      appId = fatherId;
      fatherNodeId = "-1";
      localId = Integer.parseInt(NodePK.ROOT_NODE_ID);
      parentNode = cache.get(appId);
    }

    NodePK pk = new NodePK(String.valueOf(localId), appId);
    NodeDetail node = new NodeDetail(pk, name, description, depth, fatherNodeId);
    node.setCreationDate(new Date());
    node.setCreatorId("0");
    node.setLevel(1);
    node.setPath("/");
    return parentNode.addChild(node);
  }

  public TreeNode addPublication(int localId, String folderId, String name, String description) {
    TreeNode parentNode = cache.get(folderId);
    ContributionIdentifier folder = ContributionIdentifier.decode(folderId);
    PublicationPK pk = new PublicationPK(String.valueOf(localId), folder.getComponentInstanceId());
    Date today = new Date();
    PublicationDetail pub = PublicationDetail.builder(LANGUAGE)
        .setPk(pk)
        .setNameAndDescription(name, description)
        .created(today, "0")
        .setImportance(1)
        .setBeginDateTime(today, null)
        .setKeywords("")
        .setContentPagePath("")
        .build();

    pub.setStatus(PublicationDetail.VALID_STATUS);
    pub.setUpdaterId(pub.getCreatorId());
    pub.setUpdateDate(pub.getCreationDate());
    pub.setCloneId("-1");
    pub.setInfoId("0");
    return parentNode.addChild(pub);
  }

  public TreeNode addPublication(PublicationDetail publication, String folderId) {
    TreeNode parentNode = cache.get(folderId);
    ContributionIdentifier folder = ContributionIdentifier.decode(folderId);
    PublicationPK pk = new PublicationPK(String.valueOf(customLocalIdCount++),
        folder.getComponentInstanceId());
    publication.setPk(pk);
    return parentNode.addChild(publication);
  }

  public TreeNode addDocument(int localId, String pubId, String name, String description) {
    TreeNode parentNode = cache.get(pubId);
    int order = parentNode.getChildren().size() + 1;
    ContributionIdentifier pub = ContributionIdentifier.decode(pubId);
    SimpleDocumentPK pk =
        new SimpleDocumentPK(String.valueOf(localId), pub.getComponentInstanceId());
    Date today = new Date();
    SimpleAttachment attachment = SimpleAttachment.builder(LANGUAGE)
        .setFilename(name + ".pdf")
        .setTitle(name)
        .setDescription(description)
        .setSize(102400L)
        .setContentType(MimeTypes.PDF_MIME_TYPE)
        .setCreationData("0", today)
        .build();
    SimpleDocument doc = new SimpleDocument(pk, pub.getLocalId(), order, false, attachment);
    doc.setDocumentType(DocumentType.attachment);
    doc.setCreationDate(today);
    doc.setUpdatedBy("0");
    doc.setLastUpdateDate(today);
    return parentNode.addChild(doc);
  }
}
  