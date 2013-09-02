/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/** 
 *
 * @author  akhadrou
 * @version 
 */
package com.stratelia.webactiv.util.publication.info.model;

import java.io.Serializable;

public class ModelDetail implements Serializable {

  private static final long serialVersionUID = -8687004760818045824L;
  private String id = null;
  private String name = null;
  private String description = null;
  private String imageName = null;
  private String htmlDisplayer = null;
  private String htmlEditor = null;

  public ModelDetail(String id, String name, String description,
      String imageName, String htmlDisplayer, String htmlEditor) {
    setId(id);
    setName(name);
    setDescription(description);
    setImageName(imageName);
    setHtmlDisplayer(htmlDisplayer);
    setHtmlEditor(htmlEditor);
  }

  // set get on attributes
  // id
  public String getId() {
    return id;
  }

  public void setId(String val) {
    id = val;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getImageName() {
    return imageName;
  }

  public void setImageName(String imageName) {
    this.imageName = imageName;
  }

  public String getHtmlDisplayer() {
    return htmlDisplayer;
  }

  public void setHtmlDisplayer(String htmlDisplayer) {
    this.htmlDisplayer = htmlDisplayer;
  }

  public String getHtmlEditor() {
    return htmlEditor;
  }

  public void setHtmlEditor(String htmlEditor) {
    this.htmlEditor = htmlEditor;
  }

  public String toString() {
    return "(id = " + getId() + ", name = " + getName() + ", description = "
        + getDescription() + ", imageName = " + getImageName()
        + ", hmlDisplayer = " + getHtmlDisplayer() + ", htmlEditor = "
        + getHtmlEditor() + ")";
  }

}