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
    "http://www.silverpeas.org/legal/licensing"

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

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>


<%@ page import="java.util.Collection, java.util.ArrayList, java.util.Iterator, java.util.Date, java.util.StringTokenizer"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
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

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.silverpeas.util.EncodeHelper" %>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>

<%@ page import="com.stratelia.silverpeas.pdc.model.*"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>

<%@ page import="com.stratelia.silverpeas.containerManager.ContainerWorkspace"%>
<%@ page import="com.stratelia.silverpeas.containerManager.ContainerContext"%>
<%@ page import="com.stratelia.silverpeas.containerManager.URLIcone"%>
<%@ page import="com.stratelia.silverpeas.contentManager.SilverContentInterface"%>

<%@ page import="com.stratelia.silverpeas.pdc.model.Value"%>

<%@ page import="com.silverpeas.util.i18n.I18NHelper"%>
<%@ page import="com.silverpeas.util.StringUtil"%>


<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>


<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");

String[] browseContext = (String[]) request.getAttribute("browseContext");
String spaceLabel = browseContext[0];
String componentLabel = browseContext[1];

boolean isAxisInvarianceUsed = (Boolean) request.getAttribute("AxisInvarianceUsed");

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
Frame frame = gef.getFrame();
Board board = gef.getBoard();

String language = resource.getLanguage();

/*
*********************************************************
*************************  HTML  ************************
*********************************************************
*/
String boardStart		= "<table cellpadding=5 cellspacing=2 border=0 width=\"98%\" class=intfdcolor><tr><td bgcolor=FFFFFF align=center>";
String boardEnd			= "</td></tr></table>";
String separator		= "<table cellpadding=0 cellspacing=0 border=0><tr><td></td><img src="+resource.getIcon("pdcPeas.noColorPix")+" width=4></tr></table>";
String cellCenterStart  = "<table cellpadding=0 cellspacing=0 border=0 width=\"100%\"><tr><td align=center>";
String cellCenterEnd	= "</td></tr></table>";

String sepOptionValueTag = "#_$#"; // attention, si l'on modifie cette valeur alors il faut egalement apporter cette modifcation au PdcRequestRouter

String pdcContext              = m_context+"/Rpdc/jsp/";
String pdcClassifyContext      = m_context+"/RpdcClassify/jsp/";
String pdcUtilizationContext   = m_context+"/RpdcUtilization/jsp/";

%>

<%!
int maxEltAuthorized		= 5; // nombre min d'elements avant la troncature du chemin 
int nbShowedEltAuthorized	= 2 ; // nombre de noeud que l'on veut afficher avant les ...
String troncateSeparator	= " ... ";
String separatorPath		= " / "; // separateur pour le chemin complet

/*
*********************************************************
*************************  METHODS  *********************
*********************************************************
*/

/**
* Cette methode construit un hyperlien a partir d'un nom et de son lien
* @param unit - un objet contenant un nom et un lien. Cette valeur ne doit pas etre nulle
* @param isLinked - vrai si l'on souhaite un hyperlien faux si l'on ne veut que du texte
* @return le texte en dur ou au format hypelien
*/
String linkedNode(Value unit, boolean isLinked, String language){
        String node = "";
        
        // Attention la partie hyperlink est a faire !!!!
        if (isLinked){
        	node = "<a href="+(String)unit.getPath()+">"+(String)unit.getName(language)+"</a>";
        } else {
            node = (String)unit.getName(language);
        }

        return node;
}


/**
* Cette methode construit le chemin complet tronque pour acceder a une valeur
* @param list - un objet contenant une liste de liste(nom+url). Cette valeur ne doit pas etre nulle
* @param completPath - le chemin que l'on veut tronquer
* @param withLastValue - on garde ou non la valeur selectionnee
* @return completPath - le chemin fabrique
*/
String troncatePath(String completPath, List list, boolean isLinked, int withLastValue, String language){
		Value value = null;
        // prend les nbShowedEltAuthorized 1er elements
        for (int nb=0; nb < nbShowedEltAuthorized; nb++){
			value = (Value) list.get(nb);
            completPath +=  linkedNode(value, isLinked, language)+separatorPath;
        }

        // colle ici les points de suspension
        completPath += troncateSeparator+separatorPath;

        // prend les nbShowedEltAuthorized derniers elements
        for (int nb=nbShowedEltAuthorized+withLastValue ; nb>withLastValue ; nb--){
			value = (Value) list.get(list.size() - nb);
            completPath +=  linkedNode(value, isLinked, language)+separatorPath;
        }

        return completPath;
}

/**
* Cette methode construit le chemin complet pour acceder a une valeur
* @param list - un objet contenant une liste de liste(nom+url). Cette valeur ne doit pas etre nulle
* @param isLinked - vrai si l'on souhaite un hyperlien faux si l'on ne veut que du texte
* @param withLastNode - 0 si l'on veut afficher le chemin complet de la valeur selectionnee. 
*                                               1 si l'on ne souhaite afficher que le chemin complet sans la valeur selectionnee
* @return completPath - le chemin fabrique
*/
String buildCompletPath(List list, boolean isLinked, int withLastValue, String language){
        String completPath = "";
        // on regarde d'en un 1er temps le nombre d'element de la liste que l'on recoit.
        // si ce nombre est strictement superieur a maxEltAuthorized alors on doit tronquer le chemin complet
        // et l'afficher comme suit : noeud1 / noeud2 / ... / noeudn-1 / noeudn
		Value value = null;
        if (list.size() > maxEltAuthorized){
                completPath = troncatePath(completPath,list,isLinked,withLastValue, language);
        } else {
                for (int nb=0; nb<list.size()-withLastValue;nb++ ){
						value = (Value) list.get(nb);
                        completPath += linkedNode(value,isLinked, language)+separatorPath;
                }
        }

        if ( (completPath == "") || (completPath.equals("/")) ){
                completPath = null;
        } else {
                completPath = completPath.substring(0,completPath.length()-separatorPath.length()); // retire le dernier separateur
        }

        return completPath;
}

String buildCompletPath(List list){
	return buildCompletPath(list, false, 0, "fr");
}

String buildCompletPath(List list, boolean isLinked, String language){
    return buildCompletPath(list, isLinked, 0, language);
}
%>