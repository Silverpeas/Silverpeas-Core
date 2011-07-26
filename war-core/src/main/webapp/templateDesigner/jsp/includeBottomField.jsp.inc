</table>
</form>
<% 
	Button validateButton 	= (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
	Button cancelButton 	= (Button) gef.getFormButton(resource.getString("GML.cancel"), "javaScript:window.close()", false);

	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(validateButton);
	buttonPane.addButton(cancelButton);
	out.println("<br/><center>"+buttonPane.print()+"</center>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>