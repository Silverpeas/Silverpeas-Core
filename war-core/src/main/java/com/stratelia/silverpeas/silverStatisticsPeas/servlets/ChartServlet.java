/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
 * @author BERTINL TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
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

  /*
   * (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest ,
   * javax.servlet.http.HttpServletResponse)
   */
  protected void service(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    try {
      String chartName = request.getParameter("chart");
      Chart chart = (Chart) request.getSession().getAttribute(chartName);
      ServletEncoderHelper.encodeJPEG13(chart, 1.0f, response);
    } catch (PropertyException propertyException) {
      propertyException.printStackTrace();
    } catch (ChartDataException dataException) {
      dataException.printStackTrace();
    }

    request.getSession().removeAttribute(CHART);
  }
}
