package com.silverpeas.workflow.engine.dataRecord;

import java.util.HashMap;
import java.util.Map;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;

public class AbstractProcessInstanceDataRecord implements DataRecord {

  private static final long serialVersionUID = 1L;

  @Override
  public String getId() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * The id of an instance is immutable.
   */
  @Override
  public void setId(String externalId) {
    // do nothing
  }

  /**
   * An instance is always registred.
   */
  @Override
  public boolean isNew() {
    return true;
  }

  @Override
  public Field getField(String fieldName) throws FormException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Field getField(String fieldName, int occurrence) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Field getField(int fieldIndex) throws FormException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getFieldNames() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getLanguage() {
    return null;
  }

  @Override
  public void setLanguage(String lang) {
    // do nothing
  }

  @Override
  public Map<String, String> getValues(String language) {
    // no implemented yet !
    return new HashMap<String, String>();
  }

}
