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

package org.silverpeas.core.persistence;

import org.silverpeas.kernel.annotation.Nullable;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

/**
 * Property of an entity to belong to a resources in Silverpeas. The entity is related to an
 * existing resource.
 *
 * @author mmoquillon
 */
public interface ResourceBelonging {

  /**
   * Get the type of the related resource.
   *
   * @return the resource type as a String.
   */
  String getResourceType();

  /**
   * Gets the unique identifier of the resource.
   *
   * @return the resource identifier serialized into a String.
   */
  String getResourceId();

  /**
   * Gets a reference to the related resource.
   *
   * @param <E> the concrete type of the entity.
   * @param <R> the concrete type of the reference to the entity.
   * @param referenceClass the expected concrete class of the <code>EntityReference</code>. This
   * class must be conform to the type of the resource.
   * @return a reference to the resource related by this object or null if there is neither no
   * resource to which this object belongs (the object is an orphan) nor no reference defined for
   * the targeted type of resource.
   */
  @Nullable
  default <E, R extends EntityReference<E>> R getResource(Class<R> referenceClass) {
    R ref = null;
    String resourceType = getResourceType();
    String resourceId = getResourceId();
    if (resourceType != null && !resourceType.equals(EntityReference.UNKNOWN_TYPE) &&
        StringUtil.isDefined(resourceId)) {
      try {
        ref = referenceClass.getConstructor(String.class).newInstance(resourceId);
        if (!ref.getType().equals(resourceType)) {
          ref = null;
        }
      } catch (Exception ex) {
        SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      }
    }
    return ref;
  }
}
