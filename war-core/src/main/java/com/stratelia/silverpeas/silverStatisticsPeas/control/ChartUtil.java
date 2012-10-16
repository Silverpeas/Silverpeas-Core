/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

/*
 * Created on 16 juin 2005
 *
 */
package com.stratelia.silverpeas.silverStatisticsPeas.control;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import org.jCharts.axisChart.AxisChart;
import org.jCharts.chartData.AxisChartDataSet;
import org.jCharts.chartData.ChartDataException;
import org.jCharts.chartData.DataSeries;
import org.jCharts.chartData.PieChartDataSet;
import org.jCharts.nonAxisChart.PieChart2D;
import org.jCharts.properties.AxisProperties;
import org.jCharts.properties.BarChartProperties;
import org.jCharts.properties.ChartProperties;
import org.jCharts.properties.LineChartProperties;
import org.jCharts.properties.PieChart2DProperties;
import org.jCharts.properties.PointChartProperties;
import org.jCharts.properties.util.ChartFont;
import org.jCharts.types.ChartType;
import org.jCharts.types.PieLabelType;

/**
 * @author bertinl
 */
public class ChartUtil {

  /**
   * @param xAxisTitle the w axis title
   * @param yAxisTitle the y axis title
   * @param title the chart title
   * @param xAxisLabels array of axis labels
   * @param values array of axis values
   * @return an AxisChart object
   * @throws ChartDataException
   */
  public static AxisChart buildBarAxisChart(String xAxisTitle, String yAxisTitle, String title,
      String[] xAxisLabels, double[] values) throws ChartDataException {
    DataSeries dataSeries = new DataSeries(xAxisLabels, xAxisTitle, yAxisTitle, title);

    double[][] data = new double[][] { values };
    String[] legendLabels = { "Bugs" };
    BarChartProperties barChartProperties = new BarChartProperties();

    Paint[] paints = getRandomPaints(1);
    AxisChartDataSet axisChartDataSet = new AxisChartDataSet(data,
        legendLabels, paints, ChartType.BAR, barChartProperties);
    dataSeries.addIAxisPlotDataSet(axisChartDataSet);

    ChartProperties chartProperties = new ChartProperties();
    chartProperties.setTitleFont(new ChartFont(new Font("Arial", Font.BOLD, 13), Color.black));

    AxisProperties axisProperties = new AxisProperties();
    axisProperties.getXAxisProperties().setAxisTitleChartFont(new ChartFont(new Font("Arial",
        Font.BOLD, 13), Color.black));
    axisProperties.getYAxisProperties().setAxisTitleChartFont(new ChartFont(new Font("Arial",
        Font.BOLD, 13), Color.black));
    return new AxisChart(dataSeries, chartProperties, axisProperties, null, 500, 300);
  }

  /**
   * @param xAxisTitle the x axis title
   * @param yAxisTitle the y axis title
   * @param title the chart title
   * @param xAxisLabels array of x axis labels
   * @param values array of values
   * @return
   * @throws ChartDataException
   */
  public static AxisChart buildLineAxisChart(String xAxisTitle, String yAxisTitle, String title,
      String[] xAxisLabels, double[] values) throws ChartDataException {
    DataSeries dataSeries = new DataSeries(xAxisLabels, xAxisTitle, yAxisTitle, title);
    if (values.length > 0) {
      double[][] data = new double[][] { values };
      String[] legendLabels = { "Bugs" };
      LineChartProperties lineChartProperties = new LineChartProperties(
          new Stroke[] { PointChartProperties.DEFAULT_POINT_BORDER_STROKE },
          new Shape[] { PointChartProperties.SHAPE_CIRCLE });
      Paint[] paints = getRandomPaints(1);
      AxisChartDataSet axisChartDataSet = new AxisChartDataSet(data, legendLabels, paints,
          ChartType.LINE, lineChartProperties);
      dataSeries.addIAxisPlotDataSet(axisChartDataSet);
    }

    ChartProperties chartProperties = new ChartProperties();
    chartProperties.setTitleFont(new ChartFont(new Font("Arial", Font.BOLD, 13), Color.black));

    AxisProperties axisProperties = new AxisProperties();
    axisProperties.getXAxisProperties().setAxisTitleChartFont(new ChartFont(new Font("Arial",
        Font.BOLD, 13), Color.black));
    axisProperties.getYAxisProperties().setAxisTitleChartFont(new ChartFont(new Font("Arial",
        Font.BOLD, 13), Color.black));
    return new AxisChart(dataSeries, chartProperties, axisProperties, null, 500, 300);
  }

