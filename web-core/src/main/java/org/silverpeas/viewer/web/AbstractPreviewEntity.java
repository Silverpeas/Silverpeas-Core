/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.viewer.web;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.silverpeas.web.Exposable;

/**
 * The preview entity is a preview instance that is exposed in the web as
 * an entity (web entity).
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractPreviewEntity<T extends AbstractPreviewEntity<T>> implements
    Exposable {
  private static final long serialVersionUID = 4118811534281560380L;

  @XmlElement(defaultValue = "")
  private URI uri;

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public T withURI(final URI uri) {
    this.uri = uri;
    return (T) this;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.Exposable#getURI()
   */
  @Override
  public URI getURI() {
    return uri;
  }
}
