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
package org.silverpeas.core.webapi.node;

import org.silverpeas.core.node.model.NodeDetail;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NodeTranslationEntity {

  @XmlElement(defaultValue = "")
  private String id;
  @XmlElement(defaultValue = "")
  private String name;
  @XmlElement(defaultValue = "")
  private String description;
  @XmlElement(defaultValue = "")
  private String language;

  public NodeTranslationEntity() {
  }

  public NodeTranslationEntity(String id, String language, NodeDetail node) {
    this.setId(id);
    this.setLanguage(language);
    this.setName(node.getName(language));
    this.setDescription(node.getDescription(language));
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getLanguage() {
    return language;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}