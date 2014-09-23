/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.profile.web;

import java.net.URI;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.silverpeas.web.Exposable;

/**
 * The count of users, exposable in the WEB, belonging to a Silverpeas grouping element such as a
 * domain or a group. This web entity can be serialized into a given media type (JSON, XML).
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UserCountEntity implements Exposable {

  private static final long serialVersionUID = 1872460584111706793L;
  
  @XmlElement(required = true)
  private URI uri;
  @XmlElement
  private int count;
  
  public UserCountEntity(int count) {
    this.count = count;
  }
  
  public UserCountEntity() {
    this.count = -1;
  }
  
  public UserCountEntity withAsUri(URI uri) {
    this.uri = uri;
    return this;
  }
  
  public void setCount(int count) {
    this.count = count;
  }
  
  public int getCount() {
    return count;
  }
  
  @Override
  public URI getURI() {
    return this.uri;
  }

}
