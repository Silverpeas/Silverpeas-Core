/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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
import java.util.Vector;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.Column;
import com.silverpeas.workflow.api.model.Columns;
import com.silverpeas.workflow.api.model.ContextualDesignation;
import com.silverpeas.workflow.api.model.ContextualDesignations;
import com.silverpeas.workflow.api.model.Presentation;

/**
 * Class implementing the representation of the &lt;presentation&gt; element of a Process Model.
 **/
public class PresentationImpl implements Presentation, Serializable {

  private static final long serialVersionUID = 8175832964614235239L;
  private ContextualDesignations titles; // object storing the titles
  private Vector<Columns> columnsList; // a vector of columns ( Columns objects )

  /**
   * Constructor
   */
  public PresentationImpl() {
    titles = new SpecificLabelListHelper();
    columnsList = new Vector<Columns>();
  }

  // //////////////////
  // titles
  // //////////////////

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Presentation#getTitles()
   */
  public ContextualDesignations getTitles() {
    return titles;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Presentation#getTitle(java.lang.String,
   * java.lang.String)
   */
  public String getTitle(String role, String language) {
    return titles.getLabel(role, language);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Presentation#addTitle(com.silverpeas.
   * workflow.api.model.ContextualDesignation)
   */
  public void addTitle(ContextualDesignation title) {
    titles.addContextualDesignation(title);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Presentation#iterateTitle()
   */
  public Iterator<ContextualDesignation> iterateTitle() {
    return titles.iterateContextualDesignation();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Presentation#createDesignation()
   */
  public ContextualDesignation createDesignation() {
    return titles.createContextualDesignation();
  }

  // //////////////////
  // itemRefs
  // //////////////////

  /**
   * Get the contents of the Columns object with the given role name, or of the 'Columns' for the
   * default role if nothing for the specified role can be found.
   * @param the name of the role
   * @return the contents of 'Columns' as an array of 'Column'
   */
  public Column[] getColumns(String strRoleName) {
    Columns columns = null;

    columns = getColumnsByRole(strRoleName);

    if (columns == null)
      return null;
    else
      return (Column[]) columns.getColumnList().toArray(new ColumnImpl[0]);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Presentation#getColumnsByRole(java.lang .String)
   */
  public Columns getColumnsByRole(String strRoleName) {
    Columns search;
    int index, indexDefault;

    if (columnsList == null)
      return null;

    search = createColumns();
    search.setRoleName(strRoleName);
    index = columnsList.indexOf(search);

    if (index == -1) {
      search.setRoleName("default");
      indexDefault = columnsList.indexOf(search);

      if (indexDefault == -1)
        return null;

      return (Columns) columnsList.get(indexDefault);
    } else
      return (Columns) columnsList.get(index);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Presentation#createColumns()
   */
  public Columns createColumns() {
    return new ColumnsImpl();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Presentation#addColumns(com.silverpeas
   * .workflow.api.model.Columns)
   */
  public void addColumns(Columns columns) {
    columnsList.addElement(columns);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Presentation#iterateColumns()
   */
  public Iterator<Columns> iterateColumns() {
    return columnsList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Presentation#deleteColumns(java.lang. String)
   */
  public void deleteColumns(String strRoleName) throws WorkflowException {
    Columns search = createColumns();

    search.setRoleName(strRoleName);
    if (!columnsList.remove(search))
      throw new WorkflowException("PresentationImpl.deleteColumns",
          "workflowEngine.EX_COLUMNS_NOT_FOUND", "Columns role name="
          + strRoleName == null ? "<null>" : strRoleName);
  }
}