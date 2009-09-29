package com.silverpeas.form.importExport;

import java.util.ArrayList;

import com.silverpeas.form.importExport.XMLField;

/**
 * @author neysseri
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class XMLModelContentType {

  private String name = null;
  private ArrayList fields;

  public XMLModelContentType() {
  }

  public XMLModelContentType(String name) {
    setName(name);
  }

  /**
   * @return the name of the XML Model used
   */
  public String getName() {
    return name;
  }

  /**
   * @param i
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the listImageParts.
   */
  public ArrayList getFields() {
    return fields;
  }

  /**
   * @param listImageParts
   *          The listImageParts to set.
   */
  public void setFields(ArrayList fields) {
    this.fields = fields;
  }

  public void addField(XMLField field) {
    if (fields == null)
      fields = new ArrayList();

    fields.add(field);
  }
}