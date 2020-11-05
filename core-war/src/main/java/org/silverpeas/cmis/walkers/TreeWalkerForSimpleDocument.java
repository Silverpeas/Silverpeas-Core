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

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PartialContentStreamImpl;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.DocumentFile;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link CmisObjectsTreeWalker} object that knows how to walk down the subtree rooted to an
 * attachment of a contribution in a given Silverpeas application. The attachment is here
 * implemented by the {@link org.silverpeas.core.contribution.attachment.model.SimpleDocument} class
 * that is a localized contribution referring a document file in the filesystem of Silverpeas.
 * The document is expected to be attached either to a folder or to a publication in the CMIS
 * objects tree.
 * @author mmoquillon
 */
@Service
@Singleton
public class TreeWalkerForSimpleDocument extends AbstractCmisObjectsTreeWalker {

  @Inject
  private AttachmentService attachmentService;
  @Inject
  private PublicationService publicationService;
  @Inject
  private NodeService nodeService;

  @Override
  public ContentStream getContentStream(final String objectId, final String language,
      final long offset, final long length) {
    org.silverpeas.core.contribution.attachment.model.Document document =
        getSilverpeasObjectById(objectId);
    SimpleDocument translation = document.getTranslation(language);

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    attachmentService.getBinaryContent(buffer, translation.getPk(), translation.getLanguage(),
        offset, length <= 0 ? -1 : length);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer.toByteArray());

    ContentStreamImpl contentStream;
    if ((offset > 0) || length > 0) {
      contentStream = new PartialContentStreamImpl();
    } else {
      contentStream = new ContentStreamImpl();
    }

    contentStream.setFileName(translation.getFilename());
    contentStream.setLength(BigInteger.valueOf(translation.getSize()));
    contentStream.setMimeType(translation.getContentType());
    contentStream.setStream(inputStream);

    return contentStream;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected org.silverpeas.core.contribution.attachment.model.Document getSilverpeasObjectById(
      final String objectId) {
    ContributionIdentifier id = ContributionIdentifier.decode(objectId);
    return new org.silverpeas.core.contribution.attachment.model.Document(id);
  }

  @Override
  protected Stream<LocalizedResource> getAllowedChildrenOfSilverpeasObject(
      final ResourceIdentifier parentId, final User user) {
    return Stream.empty();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected DocumentFile createCmisObject(final LocalizedResource resource,
      final String language) {
    org.silverpeas.core.contribution.attachment.model.Document document =
        (org.silverpeas.core.contribution.attachment.model.Document) resource;
    SimpleDocument file = document.getTranslation(language);
    LocalizedResource parent = findContribution(file.getForeignId(), file.getInstanceId());
    return getObjectFactory().createDocument(file, parent.getIdentifier());
  }

  @Override
  protected boolean isSupported(final String objectId) {
    try {
      return SimpleDocument.isASimpleDocument(ContributionIdentifier.decode(objectId));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  protected List<ObjectInFolderContainer> browseObjectsInFolderTree(final LocalizedResource object,
      final Filtering filtering, final long depth) {
    return Collections.emptyList();
  }

  @Override
  protected ObjectInFolderList browseObjectsInFolder(final LocalizedResource object,
      final Filtering filtering, final Paging paging) {
    ObjectInFolderListImpl folderList = new ObjectInFolderListImpl();
    folderList.setObjects(Collections.emptyList());
    folderList.setNumItems(BigInteger.ZERO);
    folderList.setHasMoreItems(false);
    return folderList;
  }

  @Override
  protected List<ObjectParentData> browseParentsOfObject(final LocalizedResource object,
      final Filtering filtering) {
    org.silverpeas.core.contribution.attachment.model.Document document =
        (org.silverpeas.core.contribution.attachment.model.Document) object;
    String language = filtering.getLanguage();
    ContributionIdentifier parentId = document.getSourceContribution();
    LocalizedResource parent = findContribution(parentId.getLocalId(),
        parentId.getComponentInstanceId());
    CmisFolder cmisParent = AbstractCmisObjectsTreeWalker.getCmisObject(parent, language);
    CmisObject cmisObject = createCmisObject(document, language);
    ObjectParentData parentData = buildObjectParentData(cmisParent, cmisObject, filtering);
    return Collections.singletonList(parentData);
  }

  private LocalizedResource findContribution(final String id, final String instanceId) {
    LocalizedResource contribution;
    try {
      contribution = publicationService.getDetail(new PublicationPK(id, instanceId));
    } catch (Exception e) {
      contribution = null;
    }
    if (contribution == null) {
      try {
        contribution = nodeService.getDetail(new NodePK(id, instanceId));
      } catch (Exception e) {
        throw new CmisObjectNotFoundException(
            String.format("No such parent contribution %s found in application %s", id,
                instanceId));
      }
    }
    return contribution;
  }
}
  