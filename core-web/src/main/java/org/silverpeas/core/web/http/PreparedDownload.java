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

package org.silverpeas.core.web.http;

import org.silverpeas.kernel.cache.model.SimpleCache;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.silverpeas.core.cache.service.CacheAccessorProvider.getSessionCacheAccessor;
import static org.silverpeas.core.util.JSONCodec.encodeObject;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;

/**
 * Prepared download means a download in two time.
 * <p>
 * In a first time, from an AJAX request, a download is prepared and saved into a temporary file.
 * <br/>A JSON response is returned containing an object with the attribute
 * <code>preparedDownloadUrl</code>.
 * </p>
 * <p>
 * In a second time, the prepared file is downloaded from <code>preparedDownloadUrl</code> and
 * deleted from temporary files.
 * </p>
 * <p>
 * The main objective of that is to get more control on UI in order to manage user navigation on
 * file download.
 * </p>
 * @author silveryocha
 */
public class PreparedDownload implements Serializable {
  private static final long serialVersionUID = 4282855362562277729L;

  private static final String PREPARED_DOWNLOAD = "preparedDownload";
  private static final String PREPARED_DOWNLOAD_URL = "preparedDownloadUrl";

  private final String id;
  private final String fileName;
  private final String contentType;
  private String characterEncoding = Charsets.UTF_8.name();

  public PreparedDownload(final String fileName, final String contentType) {
    this.id = UUID.randomUUID().toString();
    this.fileName = fileName;
    this.contentType = contentType;
  }

  /**
   * Gets a new {@link PreparedDownload} instance if {@link #PREPARED_DOWNLOAD} is detected into
   * request parameters.
   * @param request the request.
   * @param fileName the filename which will be downloaded.
   * @param contentType the content type of the content which will be downloaded.
   * @return an optional {@link PreparedDownload} instance.
   */
  public static Optional<PreparedDownload> getPreparedDownloadToPerform(
      final HttpServletRequest request, final String fileName, final String contentType) {
    if (StringUtil.getBooleanValue(request.getParameter(PREPARED_DOWNLOAD))) {
      return Optional.of(new PreparedDownload(fileName, contentType));
    }
    return Optional.empty();
  }

  /**
   * Gets from the given request the {@link PreparedDownload} instance registered into the
   * session if any.
   * @param request the request.
   * @throw IllegalArgumentException if parameter id is not defined into request or if it does
   * not exists {@link PreparedDownload} instance referenced by the identifier.
   * @return a {@link PreparedDownload} instance.
   */
  public static PreparedDownload getFrom(final HttpServletRequest request) {
    final String preparedDownloadId = request.getParameter("id");
    if (StringUtil.isNotDefined(preparedDownloadId)) {
      throw new IllegalArgumentException("identifier of prepared download MUST exists");
    }
    final SimpleCache cache = getSessionCacheAccessor().getCache();
    final PreparedDownload preparedDownload = cache.remove(PREPARED_DOWNLOAD + preparedDownloadId, PreparedDownload.class);
    if (preparedDownload == null) {
      throw new IllegalArgumentException(
          "prepared download " + preparedDownloadId + " does not exist anymore");
    }
    return preparedDownload;
  }

  /**
   * Gets the writer into which the content MUST be written.
   * @return a {@link Writer} instance.
   * @param charset the character encoding to set to the writer.
   * @throws IOException on technical error.
   */
  public Writer getWriter(final String charset) throws IOException {
    this.characterEncoding = charset;
    if (StringUtil.isNotDefined(fileName)) {
      throw new IllegalArgumentException("no filename has been specified to prepare the download");
    }
    final File file = getFile();
    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
  }

  /**
   * Sends the identifier into the response and put into sessions cache this
   * {@link PreparedDownload} instance.
   * @param response the response.
   * @throws IOException on technical error.
   */
  public void sendDetails(final HttpServletResponse response) throws IOException {
    final String jsonToSend = encodeObject(
        o -> o.put(PREPARED_DOWNLOAD_URL, getApplicationURL() + "/PreparedDownload?id=" + id));
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", -1);
    response.setContentType(MediaType.APPLICATION_JSON);
    response.setCharacterEncoding(Charsets.UTF_8.name());
    response.getWriter().append(jsonToSend);
    final SimpleCache cache = getSessionCacheAccessor().getCache();
    cache.put(PREPARED_DOWNLOAD + id, this);
  }

  /**
   * Sends the prepared file into response with a download context.
   * <p>
   * After a successful or an unsuccessful send, the file is deleted.
   * </p>
   * @param request a request.
   * @param response a response.
   */
  public void sendTo(final HttpServletRequest request, final HttpServletResponse response) {
    final File file = getFile();
    try {
      FileResponse.fromServlet(request, response)
                  .forceMimeType(contentType)
                  .forceCharacterEncoding(characterEncoding)
                  .forceFileName(fileName)
                  .noCache()
                  .sendPath(Paths.get(file.toURI()), true);
    } finally {
      deleteQuietly(file);
    }
  }

  private File getFile() {
    return new File(FileRepositoryManager.getTemporaryPath(), id);
  }
}
