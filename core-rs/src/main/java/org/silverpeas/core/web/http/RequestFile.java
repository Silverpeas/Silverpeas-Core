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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * A file embedded into the body of an HTTP request.
 * @author Yohann Chastagnier
 */
public class RequestFile {
  private final FileItem fileItem;

  public RequestFile(final FileItem fileItem) {
    this.fileItem = fileItem;
  }

  public String getName() {
    return StringUtil.normalize(FilenameUtils.getName(fileItem.getName()));
  }

  public long getSize() {
    return fileItem.getSize();
  }

  public String getContentType() {
    return fileItem.getContentType();
  }

  public InputStream getInputStream() {
    try {
      return new BufferedInputStream(fileItem.getInputStream());
    } catch (IOException ioe) {
      return null;
    }
  }

  public String getString() {
    try {
      return fileItem.getString(Charsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      SilverLogger.getLogger(this).error(e);
      return fileItem.getString();
    }
  }

  public void writeTo(File file) throws Exception {
    fileItem.write(file);
  }

  public void writeTo(OutputStream outputStream) throws IOException {
    try(InputStream input = getInputStream()) {
      IOUtils.copy(input, outputStream);
    }
  }
}
