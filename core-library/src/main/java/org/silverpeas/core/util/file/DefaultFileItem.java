/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util.file;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

/**
 * Default implementation of the {@link FileItem} interface whose goal is to wrap the
 * {@link DiskFileItem} object that represents concretely the file part that is uploaded through
 * HTTP.
 *
 * @author mmoquillon
 */
public class DefaultFileItem implements FileItem {

  private final DiskFileItem file;

  DefaultFileItem(DiskFileItem fileItem) {
    this.file = fileItem;
  }

  @Override
  public boolean isFormField() {
    return file.isFormField();
  }

  @Override
  public String getFieldName() {
    return file.getFieldName();
  }

  @Override
  public String getContent(Charset charset) {
    try {
      return file.getString(charset);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public String getContent() {
    try {
      return file.getString();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public String getFileName() {
    return file.getName();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return file.getInputStream();
  }

  @Override
  public long getSize() {
    return file.getSize();
  }

  @Override
  public String getContentType() {
    return file.getContentType();
  }

  @Override
  public void saveTo(File file) throws IOException {
    FileUtils.copyInputStreamToFile(getInputStream(), file);
  }

  @Override
  public void delete() throws IOException {
    file.delete();
  }
}
  