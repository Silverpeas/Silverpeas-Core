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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.attachment.model;

import java.time.OffsetDateTime;

/**
 * This class represents the description of a remotely content.
 * @author silveryocha
 */
public class RemoteContentDescriptor {
  private final SimpleDocument document;
  private String id;
  private String language;
  private long size;
  private OffsetDateTime lastModificationDate;
  private boolean officeEditorLock;

  protected RemoteContentDescriptor(final SimpleDocument document) {
    this.document = document;
  }

  /**
   * Gets the document the remote content descriptor is linked to.
   * @return a {@link SimpleDocument} instance.
   */
  public SimpleDocument getDocument() {
    return document;
  }

  /**
   * Gets the identifier of remote content.
   * @return a string representing an unique identifier.
   */
  public String getId() {
    return id;
  }

  protected void setId(final String id) {
    this.id = id;
  }

  /**
   * Gets the language of the remote content.
   * @return a string representing the language.
   */
  public String getLanguage() {
    return language;
  }

  protected void setLanguage(final String language) {
    this.language = language;
  }

  /**
   * Gets the size of the remote content.
   * @return a long representing a size in bytes.
   */
  public long getSize() {
    return size;
  }

  protected void setSize(final long size) {
    this.size = size;
  }

  /**
   * Gets the last modification date of the remote content.
   * @return an {@link OffsetDateTime} representing a date.
   */
  public OffsetDateTime getLastModificationDate() {
    return lastModificationDate;
  }

  protected void setLastModificationDate(final OffsetDateTime lastModificationDate) {
    this.lastModificationDate = lastModificationDate;
  }

  /**
   * Indicates if an document office editor is locking the file.
   * @return true if locking file, false otherwise.
   */
  public boolean isOfficeEditorLock() {
    return officeEditorLock;
  }

  protected void setOfficeEditorLock(final boolean officeEditorLock) {
    this.officeEditorLock = officeEditorLock;
  }
}
