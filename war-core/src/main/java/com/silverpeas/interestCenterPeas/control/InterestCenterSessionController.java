package com.silverpeas.interestCenterPeas.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.ejb.RemoveException;

import com.silverpeas.interestCenter.InterestCenterRuntimeException;
import com.silverpeas.interestCenter.ejb.InterestCenterBm;
import com.silverpeas.interestCenter.ejb.InterestCenterBmHome;
import com.silverpeas.interestCenter.model.InterestCenter;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

public class InterestCenterSessionController extends AbstractComponentSessionController
{

	private InterestCenterBm icEjb = null;

	/** Constructor
	 * Creates new InterestCenter Session Controller
	 */

	public InterestCenterSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
	{
		super(
			mainSessionCtrl,
			componentContext,
			"com.silverpeas.interestCenterPeas.multilang.interestCenterBundle",
			"com.silverpeas.interestCenterPeas.settings.interestCenterPeasIcons");
	}

	/** Method initEJB
	 * initializes EJB
	 */
	private void initEJB()
	{
		if (icEjb == null)
		{
			try
			{
				InterestCenterBmHome icEjbHome =
					(InterestCenterBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.INTEREST_CENTER_EJBHOME, InterestCenterBmHome.class);
				icEjb = icEjbHome.create();
			}
			catch (Exception e)
			{
				throw new InterestCenterRuntimeException(
					"InterestCenterSessionController.initEJB()",
					"",
					"root.EX_CANT_GET_REMOTE_OBJECT",
					e);
			}
		}
	}

	/**    Method getICByUserId
	 *
	 *      returns ArrayList of all InterestCenter objects for user given by userId
	 *
	 */
	public ArrayList getICByUserId() throws RemoteException
	{
		initEJB();
		return icEjb.getICByUserID(Integer.parseInt(getUserId()));
	}

	/**     Method   getICByPK
	 *
	 *      returns InterestCenter object by pk
	 *
	 */
	public InterestCenter getICByPK(int pk) throws RemoteException
	{
		initEJB();
		return icEjb.getICByID(pk);
	}

	/**    Method createIC
	 *
	 *     creates new InterestCenter
	 *
	 */
	public void createIC(InterestCenter icToCreate) throws RemoteException
	{
		initEJB();
		icEjb.createIC(icToCreate);
	}

	/**    Method updateIC
	 *
	 *     updates existing InterestCenter
	 *
	 */
	public void updateIC(InterestCenter icToUpdate) throws RemoteException
	{
		initEJB();
		icEjb.updateIC(icToUpdate);
	}

	/**    Method removeICByPKs
	 *
	 *     removes InterestCenter objects corresponding to PKs from given ArrayList
	 *
	 */
	public void removeICByPKs(String[] iDs) throws RemoteException
	{
		initEJB();
		ArrayList pkToRemove = new ArrayList();
		for (int i = 0; i < iDs.length; i++)
		{
			pkToRemove.add(new Integer(iDs[i]));
		}
		icEjb.removeICByPK(pkToRemove);
	}

	/**    Method removeICByPK
	 *
	 *     removes InterestCenter object corresponding to given PK
	 *
	 */
	public void removeICByPK(int pk) throws RemoteException
	{
		initEJB();
		icEjb.removeICByPK(pk);
	}

	public boolean isICExists(String nameIC) throws RemoteException
	{
		ArrayList icList;
		InterestCenter ic;
		icList = getICByUserId();
		Iterator it = icList.iterator();
		while (it.hasNext())
		{
			ic = (InterestCenter) it.next();
			if (nameIC.equals(ic.getName()))
			{
				return true;
			}
		}

		return false;

	}
	
	public void close()
	{
		try
		{
			if (icEjb != null) icEjb.remove();
		}
		catch (RemoteException e)
		{
			SilverTrace.error("interestCenter", "InterestCenterSessionController.close", "", e);			
		}
		catch (RemoveException e)
		{
			SilverTrace.error("interestCenter", "InterestCenterSessionController.close", "", e);
		}
	}
}
