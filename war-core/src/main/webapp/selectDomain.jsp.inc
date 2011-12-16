<%@ page import="java.util.Enumeration"%>
<%@ page import="java.util.List"%>
<select name="DomainId" size="1">
	<%
	if (listDomains==null || listDomains.size()==0)
	{ %>
	<option> --- </option>
	<%  }
	else {
		String dId 		= null;
		String dName 	= null;
		String selected	= "";
		for (Domain curDomain : listDomains) {
		  dId = curDomain.getId();
		  dName = curDomain.getName();
		  selected  = "";
		                          
		  if (dId.equals(request.getAttribute("Silverpeas_DomainId"))) {
		    selected = "selected=\"selected\"";
		  }
           %>
             <option value="<%=dId%>" <%=selected%>><%=dName%></option>
          <%  
	    }
	}
		%>
</select>