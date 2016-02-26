</table>
</form>
<% if (shownInNewWindow) {
	Button validateButton 	= gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
	Button cancelButton 	= gef.getFormButton(resource.getString("GML.cancel"), "javaScript:window.close()", false);

	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(validateButton);
	buttonPane.addButton(cancelButton);
	out.println("<br/><center>"+buttonPane.print()+"</center>");
} %>