package com.silverpeas.form.importExport;

import java.io.Serializable;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * @author neysseri
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class XMLField implements Serializable {

  private String name = null;
  private String value = null;

  public XMLField() {
  }

  public XMLField(String name, String value) {
    SilverTrace.info("form", "XMLField.contructor",
        "root.MSG_GEN_ENTER_METHOD", "name = " + name + ", value = " + value);
    setName(name);
    setValue(value);
  }

  /**
   * @return the name of the XML field
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

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}