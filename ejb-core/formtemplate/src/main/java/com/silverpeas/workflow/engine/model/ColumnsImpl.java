/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.silverpeas.workflow.api.model.Column;
import com.silverpeas.workflow.api.model.Columns;
import com.silverpeas.workflow.engine.AbstractReferrableObject;
import java.util.ArrayList;

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
    columnList = new ArrayList<Column>();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.engine.model.Columns#getItemRefList()
   */
  @Override
  public List<Column> getColumnList() {
    return columnList;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.engine.model.Columns#getRoleName()
   */
  @Override
  public String getRoleName() {
    return roleName;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.engine.model.Columns#setRoleName(java.lang.String)
   */
  @Override
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Columns#getColumn(java.lang.String)
   */
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

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Columns#addColumn(com.silverpeas.workflow
   * .api.model.Column)
   */
  @Override
  public void addColumn(Column column) {
    columnList.add(column);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Columns#createColumn()
   */
  @Override
  public Column createColumn() {
    return new ColumnImpl();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Columns#iterateColumn()
   */
  @Override
  public Iterator<Column> iterateColumn() {
    return columnList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Columns#removeAllColumns()
   */
  @Override
  public void removeAllColumns() {
    columnList.clear();
  }
}