  /**
   * @param title
   * @param data
   * @param legendLabels
   * @return
   * @throws ChartDataException
   */
  public static PieChart2D buildPieChart(String title, double[] data, String[] legendLabels)
      throws ChartDataException {
    // filter data to remove unsignificant data (<1%)
    // -------------------------------------------------
    // 1 - compute one percent value
    long onePercent = 0;
    for (double aData : data) {
      onePercent += aData;
    }
    onePercent = onePercent / 100;
    // -------------------------------------------------
    // 2 - compute new datas and legends
    List<Double> vNewData = new ArrayList<Double>();
    List<String> vNewLegendLabels = new ArrayList<String>();
    for (int i = 0; i < data.length; i++) {
      if (data[i] > onePercent) {
        vNewData.add(data[i]);
        vNewLegendLabels.add(legendLabels[i]);
      }
    }
    // -------------------------------------------------
    // 3 - build new arrays
    double[] newData = new double[vNewData.size()];
    String[] newLegendLabels = new String[vNewData.size()];
    for (int i = 0; i < vNewData.size(); i++) {
      newData[i] = vNewData.get(i);
      newLegendLabels[i] = vNewLegendLabels.get(i);
    }
    Paint[] paints = getRandomPaints(newLegendLabels.length);
    // set chart properties
    PieChart2DProperties pieChart2DProperties = new PieChart2DProperties();
    pieChart2DProperties.setValueLabelFont(new ChartFont(new Font("Arial",
        Font.PLAIN, 10), Color.black));
    pieChart2DProperties.setPieLabelType(PieLabelType.LEGEND_LABELS);
    PieChartDataSet pieChartDataSet = new PieChartDataSet(title, newData, newLegendLabels, paints,
        pieChart2DProperties);
    ChartProperties chartProperties = new ChartProperties();
    chartProperties.setTitleFont(new ChartFont(new Font("Arial", Font.BOLD, 13), Color.black));
    return new PieChart2D(pieChartDataSet, null, chartProperties, 500, 350);
  }

  /*****************************************************************************************
   * Random Color generator.
   * @return Color
   ******************************************************************************************/
  protected static Color getRandomColor() {
    int transparency = (int) getRandomNumber(100, 375);
    if (transparency > 255) {
      transparency = 255;
    }

    return new Color((int) getRandomNumber(255), (int) getRandomNumber(255),
        (int) getRandomNumber(255), transparency);
  }

  /*****************************************************************************************
   * Random number generator.
   * @param maxValue
   * @return double
   ******************************************************************************************/
  public static double getRandomNumber(double maxValue) {
    return Math.random() * maxValue;
  }

  /*****************************************************************************************
   * Random number generator in specified range.
   * @param minValue
   * @param maxValue
   * @return double
   ******************************************************************************************/
  protected static double getRandomNumber(double minValue, double maxValue) {
    return (minValue + (Math.random() * (maxValue - minValue)));
  }

  /*****************************************************************************************
   * Random Paint generator.
   * @return Paint
   ******************************************************************************************/
  protected static Paint getRandomPaint() {
    if (getRandomNumber(1) > 0.5) {
      return getRandomColor();
    } else {
      float width = (float) getRandomNumber(10, 800);
      float height = (float) getRandomNumber(10, 600);
      float x = (float) getRandomNumber(0, 800);
      float y = (float) getRandomNumber(0, 600);
      return new GradientPaint(x, y, getRandomColor(), width, height, getRandomColor());
    }
  }

  /*****************************************************************************************
   * Random Color generator.
   * @return Paint[]
   ******************************************************************************************/
  public static Paint[] getRandomPaints(int numToCreate) {
    Paint paints[] = new Paint[numToCreate];
    for (int i = 0; i < numToCreate; i++) {
      paints[i] = getRandomPaint();
    }
    return paints;
  }

}
