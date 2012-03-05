/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.attachment.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.StringTokenizer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.silverpeas.rest.RESTWebService;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.ZipManager;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

@Service
@Scope("request")
@Path("attachments/{componentId}")
public class AttachmentRessource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;
  
  @Override
  protected String getComponentId() {
    return componentId;
  }
  
  @GET
  @Path("{id}/{name}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getFileContent(@PathParam("id") String attachmentId) {
    AttachmentDetail attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(
        attachmentId));
    File realFile = getFile(attachment);
    if (!realFile.exists() && !realFile.isFile()) {
      return Response.ok().build();
    } else {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try {
        output.write(new FileInputStream(realFile));
      } catch (Exception e) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
      return Response.ok().entity(output.toByteArray()).type(attachment.getType()).build();
    }
  }
  
  @GET
  @Path("{ids}/zip")
  @Produces(MediaType.APPLICATION_JSON)
  public ZipEntity zipFiles(@PathParam("ids") String attachmentIds) {
    ZipEntity zip = null;
    StringTokenizer tokenizer = new StringTokenizer(attachmentIds, ",");
    String subDirName = Long.toString(new Date().getTime());
    String zipFileName = subDirName + ".zip";
    String exportDir = FileRepositoryManager.getTemporaryPath()+subDirName;
    while (tokenizer.hasMoreTokens()) {
      AttachmentDetail attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(
          tokenizer.nextToken()));
      File file = getFile(attachment);
      File destFile = new File(exportDir+File.separator+attachment.getLogicalName());
      try {
        FileUtils.copyFile(file, destFile);
      } catch (IOException e) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
    }
    try {
      URI downloadUri =
          getUriInfo().getBaseUriBuilder().path("attachments").path(componentId)
              .path("zipcontent/" + zipFileName).build();
      long size = ZipManager.compressPathToZip(exportDir, exportDir+".zip");
      zip = new ZipEntity(getUriInfo().getRequestUri(), downloadUri.toString(), size);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return zip;
  }
  
  @GET
  @Path("zipcontent/{name}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getZipContent(@PathParam("name") String zipFileName) {
    String zipFilePath = FileRepositoryManager.getTemporaryPath()+zipFileName;
    File realFile = new File(zipFilePath);
    if (!realFile.exists() && !realFile.isFile()) {
      return Response.ok().build();
    } else {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try {
        output.write(new FileInputStream(realFile));
      } catch (Exception e) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
      return Response.ok().entity(output.toByteArray()).type(MimeTypes.SHORT_ARCHIVE_MIME_TYPE).build();
    }
  }
  
  private File getFile(AttachmentDetail attachment) {
    String filePath = FileRepositoryManager.getAbsolutePath(componentId)
    + FileRepositoryManager.getRelativePath(FileRepositoryManager.getAttachmentContext(
        attachment.getContext())) + File.separator + attachment.getPhysicalName();
    File realFile = new File(filePath);
    return realFile;
  }

}
