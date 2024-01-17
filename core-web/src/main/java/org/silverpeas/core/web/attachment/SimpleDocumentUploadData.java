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
package org.silverpeas.core.web.attachment;

import org.silverpeas.core.documenttemplate.DocumentTemplate;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.http.RequestFile;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.core.webapi.documenttemplate.DocumentTemplateWebManager;

import javax.ws.rs.FormParam;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.core.admin.user.model.User.getCurrentRequester;
import static org.silverpeas.core.io.upload.FileUploadManager.getUploadedFiles;
import static org.silverpeas.core.web.util.IFrameAjaxTransportUtil.X_REQUESTED_WITH;

/**
 * @author: Yohann Chastagnier
 */
public class SimpleDocumentUploadData {

  /**
   * Decodes the request parameters in order to return a {@link SimpleDocumentUploadData} instance.
   * @param request the {@link HttpRequest} wrapper that handle efficiently all parameters,
   * included
   * those of multipart request type.
   * @return a {@link SimpleDocumentUploadData} instance.
   */
  public static SimpleDocumentUploadData decode(HttpRequest request) {
    final SimpleDocumentUploadData uploadData = RequestParameterDecoder.decode(request,
        SimpleDocumentUploadData.class);
    if (uploadData.requestFile == null) {
      final List<UploadedFile> uploadedFiles = getUploadedFiles(request, getCurrentRequester());
      if (uploadedFiles.size() == 1) {
        uploadData.requestFile = new RequestFile(uploadedFiles.get(0).asFileItem());
      }
    }
    return uploadData;
  }

  /**
   * Detail about the uploaded file like the filename for example.
   * It provides the input stream from which the content of the file can be read.
   */
  @FormParam("file_upload")
  private RequestFile requestFile;

  /**
   * A parameter indicating from which the upload was performed. It is valued
   * with the identifier of the HTML or javascript component at the origin of the uploading.
   * According to his value, the expected response can be different.
   */
  @FormParam(X_REQUESTED_WITH)
  private String xRequestedWith;

  /**
   * Identifier of a document template. If an identifier exists, then no {@link #requestFile} has
   * been uploaded.
   */
  @FormParam("documentTemplateId")
  private String documentTemplateId;

  /**
   * The two-characters code of the language in which the document's content is
   * written.
   */
  @FormParam("fileLang")
  private String language;

  /**
   * The title of the document as indicated by the user.
   */
  @FormParam("fileTitle")
  private String title;

  /**
   * A short description of the document's content as indicated by the user.
   */
  @FormParam("fileDescription")
  private String description;

  /**
   * The identifier of the resource to which the document belong (a publication for
   * example).
   */
  @FormParam("foreignId")
  private String foreignId;

  /**
   * Flag meaning the indexation or not of the document's content.
   */
  @FormParam("indexIt")
  private boolean indexIt;

  /**
   * The scope of the version (public or private) in the case of a versioned document.
   */
  @FormParam("versionType")
  private Integer versionType;

  /**
   * A comment about the upload.
   */
  @FormParam("commentMessage")
  private String comment;

  /**
   * The context in which the document is created: in the case of a file attachment,
   * in the case of a WYSIWYG edition, ...
   */
  @FormParam("context")
  private String context;

  /**
   * @see #requestFile
   */
  public RequestFile getRequestFile() {
    return requestFile;
  }

  /**
   * @see #xRequestedWith
   */
  public String getXRequestedWith() {
    return xRequestedWith;
  }

  /**
   * @see #documentTemplateId
   */
  public Optional<DocumentTemplate> getDocumentTemplate() {
    return Optional.ofNullable(documentTemplateId)
        .filter(StringUtil::isDefined)
        .map(DocumentTemplateWebManager.get()::getDocumentTemplate);
  }

  /**
   * @see #language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @see #title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @see #description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @see #foreignId
   */
  public String getForeignId() {
    return foreignId;
  }

  /**
   * @see #indexIt
   */
  public boolean getIndexIt() {
    return indexIt;
  }

  /**
   * @see #versionType
   */
  public Integer getVersionType() {
    return versionType;
  }

  /**
   * @see #comment
   */
  public String getComment() {
    return comment;
  }

  /**
   * @see #context
   */
  public String getContext() {
    return context;
  }
}
