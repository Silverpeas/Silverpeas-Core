<%@ page import="org.silverpeas.core.pdc.subscription.model.PdcSubscription" %><%--

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

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%!
    public final static String separatorPath = ">";
    public final static int maxEltAuthorized = 5;
    public final static String troncateSeparator = "...";
    public final static int nbShowedEltAuthorized = 2;

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

    String buildCompletPath(List list, String language){
            boolean  isLinked = false;
            int withLastValue = 0;
            String completPath = "";

            // on regarde d'en un 1er temps le nombre d'element de la liste que l'on recoit.
            // si ce nombre est strictement superieur a maxEltAuthorized alors on doit tronquer le chemin complet
            // et l'afficher comme suit : noeud1 / noeud2 / ... / noeudn-1 / noeudn
			Value value = null;
            if (list.size() > maxEltAuthorized){
                    completPath = troncatePath(completPath, list, isLinked, withLastValue, language);
            } else {
                    for (int nb=0; nb<list.size()-withLastValue;nb++ ){
						value = (Value) list.get(nb);
                        completPath += linkedNode(value, isLinked, language)+separatorPath;
                    }
            }

            if ( (completPath == "") || (completPath.equals(">")) ){
                    completPath = null;
            } else {
                    completPath = completPath.substring(0,completPath.length()-separatorPath.length()); // retire le dernier separateur
            }

            return completPath;
    }


   public String formatPDCContext(List pathCriteria, String language) {
       StringBuffer result = new StringBuffer();
       List list = null;
       String completPath = "";

       if (pathCriteria == null) {
           return "";
       }
       int size = pathCriteria.size();
       for (int k=0; k < size; k++ ){
           list = (List)pathCriteria.get(k);
           completPath = buildCompletPath(list, language);
           result.append(completPath);
           if (k < size - 1) {
               result.append(" X ");
           }
       }

       return result.toString();
   }

    %>

<%
    List		pathContext			= (List) request.getAttribute("PathContext");
    ArrayList	subscriptionList	= (ArrayList) request.getAttribute("subscriptionList");
	String		action				= (String) request.getAttribute("action");
	String		userId				= (String) request.getAttribute("userId");

    boolean isReadOnly = false;
	if ( action != null && action.equals("showUserSubscriptions")) {
        isReadOnly = true;
    }

	final String iconEdit		= m_context+"/util/icons/update.gif";
    final String iconAdd		= resource.getIcon("icoAddNew");
    final String iconDelete		= resource.getIcon("icoDelete");
    final String path			= resource.getString("Path");

    if ( subscriptionList == null ){
       subscriptionList = new ArrayList();
    }

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script type="text/javascript">

function areYouSure(){
    return confirm("<%=resource.getString("confirmDeleteSubscription")%>");
}

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

                var boxItems = document.subscriptionList.pdcCheck;
                var selectItems = "";
                if (boxItems != null){
                        // au moins une checkbox exist
                        var nbBox = boxItems.length;
                        if ( (nbBox == null) && (boxItems.checked == true) ){
                                selectItems += boxItems.value;
                        } else{
                                for (i=0;i<boxItems.length ;i++ ){
                                        if (boxItems[i].checked == true){
                                                selectItems += boxItems[i].value+",";
                                        }
                                }
                                selectItems = selectItems.substring(0,selectItems.length-1);
                        }
         }
 if ( (selectItems.length > 0) && (areYouSure()) ) {
    document.subscriptionList.mode.value = 'delete';
    document.subscriptionList.submit();
  }
}

</script>
</head>
<body class="txtlist">
<form name="subscriptionList" action="<%=action%>" method="post">
<input type="hidden" name="mode"/>

 <%
    browseBar.setComponentName(path);

	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("pdc"), "#", true);
	tabbedPane.addTab(resource.getString("thematique"), "ViewSubscriptionTheme?userId="+userId+"&action="+action, false);
   tabbedPane.addTab(resource.getString("application"),
       "ViewSubscriptionComponent?userId=" + userId + "&action=" + action, false);

      if (!isReadOnly) {
          operationPane.addOperationOfCreation(iconAdd , resource.getString("AddSC"),m_context + "/RpdcSubscriptionPeas/jsp/PdcSubscription");
          if (subscriptionList != null && subscriptionList.size() > 0) {
              operationPane.addOperation(iconDelete , resource.getString("DeleteSC"),"javascript:deleteSubscription()");
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

      ArrayLine ligne;
	  IconPane iconPane;
      Icon updateIcon;

	  PdcSubscription ps = null;
	  for (int i =0 ; i < subscriptionList.size(); i++ ) {
		  ps	= (PdcSubscription) subscriptionList.get(i);
		  ligne = arrayPane.addArrayLine();
		  ligne.addArrayCellText(ps.getName());
		  ligne.addArrayCellText(formatPDCContext((List)pathContext.get(i), resource.getLanguage()));

		  iconPane	= gef.getIconPane();
          updateIcon = iconPane.addIcon();
          updateIcon.setProperties(iconEdit, resource.getString("EditSC"), m_context + "/RpdcSubscriptionPeas/jsp/PdcSubscription?pdcSId="+ps.getId());

		  if (!isReadOnly) {
			  ligne.addArrayCellText(updateIcon.print()+"&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"pdcCheck\" value=\""+ps.getId()+"\"/>");
		  }
	  }

  out.println(arrayPane.print());
%>
</view:frame>
<%
  out.println(window.printAfter());
 %>
</form>
</body>
</html>