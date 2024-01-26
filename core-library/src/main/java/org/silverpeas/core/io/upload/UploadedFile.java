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
package org.silverpeas.core.io.upload;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.ResourceReference;
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
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;

import java.io.File;
import java.util.Map;

import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

/**
 * Representation of an uploaded file.<br> Each {@link UploadedFile} is associated to a unique
 * {@link UploadSession} instance. So, it can not be possible to get several {@link UploadedFile}
 * from an {@link UploadSession}.
 * @author Yohann Chastagnier
 */
public class UploadedFile {

  private final UploadSession uploadSession;
  private final File file;
  private final String title;
  private final String description;
  private final String uploader;

  /**
   * Creates a representation of an uploaded file from the specified file parameters, the upload
   * session identifier and the user uploading the file.
   * identifier.
   * @param parameters upload parameters in which are provided data about the file (title,
   * description, ...)
   * @param uploadSessionId the unique identifier of a files upload session.
   * @param uploader the user that has uploaded a file.
   * @return an {@link UploadedFile} instance corresponding to a file that has been uploaded by a
   * user.
   */
  public static UploadedFile from(Map<String, String[]> parameters, String uploadSessionId,
      User uploader) {
    UploadSession uploadSession = UploadSession.from(uploadSessionId);
    return new UploadedFile(uploadSession, getUploadedFile(uploadSession),
        parameters.get(uploadSessionId + "-title")[0],
        parameters.get(uploadSessionId + "-description")[0], uploader.getId());
  }

  private UploadedFile(final UploadSession uploadSession, final File file, final String title,
      final String description, String uploader) {
    this.uploadSession = uploadSession;
    this.file = file;
    this.title = title;
    this.description = description;
    this.uploader = uploader;
  }

  /**
   * Gets the upload files session.
   * @return the session in which this file has been uploaded.
   */
  public UploadSession getUploadSession() {
    return uploadSession;
  }

  /**
   * Gets the uploaded file.
   * @return a {@link File} instance of the uploaded file.
   */
  public File getFile() {
    return file;
  }

  /**
   * Gets the title filled by the user for the uploaded file.
   * @return the title of the content of the file.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the description filled by the user for the uploaded file.
   * @return a short description about the content of the file.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Converts the current instance into a {@link FileItem} one.
   * @return a {@link FileItem} instance.
   */
  public FileItem asFileItem() {
    return new UploadedFileItem(this);
  }

  /**
   * Indicates that the uploaded file has been processed. Uploaded physical file is deleted from
   * its temporary upload repository.
   */
  private void markAsProcessed() {
    uploadSession.clear();
  }

  /**
   * Registers in Silverpeas a document attached to the given contribution and for which this file
   * stores its content. Please notice that the uploaded content is deleted from its original
   * location. For now, as this method is exclusively used for contribution creations, the treatment
   * doesn't search for existing attachments. In the future and if updates will be handled, the
   * treatment must evolve to search for existing attachment related to the contribution.
   * @param contributionId the identifier of a contribution in Silverpeas.
   * @param contributionLanguage the language in which the contribution is authored.
   * @param indexIt should the content of the file be indexed?
   */
  public void registerAttachment(ContributionIdentifier contributionId, String contributionLanguage,
      boolean indexIt) {
    registerAttachment(contributionId.getLocalId(), contributionId.getComponentInstanceId(),
        contributionLanguage, indexIt);
  }

  private void registerAttachment(String resourceId, String componentInstanceId,
      String contributionLanguage, boolean indexIt) {

    // Retrieve the simple document
    SimpleDocument document =
        createSimpleDocument(new ResourceReference(resourceId, componentInstanceId),
            contributionLanguage);

    // Create attachment (please read the method documentation ...)
    AttachmentServiceProvider.getAttachmentService()
        .createAttachment(document, getFile(), indexIt, false);

    // Delete the original content from its original location.
    markAsProcessed();
  }

  private SimpleDocument createSimpleDocument(ResourceReference contribution,
      String contributionLanguage) {

    // Contribution language
    String lang = I18NHelper.checkLanguage(contributionLanguage);

    // Title and description
    String theTitle = defaultStringIfNotDefined(getTitle());
    String theDescription = defaultStringIfNotDefined(getDescription());
    if (AttachmentSettings.isUseFileMetadataForAttachmentDataEnabled() &&
        !StringUtil.isDefined(theTitle)) {
      MetadataExtractor extractor = MetadataExtractor.get();
      MetaData metadata = extractor.extractMetadata(getFile());
      if (StringUtil.isDefined(metadata.getTitle())) {
        theTitle = metadata.getTitle();
      }
      if (!StringUtil.isDefined(theDescription) && StringUtil.isDefined(metadata.getSubject())) {
        theDescription = metadata.getSubject();
      }
    }
    // Simple document PK
    SimpleDocumentPK pk = new SimpleDocumentPK(null, contribution);

    // Simple document
    SimpleAttachment attachment = SimpleAttachment.builder(lang)
        .setFilename(getFile().getName())
        .setTitle(theTitle)
        .setDescription(theDescription)
        .setSize(getFile().length())
        .setContentType(FileUtil.getMimeType(getFile().getPath()))
        .setCreationData(uploader, DateUtil.getNow())
        .build();
    SimpleDocument document =
        new SimpleDocument(pk, contribution.getId(), 0, false, null, attachment);

    // Simple document details
    document.setLanguage(lang);
    document.setTitle(theTitle);
    document.setDescription(theDescription);
    document.setSize(getFile().length());

    // Result
    return document;
  }

  private static File getUploadedFile(UploadSession uploadSession) {
    File[] files = uploadSession.getRootFolderFiles();
    if (files.length != 1) {
      return new File(FileRepositoryManager.getTemporaryPath(), "unexistingFile");
    }
    return files[0];
  }
}
