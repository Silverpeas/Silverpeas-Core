/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.contribution.model;

import org.silverpeas.core.IdentifiableResource;

/**
 * A contribution in Silverpeas. A contribution is an identifiable resource that is pushed by a
 * user onto Silverpeas and that is manageable by the users within Silverpeas.
 * @author mmoquillon
 */
public interface Contribution extends IdentifiableResource {

  /**
   * Gets the unique identifier of this contribution.
   * @return the unique identifier of the contribution.
   */
  public ContributionIdentifier getId();

  /**
   * Gets the content of this contribution. A contribution can support several type of contents.
   * @param <T> the concrete or generic type of the content.
   * @return the content of this contribution or null if this contribution has not yet a content or
   * it doesn't support any content.
   */
  public <T extends ContributionContent> T getContent();

  /**
   * Does this contribution have a content?
   * @return true of this contribution has a content. False if this contribution has not yet a
   * content or doesn't support any content.
   */
  public default boolean hasContent() {
    return getContent() != null;
  }
}
