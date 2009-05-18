package com.stratelia.silverpeas.pdcPeas.control;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.silverpeas.thesaurus.model.Jargon;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class PdcClassifySessionController extends AbstractComponentSessionController
{
	private int		currentSilverObjectId	= -1;
	private List	currentSilverObjectIds	= null;
	private String	currentComponentId		= null;
	private String	currentComponentLabel	= null;
	private String	currentComponentName	= null;
	private String	currentSpaceLabel		= null;
	private PdcBm	pdcBm					= null;
	private boolean sendSubscriptions		= true;

	private ThesaurusManager thesaurus = new ThesaurusManager();
	
	//jargon utilisé par l'utilisateur
	private Jargon	jargon = null;


	public PdcClassifySessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext, String multilangBundle, String iconBundle)
	{
		super(mainSessionCtrl, componentContext, multilangBundle, iconBundle);
	}

	private PdcBm getPdcBm() {
		if (pdcBm == null) {
			pdcBm = (PdcBm) new PdcBmImpl();
		}
		return pdcBm;
	}

	public void setCurrentSilverObjectId(String silverObjectId) {
		currentSilverObjectId = new Integer(silverObjectId).intValue();
	}
	
	public void setCurrentSilverObjectId(int silverObjectId) {
		currentSilverObjectId = silverObjectId;
	}

	public int getCurrentSilverObjectId() throws PdcException {
		return currentSilverObjectId;
	}
	
	public void addCurrentSilverObjectId(String silverObjectId) {
		if (currentSilverObjectIds == null)
			currentSilverObjectIds = new ArrayList();
		currentSilverObjectIds.add(silverObjectId);
	}
	
	public List getCurrentSilverObjectIds() throws PdcException {
		return currentSilverObjectIds;
	}
	
	public void clearCurrentSilverObjectIds()
	{
		if (currentSilverObjectIds != null)
			currentSilverObjectIds.clear();
	}

	public void setCurrentComponentId(String componentId) {
		OrganizationController	orga			= getOrganizationController();
		ComponentInst			componentInst	= orga.getComponentInst(componentId);
		String					currentSpaceId	= componentInst.getDomainFatherId();
		SpaceInst				spaceInst		= orga.getSpaceInstById(currentSpaceId);
		currentComponentId		= componentId;
		currentComponentLabel	= componentInst.getLabel();
		currentComponentName	= componentInst.getName();
		currentSpaceLabel		= spaceInst.getName();
	}

	public String getComponentLabel() {
		return this.currentComponentLabel;
	}

	public String getSpaceLabel() {
		return this.currentSpaceLabel;
	}

	public String getCurrentComponentId() {
		return currentComponentId;
	}

	public String getCurrentComponentName() {
		return currentComponentName;
	}

	public List getUsedAxisToClassify() throws PdcException {
		return getPdcBm().getUsedAxisToClassify(getCurrentComponentId(), getCurrentSilverObjectId());
	}

	public int addPosition(ClassifyPosition position) throws PdcException {
		int result = -1;
		if (getCurrentSilverObjectId() != -1)
		{
			//classical classification = addPosition to one object
			result = getPdcBm().addPosition(getCurrentSilverObjectId(), position, getCurrentComponentId(), isSendSubscriptions());
		}
		else if (getCurrentSilverObjectIds() != null)
		{
			String silverObjectId = null;
			for (int i=0; i<getCurrentSilverObjectIds().size(); i++)
			{
				silverObjectId = (String) getCurrentSilverObjectIds().get(i);
				getPdcBm().addPosition(Integer.parseInt(silverObjectId), position, getCurrentComponentId(), isSendSubscriptions());
			}
		}
		return result;
	}

	public int updatePosition(ClassifyPosition position) throws PdcException {
		return getPdcBm().updatePosition(position,getCurrentComponentId(),getCurrentSilverObjectId(), isSendSubscriptions());
	}

	public void deletePosition(int positionId) throws PdcException {
		getPdcBm().deletePosition(positionId, getCurrentComponentId());
	}

	public void deletePosition(String positionId) throws PdcException {
		deletePosition(new Integer(positionId).intValue());
	}

	public List getPositions() throws PdcException {
		return getPdcBm().getPositions(getCurrentSilverObjectId(), getCurrentComponentId());
	}

	public List getUsedAxis() throws PdcException {
		return getPdcBm().getUsedAxisByInstanceId(getCurrentComponentId());
	}

	public synchronized boolean getActiveThesaurus() throws PdcException, RemoteException
	{
		try {
			return getPersonalization().getThesaurusStatus();
		} catch (NoSuchObjectException nsoe)
		{
			initPersonalization();
			return getPersonalization().getThesaurusStatus();
		}
		catch (Exception e)
		{
			throw new PdcException("PdcClassifySessionController.getActiveThesaurus()",SilverpeasException.ERROR,"pdcPeas.EX_CANT_GET_ACTIVE_THESAURUS","",e);
		}
	}

	public void initializeJargon() throws PdcException
	{
		try {
			Jargon theJargon = thesaurus.getJargon(getUserId());
			this.jargon = theJargon;
		}
		catch ( ThesaurusException e )
		{
			throw new PdcException("PdcClassifySessionController.initializeJargon",SilverpeasException.ERROR,"pdcPeas.EX_CANT_INITIALIZE_JARGON","",e);
		}
	}

	public Jargon getJargon()
	{
			return this.jargon;
	}

	public boolean isSendSubscriptions() {
		return sendSubscriptions;
	}

	public void setSendSubscriptions(boolean sendSubscriptions) {
		this.sendSubscriptions = sendSubscriptions;
	}

}