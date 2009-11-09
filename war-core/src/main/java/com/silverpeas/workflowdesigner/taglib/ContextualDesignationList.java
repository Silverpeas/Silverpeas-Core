package com.silverpeas.workflowdesigner.taglib;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.silverpeas.workflow.api.model.ContextualDesignation;
import com.silverpeas.workflow.api.model.ContextualDesignations;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.viewGenerator.html.Encode;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn;
import com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine;
import com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane;
import com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane;
import com.stratelia.webactiv.util.viewGenerator.html.icons.Icon;

/**
 * Class implementing the tag &lt;contextualDesignationList&gt; from workflowEditor.tld
 */
public class ContextualDesignationList extends TagSupport
{
    private static final String    UTF8 = "UTF-8";  // encoding
    
    private String                 strContext,        // the context of the designation
                                   strParentScreen,   // TODO refactor, use the RR.calculateParentScreen()
                                   strPaneTitleKey,   // The resource key to retrieve the pane title
                                   strColumnLabelKey; // The resource key to retrieve the column label
    
    private ContextualDesignations designations;
    
    /* (non-Javadoc)
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    public int doStartTag() throws JspException
    {
        ArrayPane              designationPane;
        ArrayLine              ligne;
        IconPane               iconPane;
        Icon                   updateIcon;
        Icon                   delIcon;
        ArrayColumn            column;
        ContextualDesignation  designation;
        StringBuffer           sb;
        String                 strParametersEncoded,
                               strEditURL,
                               strPaneTitle,
                               strColumnLabel;
        GraphicElementFactory  gef;
        ResourcesWrapper       resource;
        Iterator               iterDesignations;
        
        try
        {
            gef = (GraphicElementFactory)pageContext.getSession().getAttribute("SessionGraphicElementFactory");
            resource = (ResourcesWrapper)pageContext.getRequest().getAttribute("resources");
            strPaneTitle = resource.getString(strPaneTitleKey);
            strColumnLabel = resource.getString(strColumnLabelKey);
            strContext = URLEncoder.encode( strContext, UTF8 );
            strParentScreen = URLEncoder.encode( strParentScreen, UTF8 );
    
            designationPane = gef.getArrayPane("designationList", strParentScreen, pageContext.getRequest(), pageContext.getSession());
            designationPane.setVisibleLineNumber(20);
            designationPane.setTitle(strPaneTitle);
            designationPane.addArrayColumn(resource.getString("GML.language"));
            designationPane.addArrayColumn(resource.getString("workflowDesigner.role" ) );
            designationPane.addArrayColumn(strColumnLabel);
            column = designationPane.addArrayColumn(resource.getString("GML.operations"));
            column.setSortable( false );

            iterDesignations = designations.iterateContextualDesignation();
            sb = new StringBuffer();
            
            while ( iterDesignations.hasNext() )
            {
                designation = (ContextualDesignation)iterDesignations.next();
                
                // Create the parameters
                //
                sb.setLength( 0 );
                sb.append( "?role=" );
                sb.append( URLEncoder.encode( designation.getRole(), UTF8 ) );
                sb.append( "&lang=" );
                sb.append( URLEncoder.encode( designation.getLanguage(), UTF8 ) );
                sb.append( "&parentScreen=" );  // FIXME refactor 
                sb.append( strParentScreen );
                sb.append( "&context=" );
                sb.append( strContext );
                
                strParametersEncoded = sb.toString();
                strEditURL = "ModifyContextualDesignation" + strParametersEncoded;
    
                // Create the remove link
                //
                sb.setLength(0);
                sb.append( "javascript:confirmRemove('RemoveContextualDesignation" ); 
                sb.append( strParametersEncoded );
                sb.append( "', '" );
                sb.append( resource.getString("workflowDesigner.confirmRemoveJS") );
                sb.append( " " );
                sb.append( Encode.javaStringToJsString( designation.getLanguage() ) );
                sb.append( ", " );
                sb.append( Encode.javaStringToJsString( designation.getRole() ) );
                sb.append( " ?');" );
    
                iconPane = gef.getIconPane();
                updateIcon = iconPane.addIcon();
                delIcon = iconPane.addIcon();
                updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                  resource.getString("GML.modify"),
                                  strEditURL );
                delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                               resource.getString("GML.delete"),
                               sb.toString() );
                iconPane.setSpacing("30px");
                
                ligne = designationPane.addArrayLine();
                ligne.addArrayCellLink( designation.getLanguage(), strEditURL );
                ligne.addArrayCellLink( designation.getRole(), strEditURL );
                ligne.addArrayCellLink( designation.getContent(), strEditURL );
                ligne.addArrayCellIconPane(iconPane);
            }

            pageContext.getOut().println(designationPane.print());
        } 
        catch (IOException e)
        {
            throw new JspException("Error when printing the Contextual Designations", e);
        }
        return super.doStartTag();
    }

    /**
     * @return the context
     */
    public String getContext()
    {
        return strContext;
    }

    /**
     * @param context the context to set
     */
    public void setContext(String context)
    {
        strContext = context;
    }

    /**
     * @return the designations
     */
    public ContextualDesignations getDesignations()
    {
        return designations;
    }

    /**
     * @param designations the designations to set
     */
    public void setDesignations(ContextualDesignations designations)
    {
        this.designations = designations;
    }

    /**
     * @return the relative URL of the partent screen
     */
    public String getParentScreen()
    {
        return strParentScreen;
    }

    /**
     * @param parentScreen the relative URL of the parent screen to set
     */
    public void setParentScreen(String parentScreen)
    {
        strParentScreen = parentScreen;
    }

    /**
     * @return the paneTitleKey
     */
    public String getPaneTitleKey()
    {
        return strPaneTitleKey;
    }

    /**
     * @param paneTitleKey the paneTitleKey to set
     */
    public void setPaneTitleKey(String paneTitleKey)
    {
        strPaneTitleKey = paneTitleKey;
    }

    /**
     * @return the columnLabelKey
     */
    public String getColumnLabelKey()
    {
        return strColumnLabelKey;
    }

    /**
     * @param columnLabelKey the columnLabelKey to set
     */
    public void setColumnLabelKey(String columnLabelKey)
    {
        strColumnLabelKey = columnLabelKey;
    }

}
