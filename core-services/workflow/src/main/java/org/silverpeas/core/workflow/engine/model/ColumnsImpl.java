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

package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.silverpeas.core.workflow.api.model.Column;
import org.silverpeas.core.workflow.api.model.Columns;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;columns&gt; element of a Process Model.
 **/
public class ColumnsImpl extends AbstractReferrableObject implements Serializable, Columns {
  private static final long serialVersionUID = -179308759997989687L;
  private List<Column> columnList; // a list of columns ( Column objects )
  private String roleName = "default"; // the name of the role.

  /**
   * Constructor
   */
  public ColumnsImpl() {
    super();
    columnList = new ArrayList<>();
  }

  @Override
  public List<Column> getColumnList() {
    return columnList;
  }

  @Override
  public String getRoleName() {
    return roleName;
  }

  @Override
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  @Override
  public Column getColumn(String strItemName) {
    ItemImpl search = new ItemImpl();
    search.setName(strItemName);
    int i = columnList.indexOf(search);

    if (i < 0) {
      return null;
    }
    return columnList.get(i);
  }

  /**
   * Get the unique key, used by equals method
   * @return unique key
   */
  @Override
  public String getKey() {
    return (this.roleName);
  }

  @Override
  public void addColumn(Column column) {
    columnList.add(column);
  }

  @Override
  public Column createColumn() {
    return new ColumnImpl();
  }

  @Override
  public Iterator<Column> iterateColumn() {
    return columnList.iterator();
  }

  @Override
  public void removeAllColumns() {
    columnList.clear();
  }
}
