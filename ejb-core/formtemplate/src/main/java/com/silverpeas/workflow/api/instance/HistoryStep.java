package com.silverpeas.workflow.api.instance;

import java.util.*;
import com.silverpeas.workflow.api.user.*;
import com.silverpeas.workflow.api.*;
import com.silverpeas.form.*;

public interface HistoryStep 
{
   /**
    * @return ProcessInstance
    */
   public ProcessInstance getProcessInstance();

   /**
    * @return the actor
    */
   public User getUser() throws WorkflowException;
   
   /**
    * Get the step id
    * @return	the step id
    */
   public String getId();

   /**
    * Get the role under which the user did the action
    * @return	the role's name
    */
   public String getUserRoleName();

   /**
    * @return the action name
    */
   public String getAction();

   /**
    * @return the action date
	*/
   public Date getActionDate();

   /**
    * @return the resolved state name
    */
   public String getResolvedState();

   /**
    * @return the resulting state name
    */
   public String getResultingState();

   /**
    * @return int
    */
   public int getActionStatus();

   /**
    * Get the data filled at this step
	*/
	public DataRecord getActionRecord() throws WorkflowException;

   /**
    * Set the data filled at this step
	*/
	public void setActionRecord(DataRecord data) throws WorkflowException;

   /**
    * Delete the data filled at this step
	*/
	public void deleteActionRecord() throws WorkflowException;
}
