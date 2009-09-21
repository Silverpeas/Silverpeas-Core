package com.silverpeas.form.record;

import java.io.Serializable;

import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;

/**
 * An Identified RecordTemplate
 * adds a database id and an external id
 * to a RecordTemplate.
 */
public class IdentifiedRecordTemplate implements RecordTemplate, Serializable
{
	private int            id = -1;
	private String         externalId = null;
	private RecordTemplate wrappedTemplate = null;
	private String		   templateName		= null;

   /**
    * A IdentifiedRecordTemplate is built upon a wrapped template.
    */
   public IdentifiedRecordTemplate(RecordTemplate wrappedTemplate)
   {
	   this.wrappedTemplate = wrappedTemplate;
   }

   /**
    * Returns the wrapped template.
    */
   public RecordTemplate getWrappedTemplate()
   {
      return wrappedTemplate;
   }

   /**
    * Returns all the field names of the DataRecord built on this template.
    */
   public String[] getFieldNames()
   {
      return wrappedTemplate.getFieldNames();
   }

   /**
    * Returns all the field templates.
    */
   public FieldTemplate[] getFieldTemplates() throws FormException
   {
      return wrappedTemplate.getFieldTemplates();
   }

   /**
    * Returns the FieldTemplate of the named field.
    *
    * @throw FormException if the field name is unknown.
    */
   public FieldTemplate getFieldTemplate(String fieldName) throws FormException
   {
      return wrappedTemplate.getFieldTemplate(fieldName);
   }

   /**
    * Returns the field index of the named field.
    *
    * @throw FormException if the field name is unknown.
    */
   public int getFieldIndex(String fieldName) throws FormException
   {
      return wrappedTemplate.getFieldIndex(fieldName);
   }

   /**
    * Returns an empty DataRecord built on this template.
    */
   public DataRecord getEmptyRecord()
      throws FormException
   {
      return wrappedTemplate.getEmptyRecord();
   }

   /**
    * Returns true if the data record is built on this template
    * and all the constraints are ok.
    */
   public boolean checkDataRecord(DataRecord record)
	{
      return wrappedTemplate.checkDataRecord(record);
	}

   /**
    * Returns the external template id.
    */
   public String getExternalId()
   {
      return externalId;
   }

   /**
    * Gives an external id to the template.
    */
   public void setExternalId(String externalId)
   {
      if (this.externalId == null) this.externalId = externalId;
   }

   /**
    * Gets the internal id.
    */
   public int getInternalId()
   {
	   return id;
   }

   /**
    * Sets the internal id.
    */
   public void setInternalId(int id)
   {
	   if (this.id == -1) this.id = id;
   }

   public String getTemplateName()
   {
      return templateName;
   }

   public void setTemplateName(String templateName)
   {
		this.templateName = templateName;
   }
}