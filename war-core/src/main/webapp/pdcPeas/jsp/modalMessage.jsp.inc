<script type="text/javascript">

messageObj = new DHTML_modalMessage();	// We only create one object of this class
messageObj.setShadowOffset(5);	// Large shadow

<!-- avec le script modal-message.js -->

function displayStaticMessage()
{
	messageObj.setHtmlContent("<center><table><tr><td align=\"center\" class=\"txtnote\"><%=resource.getString("pdcPeas.inProgress")%></td></tr><tr><td><br/></td></tr><tr><td align=\"center\"><img src=\"<%=resource.getIcon("pdcPeas.inProgress")%>\"/></td></tr></table></center>");
	messageObj.setSize(300,100);
	messageObj.setCssClassMessageBox(false);
	messageObj.setShadowDivVisible(true);	// Disable shadow for these boxes	
	messageObj.display();
}

function closeMessage()
{
	messageObj.close();	
}

</script>

