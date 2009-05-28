package com.stratelia.silverpeas.versioningPeas.control;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class VersioningInitialize implements IInitialize
{
    public VersioningInitialize()
	{
	}

    public boolean Initialize()
    {
        try
		{
				VersioningSchedulerImpl asi = new VersioningSchedulerImpl();
				asi.initialize();
		}
		catch (Exception e)
		{
			SilverTrace.error("VersioningPeas", "VersioningInitialize.Initialize()", "", e);
		}
		return true;
    }
}
