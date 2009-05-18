package com.stratelia.webactiv.util.attachment.control;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class AttachmentInitialize implements IInitialize
{
    public AttachmentInitialize()
	{
	}

    public boolean Initialize()
    {
        try
		{
				AttachmentSchedulerImpl asi = new AttachmentSchedulerImpl();
				asi.initialize();
				
				// pour les réservations de fichiers
				ScheduledReservedFile rf = new ScheduledReservedFile();
				rf.initialize();
		}
		catch (Exception e)
		{
			SilverTrace.error("Attachment", "AttachmentInitialize.Initialize()", "", e);
		}
		return true;
    }
}
