package com.stratelia.webactiv.beans.admin.instance.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class SPParameter implements Serializable, Comparable {
  private String name = null;
  private String label = null;
  private String value = null;
  private boolean mandatory = false;
  private String updatable = null;
  private String type = null;
  private Hashtable helps = null;
  private String size = null;
  private Integer displayOrder = null;

  private String defaultValue = null;

  private ArrayList options = null;

  public SPParameter(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public SPParameter(String name, String value, String label) {
    this.name = name;
    this.value = value;
    this.label = label;
  }

  public SPParameter(String name, String label, String value, String mandatory,
      String updatable, String type, Hashtable helps, String size,
      String defaultValue, String displayOrder, ArrayList options) {
    SilverTrace.info("admin", "SPParameter.SPParameter",
        "root.MSG_GEN_PARAM_VALUE", "name: " + name + ", value: " + value
            + " (mandatory ? " + mandatory + ")+(updatable ? " + updatable
            + ") options=" + options);
    this.name = name;
    this.label = label;
    this.value = value;

    if (mandatory != null && mandatory.equals("Y"))
      this.mandatory = true;
    else
      this.mandatory = false;

    this.updatable = updatable;
    this.type = type;
    this.helps = helps;
    this.size = size;
    this.defaultValue = defaultValue;

    if (displayOrder == null || displayOrder.length() == 0)
      this.displayOrder = new Integer(1);
    else
      this.displayOrder = new Integer(displayOrder);

    this.options = options;
  }

  public SPParameter(String name, String label, String value,
      boolean mandatory, String updatable, String type, Hashtable helps,
      String size, String defaultValue, ArrayList options) {
    this.name = name;
    this.label = label;
    this.value = value;
    this.mandatory = mandatory;
    this.updatable = updatable;
    this.type = type;
    this.helps = helps;
    this.size = size;
    this.defaultValue = defaultValue;
    this.options = options;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }

  public String getValue() {
    if (value == null)
      return defaultValue;
    else
      return value;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public String getMandatoryAsString() {
    return mandatory ? "Y" : "N";
  }

  public void setMandatory(String strMandatory) {
    if (strMandatory != null && strMandatory.equals("Y"))
      this.mandatory = true;
    else
      this.mandatory = false;
  }

  public String getUpdatable() {
    return updatable;
  }

  public String getType() {
    return type;
  }

  public Hashtable getHelps() {
    return helps;
  }

  public String getHelp(String language) {
    String help = null;
    if (helps != null) {
      help = (String) helps.get(language);
    }
    return help;
  }

  public String getSize() {
    return size;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public ArrayList getOptions() {
    return options;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  public Object clone() {
    SPParameter clonedParameter = new SPParameter(name, label, value,
        mandatory, updatable, type, helps, size, defaultValue, options);
    clonedParameter.setDisplayOrder(getDisplayOrder());
    return clonedParameter;
  }

  public int compareTo(Object o) {
    SPParameter parameterToCompare = (SPParameter) o;

    return getDisplayOrder().compareTo(parameterToCompare.getDisplayOrder());
  }

  public String toString() {
    return "name = " + name + ", value = " + value;
  }
}