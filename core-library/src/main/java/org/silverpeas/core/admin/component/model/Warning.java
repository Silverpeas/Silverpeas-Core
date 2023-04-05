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
package org.silverpeas.core.admin.component.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * A list of message with an attribute to specify whether the warning message MUST be displayed
 * on all changes or not (default).
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WarningType", propOrder = { "message" })
public class Warning {

  @XmlAttribute
  protected boolean always = false;

  protected List<Message> message = new ArrayList<>();

  @XmlTransient
  private Map<String, String> messages;

  public Warning() {
  }

  Warning(final Warning other) {
    this.always = other.always;
    this.message = new ArrayList<>(other.message);
    this.messages = other.messages != null ? new HashMap<>(other.messages) : null;
  }

  /**
   * Must the warning be displayed whatever the parameter value ?
   * @return true if the warning have to be displayed at any parameter value change, false to
   * display the warning only when the parameter value change to true.
   */
  public boolean isAlways() {
    return always;
  }

  /**
   * Sets the data returned by {@link #isAlways()} method.
   * @param always a boolean.
   * @see #isAlways()
   */
  public void setAlways(final boolean always) {
    this.always = always;
  }

  /**
   * Gets a copy of registered messages.
   * @return messages indexed by languages.
   */
  public Map<String, String> getMessages() {
    return Map.copyOf(safeMessages());
  }

  private Map<String, String> safeMessages() {
    if (messages == null) {
      messages = message.stream().collect(toMap(Message::getLang, Message::getValue));
    }
    return messages;
  }

  /**
   * Sets messages.
   * @param messages a map of messages indexed by languages.
   */
  public void setMessages(final Map<String, String> messages) {
    this.messages = null;
    this.message = messages.entrySet().stream().map(e -> {
      final Message msg = new Message();
      msg.setLang(e.getKey());
      msg.setValue(e.getValue());
      return msg;
    }).collect(Collectors.toList());
  }
}
