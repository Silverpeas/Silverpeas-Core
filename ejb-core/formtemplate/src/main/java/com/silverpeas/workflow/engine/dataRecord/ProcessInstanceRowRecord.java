package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.Actor;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.user.User;

/**
 * ProcessInstanceDataRecord
 *
 */
public class ProcessInstanceRowRecord implements DataRecord
{
  /**
   * Builds the data record representation of a process instance.
	*/
  public ProcessInstanceRowRecord(ProcessInstance instance,
                                   String role, String lang)
     throws WorkflowException
  {
     this.instance = instance;
	  this.template = (ProcessInstanceRowTemplate)
	     instance.getProcessModel().getRowTemplate(role, lang);
	  this.fields = template.buildFieldsArray();
  }

  /**
   * Returns the data record id.
   */
  public String getId()
  {
     return instance.getInstanceId();
  }

  /**
   * The id of an instance is immutable.
   */
  public void setId(String externalId)
  {
     // do nothing
  }

  /**
   * Returns true if this instance is locked by the workflow engine.
   */
  public boolean isLockedByAdmin()
  {
     return instance.isLockedByAdmin();
  }

  /**
   * Returns error status.
   */
  public boolean isInError()
  {
     return instance.getErrorStatus();
  }

  /**
   * Returns timeout status.
   */
  public boolean isInTimeout()
  {
     return instance.getTimeoutStatus();
  }

  /**
   * Returns true if the given user is a working on this instance.
	*/
  public boolean isWorking(User user)
  {
     if (user == null) return false;

     Actor[] workers = null;
	  try
	  {
	     workers = instance.getWorkingUsers();
	  }
	  catch (WorkflowException e)
	  {
	     return false;
	  }

	  for (int i=0; i<workers.length ; i++)
	  {
	     if (user.equals(workers[i].getUser())) return true;
	  }
	  return false;
  }

  /**
   * An instance is always registred.
   */
  public boolean isNew()
  {
     return true;
  }

  /**
   * Returns the named field.
   *
   * @throw FormException when the fieldName is unknown.
   */
  public Field getField(String fieldName) throws FormException
  {
     return getField(template.getFieldIndex(fieldName));
  }

  /**
   * Returns the field at the index position in the record.
   *
   * @throw FormException when the fieldIndex is unknown.
   */
  public Field getField(int fieldIndex) throws FormException
  {
     Field field = fields[fieldIndex];
	  if (field == null)
	  {
	     ProcessInstanceFieldTemplate fieldTemplate
		     = (ProcessInstanceFieldTemplate)
			    template.getFieldTemplate(fieldIndex);
		  field = fieldTemplate.getField(instance);
		  fields[fieldIndex] = field;
	  }
     return field;
  }
  
  public String[] getFieldNames() {
	  return template.getFieldNames();
  }
  
  public String getLanguage()
  {
	  return null;
  }
  
  public void setLanguage(String lang)
  {
	  //do nothing
  }
  
  public ProcessInstance getFullProcessInstance()
  {
	return instance;  
  }

  /**
   * The process instance whose data are managed by this data record.
	*/
  final ProcessInstance instance;

  /**
   * The record template associated to this data record.
	*/
  final ProcessInstanceRowTemplate template;

  /**
   * The fields.
	*/
  final Field[] fields;

}
