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

<!-- import java -->
<%@ page import="org.silverpeas.core.pdc.interests.model.Interests"%>
<%@ page import="org.silverpeas.core.pdc.thesaurus.model.ThesaurusException"%>
<%@ page import="org.silverpeas.core.pdc.thesaurus.service.ThesaurusManager"%>
<%@ page import="org.silverpeas.core.pdc.thesaurus.model.Jargon"%>
<%@ page import="org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent"%>
<%@ page import="org.silverpeas.core.pdc.pdc.model.SearchAxis"%>
<%@ page import="org.silverpeas.core.pdc.pdc.model.SearchContext"%>
<%@ page import="org.silverpeas.core.pdc.pdc.model.SearchCriteria"%>
<%@ page import="org.silverpeas.core.pdc.pdc.model.Value"%>
<%@ page import="org.silverpeas.core.pdc.pdc.model.GlobalSilverResult"%>
<%@ page import="org.silverpeas.core.pdc.pdc.model.QueryParameters"%>
<%@ page import="org.silverpeas.core.admin.component.model.ComponentInstLight"%>
<%@ page import="org.silverpeas.core.admin.space.SpaceInstLight"%>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>

<!-- import PDC -->
<%@ page import="org.silverpeas.core.util.DateUtil"%>
<%@ page import="org.silverpeas.core.util.EncodeHelper"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.util.WAAttributeValuePair"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="javax.servlet.jsp.JspWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.text.NumberFormat"%>


<!-- import plainSearch -->
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Map.Entry"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.StringTokenizer"%>
<%@ page import="java.util.Vector"%>

<!-- common -->

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>


<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

MultiSilverpeasBundle resource = (MultiSilverpeasBundle) request.getAttribute("resources");

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
BrowseBar browseBarResult = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
Frame frame = gef.getFrame();
TabbedPane tabs = null;
//Board board = gef.getBoard();

String language = resource.getLanguage();

/*
*********************************************************
*************************  HTML  ************************
*********************************************************
*/
String separator  = "<table cellpadding=0 cellspacing=0 border=0><tr><td></td><img src="+resource.getIcon("pdcPeas.noColorPix")+" width=4></tr></table>";

String pdcContext = m_context+"/Rpdc/jsp/";
%>

<%!
int maxEltAuthorized = 5; // nombre min d'elements avant la troncature du chemin
int nbShowedEltAuthorized = 2 ; // nombre de noeud que l'on veut afficher avant les ...
String troncateSeparator = " ... ";
String separatorPath = " / "; // separateur pour le chemin complet

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
String linkedNode(Value unit, boolean isLinked){
  String node = "";

  // Attention la partie hyperlink est a faire !!!!
  if (isLinked){
    node = "<a href=" + unit.getPath() + ">" + unit.getName() + "</a> ";
  } else {
    node = unit.getName();
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
String troncatePath(String completPath, ArrayList list, boolean isLinked, int withLastValue){
  Value value = null;
  // prend les nbShowedEltAuthorized 1er elements
  for (int nb=0; nb < nbShowedEltAuthorized; nb++){
	value = (Value) list.get(nb);
    completPath +=  linkedNode(value, isLinked)+separatorPath;
    //completPath += (String)( (ArrayList)(list.get(nb)) ).get(0);
  }

  // colle ici les points de suspension
  completPath += troncateSeparator+separatorPath;

  // prend les nbShowedEltAuthorized derniers elements
  for (int nb=nbShowedEltAuthorized+withLastValue ; nb>withLastValue ; nb--){
	value = (Value) list.get(list.size() - nb);
    completPath +=  linkedNode(value, isLinked)+separatorPath;
    //completPath += (String)((ArrayList)(list.get(list.size() - nb))).get(0);
  }

  return completPath;
}

/**
* Cette methode construit le chemin complet pour acceder a une valeur
* @param list - un objet contenant une liste de liste(nom+url). Cette valeur ne doit pas etre nulle
* @param isLinked - vrai si l'on souhaite un hyperlien faux si l'on ne veut que du texte
* @param withLastNode - 0 si l'on veut afficher le chemin complet de la valeur selectionnee.
*                       1 si l'on ne souhaite afficher que le chemin complet sans la valeur selectionne
* @return completPath - le chemin fabrique
*/
String buildCompletPath(ArrayList list, boolean isLinked, int withLastValue){
  String completPath = "";
  // on regarde d'en un 1er temps le nombre d'element de la liste que l'on recoit.
  // si ce nombre est strictement superieur a maxEltAuthorized alors on doit tronquer le chemin complet
  // et l'afficher comme suit : noeud1 / noeud2 / ... / noeudn-1 / noeudn
  Value value = null;
  if (list.size() > maxEltAuthorized){
    completPath = troncatePath(completPath,list,isLinked,withLastValue);
  } else {
    for (int nb=0; nb<list.size()-withLastValue;nb++ ){
      value = (Value) list.get(nb);
      completPath += linkedNode(value, isLinked)+separatorPath;
    }
  }

  if ( (completPath == "") || (completPath.equals("/")) ){
    completPath = null;
  } else {
    completPath = completPath.substring(0,completPath.length()-separatorPath.length()); // retire le dernier separateur
  }

  return completPath;
}

String buildCompletPath(ArrayList list){
  return buildCompletPath(list,false,0);
}

String buildCompletPath(ArrayList list,boolean isLinked){
  return buildCompletPath(list,isLinked,0);
}

%>
