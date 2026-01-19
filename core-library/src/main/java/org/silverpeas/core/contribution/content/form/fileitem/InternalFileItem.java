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
package org.silverpeas.core.contribution.content.form.fileitem;

import org.silverpeas.core.util.file.FileItem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * File item created manually, without being retrieved from an HTTP request. Used to update an
 * imported publication's form.
 * @author Antoine HEDIN
 */
public class InternalFileItem implements FileItem {

  private final String fieldName;
  private final String value;

  public InternalFileItem(String fieldName, String value) {
    this.fieldName = fieldName;
    this.value = value;
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }


  @Override
  public String getContent() {
    return value;
  }

  @Override
  public boolean isFormField() {
    return true;
  }

  @Override
  public String getContent(Charset charset) {
    return value;
  }

  @Override
  public String getFileName() {
    return null;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return null;
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public void saveTo(File file) {
    // does nothing. There is no content
  }

  @Override
  public void delete() {
    // does nothing. There is no content
  }
}