<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ page import="com.silverpeas.workflow.api.model.Action" %>
<%@ page import="com.silverpeas.workflow.api.model.Actions" %>
<%@ page import="com.silverpeas.workflow.api.model.AllowedActions" %>
<%@ page import="com.silverpeas.workflow.api.model.Column" %>
<%@ page import="com.silverpeas.workflow.api.model.Columns" %>
<%@ page import="com.silverpeas.workflow.api.model.Consequence" %>
<%@ page import="com.silverpeas.workflow.api.model.ContextualDesignation" %>
<%@ page import="com.silverpeas.workflow.api.model.DataFolder" %>
<%@ page import="com.silverpeas.workflow.api.model.Form" %>
<%@ page import="com.silverpeas.workflow.api.model.Forms" %>
<%@ page import="com.silverpeas.workflow.api.model.Item" %>
<%@ page import="com.silverpeas.workflow.api.model.Input" %>
<%@ page import="com.silverpeas.workflow.api.model.QualifiedUsers" %>
<%@ page import="com.silverpeas.workflow.api.model.Parameter" %>
<%@ page import="com.silverpeas.workflow.api.model.Participant" %>
<%@ page import="com.silverpeas.workflow.api.model.Participants" %>
<%@ page import="com.silverpeas.workflow.api.model.Presentation" %>
<%@ page import="com.silverpeas.workflow.api.model.ProcessModel" %>
<%@ page import="com.silverpeas.workflow.api.model.RelatedUser" %>
<%@ page import="com.silverpeas.workflow.api.model.Role" %>
<%@ page import="com.silverpeas.workflow.api.model.Roles" %>
<%@ page import="com.silverpeas.workflow.api.model.State" %>
<%@ page import="com.silverpeas.workflow.api.model.States" %>
<%@ page import="org.silverpeas.web.workflowdesigner.control.WorkflowDesignerSessionController"%>
<%@ page import="org.silverpeas.util.EncodeHelper"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellInputText"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellRadio"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellSelect"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.board.Board"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.icons.Icon"%>

<%@ page import="org.silverpeas.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.window.Window"%>
<%@ page import="java.io.UnsupportedEncodingException"%>
<%@ page import="java.net.URLEncoder"%>


<%@ page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>

<%@page import="java.util.List"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>
<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
WorkflowDesignerSessionController   wfdsc       = (WorkflowDesignerSessionController) request.getAttribute("WorkflowDesigner");

MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");
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
                                          MultiSilverpeasBundle resource,
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
                         MultiSilverpeasBundle resource,
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
                                MultiSilverpeasBundle resource,
                                String           strContext )
        throws UnsupportedEncodingException
    {
        if ( operationPane != null )
            operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
                                       resource.getString("workflowDesigner.add.relatedUser"),
                                       "AddRelatedUser?context=" + URLEncoder.encode( strContext, "UTF-8") );
    }
%>
