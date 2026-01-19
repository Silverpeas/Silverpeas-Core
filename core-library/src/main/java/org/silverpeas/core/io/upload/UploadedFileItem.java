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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.Charsets;
import org.silverpeas.core.util.file.FileItem;
import org.silverpeas.core.util.file.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static org.silverpeas.kernel.util.StringUtil.EMPTY;

/**
 * Converts an {@link UploadedFile} into a usable {@link FileItem} instance.
 * @author silveryocha
 */
public class UploadedFileItem implements FileItem {

  private final UploadedFile uploadedFile;

  UploadedFileItem(final UploadedFile uploadedFile) {
    this.uploadedFile = uploadedFile;
  }

  @Override
  public boolean isFormField() {
    return false;
  }

  @Override
  public String getFieldName() {
    return EMPTY;
  }

  @Override
  public String getContent(Charset charset) {
    return new String(read(), charset);
  }

  @Override
  public String getContent() {
    return new String(read(), Charsets.UTF_8);
  }

  @Override
  public String getFileName() {
    return uploadedFile.getFile().getName();
  }

  @Override
  public long getSize() {
    return uploadedFile.getFile().length();
  }

  @Override
  public String getContentType() {
    return FileUtil.getMimeType(getFileName());
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(uploadedFile.getFile());
  }

  @Override
  public void saveTo(File file) throws IOException {
    FileUtils.copyInputStreamToFile(getInputStream(), file);
  }

  @Override
  public void delete() throws IOException {
    if (uploadedFile.getFile().exists()) {
      Files.delete(uploadedFile.getFile().toPath());
    }
  }

  private byte[] read() {
    byte[] fileData = new byte[(int) getSize()];
    try (InputStream fis = new FileInputStream(uploadedFile.getFile())) {
      IOUtils.readFully(fis, fileData);
    } catch (IOException e) {
      fileData = null;
    }
    return fileData;
  }
}
