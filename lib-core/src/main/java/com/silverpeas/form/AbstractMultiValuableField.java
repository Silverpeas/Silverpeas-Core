package com.silverpeas.form;

import java.util.ArrayList;
import java.util.List;

import com.silverpeas.util.StringUtil;

public class AbstractMultiValuableField implements MultiValuableField {

  private static final long serialVersionUID = -4102757054473695655L;
  
  /**
   * Returns true if the value is read only.
   */
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public String getValue() {
    if (getValues() != null) {
      return getValues().get(0);
    }
    return "";
  }

  @Override
  public void setValue(String value) throws FormException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean acceptValue(String value) {
    return !isReadOnly();
  }

  @Override
  public String getValue(String lang) {
    if (getValues(lang) != null) {
      return getValues(lang).get(0);
    }
    return "";
  }

  @Override
  public void setValue(String value, String lang) throws FormException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean acceptValue(String value, String lang) {
    return !isReadOnly();
  }

  @Override
  public String getStringValue() {
    return StringUtil.join(getStringValues(), ", ");
  }

  @Override
  public void setStringValue(String value) throws FormException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean acceptStringValue(String value) {
    return !isReadOnly();
  }

  @Override
  public Object getObjectValue() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setObjectValue(Object value) throws FormException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean acceptObjectValue(Object value) {
    return !isReadOnly();
  }

  @Override
  public int compareTo(Object o) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getTypeName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getValues() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setValues(List<String> values) throws FormException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean acceptValues(List<String> values) {
    return !isReadOnly();
  }

  @Override
  public List<String> getValues(String lang) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setValues(List<String> values, String lang) throws FormException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean acceptValues(List<String> values, String lang) {
    return !isReadOnly();
  }

  @Override
  public List<String> getStringValues() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setStringValues(List<String> values) throws FormException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean acceptStringValues(List<String> values) {
    return !isReadOnly();
  }

  @Override
  public List getObjectValues() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setObjectValues(List<Object> values) throws FormException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean acceptObjectValues(List<Object> values) {
    return !isReadOnly();
  }

  @Override
  public boolean isNull() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setNull() throws FormException {
    // TODO Auto-generated method stub

  }
  
  public void addValue(String value) {
    List<String> values = getValues();
    if (values == null) {
      values = new ArrayList<String>();
    }
    values.add(value);
  }
  
  public void addValue(int index, String value) {
    List<String> values = getValues();
    if (values == null) {
      values = new ArrayList<String>();
    }
    values.add(index, value);
  }

}
