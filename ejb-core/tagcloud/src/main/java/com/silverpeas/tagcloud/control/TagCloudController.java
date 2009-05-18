package com.silverpeas.tagcloud.control;

import java.rmi.RemoteException;

import javax.ejb.RemoveException;

import com.silverpeas.tagcloud.ejb.TagCloudBm;
import com.silverpeas.tagcloud.ejb.TagCloudBmHome;
import com.silverpeas.tagcloud.ejb.TagCloudRuntimeException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class TagCloudController
{
	
	// Home interface of session bean TagCloudBmEJB.
	private static TagCloudBm tagCloudBm = null;

	public TagCloudController()
	{
	}

	/**
	 * Getter of the home object of TagCloud EJB (initializes it if needed).
	 */
	private static TagCloudBm getTagCloudBm()
	{
		if (tagCloudBm == null)
		{
			try
			{
				TagCloudBmHome tagCloudBmHome = (TagCloudBmHome) EJBUtilitaire.getEJBObjectRef(
					JNDINames.TAGCLOUDBM_EJBHOME, TagCloudBmHome.class);
				tagCloudBm = tagCloudBmHome.create();
			}
			catch (Exception e)
			{
				throw new TagCloudRuntimeException("TagCloudController.initHome()",
					SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
			}
		}
		return tagCloudBm;
	}

	public void close()
	{
		try
		{
			if (getTagCloudBm() != null)
			{
				tagCloudBm.remove();
			}
		}
		catch (RemoteException e)
		{
			SilverTrace.error("tagCloud", "TagCloudController.close", "", e);
		}
		catch (RemoveException e)
		{
			SilverTrace.error("tagCloud", "TagCloudController.close", "", e);
		}
	}
	
}