package com.silverpeas.look;

import java.util.List;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.node.model.NodePK;

public interface LookHelper {

	public abstract String getSpaceId();

	public abstract void setSpaceId(String spaceId);

	public abstract String getSubSpaceId();

	public abstract void setSubSpaceId(String subSpaceId);

	public abstract String getComponentId();

	public abstract void setComponentId(String componentId);

	/**
	 * @param spaceId can be id of a space or a subspace
	 */
	public abstract void setSpaceIdAndSubSpaceId(String spaceId);

	public abstract void setComponentIdAndSpaceIds(String spaceId,
			String subSpaceId, String componentId);

	public abstract void init(MainSessionController mainSessionController,
			ResourceLocator resources);

	public abstract String getUserFullName(String userId);

	public abstract String getUserFullName();

	public abstract String getUserId();

	public abstract String getAnonymousUserId();

	public abstract String getLanguage();

	public abstract boolean isAnonymousUser();

	public abstract boolean displayPDCInNavigationFrame();

	public abstract boolean displayPDCFrame();
	
	public abstract boolean displayContextualPDC();

	public abstract boolean displaySpaceIcons();

	public abstract String getSpaceId(String componentId);

	public abstract String getWallPaper(String spaceId);

	public abstract int getNBConnectedUsers();

	public abstract boolean isAnonymousAccess();

	public abstract boolean getSettings(String key);

	public abstract boolean getSettings(String key, boolean defaultValue);

	public abstract String getSettings(String key, String defaultValue);

	public abstract String getString(String key);

	public abstract boolean isBackOfficeVisible();

	/**
	 * @param componentIds - a String like that kmelia12, toolbox35,
	 * @return a List of ComponentInst available to current user 
	 */
	public abstract List getTopItems();

	public abstract List getTopSpaceIds();

	public abstract String getMainFrame();

	public abstract void setMainFrame(String mainFrame);

	public abstract String getSpaceWallPaper();
	
	public abstract String getComponentURL(String componentId);
	
	public abstract String getDate();
	
	public abstract String getDefaultSpaceId();
	
	public abstract List getLatestPublications(String spaceId, int nbPublis);
	
	public abstract List getValidPublications(NodePK nodePK);
}