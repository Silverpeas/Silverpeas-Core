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
package org.silverpeas.core.importexport.coordinates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author dlesimple
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CoordinatesPositionsType {

  @XmlElement(name = "coordinatePosition")
  private List coordinatesPositions;
  @XmlAttribute
  private boolean createEnable = false;

  public CoordinatesPositionsType() {
    // This constructor is necessary with JAXB
  }

  public List getCoordinatesPositions() {
    return coordinatesPositions;
  }

  public void setCoordinatesPositions(List coordinatesPositions) {
    this.coordinatesPositions = coordinatesPositions;
  }

  public boolean getCreateEnable() {
    return createEnable;
  }

}