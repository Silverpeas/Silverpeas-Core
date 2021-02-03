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

package org.silverpeas.core.io.media.image.thumbnail.model;

import org.silverpeas.core.ResourceReference;

import java.util.Objects;

/**
 * @author silveryocha
 */
public class ThumbnailReference extends ResourceReference {
  private static final long serialVersionUID = 6100154146002255574L;

  private int objectType;

  public ThumbnailReference(final String id, final String componentInstanceId, final int objectType) {
    super(id, componentInstanceId);
    this.objectType = objectType;
  }

  public ThumbnailReference(final int id, final String componentInstanceId, final int objectType) {
    this(String.valueOf(id), componentInstanceId, objectType);
  }

  public int getObjectId() {
    return Integer.parseInt(this.getLocalId());
  }

  public void setObjectId(int objectId) {
    setId(String.valueOf(objectId));
  }

  public int getObjectType() {
    return objectType;
  }

  public void setObjectType(final int objectType) {
    this.objectType = objectType;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final ThumbnailReference that = (ThumbnailReference) o;
    return objectType == that.objectType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), objectType);
  }
}
