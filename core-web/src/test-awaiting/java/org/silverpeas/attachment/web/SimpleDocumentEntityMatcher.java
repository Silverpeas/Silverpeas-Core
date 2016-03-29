/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.contribution.attachment.web;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

/**
 *
 * @author ehugonnet
 */
public class SimpleDocumentEntityMatcher extends BaseMatcher<SimpleDocumentEntity> {

  private SimpleDocument document;

  /**
   * Creates a new matcher with the specified SimpleDocument.
   *
   * @param document1 the SimpleDocument to match.
   * @return a SimpleDocumentEntity matcher.
   */
  public static SimpleDocumentEntityMatcher matches(final SimpleDocument document1) {
    return new SimpleDocumentEntityMatcher(document1);
  }

  @Override
  public boolean matches(Object item) {
    boolean match = false;
    if (item instanceof SimpleDocumentEntity) {
      SimpleDocumentEntity actual = (SimpleDocumentEntity) item;
      match = check(actual);
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(document);
  }

  private SimpleDocumentEntityMatcher(final SimpleDocument document) {
    this.document = document;
  }

  private boolean check(SimpleDocumentEntity actual) {
    if ((document.getId() == null) ? (actual.getId() != null) : !document.getId().equals(actual.
        getId())) {
      return false;
    }
    if ((document.getInstanceId() == null) ? (actual.getInstanceId() != null)
        : !document.getInstanceId().equals(actual.getInstanceId())) {
      return false;
    }
    if ((document.getFilename() == null) ? (actual.getFileName() != null) : !document.getFilename().
        equals(actual.getFileName())) {
      return false;
    }
    if ((document.getDescription() == null) ? (actual.getDescription() != null)
        : !document.getDescription().equals(actual.getDescription())) {
      return false;
    }
    if ((document.getContentType() == null) ? (actual.getContentType() != null)
        : !document.getContentType().equals(actual.getContentType())) {
      return false;
    }
    if (document.getCreated().getTime() != actual.getCreationDate()) {
      return false;
    }
    if ((document.getCreatedBy() == null) ? (actual.getCreatedBy() != null)
        : !document.getCreatedBy().equals(actual.getCreatedBy())) {
      return false;
    }
    if (document.getUpdated() != null && document.getUpdated().getTime() != actual.getUpdateDate()) {
      return false;
    }
    if ((document.getUpdatedBy() == null) ? (actual.getUpdatedBy() != null)
        : !document.getUpdatedBy().equals(actual.getUpdatedBy())) {
      return false;
    }
    if (document.getSize() != actual.getSize()) {
      return false;
    }
    if ((document.getTitle() == null) ? (actual.getTitle() != null) : !document.getTitle().
        equals(actual.getTitle())) {
      return false;
    }
    if ((document.getLanguage() == null) ? (actual.getLang() != null) : !document.getLanguage().
        equals(actual.getLang())) {
      return false;
    }
    if ((document.getDisplayIcon() == null) ? (actual.getIcon() != null) : !document.
        getDisplayIcon().equals(actual.getIcon())) {
      return false;
    }
    if ((document.getAttachmentURL() == null) ? (actual.getDownloadUrl() != null)
        : !document.getAttachmentURL().equals(actual.getDownloadUrl())) {
      return false;
    }
    return true;
  }
}
