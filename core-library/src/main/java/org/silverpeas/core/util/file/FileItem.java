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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;

/**
 * The item of a file uploaded to Silverpeas through an HTTP request.
 * <p>Its goal is to encapsulate the concrete implementation of a such concept in order to be
 * agnostic both by the library used to take in charge the file upload mechanism and by any change
 * in the API of that library.</p>
 *
 * @author mmoquillon
 */
public interface FileItem {
  /**
   * Is this file item is just a field in a multipart form?
   *
   * @return {@code true} if this instance represents a simple form field; {@code false} if it
   * represents an uploaded file.
   */
  boolean isFormField();

  /**
   * Gets the name of the field in the multipart form corresponding to this file item.
   *
   * @return The name of the form field.
   */
  String getFieldName();

  /**
   * Gets the contents of this file item as a String, using the specified charset.
   *
   * @param charset The charset to use.
   * @return The contents of the file, as a string.
   * @throws UncheckedIOException if an I/O error occurs.
   */
  String getContent(Charset charset);

  /**
   * Gets the contents of this file item as a String, using the default character encoding.
   *
   * @return The contents of the file, as a string.
   * @throws UncheckedIOException if an I/O error occurs
   */
  String getContent();

  /**
   * Gets the original file name in the client's file system.
   *
   * @return The original file name in the client's file system.
   * @throws InvalidPathException The file name contains a NUL character, which might be an
   * indicator of a security attack. If you intend to use the file name anyway, catch the exception
   * and use {@link InvalidPathException#getInput()}.
   */
  String getFileName();

  /**
   * Gets an {@link java.io.InputStream InputStream} that can be used to retrieve the contents of
   * the file.
   *
   * @return An {@link java.io.InputStream InputStream} that can be used to retrieve the contents of
   * the file.
   * @throws IOException if an error occurs.
   */
  InputStream getInputStream() throws IOException;

  /**
   * Gets the size of the file.
   *
   * @return The size of the file, in bytes.
   */
  long getSize();

  /**
   * Gets the content type passed by the agent or {@code null} if not defined.
   *
   * @return The content type passed by the agent or {@code null} if not defined.
   */
  String getContentType();

  /**
   * Saves the content to the specified file.
   *
   * @param file the file into which the content will be saved.
   * @throws IOException if the saving fails.
   */
  void saveTo(File file) throws IOException;

  /**
   * Deletes the underlying storage for a file item, including deleting any associated temporary
   * disk file.
   */
  void delete() throws IOException;
}
