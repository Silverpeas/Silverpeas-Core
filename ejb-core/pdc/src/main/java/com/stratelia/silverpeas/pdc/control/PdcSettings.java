package com.stratelia.silverpeas.pdc.control;

import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.ResourceLocator;

public class PdcSettings {

	public static boolean delegationEnabled = false;
	
	static
	{
		ResourceLocator resource = new ResourceLocator("com.silverpeas.pdc.pdc", "");
		
		delegationEnabled = SilverpeasSettings.readBoolean(resource, "EnableDelegation", false);
	}
}
