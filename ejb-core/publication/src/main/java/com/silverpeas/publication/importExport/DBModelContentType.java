/*
 * Created on 24 janv. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.silverpeas.publication.importExport;

import java.util.ArrayList;

/**
 * @author tleroi
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DBModelContentType {

  private int id = -1;
  private ArrayList listTextParts;
  private ArrayList listImageParts;

  /**
   * @return
   */
  public int getId() {
    return id;
  }

  /**
   * @param i
   */
  public void setId(int i) {
    id = i;
  }

  /**
   * @return Returns the listImageParts.
   */
  public ArrayList getListImageParts() {
    return listImageParts;
  }

  /**
   * @param listImageParts
   *          The listImageParts to set.
   */
  public void setListImageParts(ArrayList listImageParts) {
    this.listImageParts = listImageParts;
  }

  /**
   * @return Returns the listTextParts.
   */
  public ArrayList getListTextParts() {
    return listTextParts;
  }

  /**
   * @param listTextParts
   *          The listTextParts to set.
   */
  public void setListTextParts(ArrayList listTextParts) {
    this.listTextParts = listTextParts;
  }
}
