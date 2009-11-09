package com.silverpeas.workflowdesigner.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane;

/**
 * Class implementing the tag &lt;processModelTabs&gt; from workflowEditor.tld
 *
 */
public class ProcessModelTabs extends TagSupport
{
    private String        strCurrentTab;
    
    /**
     * @return the current tab name
     */
    public String getCurrentTab()
    {
        return strCurrentTab;
    }

    /**
     * @param currentTab the current Tab name to set
     */
    public void setCurrentTab(String currentTab)
    {
        strCurrentTab = currentTab;
    }

    /* (non-Javadoc)
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    public int doStartTag() throws JspException
    {
        GraphicElementFactory gef;
        ResourcesWrapper      resource; 
        TabbedPane            tabbedPane;

        gef = (GraphicElementFactory)pageContext.getSession().getAttribute("SessionGraphicElementFactory");
        resource = (ResourcesWrapper)pageContext.getRequest().getAttribute("resources");
        tabbedPane = gef.getTabbedPane();
        
        tabbedPane.addTab(resource.getString("GML.head"),
                          "ModifyWorkflow".equals( strCurrentTab ) ? "#" : "ModifyWorkflow",
                          "ModifyWorkflow".equals( strCurrentTab ) );
        tabbedPane.addTab(resource.getString("workflowDesigner.roles"),
                          "ViewRoles".equals( strCurrentTab ) ? "#" : "ViewRoles",
                          "ViewRoles".equals( strCurrentTab ) );
        tabbedPane.addTab(resource.getString("workflowDesigner.presentation"),
                          "ViewPresentation".equals( strCurrentTab ) ? "#" : "ViewPresentation",
                          "ViewPresentation".equals( strCurrentTab ) );
        tabbedPane.addTab(resource.getString("workflowDesigner.participants"),
                          "ViewParticipants".equals( strCurrentTab ) ? "#" : "ViewParticipants",
                          "ViewParticipants".equals( strCurrentTab ) );
        tabbedPane.addTab(resource.getString("workflowDesigner.states"),
                          "ViewStates".equals( strCurrentTab ) ? "#" : "ViewStates",
                          "ViewStates".equals( strCurrentTab ) );
        tabbedPane.addTab(resource.getString("workflowDesigner.actions"),
                          "ViewActions".equals( strCurrentTab ) ? "#" : "ViewActions",
                          "ViewActions".equals( strCurrentTab ) );
        tabbedPane.addTab(resource.getString("workflowDesigner.userInfos"),
                          "ViewUserInfos".equals( strCurrentTab ) ? "#" : "ViewUserInfos",
                          "ViewUserInfos".equals( strCurrentTab ) );
        tabbedPane.addTab(resource.getString("workflowDesigner.dataFolder"), 
                          "ViewDataFolder".equals( strCurrentTab ) ? "#" : "ViewDataFolder",
                          "ViewDataFolder".equals( strCurrentTab ) );
        tabbedPane.addTab(resource.getString("workflowDesigner.forms"),
                          "ViewForms".equals( strCurrentTab ) ? "#" : "ViewForms",
                          "ViewForms".equals( strCurrentTab ) );

        try
        {
            pageContext.getOut().println(tabbedPane.print());
        } 
        catch (IOException e)
        {
            throw new JspException("Error when printing the Workflow Designer tabs", e);
        }
        return super.doStartTag();
    }

}
