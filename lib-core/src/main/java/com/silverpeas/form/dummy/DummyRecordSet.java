package com.silverpeas.form.dummy;

import com.silverpeas.form.*;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;

/**
 * A dummy record set.
 */
public class DummyRecordSet implements RecordSet
{
  private DummyRecordTemplate recordTemplate = null;
  
  /**
   * The no paramaters constructor.
	*/
  public DummyRecordSet()
  {
  		this.recordTemplate = new DummyRecordTemplate();
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
   */
  public DataRecord getEmptyRecord() throws FormException
  {
     return recordTemplate.getEmptyRecord();
  }

  /**
   * This dummy record set always return a dummy record.
   */
  public DataRecord getRecord(String recordId) throws FormException
  {
     return recordTemplate.getEmptyRecord();
  }
  
  public DataRecord getRecord(String recordId, String language) throws FormException
  {
     return recordTemplate.getEmptyRecord();
  }

  /**
   * This dummy record set simply do nothing.
   */
  public void insert(DataRecord record) throws FormException
  {
  }

  /**
   * This dummy record set simply do nothing.
   */
  public void update(DataRecord record) throws FormException
  {
  }
 

  /**
   * This dummy record set simply do nothing.
   */
  public void save(DataRecord record) throws FormException
  {
  }

  /**
   * This dummy record set simply do nothing.
   */
  public void delete(DataRecord record) throws FormException
  {
  }

  public void clone(String originalExternalId, String cloneExternalId) throws FormException 
  {
  }

  public void merge(String fromExternalId, String toExternalId) throws FormException 
  {
  }
  
  public void indexRecord(String recordId, String formName, FullIndexEntry indexEntry) throws FormException
  {
  }
}