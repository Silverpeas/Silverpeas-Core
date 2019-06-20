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
package org.silverpeas.core.io.upload;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.util.AttachmentSettings;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;

import java.io.File;
import java.util.Map;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * Representation of an uploaded file.<br>
 * Each {@link UploadedFile} is associated to a unique {@link UploadSession} instance. So, it can
 * not be possible to get several {@link UploadedFile} from an {@link UploadSession}.
 * @author Yohann Chastagnier
 */
public class UploadedFile {

  private UploadSession uploadSession;
  private File file;
  private String title;
  private String description;
  private String uploader;

  /**
   * Creates a representation of an uploaded file from HttpServletRequest and a given uploaded file
   * identifier.
   * @param parameters
   * @param uploadSessionId
   * @param uploader
   * @return
   */
  public static UploadedFile from(Map<String, String[]> parameters, String uploadSessionId,
      User uploader) {
    UploadSession uploadSession = UploadSession.from(uploadSessionId);
    return new UploadedFile(uploadSession, getUploadedFile(uploadSession),
        parameters.get(uploadSessionId + "-title")[0],
        parameters.get(uploadSessionId + "-description")[0], uploader.getId());
  }

  /**
   * Default constructor.
   * @param uploadSession
   * @param file
   * @param title
   * @param description
   */
  private UploadedFile(final UploadSession uploadSession, final File file, final String title,
      final String description, String uploader) {
    this.uploadSession = uploadSession;
    this.file = file;
    this.title = title;
    this.description = description;
    this.uploader = uploader;
  }

  /**
   * Gets the identifier of the uploaded file.
   * @return
   */
  public UploadSession getUploadSession() {
    return uploadSession;
  }

  /**
   * Gets the uploaded file.
   * @return
   */
  public File getFile() {
    return file;
  }

  /**
   * Gets the title filled by the user for the uploaded file.
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the description filled by the user for the uploaded file.
   * @return
   */
  public String getDescription() {
    return description;
  }

  /**
   * Indicates that the uploaded file has been processed. Uploaded physical file is deleted from
   * its temporary upload repository.
   */
  private void markAsProcessed() {
    uploadSession.clear();
  }

  /**
   * Register an attachment attached in relation to the given contribution identifiers. Please
   * notice that the original content is deleted from its original location. For now, as this
   * method
   * is exclusively used for contribution creations, the treatment doesn't search for existing
   * attachments. In the future and if updates will be handled, the treatment must evolve to search
   * for existing attachments ...
   * @param contributionId the identifier of a contribution.
   * @param contributionLanguage the language in which the contribution is authored.
   */
  public void registerAttachment(ContributionIdentifier contributionId, String contributionLanguage,
      boolean indexIt) {
    registerAttachment(contributionId.getLocalId(), contributionId.getComponentInstanceId(),
        contributionLanguage, indexIt);
  }

  /**
   * Register an attachment in relation to the given contribution identifiers. Please notice that
   * the original content is deleted from its original location. For now, as this method is
   * exclusively used for contribution creations, the treatment doesn't search for existing
   * attachments. In the future and if updates will be handled, the treatment must evolve to search
   * for existing attachments ...
   * @param resourceId
   * @param componentInstanceId
   * @param contributionLanguage
   * @param indexIt
   */
  private void registerAttachment(String resourceId, String componentInstanceId,
      String contributionLanguage, boolean indexIt) {

    // Retrieve the simple document
    SimpleDocument document = retrieveSimpleDocument(new ResourceReference(resourceId, componentInstanceId),
        contributionLanguage);

    // Create attachment (please read the method documentation ...)
    AttachmentServiceProvider.getAttachmentService()
        .createAttachment(document, getFile(), indexIt, false);

    // Delete the original content from its original location.
    markAsProcessed();
  }

  /**
   * Retrieve the SimpleDocument in relation with uploaded file. For now, as this method is
   * exclusively used for contribution creations, the treatment doesn't search for existing
   * attachments. In the future and if updates will be handled, the treatment must evolve to search
   * for existing attachments ...
   * @param contributionLanguage (be careful, not the user language ...)
   * @return
   */
  public SimpleDocument retrieveSimpleDocument(WAPrimaryKey resourcePk,
      String contributionLanguage) {

    // Contribution language
    String lang = I18NHelper.checkLanguage(contributionLanguage);

    // Title and description
    String title = defaultStringIfNotDefined(getTitle());
    String description = defaultStringIfNotDefined(getDescription());
    if (AttachmentSettings.isUseFileMetadataForAttachmentDataEnabled() &&
        !StringUtil.isDefined(title)) {
      MetadataExtractor extractor = MetadataExtractor.get();
      MetaData metadata = extractor.extractMetadata(getFile());
      if (StringUtil.isDefined(metadata.getTitle())) {
        title = metadata.getTitle();
      }
      if (!StringUtil.isDefined(description) && StringUtil.isDefined(metadata.getSubject())) {
        description = metadata.getSubject();
      }
    }
    // Simple document PK
    SimpleDocumentPK pk = new SimpleDocumentPK(null, resourcePk);

    // Simple document
    SimpleDocument document = new SimpleDocument(pk, resourcePk.getId(), 0, false, null,
        new SimpleAttachment(getFile().getName(), lang, title, description, getFile().length(),
            FileUtil.getMimeType(getFile().getPath()), uploader, DateUtil.getNow(), null)
    );

    // Simple document details
    document.setLanguage(lang);
    document.setTitle(title);
    document.setDescription(description);
    document.setSize(getFile().length());

    // Result
    return document;
  }

  /**
   * Gets an uploaded file from a given upload session.
   * @param uploadSession
   * @return
   */
  private static File getUploadedFile(UploadSession uploadSession) {
    File[] files = uploadSession.getRootFolderFiles();
    if (files.length != 1) {
      return new File(FileRepositoryManager.getTemporaryPath(), "unexistingFile");
    }
    return files[0];
  }
}
