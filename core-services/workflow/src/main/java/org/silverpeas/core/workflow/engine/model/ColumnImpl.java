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
package org.silverpeas.core.workflow.engine.model;

import org.silverpeas.core.workflow.api.model.Column;
import org.silverpeas.core.workflow.api.model.Item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Class implementing the representation of the &lt;column&gt; element of a Process Model.
 **/
@XmlRootElement(name = "column")
@XmlAccessorType(XmlAccessType.NONE)
public class ColumnImpl implements Column, Serializable {
  private static final long serialVersionUID = 3766121048611753846L;

  @XmlIDREF
  @XmlAttribute
  private ItemImpl item;

  /*
   * @see Column#getItem()
   */
  @Override
  public Item getItem() {
    return item;
  }

  /*
   * @see Column#setItem(Item)
   */
  @Override
  public void setItem(Item item) {
    this.item = (ItemImpl) item;
  }
}