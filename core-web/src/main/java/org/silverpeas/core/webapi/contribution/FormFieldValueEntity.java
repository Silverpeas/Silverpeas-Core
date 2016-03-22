/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.contribution;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FormFieldValueEntity {

  @XmlElement(defaultValue = "")
  private String id;

  @XmlElement(defaultValue = "")
  private String displayedValue;

  @XmlElement(defaultValue = "")
  private String link;

  @XmlElement(defaultValue = "")
  private URI attachmentUri;

  /**
   * Sets a link to this entity.
   * @param link the link.
   * @return itself.
   */
  public FormFieldValueEntity withLink(final String link) {
    this.link = link;
    return this;
  }

  /**
   * Sets an attachment URI to this entity.
   * @param attachmentUri the web attachment entity URI.
   * @return itself.
   */
  public FormFieldValueEntity withAttachmentURI(final URI attachmentUri) {
    this.attachmentUri = attachmentUri;
    return this;
  }

  /**
   * Creates a new form field value entity from the specified form field value data
   * @param id
   * @param displayedValue
   * @return the entity representing the specified form field value data.
   */
  public static FormFieldValueEntity createFrom(String id, String displayedValue) {
    return new FormFieldValueEntity(id, displayedValue);
  }

  /**
   * Default hidden constructor.
   */
  private FormFieldValueEntity(String id, String displayedValue) {
    this.id = id;
    this.displayedValue = displayedValue;
  }

  protected FormFieldValueEntity() {

  }

  protected String getId() {
    return id;
  }

  protected String getDisplayedValue() {
    return displayedValue;
  }
}
