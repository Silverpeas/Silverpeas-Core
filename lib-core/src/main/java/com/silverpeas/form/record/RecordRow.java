package com.silverpeas.form.record;

public class RecordRow {
  
  private int recordId;
  private String fieldName;
  private String fieldValue;
  
  public RecordRow(int recordId, String fieldName, String fieldValue) {
    this.recordId = recordId;
    this.fieldName = fieldName;
    this.fieldValue = fieldValue;
  }
  
  public int getRecordId() {
    return recordId;
  }
  public void setRecordId(int recordId) {
    this.recordId = recordId;
  }
  public String getFieldName() {
    return fieldName;
  }
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }
  public String getFieldValue() {
    return fieldValue;
  }
  public void setFieldValue(String fieldValue) {
    this.fieldValue = fieldValue;
  }
  
  

}
