<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache");        //HTTP 1.0
  response.setDateHeader("Expires", -1);          //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.pdc.pdc.model.Axis" %>
<%@ page import="org.silverpeas.core.pdc.pdc.model.AxisHeader" %>
<%@ page import="org.silverpeas.core.pdc.pdc.model.ClassifyPosition" %>
<%@ page import="org.silverpeas.core.pdc.pdc.model.ClassifyValue" %>
<%@ page import="org.silverpeas.core.pdc.pdc.model.SearchAxis" %>


<%@ page import="org.silverpeas.core.pdc.pdc.model.SearchContext" %>
<%@ page import="org.silverpeas.core.pdc.pdc.model.SearchCriteria" %>
<%@ page import="org.silverpeas.core.pdc.pdc.model.UsedAxis" %>
<%@ page import="org.silverpeas.core.pdc.pdc.model.Value" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle" %>
<%@ page import="org.silverpeas.kernel.bundle.ResourceLocator" %>
<%@ page import="org.silverpeas.kernel.util.StringUtil" %>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane" %>

<%// En fonction de ce dont vous avez besoin %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page import="jakarta.servlet.jsp.JspWriter" %>

<%@ page import="java.io.IOException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>


<%@ page import="java.util.List" %>
<%@ page import="java.util.StringTokenizer" %>


<%@ page errorPage="../../admin/jsp/errorpageMain.jsp" %>


<%
  GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

  String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

  MultiSilverpeasBundle resource = (MultiSilverpeasBundle) request.getAttribute("resources");

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
  String boardStart = "<table cellpadding=5 cellspacing=2 border=0 width=\"98%\" class=intfdcolor><tr><td bgcolor=FFFFFF align=center>";
  String boardEnd = "</td></tr></table>";
  String separator = "<table cellpadding=0 cellspacing=0 border=0><tr><td></td><img src=" + resource.getIcon("pdcPeas.noColorPix") + " width=4></tr></table>";
  String cellCenterStart = "<table cellpadding=0 cellspacing=0 border=0 width=\"100%\"><tr><td align=center>";
  String cellCenterEnd = "</td></tr></table>";

  String sepOptionValueTag = "#_$#"; // attention, si l'on modifie cette valeur alors il faut egalement apporter cette modifcation au PdcRequestRouter

  String pdcContext = m_context + "/Rpdc/jsp/";
  String pdcClassifyContext = m_context + "/RpdcClassify/jsp/";
  String pdcUtilizationContext = m_context + "/RpdcUtilization/jsp/";

%>

<%!
  int maxEltAuthorized = 5; // nombre min d'elements avant la troncature du chemin
  int nbShowedEltAuthorized = 2; // nombre de nœuds que l'on veut afficher avant les "..."
  String troncateSeparator = " ... ";
  String separatorPath = " / "; // séparateur pour le chemin complet

  /*
   *********************************************************
   *************************  METHODS  *********************
   *********************************************************
   */

  /**
   * Cette methode construit un hyperlien à partir d'un nom et de son lien
   * @param unit un objet contenant un nom et un lien. Cette valeur ne doit pas etre nulle
   * @param isLinked - vrai si l'on souhaite un faux hyperlien si l'on ne veut que du texte
   * @return le texte en dur ou au format hyperlien
   */
  String linkedNode(Value unit, boolean isLinked, String language) {
    String node = "";

    // Attention la partie hyperlink est à faire !!!!
    if (isLinked) {
      node = "<a href=" + unit.getPath() + ">" + unit.getName(language) + "</a>";
    } else {
      node = unit.getName(language);
    }

    return node;
  }


  /**
   * Cette methode construit le chemin complet tronque pour acceder a une valeur
   * @param list un objet contenant une liste de liste (nom+url). Cette valeur ne doit pas etre nulle
   * @param completPath le chemin que l'on veut tronquer
   * @param withLastValue on garde ou non la valeur sélectionnée
   * @return completPath le chemin fabrique
   */
  String troncatePath(String completPath, List<Value> list, boolean isLinked, int withLastValue,
      String language) {
    // prend les nbShowedEltAuthorized 1er elements
    StringBuilder completPathBuilder = new StringBuilder(completPath);
    for (int nb = 0; nb < nbShowedEltAuthorized; nb++) {
      Value value = list.get(nb);
      completPathBuilder.append(linkedNode(value, isLinked, language)).append(separatorPath);
    }
    String path = completPathBuilder.toString();

    // colle ici les points de suspension
    path += troncateSeparator + separatorPath;

    // prend les nbShowedEltAuthorized derniers elements
    completPathBuilder = new StringBuilder(completPath);
    for (int nb = nbShowedEltAuthorized + withLastValue; nb > withLastValue; nb--) {
      Value value = list.get(list.size() - nb);
      completPathBuilder.append(linkedNode(value, isLinked, language)).append(separatorPath);
    }
    path += completPathBuilder.toString();

    return path;
  }

  /**
   * Cette methode construit le chemin complet pour acceder a une valeur
   * @param list un objet contenant une liste de liste (nom+url). Cette valeur ne doit pas etre nulle
   * @param isLinked vrai si l'on souhaite un faux hyperlien si l'on ne veut que du texte
   * @param withLastValue 0 si l'on veut afficher le chemin complet de la valeur sélectionnée.
   *                      1 si l'on ne souhaite afficher que le chemin complet sans la valeur sélectionnée
   * @return completPath le chemin fabrique
   */
  String buildCompletPath(List<Value> list, boolean isLinked, int withLastValue, String language) {
    StringBuilder completPath = new StringBuilder();
    // On regarde dans un 1er temps le nombre d'éléments de la liste que l'on reçoit.
    // Si ce nombre est strictement supérieur à maxEltAuthorized alors, on doit tronquer le chemin complet
    // et l'afficher comme suit : node1 / node2 / ... / node<n>-1 / node<n>
    Value value;
    if (list.size() > maxEltAuthorized) {
      completPath.append(troncatePath(completPath.toString(), list, isLinked, withLastValue,
          language));
    } else {
      for (int nb = 0; nb < list.size() - withLastValue; nb++) {
        value = list.get(nb);
        completPath.append(linkedNode(value, isLinked, language)).append(separatorPath);
      }
    }

    String path;
    if ((completPath.toString().isEmpty()) || (completPath.toString().equals("/"))) {
      path = null;
    } else {
      path = completPath.substring(0, completPath.length() - separatorPath.length());
      // retire le dernier séparateur
    }

    return path;
  }

  String buildCompletPath(List<Value> list) {
    return buildCompletPath(list, false, 0, "fr");
  }

  String buildCompletPath(List<Value> list, boolean isLinked, String language) {
    return buildCompletPath(list, isLinked, 0, language);
  }
%>