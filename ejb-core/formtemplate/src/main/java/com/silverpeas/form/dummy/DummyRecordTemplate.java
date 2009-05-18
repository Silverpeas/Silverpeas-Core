package com.silverpeas.form.dummy;

import com.silverpeas.form.*;

/**
 * A dummy record template.
 */
public class DummyRecordTemplate implements RecordTemplate
{
   private DataRecord    dataRecord;
   private FieldTemplate fieldTemplate;

   /**
    * A DummyRecordTemplate is empty.
    */
   public DummyRecordTemplate()
   {
	   fieldTemplate = new DummyFieldTemplate();
   }
	
   /**
    * Returns all the field names of the DataRecord built on this template.
    */
   public String[] getFieldNames()
   {
      return new String[0];
   }

   /**
    * Returns all the field templates.
    */
   public FieldTemplate[] getFieldTemplates()
   {
      return new FieldTemplate[0];
   }

   /**
    * Returns the FieldTemplate of the named field.
    */
   public FieldTemplate getFieldTemplate(String fieldName) throws FormException
   {
	   return fieldTemplate;
   }

   /**
    * Returns the field index of the named field.
    *
    * @throw FormException if the field name is unknown.
    */
   public int getFieldIndex(String fieldName) throws FormException
   {
	   return 0;
   }

   /**
    * Returns an empty DataRecord built on this template.
    */
   public DataRecord getEmptyRecord()
      throws FormException
   {
      return dataRecord;
   }

   /**
    * Returns true if the data record is built on this template
    * and all the constraints are ok.
    */
   public boolean checkDataRecord(DataRecord record)
	{
	   return true;
	}

}
