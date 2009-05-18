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