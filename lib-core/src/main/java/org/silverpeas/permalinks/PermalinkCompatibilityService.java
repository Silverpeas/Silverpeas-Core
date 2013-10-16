/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.permalinks;

import javax.inject.Inject;

import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.permalinks.model.DocumentPermalink;
import org.silverpeas.permalinks.model.VersionPermalink;
import org.silverpeas.permalinks.repository.DocumentPermalinkRepository;
import org.silverpeas.permalinks.repository.VersionPermalinkRepository;

import com.silverpeas.annotation.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PermalinkCompatibilityService {

  @Inject
  private AttachmentService service;

  @Inject
  private DocumentPermalinkRepository docRepository;

  @Inject
  private VersionPermalinkRepository versionRepository;

  public SimpleDocument findDocumentVersionByOldId(int oldId) {
    VersionPermalink link = versionRepository.findOne(oldId);
    if (link != null) {
      SimpleDocumentPK pk = new SimpleDocumentPK(link.getUuid(), "");
      return service.searchDocumentById(pk, null);
    }
    return null;
  }

  public SimpleDocument findVersionnedDocumentByOldId(int oldId) {
    DocumentPermalink link = docRepository.findOne(oldId);
    if (link != null) {
      SimpleDocumentPK pk = new SimpleDocumentPK(link.getUuid(), "");
      return service.searchDocumentById(pk, null);
    }
    return null;
  }

}
