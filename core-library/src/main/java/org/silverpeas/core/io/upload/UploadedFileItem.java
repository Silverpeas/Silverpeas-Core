/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.util.file.FileUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.silverpeas.core.util.StringUtil.EMPTY;

/**
 * Converts an {@link UploadedFile} into a usable {@link FileItem} instance.
 * @author silveryocha
 */
public class UploadedFileItem extends DiskFileItem {

  private UploadedFile uploadedFile;

  UploadedFileItem(final UploadedFile uploadedFile) {
    super(EMPTY, EMPTY, false, EMPTY, 0, null);
    this.uploadedFile = uploadedFile;
  }

  @Override
  public String getName() {
    return uploadedFile.getFile().getName();
  }

  @Override
  public long getSize() {
    return uploadedFile.getFile().length();
  }

  @Override
  public String getContentType() {
    return FileUtil.getMimeType(getName());
  }

  @Override
  public byte[] get() {
    byte[] fileData = new byte[(int) getSize()];
    try (InputStream fis = new FileInputStream(uploadedFile.getFile())) {
      IOUtils.readFully(fis, fileData);
    } catch (IOException e) {
      fileData = null;
    }
    return fileData;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(uploadedFile.getFile());
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return new FileOutputStream(uploadedFile.getFile());
  }
}
