/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.admin.components;


import com.stratelia.webactiv.util.WAPrimaryKey;

public abstract class PasteDetailFromToPK<F extends WAPrimaryKey, T extends WAPrimaryKey>
    extends PasteDetail {

  private F fromPK;

  private T toPK;

  public PasteDetailFromToPK() {

  }

  public PasteDetailFromToPK(String userId) {
    setUserId(userId);
  }

  public F getFromPK() {
    return fromPK;
  }

  public void setFromPK(final F fromPK) {
    this.fromPK = fromPK;
  }

  public T getToPK() {
    return toPK;
  }

  public void setToPK(final T toPK) {
    this.toPK = toPK;
  }
}
