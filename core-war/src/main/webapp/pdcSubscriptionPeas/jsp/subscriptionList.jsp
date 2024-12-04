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
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.Tab" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%!
    public final static String separatorPath = ">";
    public final static int maxEltAuthorized = 5;
    public final static String troncateSeparator = "...";
    public final static int nbShowedEltAuthorized = 2;

    void troncatePath(StringBuilder completPath, List<Value> list, boolean isLinked, int
            withLastValue,
            String language) {
        Value value;
        // prend les nbShowedEltAuthorized 1er elements
        for (int nb = 0; nb < nbShowedEltAuthorized; nb++) {
            value = list.get(nb);
            completPath.append(linkedNode(value, isLinked, language))
                    .append(separatorPath);
        }

        // colle ici les points de suspension
        completPath.append(troncateSeparator).append(separatorPath);

        // prend les nbShowedEltAuthorized derniers elements
        for (int nb = nbShowedEltAuthorized + withLastValue; nb > withLastValue; nb--) {
            value = list.get(list.size() - nb);
            completPath.append(linkedNode(value, isLinked, language))
                    .append(separatorPath);
        }
    }

    String linkedNode(Value unit, boolean isLinked, String language) {
        String node;

        // Attention la partie hyperlink est a faire !!!!
        if (isLinked) {
            node = "<a href=" + unit.getPath() + ">" + Encode.forHtml(unit.getName(language)) +
                    "</a>";
        } else {
            node = Encode.forHtml(unit.getName(language));
        }

        return node;
    }

    String buildCompletPath(List<Value> list, String language) {
        boolean isLinked = false;
        int withLastValue = 0;
        StringBuilder completPath = new StringBuilder();

        // on regarde d'en un 1er temps le nombre d'element de la liste que l'on recoit.
        // si ce nombre est strictement superieur a maxEltAuthorized alors on doit tronquer le chemin complet
        // et l'afficher comme suit : noeud1 / noeud2 / ... / noeudn-1 / noeudn
        if (list.size() > maxEltAuthorized) {
            troncatePath(completPath, list, isLinked, withLastValue, language);
        } else {
            for (int nb = 0; nb < list.size() - withLastValue; nb++) {
                Value value = list.get(nb);
                completPath.append(linkedNode(value, isLinked, language))
                        .append(separatorPath);
            }
        }

        String path = completPath.toString().trim();
        if (path.isEmpty() || path.equals(">")) {
            path = null;
        } else {
            path = path.substring(0, completPath.length() - separatorPath.length());
            // retire le dernier separateur
        }

        return path;
    }


    public String formatPDCContext(List<List<Value>> pathCriteria, String language) {
        if (pathCriteria == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        int size = pathCriteria.size();
        for (int k = 0; k < size; k++) {
            List<Value> list = pathCriteria.get(k);
            String fullPath = buildCompletPath(list, language);
            result.append(fullPath);
            if (k < size - 1) {
                result.append(" X ");
            }
        }

        return result.toString();
    }

    %>

<%
    //noinspection unchecked
    List<List<List<Value>>>	pathContext	= (List<List<List<Value>>>) request.getAttribute("PathContext");
    //noinspection unchecked
    List<PdcSubscription>	subscriptionList = (List<PdcSubscription>) request.getAttribute("subscriptionList");
	String action = (String) request.getAttribute("action");
	String userId = (String) request.getAttribute("userId");

    boolean isReadOnly = action != null && action.equals("showUserSubscriptions");

    final String iconEdit		= m_context+"/util/icons/update.gif";
    final String iconAdd		= resource.getIcon("icoAddNew");
    final String iconDelete		= resource.getIcon("icoDelete");
    final String path			= resource.getString("Path");

    if ( subscriptionList == null ){
       subscriptionList = new ArrayList<>();
    }

%>
<view:sp-page>
<view:sp-head-part withCheckFormScript="true">
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script type="text/javascript">
function newSubscription() {
        chemin = '<%=m_context%>/RpdcSubscriptionPeas/jsp/PdcSubscription';
                largeur = "600";
                hauteur = "440";
                SP_openWindow(chemin,"",largeur,hauteur,"resizable=yes,scrollbars=yes");
}

function editSubscription(scid) {
		chemin = '<%=m_context%>/RpdcSubscriptionPeas/jsp/PdcSubscription?pdcSId=' + scid ;
                largeur = "600";
                hauteur = "440";
                SP_openWindow(chemin,"",largeur,hauteur,"resizable=yes,scrollbars=yes");
}

function deleteSubscription() {
  const boxItems = document.subscriptionList.pdcCheck;
  let selectItems = "";
  if (boxItems != null) {
    // au moins une checkbox exist
    const nbBox = boxItems.length;
    if ((nbBox == null) && (boxItems.checked === true)) {
      selectItems += boxItems.value;
    } else {
      for (i = 0; i < boxItems.length; i++) {
        if (boxItems[i].checked === true) {
          selectItems += boxItems[i].value + ",";
        }
      }
      selectItems = selectItems.substring(0, selectItems.length - 1);
    }
  }
  if (selectItems.length > 0) {
    jQuery.popup.confirm("<%=resource.getString("confirmDeleteSubscription")%>", function() {
      document.subscriptionList.mode.value = 'delete';
      document.subscriptionList.submit();
    });
  }
}
</script>
</view:sp-head-part>
<view:sp-body-part cssClass="txtlist">
<form name="subscriptionList" action="<%=action%>" method="post">
<input type="hidden" name="mode"/>

 <%
     browseBar.setComponentName(path);

     TabbedPane tabbedPane = gef.getTabbedPane();
     sessionController.getSubscriptionCategories().forEach(c -> {
         final String subscriptionResourceCategoryUrl = "ViewSubscriptionOfCategory?userId=" + userId + "&action=" + action + "&subResCategory=" + c.getId();
         Tab tab = tabbedPane.addTab(Encode.forHtml(c.getLabel()),
                 subscriptionResourceCategoryUrl, false);
         tab.setName(c.getId());
     });
     Tab tabPDC = tabbedPane.addTab(resource.getString("pdc"), "#", true);
     tabPDC.setName("PDC");

     if (!isReadOnly) {
         operationPane.addOperationOfCreation(iconAdd, resource.getString("AddSC"), m_context + "/RpdcSubscriptionPeas/jsp/PdcSubscription");
         if (!subscriptionList.isEmpty()) {
             operationPane.addOperation(iconDelete, resource.getString("DeleteSC"), "javascript:deleteSubscription()");
         }
     }

     out.println(window.printBefore());
     out.println(tabbedPane.print());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<%
      ArrayPane arrayPane = gef.getArrayPane("tableau1", action + "?userId=" + userId, request, session);

      arrayPane.addArrayColumn(resource.getString("name"));
      ArrayColumn column = arrayPane.addArrayColumn(resource.getString("value"));
      column.setSortable(false);
      if (!isReadOnly) {
          ArrayColumn column0 = arrayPane.addArrayColumn(resource.getString("Operations"));
		  column0.setSortable(false);
      }

	  for (int i =0 ; i < subscriptionList.size(); i++ ) {
          PdcSubscription ps = subscriptionList.get(i);
          ArrayLine line = arrayPane.addArrayLine();
		  line.addArrayCellText(Encode.forHtml(ps.getName()));
		  line.addArrayCellText(formatPDCContext(pathContext.get(i), resource.getLanguage()));

          IconPane iconPane	= gef.getIconPane();
          Icon updateIcon = iconPane.addIcon();
          updateIcon.setProperties(iconEdit, resource.getString("EditSC"), m_context + "/RpdcSubscriptionPeas/jsp/PdcSubscription?pdcSId="+ps.getId());

		  if (!isReadOnly) {
			  line.addArrayCellText(updateIcon.print()+"&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"pdcCheck\" value=\""+ps.getId()+"\"/>");
		  }
	  }

  out.println(arrayPane.print());
%>
</view:frame>
<%
  out.println(window.printAfter());
 %>
</form>
</view:sp-body-part>
</view:sp-page>