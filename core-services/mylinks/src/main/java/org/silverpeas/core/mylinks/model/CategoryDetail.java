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
package org.silverpeas.core.mylinks.model;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.Securable;

import java.io.Serializable;
import java.util.Objects;

public class CategoryDetail implements Serializable, Securable {

  private static final long serialVersionUID = -4206112805280800081L;
  private int id;
  private int position;
  private boolean hasPosition;
  private String name;
  private String description;
  private String userId;

  public CategoryDetail() {
  }

  public CategoryDetail(final CategoryDetail other) {
    this.id = other.id;
    this.position = other.position;
    this.hasPosition = other.hasPosition;
    this.name = other.name;
    this.description = other.description;
    this.userId = other.userId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public boolean hasPosition() {
    return hasPosition;
  }

  public void setHasPosition(boolean hasPosition) {
    this.hasPosition = hasPosition;
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return user.getId().equals(userId);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final CategoryDetail that = (CategoryDetail) o;
    return id == that.id && position == that.position && name.equals(that.name) &&
        Objects.equals(description, that.description) && userId.equals(that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, position, name, description, userId);
  }
}