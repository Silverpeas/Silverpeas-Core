<%@ page import="java.util.Enumeration"%>
<select name="DomainId" size="1">
	<%
	if (domains==null ||domains.size()==0)
	{ %>
	<option> --- </option>
	<%  }
	else
	{
		String dId 		= null;
		String dName 	= null;
		String selected	= "";

		for (Enumeration e = domains.keys() ; e.hasMoreElements() ;)
		{
			dId 		= (String) e.nextElement();
			dName 		= (String) domains.get(dId);
			selected 	= "";

			if (dId.equals(request.getAttribute("Silverpeas_DomainId")))
				selected = "selected=\"selected\"";
	%>
		<option value="<%=dId%>" <%=selected%>> <%=dName%></option>
	<%  }
	}
		%>
</select>
