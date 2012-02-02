<jsp:directive.page contentType="image/svg+xml;charset=UTF-8" pageEncoding="UTF-8"/>
<svg xmlns:svg="http://www.w3.org/2000/svg" xmlns="http://www.w3.org/2000/svg" version="1.1" width="100%" height="100%">
<defs>
		<linearGradient id="linear-gradient" x1="0%" y1="0%" x2="<%=request.getParameter("vertical")%>%" y2="<%=request.getParameter("horizontal")%>%">
				<stop offset="0%" stop-color="#<%=request.getParameter("from")%>" stop-opacity="1"/>
				<stop offset="100%" stop-color="#<%=request.getParameter("to")%>" stop-opacity="1"/>
		</linearGradient>
</defs>
<rect width="100%" height="100%" fill="url(#linear-gradient)"/>
</svg>