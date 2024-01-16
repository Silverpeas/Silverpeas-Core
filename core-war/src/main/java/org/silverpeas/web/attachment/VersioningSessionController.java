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
package org.silverpeas.web.attachment;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.repository.HistoryDocumentSorter;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

public class VersioningSessionController extends AbstractComponentSessionController {
  private static final long serialVersionUID = -6068845833609838967L;

  private String contentLanguage;
  private String currentProfile = null;
  public static final String ADMIN = SilverpeasRole.admin.toString();
  public static final String PUBLISHER = SilverpeasRole.publisher.toString();

  public String getProfile() {
    if (!StringUtil.isDefined(this.currentProfile)) {
      this.currentProfile = getHighestSilverpeasUserRole().getName();
    }
    return this.currentProfile;
  }

  public void setProfile(String profile) {
    if (StringUtil.isDefined(profile)) {
      this.currentProfile = profile;
    }
  }

  public VersioningSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.versioningPeas.multilang.versioning",
        null);
    setComponentRootName(URLUtil.CMP_VERSIONINGPEAS);
  }

  public SimpleDocument getDocument(SimpleDocumentPK documentPK) {
    return AttachmentServiceProvider.getAttachmentService()
        .searchDocumentById(documentPK, getContentLanguage());
  }

  List<SimpleDocument> getAccessibleDocumentVersions(final SimpleDocument document,
      final boolean fromAlias) {
    List<SimpleDocument> versions;
    if (fromAlias || !document.canBeModifiedBy(getUserDetail())) {
      versions = getPublicDocumentVersions(document.getPk());
      if (document.isPublic() && !versions.contains(document)) {
        versions.add(document);
      }
    } else {
      versions = getDocumentVersions(document.getPk());
      versions.add(document);
    }
    HistoryDocumentSorter.sortHistory(versions);
    return versions;
  }

  @SuppressWarnings("unchecked")
  private List<SimpleDocument> getDocumentVersions(SimpleDocumentPK documentPK) {
    return (List)((HistorisedDocument) AttachmentServiceProvider.getAttachmentService()
        .searchDocumentById(documentPK, getContentLanguage())).getFunctionalHistory();
  }

  private List<SimpleDocument> getPublicDocumentVersions(SimpleDocumentPK documentPK) {
    SimpleDocument currentDoc = AttachmentServiceProvider.getAttachmentService().searchDocumentById(
        documentPK, getContentLanguage());
    if (currentDoc.isVersioned()) {
      return ((HistorisedDocument) currentDoc).getPublicVersions();
    }
    return Collections.singletonList(currentDoc);
  }

  public String getContentLanguage() {
    return contentLanguage;
  }

  public void setContentLanguage(final String contentLanguage) {
    this.contentLanguage = defaultStringIfNotDefined(contentLanguage, this.contentLanguage);
  }
}
