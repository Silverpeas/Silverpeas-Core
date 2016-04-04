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

import com.silverpeas.workflow.api.ProcessModelManager;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.model.Role;
import junit.framework.TestCase;

/**
 * Test class for ProcessModel
 * @author Ludovic Bertin
 * @version $Revision: 1.2 $ $Date: 2008/05/28 08:40:22 $
**/
public class ProcessModelTest extends TestCase
{

  public ProcessModelTest(String name) {
		super(name);
	}
    public void testProcessModel()
	{
		/*try
		{
			ProcessModelManager modelManager = Workflow.getProcessModelManager();
			/*ProcessModel process = modelManager.getProcessModel("test");

			System.out.println();
			System.out.println("Process name		: " + process.getName());
			System.out.println("Process label		: " + process.getLabel("", ""));
			System.out.println("Process description	: " + process.getDescription("", ""));

			// Roles
			System.out.println("Roles");
			System.out.println("-----");
			Role[] roles = process.getRoles();
			for (int i=0; i<roles.length; i++)
			{
				System.out.println();
				System.out.println("name		: " + roles[i].getName());
				System.out.println("label		: " + roles[i].getLabel("", ""));
				System.out.println("description : " + roles[i].getDescription("", ""));
		}
		catch (WorkflowException e)
		{
			e.printStackTrace();
		}*/
    }
}