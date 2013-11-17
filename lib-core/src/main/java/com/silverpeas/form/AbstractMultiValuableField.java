package com.silverpeas.form;

import java.util.ArrayList;
import java.util.List;

import com.silverpeas.util.StringUtil;

public class AbstractMultiValuableField implements MultiValuableField {

  private static final long serialVersionUID = -4102757054473695655L;

  @Override
  public String getValue() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setValue(String value) throws FormException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean acceptValue(String value) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getValue(String lang) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setValue(String value, String lang) throws FormException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean acceptValue(String value, String lang) {
    // TODO Auto-generated method stub
    return false;
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
    // TODO Auto-generated method stub
    return false;
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
    // TODO Auto-generated method stub
    return false;
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
    // TODO Auto-generated method stub
    return false;
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
    // TODO Auto-generated method stub
    return false;
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
    // TODO Auto-generated method stub
    return false;
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
    // TODO Auto-generated method stub
    return false;
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
