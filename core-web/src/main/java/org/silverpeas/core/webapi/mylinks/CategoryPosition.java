/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.webapi.mylinks;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The {@link CategoryPosition} object just represents a category id and its position
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoryPosition {

  @XmlElement
  @NotNull
  private int catId;

  @XmlElement
  private int position;

  /**
   * Default constructor
   */
  protected CategoryPosition() {
  }

  /**
   * @return the category identifier
   */
  public int getCatId() {
    return catId;
  }

  /**
   * @return the position
   */
  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public void setCatId(int catId) {
    this.catId = catId;
  }

}
