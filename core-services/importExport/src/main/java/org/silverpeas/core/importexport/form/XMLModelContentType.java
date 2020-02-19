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
package org.silverpeas.core.importexport.form;

import org.silverpeas.core.contribution.content.form.XMLField;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author neysseri
 */
@XmlRootElement(name = "xmlModel", namespace = "http://www.silverpeas.org/exchange")
@XmlAccessorType(XmlAccessType.NONE)
public class XMLModelContentType implements Serializable {

  @XmlAttribute
  private String name;
  @XmlElement(name = "xmlField", namespace = "http://www.silverpeas.org/exchange")
  private List<XMLField> fields;

  public XMLModelContentType() {
  }

  public XMLModelContentType(String name) {
    this.name = name;
  }

  /**
   * @return the name of the XML Model used
   */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the listImageParts.
   */
  public List<XMLField> getFields() {
    return fields;
  }

  /**
   * @param fields the XML field list to set.
   */
  public void setFields(List<XMLField> fields) {
    this.fields = fields;
  }

  public void addField(XMLField field) {
    if (fields == null) {
      fields = new ArrayList<>();
    }

    fields.add(field);
  }
}