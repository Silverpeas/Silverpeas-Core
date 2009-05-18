package com.silverpeas.form;

import java.io.Serializable;

/**
 * A DataRecord is the interface used by all the form components
 * to exchange, display and save
 * a set of named and typed fields
 * which are unknown at compile time
 * but defined by a silverpeas end user
 * in a workflow process model or a publication model.
 *
 * A DataRecord is modelized by a RecordTemplate
 * giving all the fields names and types.
 *
 * A DataRecord is built, managed and saved by a RecordSet.
 *
 * @see Field
 * @see RecordSet
 * @see RecordTemplate
 */
public interface DataRecord extends Serializable
{
  /**
   * Returns the data record id.
   *
   * This id is unique within the RecordSet from witch this
   * DataRecord has been extracted.
   * 
   * This id is null when the DataRecord
   * has been built from a RecordTemplate
   * but not yet inserted in a recordTemplate.
   */
  public String getId() ;

  /**
   * Gives an id to the data record.
   *
   * This id must be set before the record is inserted in a RecordSet.
   */
  public void setId(String externalId) ;

  /**
   * Return true if this record has not been inserted in a RecordSet.
   */
  public boolean isNew();

  /**
   * Returns the named field.
   *
   * @throw FormException when the fieldName is unknown.
   */
  public Field getField(String fieldName) throws FormException;

  /**
   * Returns the field at the index position in the record.
   *
   * @throw FormException when the fieldIndex is unknown.
   */
  public Field getField(int fieldIndex) throws FormException;
  
  public String[] getFieldNames();
  
  public String getLanguage();
  
  public void setLanguage(String language);

}
