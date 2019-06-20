/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.webapi.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * The type of a node.
 * @author mmoquillon
 */
@XmlType
@XmlEnum(String.class)
public enum NodeType {

  @XmlEnumValue("root")
  ROOT("root"),
  @XmlEnumValue("folder")
  FOLDER("folder"),
  @XmlEnumValue("folder-with-rights")
  FOLDER_WITH_RIGHTS("folder-with-rights"),
  @XmlEnumValue("bin")
  BIN("bin"),
  @XmlEnumValue("tovalidate")
  TO_VALIDATE("tovalidate");

  private final String name;

  NodeType(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  @JsonValue
  public String value() {
    return name;
  }

  @JsonCreator
  public static NodeType fromValue(String value) {
    for (NodeType nodeType: NodeType.values()) {
      if (nodeType.value().equals(value)) {
        return nodeType;
      }
    }
    throw new IllegalArgumentException(value);
  }
}
