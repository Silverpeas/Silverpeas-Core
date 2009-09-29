package com.stratelia.silverpeas.pdc.model;

import java.util.List;

import com.stratelia.silverpeas.treeManager.model.TreeNode;

/**
 * @author Sébastien Antonio
 */
public class Value extends TreeNode implements java.io.Serializable {

  /**
   * The primary key of the object Value
   */
  ValuePK pk = null;

  private int nbObjects = 0;
  private List pathValues = null;
  private String axisId = "unknown";
  private String fullPath = null;

  //
  // constructor
  //

  public Value() {
    super();
  }

  public Value(String id, String treeId, String name, String description,
      String creationDate, String creatorId, String path, int level, int order,
      String fatherId) {
    super(id, treeId, name, description, creationDate, creatorId, path, level,
        order, fatherId);
    setValuePK(new ValuePK(id));
  }

  public Value(String id, String treeId, String name, String creationDate,
      String creatorId, String path, int level, int order, String fatherId) {
    super(id, treeId, name, null, creationDate, creatorId, path, level, order,
        fatherId);
    setValuePK(new ValuePK(id));
  }

  //
  // public methods
  //

  public void setValuePK(ValuePK pk) {
    this.pk = pk;
  }

  public ValuePK getValuePK() {
    return this.pk;
  }

  /**
   * Returns the id of the mother value
   * 
   * @return an id
   */
  public String getMotherId() {
    return getFatherId();
  }

  public String getFullPath() {
    if (fullPath == null)
      fullPath = getPath() + getValuePK().getId() + "/";
    return fullPath;
  }

  public int getNbObjects() {
    return nbObjects;
  }

  public void setNbObjects(int nbObjects) {
    this.nbObjects = nbObjects;
  }

  public void setPathValues(List pathValues) {
    this.pathValues = pathValues;
  }

  public List getPathValues() {
    return this.pathValues;
  }

  public void setAxisId(int axisId) {
    this.axisId = new Integer(axisId).toString();
  }

  public String getAxisId() {
    return this.axisId;
  }

  public String getStringFullPath(String delimitor) {
    String fullPath = "";
    Value value = null;
    for (int i = 0; i < pathValues.size(); i++) {
      value = (Value) pathValues.get(i);
      if (i != 0)
        fullPath += delimitor;
      fullPath += value.getName();
    }
    return fullPath;
  }

  public String toString() {
    return "(pk = " + getValuePK().toString() + ")";
  }

}