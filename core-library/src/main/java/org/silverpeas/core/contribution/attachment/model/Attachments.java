/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.contribution.attachment.model;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.io.upload.FileUploadManager;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.contribution.attachment.AttachmentServiceProvider.getAttachmentService;
import static org.silverpeas.core.contribution.attachment.util.AttachmentSettings.listFromYoungestToOldestAdd;

/**
 * A collection of files attached or in being attached to a contribution in Silverpeas. With this
 * class we can operate on a whole of an contribution's attachments instead of each one of them.
 * @author mmoquillon
 */
public class Attachments {

  private Collection<UploadedFile> uploadedFiles;

  private Attachments() {

  }

  /**
   * Creates a collection of attachments from the specified uploaded files.
   * @param uploadedFiles a collection of uploaded files.
   * @return an {@link Attachments} representing the collection of uploaded files.
   */
  public static Attachments from(final Collection<UploadedFile> uploadedFiles) {
    Attachments attachments = new Attachments();
    attachments.uploadedFiles = uploadedFiles;
    return attachments;
  }

  /**
   * Creates a collection of attachments from the specified parameters some previously uploaded
   * files.
   * @param fileParameters parameters on the uploaded files so that they can be retrieved.
   * @return an {@link Attachments} representing the collection of uploaded files.
   */
  public static Attachments from(final Map<String, String[]> fileParameters) {
    Attachments attachments = new Attachments();
    User user = User.getCurrentRequester();
    if (user != null) {
      attachments.uploadedFiles = FileUploadManager.getUploadedFiles(fileParameters, user);
    }
    return attachments;
  }

  public boolean isEmpty() {
    return this.uploadedFiles == null || this.uploadedFiles.isEmpty();
  }

  /**
   * Attaches all the files to the specified contribution. The default language of the platform is
   * used to set the language of each of the attachment.
   * @param contribution the contribution to which the attachments has to be attached.
   */
  public void attachTo(final Contribution contribution) {
    attachTo(contribution, I18NHelper.defaultLanguage);
  }

  /**
   * Attaches all the files to the specified localized contribution. The localization of the
   * contribution is used to set the localization of each attachment.
   * <p>
   * In the case the contribution is a localized instance of an i18n contribution, then its
   * expected the attachments are for this instance and hence their localization matches the one of
   * the localized contribution.
   * </p>
   * @param contribution the contribution to which the attachments has to be attached.
   */
  public void attachTo(final LocalizedContribution contribution) {
    attachTo(contribution, contribution.getLanguage());
  }

  private void attachTo(final Contribution contribution, final String language) {
    if (CollectionUtil.isNotEmpty(this.uploadedFiles)) {
      List<UploadedFile> files = new ArrayList<>(this.uploadedFiles);
      final ContributionIdentifier contributionId = contribution.getContributionId();
      if (listFromYoungestToOldestAdd() && !getAttachmentService().
          listDocumentsByForeignKeyAndType(new ResourceReference(contributionId.getLocalId(),
              contributionId.getComponentInstanceId()), DocumentType.attachment, null)
          .isManuallySorted()) {
        Collections.reverse(files);
      }
      for (UploadedFile uploadedFile : files) {
        // Register attachment
        uploadedFile.registerAttachment(contributionId, language,
            contribution.isIndexable());
      }
    }
  }
}
  