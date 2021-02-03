/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.encryption;

import org.silverpeas.core.security.encryption.cipher.CryptoException;

import java.util.Iterator;
import java.util.Map;

/**
 * An iterator over a collection of contents for encryption purpose. The content is here represented
 * by a {@link Map} object in which each entry represents a field or a property of a content.
 * <p>
 * Any provider of contents to be encrypted or decrypted must provide an implementation of this
 * interface. It has been defined to be used in conjunction with {@link ContentEncryptionService}.
 * The provider can implement this interface by wrapping the retrieve of the contents to be
 * encrypted or to be decrypted and to put them at disposition of the caller.
 * <p>
 * It extends the {@link Iterator} interface by adding the following methods:
 * <ul>
 * <li>{@link #update(java.util.Map)} to update the last content returned by the iterator,</li>
 * <li>{@link #onError(java.util.Map, CryptoException)} to inform the
 * implementation of this interface of an error while processing the last content returned by it, so
 * that it can perform specific treatments with any retrieve mechanism (like closing correctly a
 * connection for example).</li>
 * </ul>
 */
public interface EncryptionContentIterator extends Iterator<Map<String, String>> {


  /**
   * Initialize contents to iterate. It prepares iterator for the encryption operations.
   * It will be invoked by content encryption service before any iteration.
   */
  void init();

  /**
   * Gets the next content in the iteration.
   *
   * @return the next content to update in the form of a {@link Map} object in which each entry
   * represents a field or a property of the content.
   */
  @Override
  Map<String, String> next();

  /**
   * Is there another content in this iteration?
   * <p>
   * This method is called by the encryption service each time before asking the next content to
   * encrypt or to decrypt with the {@link #next()} method.
   *
   * @return true if there is another content in the iteration. False otherwise.
   */
  @Override
  boolean hasNext();

  /**
   * Updates the last content returned by this iterator.
   *
   * @param updatedContent the content updated by the encryption service in the form of a
   * {@link Map} object in which each entry represents a field or a property of the content.
   */
  void update(Map<String, String> updatedContent);

  /**
   * An error occurred while processing the last content returned by this iterator.
   * <p>
   * This method is invoked by the encryption service when an error occurs while processing the last
   * content returned by the iterator.
   *
   * @param content the content with which the error occurred.
   * @param ex the exception raised at the error.
   */
  void onError(Map<String, String> content, CryptoException ex);
}
