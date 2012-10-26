<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="java.io.UnsupportedEncodingException"%><%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>

<%// En fonction de ce dont vous avez besoin %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>

<%@page import="com.silverpeas.form.DataRecord"%>
<%@page import="com.silverpeas.form.PagesContext"%>

<%@ page import="com.silverpeas.workflow.api.model.Action"%>
<%@ page import="com.silverpeas.workflow.api.model.Actions"%>
<%@ page import="com.silverpeas.workflow.api.model.AllowedAction"%>
<%@ page import="com.silverpeas.workflow.api.model.AllowedActions"%>
<%@ page import="com.silverpeas.workflow.api.model.Column"%>
<%@ page import="com.silverpeas.workflow.api.model.Columns"%>
<%@ page import="com.silverpeas.workflow.api.model.Consequence"%>
<%@ page import="com.silverpeas.workflow.api.model.Consequences"%>
<%@ page import="com.silverpeas.workflow.api.model.ContextualDesignation"%>
<%@ page import="com.silverpeas.workflow.api.model.ContextualDesignations"%>
<%@ page import="com.silverpeas.workflow.api.model.DataFolder"%>
<%@ page import="com.silverpeas.workflow.api.model.Form"%>
<%@ page import="com.silverpeas.workflow.api.model.Forms"%>
<%@ page import="com.silverpeas.workflow.api.model.Input"%>
<%@ page import="com.silverpeas.workflow.api.model.Item"%>
<%@ page import="com.silverpeas.workflow.api.model.Parameter"%>
<%@ page import="com.silverpeas.workflow.api.model.Participant"%>
<%@ page import="com.silverpeas.workflow.api.model.Participants"%>
<%@ page import="com.silverpeas.workflow.api.model.Presentation"%>
<%@ page import="com.silverpeas.workflow.api.model.ProcessModel" %>
<%@ page import="com.silverpeas.workflow.api.model.QualifiedUsers"%>
<%@ page import="com.silverpeas.workflow.api.model.RelatedUser"%>
<%@ page import="com.silverpeas.workflow.api.model.Role" %>
<%@ page import="com.silverpeas.workflow.api.model.Roles"%>
<%@ page import="com.silverpeas.workflow.api.model.State"%>
<%@ page import="com.silverpeas.workflow.api.model.States"%>
<%@ page import="com.silverpeas.workflow.api.model.StateSetter"%>
<%@ page import="com.silverpeas.workflow.api.model.UserInRole"%>

<%@ page import="com.silverpeas.workflowdesigner.control.WorkflowDesignerSessionController"%>

<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.net.URLEncoder"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>
<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");
Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
Frame frame = gef.getFrame();
Board board = gef.getBoard();
Board boardHelp = gef.getBoard();
ArrayLine row;
ArrayColumn column;
IconPane  iconPane;
Icon                   updateIcon;
Icon                   delIcon;
ArrayCellInputText cellInput;
ArrayCellText      cellText;
ArrayCellSelect cellSelect;
ArrayCellRadio  cellRadio;


 String[] browseContext = (String[]) request.getAttribute("browseContext");
 String spaceLabel = browseContext[0];    // TODO check where these should be applied
 String componentLabel = browseContext[1];
 String spaceId = browseContext[2];
 String componentId = browseContext[3];
%>

<%! public static final String UTF8 = "UTF-8";

    public void addContextualDesignation( OperationPane    operationPane,
                                          ResourcesWrapper resource,
                                          String           strContext,
                                          String           strAddActionKey,
                                          String           strParentScreen )
        throws UnsupportedEncodingException 
    {
        if ( operationPane != null )
            operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
                                       resource.getString(strAddActionKey),
                                       "AddContextualDesignation?context=" 
                                       + URLEncoder.encode( strContext, UTF8 ) 
                                       + "&parentScreen=" 
                                       + URLEncoder.encode( strParentScreen, UTF8 ) );
    }

    public void addItem( OperationPane    operationPane,
                         ResourcesWrapper resource,
                         String           strContext,
                         String           strAddActionKey )
        throws UnsupportedEncodingException 
    {
        if ( operationPane != null )
            operationPane.addOperationOfCreation( resource.getIcon("workflowDesigner.add"),
                                        resource.getString( strAddActionKey ),
                                        "AddItem?context=" + URLEncoder.encode( strContext, "UTF-8") );
    }
    
    public void addRelatedUser( OperationPane    operationPane,
                                ResourcesWrapper resource,
                                String           strContext )
        throws UnsupportedEncodingException 
    {
        if ( operationPane != null )
            operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
                                       resource.getString("workflowDesigner.add.relatedUser"),
                                       "AddRelatedUser?context=" + URLEncoder.encode( strContext, "UTF-8") );
    }
%>
