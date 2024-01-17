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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.selection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The context of a selection by a user of a resource in Silverpeas. It explains in what aim a
 * resource has been put into the {@link SelectionBasket}, in other words for what operation the
 * selection has been done. As such it can contain additional information required by the operation
 * to be performed against the selected resource. By specifying a context to a selection, the
 * consumer of the selected resources in the basket can then either filter the resources on which it
 * has to operate or apply a different behaviour according to their selection context.
 * @author mmoquillon
 */
@XmlRootElement
public class SelectionContext implements Serializable {

  @XmlElement(required = true)
  private Reason reason = Reason.TRANSFER;

  @XmlElement
  private final Map<String, String> attributes = new HashMap<>();

  protected SelectionContext() {

  }

  public SelectionContext(final Reason reason) {
    this.reason = reason;
  }

  /**
   * Defines a context attribute. Such attributes qualifies the context of a selection.
   * @param attrName the name of the attribute.
   * @param attrValue the value of the attribute.
   */
  public void putAttribute(final String attrName, final String attrValue) {
    attributes.put(attrName, attrValue);
  }

  /**
   * Gets the value of the specified attribute. If no such attribute is defined for this context,
   * then null is returned.
   * @param attrName the name of the attribute.
   * @return the value of the attribute or null if no such attribute exists.
   */
  public String getAttribute(final String attrName) {
    return attributes.get(attrName);
  }

  /**
   * Gets the reason of a why a resource has been put into the selection basket.
   * @return the reason.
   */
  public Reason getReason() {
    return reason;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SelectionContext that = (SelectionContext) o;
    return reason == that.reason && attributes.equals(that.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reason, attributes);
  }

  /**
   * Reason about the selection of a resource by a user.
   */
  public enum Reason {
    /**
     * Transfer information about the resource to perform some tasks. Default reason.
     */
    TRANSFER,
    /**
     * Copy the resource in another location in Silverpeas.
     */
    COPY,
    /**
     * Move the resource in another location in Silverpeas.
     */
    MOVE
  }

}
