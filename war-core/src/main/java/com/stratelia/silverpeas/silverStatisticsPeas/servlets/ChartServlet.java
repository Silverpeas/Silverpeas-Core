/*
 * Created on 12 juil. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.stratelia.silverpeas.silverStatisticsPeas.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jCharts.Chart;
import org.jCharts.chartData.ChartDataException;
import org.jCharts.encoders.ServletEncoderHelper;
import org.jCharts.properties.PropertyException;

/**
 * @author BERTINL
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ChartServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String CHART = "CHART";
	public static final String IMAGE_MAP = "IMAGE_MAP";
	public static final String LOGINCHART = "LOGIN_CHART";
	public static final String USERCHART = "USER_CHART";
	public static final String USERFQCHART = "USER_FQ_CHART";
	public static final String USERVENTILCHART = "USER_VENTIL_CHART";
	public static final String EVOLUTIONUSERCHART = "EVOLUTION_USER_CHART";
	public static final String DOCVENTILCHART = "DOC_VENTIL_CHART";
	public static final String DOCSIZEVENTILCHART = "DOCSIZE_VENTIL_CHART";
	public static final String EVOLUTIONDOCSIZECHART = "EVOLUTION_DOCSIZE_CHART";
	public static final String PUBLIVENTILCHART = "PUBLI_VENTIL_CHART";
	public static final String KMINSTANCESCHART = "KM_INSTANCES_CHART";
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		try
		{
			String chartName = request.getParameter("chart");
			Chart chart = (Chart) request.getSession().getAttribute(chartName);
			ServletEncoderHelper.encodeJPEG13(chart, 1.0f, response);
		}
		catch( PropertyException propertyException )
		{
			propertyException.printStackTrace();
		}
		catch( ChartDataException dataException )
		{
			dataException.printStackTrace();
		}

		request.getSession().removeAttribute( CHART );
	}
}
