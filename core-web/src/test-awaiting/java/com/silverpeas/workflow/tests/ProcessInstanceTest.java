/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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