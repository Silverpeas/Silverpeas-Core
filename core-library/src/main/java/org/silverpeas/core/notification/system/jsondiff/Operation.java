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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.system.jsondiff;

import java.io.Serializable;

/**
 * An operation on a property.
 *
 * @author ehugonnet
 */
public class Operation implements Serializable {

  private static final long serialVersionUID = 1L;

  private Op op;
  private String path;
  private String value;

  public Operation() {
  }

  public Operation(Op type, String path, String value) {
    this.op = type;
    this.path = path;
    this.value = value;
  }

  public void setOp(Op type) {
    this.op = type;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Op getOp() {
    return op;
  }

  public String getPath() {
    return path;
  }

  public String getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + (this.op != null ? this.op.hashCode() : 0);
    hash = 59 * hash + (this.path != null ? this.path.hashCode() : 0);
    hash = 59 * hash + (this.value != null ? this.value.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Operation other = (Operation) obj;
    if (this.op != other.op) {
      return false;
    }
    if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
      return false;
    }
    if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Operation{" + "op=" + op + ", path=" + path + ", value=" + value + '}';
  }

  public static Operation determineOperation(String path, String oldValue, String newValue) {
    if (oldValue == null && newValue != null) {
      return new Operation(Op.add, path, newValue);
    }
    if (oldValue != null && newValue == null) {
      return new Operation(Op.remove, path, "");
    }
    if ((oldValue == null && newValue == null) || oldValue.equals(newValue)) {
      return new Operation(Op.none, path, newValue);
    }
    return new Operation(Op.replace, path, newValue);
  }
}
