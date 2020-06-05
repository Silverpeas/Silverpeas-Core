/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.webapi.attachment;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;

import org.silverpeas.core.webapi.base.WebEntity;

public class ZipEntity implements WebEntity {

  private static final long serialVersionUID = -1614659571095493071L;

  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  private String url;
  @XmlElement(required = true)
  private long size;

  public ZipEntity(URI uri, String url, long size) {
    this.uri = uri;
    this.url = url;
    this.size = size;
  }

  @Override
  public URI getURI() {
    return uri;
  }

}
