/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.util;

import java.util.List;

/**
 * This is the data structure that represents one part of a Join Statement
 */
public class JoinStatement {
  private String sTable = "";
  private String sWhere = "";
  private List<String> alTables = null;
  private List<String> alKeys = null;

  public JoinStatement() {
  }

  public void setTable(String sGivenTable) {
    sTable = sGivenTable;
  }

  public void setTables(List<String> alGivenTables) {
    alTables = alGivenTables;
  }

  public List<String> getTables() {
    return alTables;
  }

  public String getTable(int position) {
    if (alTables != null) {
      return alTables.get(position);
    }
    return sTable;
  }

  public void setJoinKeys(List<String> alGivenJoinKey) {
    this.alKeys = alGivenJoinKey;
  }

  public List<String> getJoinKeys() {
    return this.alKeys;
  }

  public String getJoinKey(int position) {
    return alKeys.get(position);
  }

  public void setWhere(String sGivenWhere) {
    sWhere = sGivenWhere;
  }

  public String getWhere() {
    return sWhere;
  }
}
