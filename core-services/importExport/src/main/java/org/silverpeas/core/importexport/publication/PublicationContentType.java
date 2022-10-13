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
package org.silverpeas.core.importexport.publication;

import org.silverpeas.core.importexport.form.XMLModelContentType;
import org.silverpeas.core.importexport.wysiwyg.WysiwygContentType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author tleroi
 */
@XmlRootElement(name = "publicationContent", namespace = "http://www.silverpeas.org/exchange")
@XmlAccessorType(XmlAccessType.NONE)
public class PublicationContentType {

  @XmlElement(name = "wysiwyg", namespace = "http://www.silverpeas.org/exchange")
  private WysiwygContentType wysiwygContentType;
  @XmlElement(name = "xmlModel", namespace = "http://www.silverpeas.org/exchange")
  private XMLModelContentType xmlModelContentType;

  public PublicationContentType() {
    // This constructor is necessary with JAXB
  }

  public XMLModelContentType getXMLModelContentType() {
    return xmlModelContentType;
  }

  public void setXMLModelContentType(XMLModelContentType xmlModelContentType) {
    this.xmlModelContentType = xmlModelContentType;
  }

  /**
   * @return Returns the wysiwygContentType.
   */
  public WysiwygContentType getWysiwygContentType() {
    return wysiwygContentType;
  }

  /**
   * @param wysiwygContentType The wysiwygContentType to set.
   */
  public void setWysiwygContentType(WysiwygContentType wysiwygContentType) {
    this.wysiwygContentType = wysiwygContentType;
  }
}