/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.accesscontrol;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import java.rmi.RemoteException;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Check the access to a document version for a user.
 * @author ehugonnet
 */
@Named
public class DocumentVersionAccessController implements AccessController<DocumentVersion> {

  private VersioningUtil versioning;
  @Inject
  private DocumentAccessController accessController;

  public DocumentVersionAccessController() {
    versioning = new VersioningUtil();
  }

  /**
   * For test only.
   * @param versioning
   */
  DocumentVersionAccessController(VersioningUtil versioning) {
    this.versioning = versioning;
  }

  @Override
  public boolean isUserAuthorized(String userId, DocumentVersion object) {
    Document doc;
    try {
      doc = versioning.getDocument(object.getDocumentPK());
    } catch (RemoteException ex) {
      SilverTrace.error("accesscontrol", getClass().getSimpleName() + ".isUserAuthorized()",
          "root.NO_EX_MESSAGE", ex);
      return false;
    }
    return getDocumentAccessController().isUserAuthorized(userId, doc);
  }

  /**
   * Gets a controller of access on documents.
   * @return a DocumentAccessController instance.
   */
  private DocumentAccessController getDocumentAccessController() {
    if (accessController == null) {
      accessController = new DocumentAccessController();
    }
    return accessController;
  }
}
