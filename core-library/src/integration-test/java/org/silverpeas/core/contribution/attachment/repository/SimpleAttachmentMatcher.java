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

import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;

/**
 *
 * @author ehugonnet
 */
public class SimpleAttachmentMatcher extends BaseMatcher<SimpleAttachment> {

  private SimpleAttachment attachment;

  @Override
  public boolean matches(Object item) {
    boolean match = false;
    if (item instanceof SimpleAttachment) {
      SimpleAttachment actual = (SimpleAttachment) item;
      match = attachment.equals(actual);
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(attachment);
  }

  /**
   * Creates a new matcher with the specified attachment.
   * @param attachment the attachment to match.
   * @return a attachment matcher.
   */
  public static SimpleAttachmentMatcher matches(final SimpleAttachment attachment) {
    return new SimpleAttachmentMatcher(attachment);
  }

  private SimpleAttachmentMatcher(final SimpleAttachment attachment) {
    this.attachment = attachment;
  }
}
