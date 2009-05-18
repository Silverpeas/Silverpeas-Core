function openPdc()
{
	chemin = getContext()+"/RpdcSearch/jsp/AxisTree?query=";
	SP_openWindow(chemin,"Pdc_Pop","700","500","scrollbars=yes,resizable=yes");
}

function notifyAdministrators()
{
    SP_openWindow(getContext()+'/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId=&theTargetsUsers=Administrators&theTargetsGroups=', 'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
}