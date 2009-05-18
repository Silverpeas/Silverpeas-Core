package com.silverpeas.pdc.ejb;

import java.util.*;
import javax.ejb.*;
import java.rmi.RemoteException;

import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.Value;

import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;

/**
 * Interface declaration
 *
 * @author neysseri
 */
public interface PdcBm extends EJBObject 
{
	public ArrayList getDaughters(String axisId, String valueId) throws RemoteException;

	public ArrayList getSubAxisValues(String axisId, String valueId) throws RemoteException;

	public ArrayList findGlobalSilverContents(ContainerPositionInterface containerPosition, List componentIds, boolean recursiveSearch, boolean visibilitySensitive) throws RemoteException;

	public ArrayList findGlobalSilverContents(ContainerPositionInterface containerPosition, List componentIds, String authorId, String afterDate, String beforeDate, boolean recursiveSearch, boolean visibilitySensitive) throws RemoteException;

	public int getSilverContentId(String objectId, String componentId) throws RemoteException;

	public ArrayList getPositions(int silverContentId, String componentId) throws RemoteException;

	public Value getValue(String axisId, String valueId) throws RemoteException;

	public List getSilverContentIds(List docFeatures) throws RemoteException;

	public String getInternalContentId(int silverContentId) throws RemoteException;
	
	public AxisHeader getAxisHeader(String axisId) throws RemoteException;

}