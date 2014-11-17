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
package org.silverpeas.upload.web;

import com.silverpeas.annotation.Authenticated;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.web.RESTWebService;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.json.JSONObject;
import org.silverpeas.servlet.RequestParameterDecoder;
import org.silverpeas.util.Charsets;
import org.silverpeas.util.FileRepositoryManager;
import org.silverpeas.util.JSONCodec;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.UnitUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.silverpeas.web.util.IFrameAjaxTransportUtil.*;

/**
 * A REST Web resource that permits to upload files. It has to be used with silverpeas-filUpload.js
 * jQuery plugin on client side.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path("fileUpload")
@Authenticated
public class FileUploadResource extends RESTWebService {

  private final static String X_FILENAME = "X-FILENAME";

  /**
   * Permits to upload files from multipart http request.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response in relation with jQuery plugins used on the client side: a html textarea
   *         tag that contains a JSON array structure. Each line of this array contains
   *         informations
   *         of an uploaded file :<br/>
   *         - <b>fileId</b> : the uploaded file identifier<br/>
   *         - <b>name</b> : the name of the uploaded file (without its path)<br/>
   *         - <b>size</b> : the byte size of the uploaded file<br/>
   *         - <b>formattedSize</b> : the formatted file size according to the language of
   *         user<br/>
   *         - <b>iconUrl</b> : the url of the icon that represents the type of the uploaded
   *         file<br/>
   */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Response uploadFiles() {
    try {
      String uploadId = UUID.randomUUID().toString();
      UploadedRequestFile uploadedRequestFile =
          RequestParameterDecoder.decode(getHttpRequest(), UploadedRequestFile.class);
      Function<JSONCodec.JSONObject, JSONCodec.JSONObject> builder =
          uploadFile(uploadId, uploadedRequestFile.getRequestFile().getName(),
              uploadedRequestFile.getRequestFile().getInputStream());
      String jsonFiles = packJSonArrayWithHtmlContainer(a -> a.addJSONObject(builder));
      return Response.ok().entity(jsonFiles).build();
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Permits to upload one file from http request.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response in relation with jQuery plugins used on the client side: a html textarea
   *         tag that contains a JSON array structure. Each line of this array contains
   *         informations
   *         of an uploaded file :<br/>
   *         - <b>fileId</b> : the uploaded file identifier<br/>
   *         - <b>name</b> : the name of the uploaded file (without its path)<br/>
   *         - <b>size</b> : the byte size of the uploaded file<br/>
   *         - <b>formattedSize</b> : the formatted file size according to the language of
   *         user<br/>
   *         - <b>iconUrl</b> : the url of the icon that represents the type of the uploaded
   *         file<br/>
   */
  @POST
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @Produces(MediaType.TEXT_HTML)
  public Response uploadFile(InputStream inputStream) {
    try {
      String brutFileName = getHttpServletRequest().getHeader(X_FILENAME);
      if (!StringUtil.isDefined(brutFileName)) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }
      String fileId = UUID.randomUUID().toString();
      String fileName = URLDecoder.decode(brutFileName, Charsets.UTF_8.name());
      String jsonFile = packJSonObjectWithHtmlContainer(uploadFile(fileId, fileName, inputStream));
      return Response.ok().entity(jsonFile).build();
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Handles the upload of one file.
   * @param fileId
   * @param fileName
   * @param inputStream
   * @return a builder of the JSON representation of the uploaded file (more information on
   * {@link FileUploadResource#uploadFiles()})
   * @throws IOException
   */
  private Function<JSONCodec.JSONObject, JSONCodec.JSONObject> uploadFile(String fileId,
      String fileName, InputStream inputStream)
      throws IOException {

    // Destination of the file that going to be upload
    File uploadedFileLocation = getDestinationFileName(fileId, fileName);

    // Upload
    saveToFile(inputStream, uploadedFileLocation);

    // JSON response
    return asJSON(fileId, uploadedFileLocation);
  }

  /**
   * Builds a JSON representation of the given uploaded file.
   * @param fileId
   * @param file
   * @return a builder of the JSON representation of the uploaded file (more information on
   * {@link FileUploadResource#uploadFiles()})
   */
  private Function<JSONCodec.JSONObject, JSONCodec.JSONObject> asJSON(String fileId, File file) {
    return (o -> o.put("fileId", fileId)
        .put("name", file.getName().substring(fileId.length() + 1))
        .put("size", file.length())
        .put("formattedSize", UnitUtil.formatMemSize(new BigDecimal(String.valueOf(file.length()))))
        .put("iconUrl",
            FileRepositoryManager.getFileIcon(FilenameUtils.getExtension(file.getName()))));
  }

  /**
   * Saving the file.
   * @param uploadedInputStream
   * @param uploadedFileLocation
   * @throws IOException
   */
  private void saveToFile(InputStream uploadedInputStream, File uploadedFileLocation)
      throws IOException {
    try {
      FileOutputStream fOS = FileUtils.openOutputStream(uploadedFileLocation);
      try {
        IOUtils.copy(uploadedInputStream, fOS);
      } finally {
        IOUtils.closeQuietly(fOS);
      }
    } finally {
      IOUtils.closeQuietly(uploadedInputStream);
    }
  }

  @DELETE
  @Path("{fileId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@PathParam("fileId") String fileId) {
    try {
      File tempDir = new File(FileRepositoryManager.getTemporaryPath());
      Collection<File> files =
          FileUtils.listFiles(tempDir, new PrefixFileFilter(fileId), FalseFileFilter.FALSE);
      if (!files.isEmpty()) {
        for (File file : files) {
          FileUtils.deleteQuietly(file);
        }
      } else {
        SilverTrace.error("upload", "FileUploadResource.delete()", "",
            "Trying to delete unexisting file with id: " + fileId);
      }
      return Response.ok().build();
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Get file name.
   * @return
   */
  private File getDestinationFileName(String fileId, String fileName) {
    return new File(getBaseFileName(fileId) + "-" + fileName);
  }

  /**
   * Get file name base.
   * @return
   */
  private File getBaseFileName(String fileId) {
    return new File(FileRepositoryManager.getTemporaryPath(), fileId);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.RESTWebService#getComponentId()
   */
  @Override
  public String getComponentId() {
    return "";
  }
}