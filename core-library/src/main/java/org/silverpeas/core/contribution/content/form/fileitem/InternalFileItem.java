/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.contribution.content.form.fileitem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

/**
 * File item created manually, without being retrieved from an HTTP request. Used to update an
 * imported publication's form.
 * @author Antoine HEDIN
 */
public class InternalFileItem implements FileItem {

  private static final long serialVersionUID = 754043464419634444L;
  private String fieldName;
  private String value;

  public InternalFileItem(String fieldName, String value) {
    setFieldName(fieldName);
    setValue(value);
  }

  @Override
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String getString() {
    return value;
  }

  @Override
  public void setFormField(boolean formField) {

  }

  @Override
  public boolean isFormField() {
    return true;
  }

  @Override
  public void delete() {
  }

  @Override
  public byte[] get() {
    return null;
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return null;
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Override
  public String getString(String arg0) throws UnsupportedEncodingException {
    return getString();
  }

  @Override
  public boolean isInMemory() {
    return false;
  }

  @Override
  public void write(File arg0) throws Exception {
  }

  @Override
  public FileItemHeaders getHeaders() {
    return null;
  }

  @Override
  public void setHeaders(final FileItemHeaders fileItemHeaders) {

  }
}