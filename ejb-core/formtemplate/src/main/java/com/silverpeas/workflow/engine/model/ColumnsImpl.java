package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.silverpeas.workflow.api.model.Column;
import com.silverpeas.workflow.api.model.Columns;
import com.silverpeas.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;columns&gt; element of a
 * Process Model.
 **/
public class ColumnsImpl extends AbstractReferrableObject implements
    Serializable, Columns {
  private Vector columnList; // a list of columns ( Column objects )
  private String roleName = "default"; // the name of the role.

  /**
   * Constructor
   */
  public ColumnsImpl() {
    super();
    columnList = new Vector();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.engine.model.Columns#getItemRefList()
   */
  public List getColumnList() {
    return columnList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.engine.model.Columns#getRoleName()
   */
  public String getRoleName() {
    return roleName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.workflow.engine.model.Columns#setRoleName(java.lang.String)
   */
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.Columns#getColumn(java.lang.String)
   */
  public Column getColumn(String strItemName) {
    ItemImpl search = new ItemImpl();
    int i = 1;

    search.setName(strItemName);
    i = columnList.indexOf(search);

    if (i == -1)
      return null;
    else
      return (Column) columnList.get(i);
  }

  /**
   * Get the unique key, used by equals method
   * 
   * @return unique key
   */
  public String getKey() {
    return (this.roleName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.workflow.api.model.Columns#addColumn(com.silverpeas.workflow
   * .api.model.Column)
   */
  public void addColumn(Column column) {
    // System.out.println("ColumnnsImpl.addColumn() ");
    columnList.addElement(column);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.Columns#createColumn()
   */
  public Column createColumn() {
    // System.out.println("ColumnnsImpl.createColumn() ");
    return new ColumnImpl();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.Columns#iterateColumn()
   */
  public Iterator iterateColumn() {
    // System.out.println("ColumnnsImpl.iterateColumn() ");
    return columnList.iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.Columns#removeAllColumns()
   */
  public void removeAllColumns() {
    columnList.clear();
  }
}
