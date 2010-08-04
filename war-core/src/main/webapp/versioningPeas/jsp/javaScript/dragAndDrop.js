var dNdVisible 	= false;
var dNdLoaded	= false;
function showHideDragDrop(targetURL1, message1, targetURL2, message2, altMessage, max_upload, webContext, expandLabel, collapseLabel)
{
  var actionDND = document.getElementById("dNdActionLabel");
	
  if (dNdVisible)
  {
    //hide applet
    hideApplet('DragAndDrop');
    hideApplet('DragAndDropDraft');

    //change link's label
    actionDND.innerHTML = expandLabel;
  }
  else
  {
    actionDND.innerHTML = collapseLabel;
		
    if (dNdLoaded)
    {
      showApplet('DragAndDrop');
      showApplet('DragAndDropDraft');
    }
    else
    {
      try {
        loadApplet('DragAndDrop', targetURL1, message1, max_upload, webContext, altMessage);
      } catch (e) {
      }
      try {
        loadApplet('DragAndDropDraft', targetURL2, message2, max_upload, webContext, altMessage);
      } catch (e) {
      }
      dNdLoaded = true;
    }
  }
  dNdVisible = !dNdVisible;
}