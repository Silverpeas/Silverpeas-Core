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
