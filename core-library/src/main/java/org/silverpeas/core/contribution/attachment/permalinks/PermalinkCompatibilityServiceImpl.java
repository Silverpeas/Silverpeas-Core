/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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
package org.silverpeas.core.contribution.attachment.permalinks;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.permalinks.model.DocumentPermalink;
import org.silverpeas.core.contribution.attachment.permalinks.model.VersionPermalink;
import org.silverpeas.core.contribution.attachment.permalinks.repository.DocumentPermalinkRepository;
import org.silverpeas.core.contribution.attachment.permalinks.repository.VersionPermalinkRepository;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Default
@Service
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class PermalinkCompatibilityServiceImpl implements PermalinkCompatibilityService {

  @Inject
  private AttachmentService service;

  @Inject
  private DocumentPermalinkRepository docManager;

  @Inject
  private VersionPermalinkRepository versionManager;

  public SimpleDocument findDocumentVersionByOldId(int oldId) {
    VersionPermalink link = versionManager.getById(Integer.toString(oldId));
    if (link != null) {
      SimpleDocumentPK pk = new SimpleDocumentPK(link.getUuid(), "");
      return service.searchDocumentById(pk, null);
    }
    return null;
  }

  public SimpleDocument findVersionnedDocumentByOldId(int oldId) {
    DocumentPermalink link = docManager.getById(Integer.toString(oldId));
    if (link != null) {
      SimpleDocumentPK pk = new SimpleDocumentPK(link.getUuid(), "");
      return service.searchDocumentById(pk, null);
    }
    return null;
  }

}
