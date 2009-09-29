/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
function searchEngine() {
        document.searchEngineForm.query.value
          = document.searchForm.query.value;
        if (document.searchForm.query.value != "")
        {
        if (<%=isPdcUsed%>){
                document.searchEngineForm.mode.value = 'clear';
        document.searchEngineForm.action = "<%=m_sContext%>/RpdcSearch/jsp/AdvancedSearch";
                document.searchForm.query.value = "";
        } else {
                document.searchEngineForm.action = "<%=m_sContext%>/RsearchEngine/jsp/resultsForm.jsp";
                document.searchForm.query.value = "";
        }

        document.searchEngineForm.submit();
        }
}

function advancedSearchEngine(){
        if (<%=isPdcUsed%>){
                document.searchEngineForm.mode.value = 'clear';
        document.searchEngineForm.action = "<%=m_sContext%>/RpdcSearch/jsp/GlobalView?mode=clear";
        } else {
                document.searchEngineForm.action = "<%=m_sContext + URLManager.getURL(URLManager.CMP_SEARCHENGINE)%>searchForm.jsp";
        }
        document.searchEngineForm.submit();
}

function reInitQueryInput(){
        document.searchForm.query.value = "";
}

function searchFocus() {
  document.searchForm.query.focus();
 }

// User Notification Popup
function notifyPopup(context,compoId,users,groups)
{
    top.scriptFrame.SP_openWindow(context+'/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId=' + compoId + '&theTargetsUsers='+users+'&theTargetsGroups='+groups, 'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
}

function goToPreferedSpace(preferedSpace) {
        top.bottomFrame.SpacesBar.location = "DomainsBar.jsp?privateDomain="+preferedSpace;
        top.bottomFrame.MyMain.location = "Main.jsp?SpaceId="+preferedSpace;
}

function viewPersonalHomePage() {
        top.bottomFrame.MyMain.location = "Main.jsp?ViewPersonalHomePage=true";
}
//-->
function openPdc()
{
                chemin="<%=m_sContext%>/RpdcSearch/jsp/AxisTree?query=";
                largeur = "700";
                hauteur = "500";
                SP_openWindow(chemin,"Pdc_Pop",largeur,hauteur,"scrollbars=yes, resizable=yes");
}