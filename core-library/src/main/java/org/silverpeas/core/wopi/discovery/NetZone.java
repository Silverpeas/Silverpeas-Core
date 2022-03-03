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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

/**
 * Defines a net zone into which the client is provided.
 * <p>
 *   In most of cases, to not say in all cases, an "external-http" zone is defined. That means
 *   that the editors are provided from an external infrastructure.
 * </p>
 * @author silveryocha
 */
@XmlRootElement(name = "net-zone")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "net-zone")
public class NetZone implements Serializable {
  private static final long serialVersionUID = 5086651622500970875L;

  @XmlAttribute
  private String name;

  @XmlElement(name = "app")
  private List<App> apps;

  protected String getName() {
    return name;
  }

  protected List<App> getApps() {
    return apps;
  }
}
