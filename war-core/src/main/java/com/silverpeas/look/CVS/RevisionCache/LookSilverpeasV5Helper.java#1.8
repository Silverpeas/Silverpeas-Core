package com.silverpeas.look;

import java.io.File;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.ejb.EJBException;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.kmelia.KmeliaTransversal;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class LookSilverpeasV5Helper implements LookHelper {

	private OrganizationController 	orga 		= null;
	private ResourceLocator 		resources 	= null;
	private ResourceLocator			messages	= null;
	private ResourceLocator			defaultMessages	= null;
	private MainSessionController	mainSC		= null;
		
	private String 			userId 			= null;
	private String			guestId			= null;
	
	private boolean	displayPDCInNav = false;
	private boolean	displayPDCFrame = false;
	private boolean displayContextualPDC = true;
	private boolean displaySpaceIcons = true;
	private boolean displayConnectedUsers = true;
	
	private List	topItems 	= null;
	private List	topSpaceIds	= null;	//sublist of topItems
	
	private String mainPage 	= "Main.jsp";
	private String mainFrame 	= "MainFrameSilverpeasV5.jsp";
	
	private String spaceId 		= null;
	private String subSpaceId	= null;
	private String componentId 	= null;
	
	private SimpleDateFormat formatter = null;
	private KmeliaTransversal kmeliaTransversal = null;
	
	private PublicationBm 	publicationBm 	= null;
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getSpaceId()
	 */
	public String getSpaceId() {
		return spaceId;
	}

	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#setSpaceId(java.lang.String)
	 */
	public void setSpaceId(String spaceId) {
		if (!spaceId.startsWith("WA"))
			spaceId = "WA"+spaceId;
		this.spaceId = spaceId;
	}

	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getSubSpaceId()
	 */
	public String getSubSpaceId() {
		return subSpaceId;
	}

	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#setSubSpaceId(java.lang.String)
	 */
	public void setSubSpaceId(String subSpaceId) {
		if (!subSpaceId.startsWith("WA"))
			subSpaceId = "WA"+subSpaceId;
		this.subSpaceId = subSpaceId;
	}

	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getComponentId()
	 */
	public String getComponentId() {
		return componentId;
	}

	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#setComponentId(java.lang.String)
	 */
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#setSpaceIdAndSubSpaceId(java.lang.String)
	 */
	public void setSpaceIdAndSubSpaceId(String spaceId)
	{
		if (StringUtil.isDefined(spaceId))
		{
			List spacePath = orga.getSpacePath(spaceId);
			
			SpaceInst space 	= (SpaceInst) spacePath.get(0);
			SpaceInst subSpace 	= (SpaceInst) spacePath.get(spacePath.size()-1);
			setSpaceId(space.getId());
			setSubSpaceId(subSpace.getId());
			setComponentId(null);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#setComponentIdAndSpaceIds(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void setComponentIdAndSpaceIds(String spaceId, String subSpaceId, String componentId)
	{
		setComponentId(componentId);
		
		if (!StringUtil.isDefined(spaceId))
		{
			List spacePath = orga.getSpacePathToComponent(componentId);
			SpaceInst space = (SpaceInst) spacePath.get(0);
			SpaceInst subSpace = (SpaceInst) spacePath.get(spacePath.size()-1);
			setSpaceId(space.getId());
			setSubSpaceId(subSpace.getId());
		}
		else
		{
			setSpaceId(spaceId);
			setSubSpaceId(subSpaceId);
		}
	}

	public LookSilverpeasV5Helper(MainSessionController mainSessionController, ResourceLocator resources) {
		init(mainSessionController, resources);
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#init(com.stratelia.silverpeas.peasCore.MainSessionController, com.stratelia.webactiv.util.ResourceLocator, com.stratelia.webactiv.util.ResourceLocator)
	 */
	public void init(MainSessionController mainSessionController, ResourceLocator resources)
	{
		this.mainSC		= mainSessionController;
		this.orga 		= mainSessionController.getOrganizationController();
		this.userId 	= mainSessionController.getUserId();
		this.resources 	= resources;
		this.defaultMessages = new ResourceLocator("com.silverpeas.lookSilverpeasV5.multilang.lookBundle", mainSessionController.getFavoriteLanguage());
		if (StringUtil.isDefined(resources.getString("MessageBundle")))
			this.messages = new ResourceLocator(resources.getString("MessageBundle"), mainSessionController.getFavoriteLanguage());
		initProperties();
		getTopItems();
	}
		
	private void initProperties()
	{
		this.guestId			= resources.getString("guestId");
		
		displayPDCInNav			= resources.getBoolean("displayPDCInNav", false);
		displayPDCFrame			= resources.getBoolean("displayPDCFrame", false);
		displayContextualPDC	= resources.getBoolean("displayContextualPDC", true);
		displaySpaceIcons		= resources.getBoolean("displaySpaceIcons", true);
		displayConnectedUsers	= resources.getBoolean("displayConnectedUsers", true);
	}
	
	protected MainSessionController getMainSessionController()
	{
		return mainSC;
	}
	
	protected OrganizationController getOrganizationController()
	{
		return orga;
	}
		
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getUserFullName(java.lang.String)
	 */
	public String getUserFullName(String userId)
	{
		return orga.getUserDetail(userId).getDisplayedName();
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getUserFullName()
	 */
	public String getUserFullName()
	{
		return orga.getUserDetail(userId).getDisplayedName();
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getUserId()
	 */
	public String getUserId()
	{
		return userId;
	}
		
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getAnonymousUserId()
	 */
	public String getAnonymousUserId()
	{
		return guestId;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getLanguage()
	 */
	public String getLanguage()
	{
		return mainSC.getFavoriteLanguage();
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#isAnonymousUser()
	 */
	public boolean isAnonymousUser()
	{
		if (StringUtil.isDefined(userId) && StringUtil.isDefined(guestId))
			return userId.equals(guestId);
		else
			return false;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#displayPDCInNavigationFrame()
	 */
	public boolean displayPDCInNavigationFrame() {
		return displayPDCInNav;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#displayPDCFrame()
	 */
	public boolean displayPDCFrame() {
		return displayPDCFrame;
	}
	
	public boolean displayContextualPDC() {
		return displayContextualPDC;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#displaySpaceIcons()
	 */
	public boolean displaySpaceIcons() {
		return displaySpaceIcons;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getSpaceId(java.lang.String)
	 */
	public String getSpaceId(String componentId)
	{
		ComponentInstLight component = orga.getComponentInstLight(componentId);
		if (component != null)
		{
			return component.getDomainFatherId();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getWallPaper(java.lang.String)
	 */
	public String getWallPaper(String spaceId)
	{
		if (!StringUtil.isDefined(spaceId))
			return "0";
		
		String path = FileRepositoryManager.getAbsolutePath("Space"+spaceId.substring(2), new String[]{"look"});
		
		File file = new File(path+"wallPaper.jpg");
		if (file.isFile())
			return "1";
		else
		{
			file = new File(path+"wallPaper.gif");
			if (file.isFile())
				return "1";
		}
		
		return "0";
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getNBConnectedUsers()
	 */
	public int getNBConnectedUsers()
	{
		int nbConnectedUsers = 0;
		if (displayConnectedUsers)
		{
			//Remove the current user
			nbConnectedUsers = SessionManager.getInstance().getNbConnectedUsersList() - 1;
		}
		return nbConnectedUsers;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#isAnonymousAccess()
	 */
	public boolean isAnonymousAccess()
	{
		return (StringUtil.isDefined(guestId) && guestId.equals(userId));
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getSettings(java.lang.String)
	 */
	public boolean getSettings(String key)
	{
		return SilverpeasSettings.readBoolean(resources, key, false);
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getSettings(java.lang.String, boolean)
	 */
	public boolean getSettings(String key, boolean defaultValue)
	{
		return SilverpeasSettings.readBoolean(resources, key, defaultValue);
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getSettings(java.lang.String, java.lang.String)
	 */
	public String getSettings(String key, String defaultValue)
	{
		return SilverpeasSettings.readString(resources, key, defaultValue);
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getString(java.lang.String)
	 */
	public String getString(String key)
	{
		if (key.startsWith("lookSilverpeasV5"))
			return SilverpeasSettings.readString(defaultMessages, key, "");
		else
			return SilverpeasSettings.readString(messages, key, "");
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#isBackOfficeVisible()
	 */
	public boolean isBackOfficeVisible()
	{
		return mainSC.isBackOfficeVisible();
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getTopItems()
	 */
	public List getTopItems()
	{
		if (topItems == null)
		{
			topItems	= new ArrayList();
			topSpaceIds	= new ArrayList();
			
			StringTokenizer tokenizer = new StringTokenizer(resources.getString("componentsTop"), ",");
			
			String 				itemId 		= null;
			ComponentInstLight 	component 	= null;
			SpaceInstLight		space		= null;
			String				spaceId		= null;
			while (tokenizer.hasMoreTokens())
			{
				itemId = tokenizer.nextToken();
				
				if (itemId.startsWith("WA"))
				{
					if (orga.isSpaceAvailable(itemId, userId))
					{
						space = orga.getSpaceInstLightById(itemId);
						
						SpaceInstLight rootSpace = orga.getRootSpace(itemId);
						
						TopItem item = new TopItem();
						item.setLabel(space.getName(getLanguage()));
						item.setSpaceId(rootSpace.getFullId());
						item.setSubSpaceId(itemId);
						
						topItems.add(item);
						topSpaceIds.add(item.getSpaceId());
					}
				}
				else
				{
					if (orga.isComponentAvailable(itemId, userId))
					{
						component 	= orga.getComponentInstLight(itemId);
						spaceId		= component.getDomainFatherId();
						
						SpaceInstLight rootSpace = orga.getRootSpace(spaceId);
						
						TopItem item = new TopItem();
						item.setLabel(component.getLabel(getLanguage()));
						item.setComponentId(itemId);
						item.setSpaceId(rootSpace.getFullId());
						item.setSubSpaceId(spaceId);
						
						topItems.add(item);
					}
				}
			}
		}
		return topItems;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getTopSpaceIds()
	 */
	public List getTopSpaceIds()
	{
		return topSpaceIds;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getMainFrame()
	 */
	public String getMainFrame() {
		return mainFrame;
	}

	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#setMainFrame(java.lang.String)
	 */
	public void setMainFrame(String mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.look.LookHelper#getSpaceWallPaper()
	 */
	public String getSpaceWallPaper()
	{
		if (!StringUtil.isDefined(getSpaceId()))
			return null;
		
		String path = FileRepositoryManager.getAbsolutePath("Space"+getSpaceId().substring(2), new String[]{"look"});
		
		File file = new File(path+"wallPaper.jpg");
		if (file.isFile())
			return FileServerUtils.getOnlineURL("Space"+getSpaceId().substring(2), file.getName(), file.getName(), "image/jpeg", "look");
		else
		{
			file = new File(path+"wallPaper.gif");
			if (file.isFile())
				return FileServerUtils.getOnlineURL("Space"+getSpaceId().substring(2), file.getName(), file.getName(), "image/gif", "look");
		}
		
		return null;
	}
	
	public String getComponentURL(String key, String function)
	{
		String componentId = resources.getString(key);
		if (!StringUtil.isDefined(function))
			function = "Main";
		return URLManager.getApplicationURL()+URLManager.getURL("useless", componentId)+function;
	}
	
	public String getComponentURL(String key)
	{
		return getComponentURL(key, "Main");
	}
	
	public String getDate()
	{
		if (formatter == null)
			formatter = new SimpleDateFormat(resources.getString("DateFormat", "dd/MM/yyyy"), new Locale(mainSC.getFavoriteLanguage()));
		
		return formatter.format(new Date());
	}
	
	public String getDefaultSpaceId()
	{
		String defaultSpaceId = resources.getString("DefaultSpaceId");
		if (!StringUtil.isDefined(defaultSpaceId))
			defaultSpaceId = mainSC.getFavoriteSpace();
		
		return defaultSpaceId;
	}
	
	private KmeliaTransversal getKmeliaTransversal()
	{
		if (kmeliaTransversal == null)
			kmeliaTransversal = new KmeliaTransversal(mainSC);
		
		return kmeliaTransversal;
	}
	
	public List getLatestPublications(String spaceId, int nbPublis)
	{
		return getKmeliaTransversal().getPublications(spaceId, nbPublis);
	}
	
	public List getValidPublications(NodePK nodePK)
	{
		List publis = null;
		try {
			publis = (List) getPublicationBm().getDetailsByFatherPK(nodePK, null, true);
		} catch (RemoteException e) {
			SilverTrace.error("lookSilverpeasV5", "LookSilverpeasV5Helper.getPublications", "root.MSG_GEN_PARAM_VALUE", e);
		}
		List filteredPublis = new ArrayList();
		PublicationDetail publi;
		for (int i=0; publis!=null && i<publis.size(); i++)
		{
			publi = (PublicationDetail) publis.get(i);
			if (publi.getStatus().equalsIgnoreCase(PublicationDetail.VALID))
				filteredPublis.add(publi);
		}
		return filteredPublis;
	}
	
	public PublicationBm getPublicationBm() 
	{
		if (publicationBm == null) {
			try {
				publicationBm = ((PublicationBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class)).create();
			} catch (Exception e) {
				throw new EJBException(e);
			}
	    }
	    return publicationBm;
	}
}