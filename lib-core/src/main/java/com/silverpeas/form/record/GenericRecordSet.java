package com.silverpeas.form.record;


import java.io.Serializable;
import java.util.List;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.TypeManager;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;

/**
 * The GenericRecordSet manage DataRecord built on
 * a RecordTemplate and saved by the GenericRecordSetManager.
 *
 * @see DataRecord
 */
public class GenericRecordSet implements RecordSet, Serializable
{
  private IdentifiedRecordTemplate recordTemplate = null;
  
  /**
   * The generic record set is built upon a RecordTemplate.
	*/
  public GenericRecordSet(IdentifiedRecordTemplate recordTemplate)
  {
  		this.recordTemplate = recordTemplate;
  }

  /**
   * Returns the RecordTemplate shared by all the DataRecord of this RecordSet.
   */
  public RecordTemplate getRecordTemplate()
  {
     return recordTemplate;
  }

  /**
   * Returns an empty DataRecord built on the RecordTemplate.
   *
   * This record is not yet managed by this RecordSet.
   * This is only an empty record which must be filled
   * and saved in order to become a DataRecord of this RecordSet.
   */
  public DataRecord getEmptyRecord() throws FormException
  {
     return recordTemplate.getEmptyRecord();
  }

  /**
   * Returns the DataRecord with the given id.
   *
   * @throw FormException when the id is unknown.
   */
  public DataRecord getRecord(String recordId) throws FormException
  {
     return GenericRecordSetManager.getRecord(recordTemplate, recordId);
  }
  
  /**
   * Returns the DataRecord with the given id.
   *
   * @throw FormException when the id is unknown.
   */
  public DataRecord getRecord(String recordId, String language) throws FormException
  {
	  if (!I18NHelper.isI18N || I18NHelper.isDefaultLanguage(language))
		  language = null;
     return GenericRecordSetManager.getRecord(recordTemplate, recordId, language);
  }

  /**
   * Inserts the given DataRecord and set its id.
   *
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has a not null id.
   * @throw FormException when the insert fail.
   */
  private void insert(DataRecord record) throws FormException
  {
     recordTemplate.checkDataRecord(record);
     GenericRecordSetManager.insertRecord(recordTemplate, record);
  }


  /**
   * Updates the given DataRecord.
   *
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has a null or unknown id.
   * @throw FormException when the update fail.
   */
  private void update(DataRecord record) throws FormException
  {
     recordTemplate.checkDataRecord(record);
     GenericRecordSetManager.updateRecord(recordTemplate, record);
  }
 

  /**
   * Save the given DataRecord.
   *
   * If the record id is null then the record is inserted in this RecordSet.
   * Else the record is updated.
   *
   * @see insert
   * @see update
   *
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has an unknown id.
   * @throw FormException when the insert or update fail.
   */ 
  public void save(DataRecord record) throws FormException
  {
     if (record.isNew()) insert(record);
	  else                update(record);
  }
  
  private void indexRecord(String recordId, String formName, FullIndexEntry indexEntry, String language) throws FormException
  {
	  SilverTrace.info("form", "GenericRecordSet.index()", "root.MSG_GEN_ENTER_METHOD", "recordId = "+recordId+", language = "+language);
	  DataRecord data = getRecord(recordId, language);
	  if (data != null)
	  {
		  String[] 		fieldNames 		= data.getFieldNames();
			String 			fieldName 		= null;

			Field 			field 			= null; 
			for (int f=0; f<fieldNames.length; f++)
			{
				fieldName = fieldNames[f];
				field = data.getField(fieldName);
				if (field != null)
				{
          FieldTemplate fieldTemplate = recordTemplate.getFieldTemplate(fieldName);
          if (fieldTemplate != null)
			  	{
            String fieldType 			= fieldTemplate.getTypeName();
				  	String fieldDisplayerName 	= fieldTemplate.getDisplayerName();
				  	try
				  	{
              if (fieldDisplayerName == null || fieldDisplayerName.equals(""))
                fieldDisplayerName = TypeManager.getDisplayerName(fieldType);
					  	FieldDisplayer fieldDisplayer = TypeManager.getDisplayer(fieldType, fieldDisplayerName);
					  	if (fieldDisplayer != null)
					  	{
                String key = formName+"$$"+fieldName;
                fieldDisplayer.index(indexEntry, key, fieldName, field, language);
              }
				  	}
				  	catch (FormException fe)
				  	{
						SilverTrace.error("form", "AbstractForm.update", "form.EXP_UNKNOWN_FIELD", null, fe);
				  	}
				  	catch (Exception e)
				  	{
						SilverTrace.error("form", "AbstractForm.update", "form.EXP_UNKNOWN_FIELD", null, e);
				  	}
			  	}					
				}
			}
		}
  }
    
  public void indexRecord(String recordId, String formName, FullIndexEntry indexEntry) throws FormException
  {
	  if (!I18NHelper.isI18N)
		  indexRecord(recordId, formName, indexEntry, null);
	  else
	  {
		  List<String> languages = GenericRecordSetManager.getLanguagesOfRecord(recordTemplate, recordId);
		  for (int l=0; l<languages.size(); l++)
		  {
			  indexRecord(recordId, formName, indexEntry, languages.get(l)); 
		  }
	  }
  }
  
  /**
   * Deletes the given DataRecord and set to null its id.
   *
   * @throw FormException when the record doesn't have the required template.
   * @throw FormException when the record has an unknown id.
   * @throw FormException when the delete fail.
   */
  public void delete(DataRecord record) throws FormException
  {
     if (record != null)
     {
	  	  GenericRecordSetManager.deleteRecord(recordTemplate, record);
	  }
  }
  
  public void clone(String originalExternalId, String cloneExternalId) throws FormException
  {
	  GenericDataRecord record = (GenericDataRecord) getRecord(originalExternalId);
	  record.setInternalId(-1);
	  record.setId(cloneExternalId);
	  insert(record);
  }
  
  public void merge(String fromExternalId, String toExternalId) throws FormException
  {
	  GenericDataRecord fromRecord = (GenericDataRecord) getRecord(fromExternalId);
	  GenericDataRecord toRecord = (GenericDataRecord) getRecord(toExternalId);
	  
	  fromRecord.setInternalId(toRecord.getInternalId());
	  fromRecord.setId(toExternalId);
	  update(fromRecord);
  }

}