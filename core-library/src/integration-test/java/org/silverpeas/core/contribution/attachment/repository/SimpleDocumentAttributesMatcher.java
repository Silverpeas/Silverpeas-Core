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
package org.silverpeas.core.contribution.attachment.repository;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

/**
 *
 * @author ehugonnet
 */
public class SimpleDocumentAttributesMatcher extends BaseMatcher<SimpleDocument> {

  private SimpleDocument document;
  @Override
  public boolean matches(Object item) {
    boolean match = false;
    if (item instanceof SimpleDocument) {
      SimpleDocument actual = (SimpleDocument) item;
      match = equals(actual);
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(document);
  }

  /**
   * Creates a new matcher with the specified document.
   *
   * @param document the document to match.
   * @return a document matcher.
   */
  public static SimpleDocumentAttributesMatcher matches(final SimpleDocument document) {
    return new SimpleDocumentAttributesMatcher(document);
  }

  private SimpleDocumentAttributesMatcher(final SimpleDocument document) {
    this.document = document;
  }

  private boolean equals(SimpleDocument other) {
    if ((document.getForeignId() == null) ? (other.getForeignId() != null)
        : !document.getForeignId().equals(other.getForeignId())) {
      return false;
    }
    if (document.getOrder() != other.getOrder()) {
      return false;
    }
    if (document.isVersioned() != other.isVersioned()) {
      return false;
    }
    if ((document.getEditedBy() == null) ? (other.getEditedBy() != null) : !document.getEditedBy().
        equals(
        other.getEditedBy())) {
      return false;
    }
    if (document.getReservation() != other.getReservation() && (document.getReservation() == null
        || !document.getReservation().equals(other.getReservation()))) {
      return false;
    }
    if (document.getAlert() != other.getAlert() && (document.getAlert() == null || !document.
        getAlert().equals(
        other.getAlert()))) {
      return false;
    }
    if (document.getExpiry() != other.getExpiry() && (document.getExpiry() == null || !document.
        getExpiry().equals(other.getExpiry()))) {
      return false;
    }
    if (document.getAttachment() != other.getAttachment() && (document.getAttachment() == null || !document.getAttachment().
        equals(other.getAttachment()))) {
      return false;
    }
    return true;
  }
}
