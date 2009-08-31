package com.stratelia.silverpeas.selectionPeas;

import com.stratelia.silverpeas.selection.SelectionExtraParams;
import com.stratelia.webactiv.util.ResourceLocator;

public class BrowseJdbcPanel extends BrowsePanelProvider
{

	public BrowseJdbcPanel(String language, ResourceLocator rs, CacheManager cm, SelectionExtraParams sep)
	{
		super(language, rs, cm, CacheManager.CM_ELEMENT);
		init(sep.getParameter("tableName"));
	}
	
	private void init(String pageName)
	{
		m_PageName = pageName;
		setSelectMiniFilter(m_Cm.getSelectMiniFilter(m_what));
		refresh(null);
	}

	public void setNewParentSet(String newSetId)
	{
		// TODO Auto-generated method stub
	}

	public void refresh(String[] filters)
	{
		int lineCount = m_Cm.getLineCount(CacheManager.CM_ELEMENT);
		m_Ids = new String[lineCount];
		for (int i = 0; i < lineCount; i++)
		{
			m_Ids[i] = String.valueOf(i);
		}
	}

}
