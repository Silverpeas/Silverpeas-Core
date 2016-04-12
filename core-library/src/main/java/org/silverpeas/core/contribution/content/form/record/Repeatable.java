/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.contribution.content.form.record;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "multivaluable")
@XmlAccessorType(XmlAccessType.FIELD)
public class Repeatable {

  private int max = 1;
  private int mandatory;

  public Repeatable() {
  }

  public Repeatable(int max, int mandatory) {
    super();
    this.max = max;
    this.mandatory = mandatory;
  }

  public void setMax(int max) {
    this.max = max;
  }
  public int getMax() {
    return max;
  }

  public void setMandatory(int mandatory) {
    this.mandatory = mandatory;
  }
  public int getMandatory() {
    return mandatory;
  }

  public boolean isRepeatable() {
   return max > 1;
  }

}
