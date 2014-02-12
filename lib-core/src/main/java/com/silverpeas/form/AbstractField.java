package com.silverpeas.form;

public abstract class AbstractField implements Field {
  
  private static final long serialVersionUID = 8716397233101185565L;
  private String name;
  private int occurrence;

  @Override
  public abstract int compareTo(Object o);

  @Override
  public abstract String getTypeName();

  @Override
  public abstract String getValue();

  @Override
  public abstract void setValue(String value) throws FormException;

  @Override
  public abstract boolean acceptValue(String value);

  @Override
  public abstract String getValue(String lang);

  @Override
  public abstract void setValue(String value, String lang) throws FormException;

  @Override
  public abstract boolean acceptValue(String value, String lang);

  @Override
  public abstract String getStringValue();

  @Override
  public abstract void setStringValue(String value) throws FormException;

  @Override
  public abstract boolean acceptStringValue(String value);

  @Override
  public abstract Object getObjectValue();

  @Override
  public abstract void setObjectValue(Object value) throws FormException;

  @Override
  public abstract boolean acceptObjectValue(Object value);

  @Override
  public abstract boolean isNull();

  @Override
  public abstract void setNull() throws FormException;

  @Override
  public int getOccurrence() {
    return occurrence;
  }
  
  public void setOccurrence(int i) {
    occurrence = i;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

}