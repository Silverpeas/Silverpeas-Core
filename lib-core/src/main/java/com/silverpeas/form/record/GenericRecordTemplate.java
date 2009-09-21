package com.silverpeas.form.record;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordTemplate;

/**
 * A GenericRecordTemplate builds GenericDataRecord.
 * It use a map :
 *   Map (FieldName -> (index,GenericFieldTemplate))
 */
public class GenericRecordTemplate implements RecordTemplate, Serializable
{
   private Map fields = new HashMap();
   private ArrayList fieldList = new ArrayList();
   private String templateName;

/**
    * A GenericRecordTemplate is built empty :
    * use addFieldTemplate for each field.
    * 
    * @see addFieldTemplate
    */
   public GenericRecordTemplate()
   {
   }
	
	public ArrayList getFieldList()
	{
		return fieldList;
	}

	public void setFieldList(ArrayList fieldList)
	{
		this.fieldList = fieldList;
	}

	public Map getFields()
	{
	   if (fields==null || fields.size()==0)
	   {
		   Iterator fieldsIter = fieldList.iterator();

		   while (fieldsIter.hasNext())
		   {
				GenericFieldTemplate field = (GenericFieldTemplate) fieldsIter.next();
				field.setTemplateName(templateName);
				addFieldTemplate((FieldTemplate) field);
		   }
	   }

	   return fields;
	}

   /**
    * Adds a new field template at the end of this record template.
    */
   public void addFieldTemplate(FieldTemplate fieldTemplate)
   {
      IndexedFieldTemplate indexed = new IndexedFieldTemplate(fields.size(), fieldTemplate);
      fields.put(fieldTemplate.getFieldName(), indexed);
   }

   /**
    * Returns all the field names of the DataRecord built on this template.
    */
   public String[] getFieldNames()
   {
      return (String[]) getFields().keySet().toArray(new String[0]);
   }

   /**
    * Returns all the field templates.
    */
   public FieldTemplate[] getFieldTemplates()
   {
		FieldTemplate[] fieldsArray = new FieldTemplate[getFields().keySet().size()];
		Iterator        fieldsEnum = getFields().values().iterator();

		while (fieldsEnum.hasNext())
		{
		   IndexedFieldTemplate field = (IndexedFieldTemplate) fieldsEnum.next();
         fieldsArray[field.index] = field.fieldTemplate;
		}
		
		return fieldsArray;
   }

   /**
    * Returns the FieldTemplate of the named field.
    *
    * @throw FormException if the field name is unknown.
    */
   public FieldTemplate getFieldTemplate(String fieldName) throws FormException
   {
      IndexedFieldTemplate indexed =(IndexedFieldTemplate) getFields().get(fieldName);

      if (indexed == null)
      {
         throw new FormException("GenericRecordTemplate",
                                 "form.EXP_UNKNOWN_FIELD",
                                 fieldName);
      }

      return indexed.fieldTemplate;
   }

   /**
    * Returns the field index of the named field.
    *
    * @throw FormException if the field name is unknown.
    */
   public int getFieldIndex(String fieldName) throws FormException
   {
      IndexedFieldTemplate indexed =(IndexedFieldTemplate) getFields().get(fieldName);

      if (indexed == null)
      {
         throw new FormException("GenericRecordTemplate",
                                 "form.EXP_UNKNOWN_FIELD",
                                 fieldName);
      }

      return indexed.index;
   }

   /**
    * Returns an empty DataRecord built on this template.
    */
   public DataRecord getEmptyRecord()
      throws FormException
   {
      return new GenericDataRecord(this);
   }

   /**
    * Returns true if the data record is built on this template
    * and all the constraints are ok.
    */
   public boolean checkDataRecord(DataRecord record)
	{
	   return true; // xoxox  à implémenter
	}
   
   	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

}

final class IndexedFieldTemplate implements Serializable
{
   public final int index;
   public final FieldTemplate fieldTemplate;

   public IndexedFieldTemplate(int index, FieldTemplate fieldTemplate)
   {
      this.index = index;
      this.fieldTemplate = fieldTemplate;
   }
}
