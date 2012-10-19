/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.attachment.web;

import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.attachment.model.UnlockOption;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.web.RESTWebService;

import com.stratelia.silverpeas.versioning.model.DocumentVersion;

/**
 *
 * @author ehugonnet
 */
@Service
@RequestScoped
@Path("documents/{componentId}/document/create")
@Authorized
public class SimpleDocumentResourceCreator extends RESTWebService {

  @PathParam("componentId")
  private String componentId;

  @Override
  public String getComponentId() {
    return componentId;
  }

  /**
   * Create the the specified document.
   *
   * @param uploadedInputStream
   * @param fileDetail
   * @param language
   * @param fileTitle
   * @param description
   * @param foreignId
   * @param indexIt
   * @param type
   * @return
   */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public SimpleDocumentEntity createDocument(
      final @FormDataParam("file_upload") InputStream uploadedInputStream,
      final @FormDataParam("file_upload") FormDataContentDisposition fileDetail,
      final @FormDataParam("fileLang") String language,
      final @FormDataParam("fileTitle") String fileTitle,
      final @FormDataParam("fileDescription") String description,
      final @FormDataParam("foreignId") String foreignId,
      final @FormDataParam("indexIt") String indexIt,
      final @FormDataParam("versionType") String type) {
    if (uploadedInputStream != null && fileDetail != null && StringUtil.isDefined(fileDetail.
        getFileName())) {
      String lang = I18NHelper.checkLanguage(language);
      String title = fileTitle;
      if (!StringUtil.isDefined(fileTitle)) {
        title = fileDetail.getFileName();
      }
      SimpleDocumentPK pk = new SimpleDocumentPK(null, componentId);
      String userId = getUserDetail().getId();
      SimpleDocument document;
      boolean needCreation = true;
      boolean publicDocument = true;
      if (StringUtil.isDefined(type) && StringUtil.isInteger(type)) {
        document = AttachmentServiceFactory.getAttachmentService().findExistingDocument(pk,
            fileDetail.getFileName(), new ForeignPK(foreignId, componentId), lang);
        publicDocument = Integer.parseInt(type) == DocumentVersion.TYPE_PUBLIC_VERSION;
        needCreation = document == null;
        if (document == null) {
          document = new HistorisedDocument(pk, foreignId, 0, userId,
              new SimpleAttachment(fileDetail.getFileName(), lang, title, "", fileDetail.getSize(),
              FileUtil.getMimeType(fileDetail.getFileName()), userId, new Date(), null));
        }
        document.setPublicDocument(publicDocument);
      } else {
        document = new SimpleDocument(pk, foreignId, 0, false, null,
            new SimpleAttachment(fileDetail.getFileName(), lang, title, "", fileDetail.getSize(),
            FileUtil.getMimeType(fileDetail.getFileName()), userId, new Date(), null));
      }
      document.setLanguage(language);
      document.setTitle(title);
      document.setDescription(description);
      if (needCreation) {
        document = AttachmentServiceFactory.getAttachmentService().createAttachment(document,
            uploadedInputStream, StringUtil.getBooleanValue(indexIt), publicDocument);
      } else {
        document.edit(userId);
        AttachmentServiceFactory.getAttachmentService().lock(document.getId(), userId, language);
        AttachmentServiceFactory.getAttachmentService().updateAttachment(document, uploadedInputStream,
            StringUtil.getBooleanValue(indexIt), publicDocument);
        UnlockContext context = new UnlockContext(document.getId(), userId, language);
        context.addOption(UnlockOption.UPLOAD);
        if (!publicDocument) {
          context.addOption(UnlockOption.PRIVATE_VERSION);
        }
        AttachmentServiceFactory.getAttachmentService().unlock(context);
      }
      URI attachmentUri = getUriInfo().getRequestUriBuilder().path("document").path(document.
          getLanguage()).build();
      return SimpleDocumentEntity.fromAttachment(document).withURI(attachmentUri);
    }
    return null;
  }
}
