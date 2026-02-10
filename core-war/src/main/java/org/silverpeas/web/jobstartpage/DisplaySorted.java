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
package org.silverpeas.web.jobstartpage;

import org.silverpeas.kernel.annotation.NonNull;

import java.util.Comparator;
import java.util.Objects;

/**
 * A node in a tree to be rendered in the GUI. It represents an organizational resource in
 * Silverpeas like a space or a component instance.
 */
public class DisplaySorted implements Comparable<DisplaySorted> {
  public static final int TYPE_UNKNOWN = 0;
  public static final int TYPE_COMPONENT = 1;
  public static final int TYPE_SPACE = 2;
  public static final int TYPE_SUBSPACE = 3;
  private static final Comparator<DisplaySorted> COMPARATOR = Comparator.comparing(
      DisplaySorted::getOrderNum).thenComparing(DisplaySorted::getId);

  private String name = "";
  private int orderNum = 0;
  private String id = "";
  private String parentId = "";
  private String typeName = "";
  private int type = TYPE_UNKNOWN;
  private int deep = 0;
  private boolean admin = true;
  private boolean visible = true;

  @Override
  public int compareTo(@NonNull DisplaySorted other) {
    return COMPARATOR.compare(this, other);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DisplaySorted that = (DisplaySorted) o;
    return orderNum == that.orderNum && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderNum, id);
  }

  public void copy(DisplaySorted src) {
    name = src.name;
    orderNum = src.orderNum;
    id = src.id;
    parentId = src.parentId;
    typeName = src.typeName;
    type = src.type;
    deep = src.deep;
    admin = src.admin;
    visible = src.visible;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public int getOrderNum() {
    return orderNum;
  }

  public void setOrderNum(final int orderNum) {
    this.orderNum = orderNum;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(final String parentId) {
    this.parentId = parentId;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(final String typeName) {
    this.typeName = typeName;
  }

  public int getType() {
    return type;
  }

  public void setType(final int type) {
    this.type = type;
  }

  public int getDeep() {
    return deep;
  }

  public void setDeep(final int deep) {
    this.deep = deep;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(final boolean admin) {
    this.admin = admin;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(final boolean visible) {
    this.visible = visible;
  }
}
