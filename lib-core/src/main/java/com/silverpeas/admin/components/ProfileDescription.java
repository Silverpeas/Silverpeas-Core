/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.admin.components;

import java.util.HashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>Java class for ProfileDescription complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProfileDescription">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="label" type="{http://silverpeas.org/xml/ns/component}multilang"/>
 *         &lt;element name="help" type="{http://silverpeas.org/xml/ns/component}multilang"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProfileDescription", propOrder = {
  "label",
  "help"
})
@XmlSeeAlso({
  Profile.class
})
public class ProfileDescription {

  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected HashMap<String, String> label;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected HashMap<String, String> help;

  /**
   * Gets the value of the label property.
   * 
   * @return
   *     possible object is
   *     {@link Multilang }
   *     
   */
  public HashMap<String, String> getLabel() {
    return label;
  }

  /**
   * Sets the value of the label property.
   * 
   * @param value
   *     allowed object is
   *     {@link Multilang }
   *     
   */
  public void setLabel(HashMap<String, String> value) {
    this.label = value;
  }

  /**
   * Gets the value of the help property.
   * 
   * @return
   *     possible object is
   *     {@link Multilang }
   *     
   */
  public HashMap<String, String> getHelp() {
    return help;
  }

  /**
   * Sets the value of the help property.
   * 
   * @param value
   *     allowed object is
   *     {@link Multilang }
   *     
   */
  public void setHelp(HashMap<String, String> value) {
    this.help = value;
  }
}
