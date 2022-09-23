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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * A behavior a WA component instance support. Such behavior denotes a transverse mechanism in
 * Silverpeas with which the functionalities of a component instance can be enriched.
 * 
 */
@XmlType(name = "BehaviorType")
@XmlEnum
public enum ComponentBehavior {

  /**
   * The component instance supports the categorization of the contributions it manages within
   * a tree of topics. These topics are manageable by the instance.
   */
  @XmlEnumValue("topicTracker")
  TOPIC_TRACKER("topicTracker"),

  /**
   * The component instance defines a workflow, that is a pipeline of processes. With such behavior,
   * the workflow won't be taken in charge directly by the component instances management of
   * Silverpeas but by another and dedicated application component named Process Manager.
   */
  @XmlEnumValue("workflow")
  WORKFLOW("workflow");
  private final String value;

  ComponentBehavior(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  @SuppressWarnings("unused")
  public static ComponentBehavior fromValue(String v) {
    for (ComponentBehavior c : ComponentBehavior.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }

}
