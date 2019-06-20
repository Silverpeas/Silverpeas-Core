/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.Column;
import org.silverpeas.core.workflow.api.model.Columns;
import org.silverpeas.core.workflow.api.model.ContextualDesignation;
import org.silverpeas.core.workflow.api.model.ContextualDesignations;
import org.silverpeas.core.workflow.api.model.Presentation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class implementing the representation of the &lt;presentation&gt; element of a Process Model.
 **/
@XmlRootElement(name = "presentation")
@XmlAccessorType(XmlAccessType.NONE)
public class PresentationImpl implements Presentation, Serializable {

  private static final long serialVersionUID = 8175832964614235239L;
  @XmlElement(name = "title", type = SpecificLabel.class)
  private List<ContextualDesignation> titles;
  @XmlElement(name = "columns", type = ColumnsImpl.class)
  private List<Columns> columnsList;

  /**
   * Constructor
   */
  public PresentationImpl() {
    titles = new ArrayList<>();
    columnsList = new Vector<>();
  }

  // //////////////////
  // titles
  // //////////////////

  /*
   * (non-Javadoc)
   * @see Presentation#getTitles()
   */
  public ContextualDesignations getTitles() {
    return new SpecificLabelListHelper(titles);
  }

  /*
   * (non-Javadoc)
   * @see Presentation#getTitle(java.lang.String,
   * java.lang.String)
   */
  public String getTitle(String role, String language) {
    return getTitles().getLabel(role, language);
  }

  /**
   * Get the contents of the Columns object with the given role name, or of the 'Columns' for the
   * default role if nothing for the specified role can be found.
   * @param strRoleName the name of the role
   * @return the contents of 'Columns' as an array of 'Column'
   */
  public Column[] getColumns(String strRoleName) {
    Columns columns = getColumnsByRole(strRoleName);
    if (columns == null) {
      return new Column[0];
    }
    return columns.getColumnList().toArray(new Column[0]);
  }

  /*
   * (non-Javadoc)
   * @see Presentation#getColumnsByRole(java.lang .String)
   */
  public Columns getColumnsByRole(String strRoleName) {
    Columns search;
    int index, indexDefault;

    if (columnsList == null) {
      return null;
    }

    search = createColumns();
    search.setRoleName(strRoleName);
    index = columnsList.indexOf(search);

    if (index == -1) {
      search.setRoleName("default");
      indexDefault = columnsList.indexOf(search);

      if (indexDefault == -1) {
        return null;
      }

      return columnsList.get(indexDefault);
    } else {
      return columnsList.get(index);
    }
  }

  /*
   * (non-Javadoc)
   * @see Presentation#createColumns()
   */
  public Columns createColumns() {
    return new ColumnsImpl();
  }

  /*
   * (non-Javadoc)
   * @see Presentation#addColumns(com.silverpeas
   * .workflow.api.model.Columns)
   */
  public void addColumns(Columns columns) {
    columnsList.add(columns);
  }

  /*
   * (non-Javadoc)
   * @see Presentation#iterateColumns()
   */
  public Iterator<Columns> iterateColumns() {
    return columnsList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see Presentation#deleteColumns(java.lang. String)
   */
  public void deleteColumns(String strRoleName) throws WorkflowException {
    Columns search = createColumns();

    search.setRoleName(strRoleName);
    if (!columnsList.remove(search)) {
      throw new WorkflowException("PresentationImpl.deleteColumns",
          "workflowEngine.EX_COLUMNS_NOT_FOUND",
          "Columns role name=" + strRoleName == null ? "<null>" : strRoleName);
    }
  }
}