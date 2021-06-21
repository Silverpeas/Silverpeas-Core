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
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.cmis.util.CmisProperties;
import org.silverpeas.core.cmis.model.CmisFile;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.Space;

import javax.inject.Inject;
import java.util.List;

/**
 * A dummy tree walker to be used as a delegator of {@link CmisObjectsTreeWalker}'s method
 * invocations to the correct {@link CmisObjectsTreeWalker} objects able to answer this invocation.
 *
 * @author mmoquillon
 */
public class CmisObjectsTreeWalkerDelegator implements CmisObjectsTreeWalker {

  @Inject
  private TreeWalkerSelector selector;

  @Override
  public CmisObject createChildData(final String folderId, final CmisProperties properties,
      final ContentStream contentStream, final String language) {
    return selector.selectByObjectIdOrFail(folderId)
        .createChildData(folderId, properties, contentStream, language);
  }

  @Override
  public CmisObject updateObjectData(final String objectId, final CmisProperties properties,
      final ContentStream contentStream, final String language) {
    return selector.selectByObjectIdOrFail(objectId)
        .updateObjectData(objectId, properties, contentStream, language);
  }

  @Override
  public CmisObject getObjectData(final String objectId, final Filtering filtering) {
    return selector.selectByObjectIdOrFail(objectId).getObjectData(objectId, filtering);
  }

  @Override
  public CmisFile getObjectDataByPath(final String path, final Filtering filtering) {
    return selector.selectByObjectIdOrFail(Space.ROOT_ID.asString()).getObjectDataByPath(path, filtering);
  }

  @Override
  public List<ObjectParentData> getParentsData(final String objectId, final Filtering filtering) {
    return selector.selectByObjectIdOrFail(objectId).getParentsData(objectId, filtering);
  }

  @Override
  public ObjectInFolderList getChildrenData(final String folderId, final Filtering filtering,
      final Paging paging) {
    return selector.selectByObjectIdOrFail(folderId).getChildrenData(folderId, filtering, paging);
  }

  @Override
  public List<ObjectInFolderContainer> getSubTreeData(final String folderId,
      final Filtering filtering, final long depth) {
    return selector.selectByObjectIdOrFail(folderId).getSubTreeData(folderId, filtering, depth);
  }

  @Override
  public ContentStream getContentStream(final String objectId, final String language,
      final long offset, final long length) {
    return selector.selectByObjectIdOrFail(objectId)
        .getContentStream(objectId, language, offset, length);
  }
}
  