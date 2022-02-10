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
package org.silverpeas.core.util;

import java.util.Objects;

/**
 * Link to a given web resource. The resource is defined by the URL at which it can be accessed
 * through the Web.
 */
public class Link {

  /**
   * An empty link is a link whose the URL and label is an empty string.
   */
  public static final Link EMPTY_LINK = new Link("", "");

  private final String linkUrl;
  private final String linkLabel;

  /**
   * Constructs a new link with the specified URL and labels.
   * @param linkUrl the URL of the linked resource.
   * @param linkLabel the label to render for that link.
   */
  public Link(final String linkUrl, final String linkLabel) {
    Objects.requireNonNull(linkUrl);
    Objects.requireNonNull(linkLabel);
    this.linkUrl = linkUrl;
    this.linkLabel = linkLabel;
  }

  /**
   * Gets the URL of the resource referred by this link.
   * @return the resource URL.
   */
  public String getLinkUrl() {
    return linkUrl;
  }

  /**
   * Gets the label with which this link should be referred.
   * @return this link's label.
   */
  public String getLinkLabel() {
    return linkLabel;
  }

}
