/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.upload;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.MetaData;
import com.silverpeas.util.MetadataExtractor;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Collection;

/**
 * Representation of an uploaded file.
 * User: Yohann Chastagnier
 * Date: 18/03/13
 */
public class UploadedFile {
  private String fileUploadId;
  private File file;
  private String title;
  private String description;

  /**
   * Creates a representation of an uploaded file from HttpServletRequest and a given uploaded file
   * identifier.
   * @param request
   * @param uploadedFileId
   * @return
   */
  public static UploadedFile from(HttpServletRequest request, String uploadedFileId) {
    return new UploadedFile(uploadedFileId, getUploadedFileFromUploadId(uploadedFileId),
        request.getParameter(uploadedFileId + "-title"),
        request.getParameter(uploadedFileId + "-description"));
  }

  /**
   * Default constructor.
   * @param fileUploadId
   * @param file
   * @param title
   * @param description
   */
  private UploadedFile(final String fileUploadId, final File file, final String title,
      final String description) {
    this.fileUploadId = fileUploadId;
    this.file = file;
    this.title = title;
    this.description = description;
  }

  /**
   * Gets the identifier of the uploaded file.
   * @return
   */
  public String getFileUploadId() {
    return fileUploadId;
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
   * Indicates that the uploaded file has been processed.
   * Uploaded physical file is deleted from its temporary upload repository.
   */
  private void markAsProcessed() {
    FileUtils.deleteQuietly(file);
  }

  /**
   * Register an attachment attached in relation to the given contribution identifiers.
   * Please notice that the original content is deleted from its original location.
   * For now, as this method is exclusively used for contribution creations,
   * the treatment doesn't search for existing attachments. In the future and if updates will be
   * handled, the treatment must evolve to search for existing attachments ...
   * @param resourceId
   * @param componentInstanceId
   * @param user
   * @param contributionLanguage
   */
  public void registerAttachment(String resourceId, String componentInstanceId, UserDetail user,
      String contributionLanguage) {
    registerAttachment(resourceId, componentInstanceId, user, contributionLanguage, true);
  }

  /**
   * Register an attachment in relation to the given contribution identifiers.
   * Please notice that the original content is deleted from its original location.
   * For now, as this method is exclusively used for contribution creations,
   * the treatment doesn't search for existing attachments. In the future and if updates will be
   * handled, the treatment must evolve to search for existing attachments ...
   * @param resourceId
   * @param componentInstanceId
   * @param user
   * @param contributionLanguage
   * @param indexIt
   */
  public void registerAttachment(String resourceId, String componentInstanceId, UserDetail user,
      String contributionLanguage, boolean indexIt) {

    // Retrieve the simple document
    SimpleDocument document =
        retrieveSimpleDocument(resourceId, componentInstanceId, user, contributionLanguage);

    // Create attachment (please read the method documentation ...)
    AttachmentServiceFactory.getAttachmentService().createAttachment(document, getFile(), indexIt);

    // Delete the original content from its original location.
    markAsProcessed();
  }

  /**
   * Retrieve the SimpleDocument in relation with uploaded file.
   * For now, as this method is exclusively used for contribution creations,
   * the treatment doesn't search for existing attachments. In the future and if updates will be
   * handled, the treatment must evolve to search for existing attachments ...
   * @param user
   * @param contributionLanguage (be careful, not the user language ...)
   * @return
   */
  public SimpleDocument retrieveSimpleDocument(String resourceId, String componentInstanceId,
      UserDetail user, String contributionLanguage) {

    // Contribution language
    String lang = I18NHelper.checkLanguage(contributionLanguage);

    // Title and description
    String title = getTitle();
    String description = getDescription();
    if (!StringUtil.isDefined(title)) {
      MetadataExtractor extractor = new MetadataExtractor();
      MetaData metadata = extractor.extractMetadata(getFile());
      if (StringUtil.isDefined(metadata.getTitle())) {
        title = metadata.getTitle();
      } else {
        title = "";
      }
      if (!StringUtil.isDefined(description) && StringUtil.isDefined(metadata.getSubject())) {
        description = metadata.getSubject();
      }
    }
    if (!StringUtil.isDefined(description)) {
      description = "";
    }

    // Simple document PK
    SimpleDocumentPK pk = new SimpleDocumentPK(null, componentInstanceId);

    // Simple document
    SimpleDocument document = new SimpleDocument(pk, resourceId, 0, false, null,
        new SimpleAttachment(getFile().getName().substring(getFileUploadId().length() + 1), lang,
            title, description, getFile().length(), FileUtil.getMimeType(getFile().getPath()),
            user.getId(), DateUtil.getNow(), null));

    // Simple document details
    document.setLanguage(lang);
    document.setTitle(title);
    document.setDescription(description);
    document.setSize(getFile().length());

    // Result
    return document;
  }

  /**
   * Gets an uploaded file from a given uploaded file identifier.
   * @param uploadedFileId
   * @return
   */
  private static File getUploadedFileFromUploadId(String uploadedFileId) {
    File tempDir = new File(FileRepositoryManager.getTemporaryPath());
    Collection<File> files =
        FileUtils.listFiles(tempDir, new PrefixFileFilter(uploadedFileId), FalseFileFilter.FALSE);
    if (files.isEmpty() || files.size() > 1) {
      return new File(tempDir, "unexistingFile");
    }
    return files.iterator().next();
  }
}
