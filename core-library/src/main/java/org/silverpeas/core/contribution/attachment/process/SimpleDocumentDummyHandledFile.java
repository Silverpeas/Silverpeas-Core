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
package org.silverpeas.core.contribution.attachment.process;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.process.io.file.AbstractDummyHandledFile;

import java.util.EnumSet;

/**
 * User: Yohann Chastagnier
 * Date: 17/10/13
 */
public class SimpleDocumentDummyHandledFile extends AbstractDummyHandledFile {
  private static final EnumSet<DocumentType> technicalDocumentTypes =
      EnumSet.of(DocumentType.wysiwyg);

  private final SimpleDocument document;
  private final boolean deleted;
  private ResourceReference target = null;

  public SimpleDocumentDummyHandledFile(final SimpleDocument document) {
    this(document, false);
  }

  public SimpleDocumentDummyHandledFile(final SimpleDocument document,
      final ResourceReference target) {
    this(document);
    this.target = target;
  }

  public SimpleDocumentDummyHandledFile(final SimpleDocument document, final boolean deleted) {
    this.document = document;
    this.deleted = deleted;
  }

  @Override
  public String getComponentInstanceId() {
    if (target != null) {
      return target.getInstanceId();
    }
    return document.getInstanceId();
  }

  @Override
  public String getPath() {
    return document.getAttachmentPath();
  }

  @Override
  public String getName() {
    return document.getFilename();
  }

  @Override
  public long getSize() {
    return document.getSize();
  }

  @Override
  public String getMimeType() {
    if (technicalDocumentTypes.contains(document.getDocumentType())) {
      return document.getDocumentType().getName();
    }
    return document.getAttachment().getContentType();
  }

  @Override
  public boolean isDeleted() {
    return deleted;
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
