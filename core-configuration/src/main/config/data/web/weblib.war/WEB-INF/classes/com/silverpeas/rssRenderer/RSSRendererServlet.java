/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.rssRenderer;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

public class RSSRendererServlet extends HttpServlet
{
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			// Get the parameters
			String xmlFileURL = request.getParameter("xmlFileURL");
			String xsltFileURL = request.getParameter("xsltFileURL");

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer( new StreamSource( xsltFileURL ) );
			StreamSource xmlsource = new StreamSource( xmlFileURL );
			StreamResult output = new StreamResult( response.getOutputStream() );
			transformer.transform( xmlsource, output );
		}
		catch (Exception e)
		{
			System.out.println(e.getClass());
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
	  doPost (request, response);
	}
}
