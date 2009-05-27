package com.silverpeas.workflow.tests;

import com.silverpeas.workflow.api.ProcessModelManager;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.model.Role;

/**
 * Test class for ProcessModel
 * @author Ludovic Bertin
 * @version $Revision: 1.2 $ $Date: 2008/05/28 08:40:22 $
**/
public class ProcessModelTest
{
    public static void main(String[] args)
	{
		try
		{
			ProcessModelManager modelManager = Workflow.getProcessModelManager();
			ProcessModel process = modelManager.getProcessModel("test");

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
		}
		catch (WorkflowException e)
		{
			e.printStackTrace();
		}
    }
}