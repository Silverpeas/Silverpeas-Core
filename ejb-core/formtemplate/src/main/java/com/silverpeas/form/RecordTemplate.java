package com.silverpeas.form;

/**
 * A RecordTemplate describes DataRecord
 * and gives the field names and type informations.
 *
 * @see DataRecord
 */
public interface RecordTemplate 
{
   /**
    * Returns all the field names of the DataRecord built on this template.
    */
   public String[] getFieldNames();

   /**
    * Returns all the field templates.
    */
   public FieldTemplate[] getFieldTemplates() throws FormException;

   /**
    * Returns the FieldTemplate of the named field.
    *
    * @throw FormException if the field name is unknown.
    */
   public FieldTemplate getFieldTemplate(String fieldName) throws FormException;

   /**
    * Returns the Field index of the named field.
    *
    * @throw FormException if the field name is unknown.
    */
   public int getFieldIndex(String fieldName) throws FormException;

   /**
    * Returns an empty DataRecord built on this template.
    */
   public DataRecord getEmptyRecord() throws FormException;

   /**
    * Returns true if the data record is built on this template
    * and all the constraints are ok.
    */
   public boolean checkDataRecord(DataRecord record);
}
