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

package org.silverpeas.core.wopi.discovery;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * The action element and its attributes in the discovery XML provides some important information
 * about Office for the web.
 * <p>
 * It provides:
 *   <ul>
 *     <li>the name of the action, (view, edit) which permits to know from Silverpeas's point of
 *     view what kind of manipulations it will be possible with the document</li>
 *     <li>the handled extension</li>
 *     <li>the client URL that permits to edit into the browser file a document</li>
 *   </ul>
 * </p>
 * @author silveryocha
 */
@XmlRootElement(name = "action")
@XmlAccessorType(XmlAccessType.FIELD)
public class Action implements Serializable {
  private static final long serialVersionUID = -24246963248719096L;

  @XmlAttribute
  private String ext;

  @XmlAttribute
  private String name;

  @XmlAttribute
  private String urlsrc;

  public String getExt() {
    return ext;
  }

  public String getName() {
    return name;
  }

  public String getUrlsrc() {
    return urlsrc;
  }
}
