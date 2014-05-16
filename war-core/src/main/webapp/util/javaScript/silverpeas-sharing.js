function shareFile(id, componentId) {
  var url = webContext+"/RfileSharing/jsp/NewTicket?componentId="+componentId+"&type=Attachment&objectId=" + id;
  SP_openWindow(url, "NewTicket", "700", "360", "scrollbars=no, resizable, alwaysRaised");
}