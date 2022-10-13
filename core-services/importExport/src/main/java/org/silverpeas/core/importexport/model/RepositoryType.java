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
/*
 * Created on 24 janv. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.silverpeas.core.importexport.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author tleroi To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@XmlRootElement(namespace = "http://www.silverpeas.org/exchange")
@XmlAccessorType(XmlAccessType.NONE)
public class RepositoryType {

  public static final int NO_RECURSIVE = 0;
  public static final int RECURSIVE_NOREPLICATE = 1;
  public static final int RECURSIVE_REPLICATE = 2;
  private static final String NO_RECURSIVE_STRING = "NO_RECURSIVE";
  private static final String RECURSIVE_NOREPLICATE_STRING = "RECURSIVE_NOREPLICATE";
  private static final String RECURSIVE_REPLICATE_STRING = "RECURSIVE_REPLICATE";

  @XmlAttribute
  private String path;
  @XmlAttribute
  private String componentId;
  @XmlAttribute
  private int topicId;
  @XmlAttribute
  private String massiveType;

  public RepositoryType() {
    // This constructor is necessary with JAXB
  }

  public String getComponentId() {
    return componentId;
  }

  public int getMassiveTypeInt() {
    if (NO_RECURSIVE_STRING.equals(massiveType)) {
      return NO_RECURSIVE;
    }
    if (RECURSIVE_NOREPLICATE_STRING.equals(massiveType)) {
      return RECURSIVE_NOREPLICATE;
    }
    if (RECURSIVE_REPLICATE_STRING.equals(massiveType)) {
      return RECURSIVE_REPLICATE;
    }
    return -1;
  }

  public String getPath() {
    return path;
  }

  public int getTopicId() {
    return topicId;
  }

  public void setComponentId(String string) {
    componentId = string;
  }

  public void setPath(String string) {
    path = string;
  }

  public void setTopicId(int i) {
    topicId = i;
  }

}