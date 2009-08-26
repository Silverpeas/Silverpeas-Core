package com.silverpeas.workflow.tests;

import com.silverpeas.workflow.api.UpdatableProcessInstanceManager;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.engine.instance.ProcessInstanceImpl;
import junit.framework.TestCase;


/**
 * Test class for ProcessInstance
 * @author Ludovic Bertin
 * @version $Revision: 1.2 $ $Date: 2008/05/28 08:40:22 $
 **/
public class ProcessInstanceTest extends TestCase
{
  public ProcessInstanceTest(String name) {
		super(name);
	}

    public void testProcessIntance() throws Exception
	{
		/*try
		{
			UpdatableProcessInstanceManager instanceManager = (UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager();
			ProcessInstanceImpl instance = (ProcessInstanceImpl) instanceManager.createProcessInstance("1");

			ProcessInstanceImpl instance = (ProcessInstanceImpl) instanceManager.getProcessInstance("5");
			Hashtable wkUsers = instance.castor_getWorkingUsers();

			System.out.println("begin");
			for (Enumeration e = wkUsers.keys() ; e.hasMoreElements() ;)
			{
				Object wkUser = e.nextElement();
				System.out.println(wkUser + " - " + wkUsers.get(wkUser));
			}
			System.out.println("end");
*/
/*			HistoryStepImpl step = new HistoryStepImpl();
			step.setUserId("131");
			step.setAction("creation");
			step.setResultingState("Validation");
			step.setActionStatus(0);

			UserDetail ud = new UserDetail();
			ud.setId("12");
			ud.setFirstName("Ludo");
			ud.setLastName("Bertin");

			UserImpl user = new UserImpl(ud);

			instance.addWorkingUser(user, "validation", "valideur");

			TextItem item = new TextItem();
			item.setName("Bureau");
			item.setValue("S301");
			step.putItem(item);
			instance.addHistoryStep( (HistoryStep) step);
			step.setActionStatus(1);
			instance.updateHistoryStep( (HistoryStep) step);
		}
		catch (WorkflowException e)
		{
			e.printStackTrace();
		}*/
    }
}