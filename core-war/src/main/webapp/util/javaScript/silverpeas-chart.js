/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

(function() {

  if (!window.ChartBundle) {
    window.ChartBundle = new SilverpeasPluginBundle();
  }

  /**
   * Chart Manager plugin.
   * It handles the display of charts and the services to download them.
   * @param parameters
   * @constructor
   */
  ChartManager = function(parameters) {

    var dateFormat = jQuery.datepicker.regional[jQuery.datechecker.settings.language];
    if (!dateFormat) {
      dateFormat = jQuery.datepicker.regional[''];
    }

    var params = extendsObject({
      downloadSelector : "",
      chartSelector : ".class",
      isDisplayAsBars : false,
      colors : [],
      chart : {
        axis : {
          x : {
            titleClass : 'axisLabel xaxisLabel',
            title : ''
          },
          y : {
            titleClass : 'axisLabel yaxisLabel',
            title : ''
          }
        },
        chartType : "",
        title : "",
        items : []
      },
      labels : {
        monthNames : dateFormat.monthNamesShort,
        dayNames : dateFormat.dayNamesShort
      },
      formatToolTipTitle : function(title) {
        return title;
      },
      formatToolTipValue : function(value, item) {
        return value;
      },
      onItemClickHelp : function(item) {
        return true;
      },
      onItemClick : false
    }, parameters);

    var isPieChart = params.chart.chartType === 'pie';
    var isPeriodChart = params.chart.chartType === 'period';

    if (isPieChart && !params.colors.length) {
      //noinspection JSUnresolvedVariable
      params.colors = defaultChartColors;
    }

    var plotChart;
    if (isPeriodChart) {
      plotChart = __periodChartDataToPlotChartData(params);
    } else if (isPieChart) {
      plotChart = __pieChartDataToPlotChartData(params);
    }
    params.plot = jQuery.plot(params.chartSelector, plotChart.data, extendsObject({
      grid : {
        clickable : params.onItemClick,
        hoverable : true,
        autoHighlight : true
      },
      tooltip : {
        show : true,
        defaultTheme : false,
        cssClass : 'chart-tooltip',
        shifts : {
          y : -40
        }
      }
    }, plotChart.options));

    if (params.onItemClick) {
      jQuery(params.chartSelector).bind("plotclick", function(event, pos, item) {
        if (item) {
          params.onItemClick.call(this, item.series);
        }
      });
    }

    if (params.downloadSelector) {
      var downloadContainer = document.querySelector(params.downloadSelector);
      var downloadButton = document.createElement('div');
      params.downloadContainer = downloadContainer;
      params.downloadButton = downloadButton;
      if (downloadButton.classList) {
        downloadButton.classList.add('chart-download');
      } else { // IE9...
        downloadButton.className = 'chart-download';
      }
      downloadButton.innerHTML = '<img src="' + webContext + '/util/icons/downloadFile.png" alt="?" />';
      downloadContainer.appendChild(downloadButton);
      TipManager.simpleHelp(downloadButton, ChartBundle.get("chart.download.help"));
      downloadButton.show = function() {
        if (downloadButton.style.display !== 'block') {
          downloadButton.style.display = 'block';
        }
      };
      downloadButton.hide = function() {
        if (downloadButton.style.display !== 'none') {
          downloadButton.style.display = 'none';
        }
      };
      downloadButton.addEventListener('click', function() {
        this.downloadChart();
      }.bind(this));
      var mouseEnter = function() {
        downloadButton.show();
        return false;
      };
      var mouseLeave = function() {
        downloadButton.hide();
        return false;
      };
      downloadContainer.addEventListener('mouseenter', mouseEnter);
      downloadContainer.addEventListener('mouseover', mouseEnter);
      downloadContainer.addEventListener('mouseleave', mouseLeave);
      downloadContainer.addEventListener('mouseout', mouseLeave);
    }

    /**
     * Downloads the chart.
     */
    this.downloadChart = function() {
      if (!params.downloadSelector) {
        return false;
      }
      var chartTitle = params.chart.title ? (params.chart.title + '.png') : 'image.png';
      params.downloadButton.hide();
      html2canvas(params.downloadContainer, {
        onrendered : function(canvas) {
          // canvas is the final rendered <canvas> element
          var image = canvas.toDataURL();
          download(image, chartTitle, "image/png");
        }
      });
      params.downloadButton.show();
      return true;
    };
  };


  function __formatToolTipValue(params, value, item) {
    var itemClickHelp = params.onItemClick && params.onItemClickHelp;
    if (itemClickHelp) {
      if (typeof params.onItemClickHelp === 'function') {
        itemClickHelp = params.onItemClickHelp.call(this, item.series);
      }
    }
    if (itemClickHelp) {
      if (typeof itemClickHelp !== 'string') {
        itemClickHelp = ChartBundle.get("chart.tooltip.click.help");
      }
      itemClickHelp = "<div class='click-help'>" + itemClickHelp + "</div>";
    } else {
      itemClickHelp = '';
    }
    return params.formatToolTipValue.call(this, value, item) + itemClickHelp;
  }

  function __periodChartDataToPlotChartData(params) {
    var chartItems = params.chart.items;
    var noChartItems = chartItems.length === 0;
    var plotData = [];
    var plotOptions = {
      legend : {
        show : true
      },
      axisLabels : {
        show : true
      }
    };

    var series;
    if (!params.isDisplayAsBars) {
      series = {
        points : {show : true}, lines : {show : true}, data : []
      };
      plotData.push(series);
      if (params.colors.length) {
        series.color = params.colors[0];
      } else {
        series.color = 0;
      }
    }

    for (var i = 0; i < chartItems.length; i++) {
      var periodData = chartItems[i];
      if (params.isDisplayAsBars) {
        var series = {
          bars : {
            show : true, barWidth : periodData.x.duration
          },
          data : [[periodData.x.startTime, periodData.y[0]]]
        };
        plotData.push(series);
        if (params.colors.length) {
          series.color = params.colors[i % params.colors.length];
        } else {
          series.color = 0;
        }
      } else {
        series.data.push([periodData.x.startTime, periodData.y[0]]);
      }
    }

    extendsObject(plotOptions, {
      yaxis : {
        min : 0,
        axisLabel : params.chart.axis.y.title
      },
      xaxis : {
        mode : "time",
        monthNames : params.labels.monthNames,
        dayNames : params.labels.dayNames,
        axisLabel : params.chart.axis.x.title
      }, tooltip : {
        content : function(label, xval, yval, flotItem) {
          var xaxis = params.plot.getAxes().xaxis;
          var periodLabel = flotItem.series.xaxis.tickFormatter(xval, xaxis);
          var d = new Date(xval);
          switch (params.chart.defaultPeriodType) {
            case "month":
              periodLabel = params.labels.monthNames[d.getMonth()] + ' ' + d.getFullYear();
            break;
          }
          return "<b>" + params.formatToolTipTitle(periodLabel) + "</b><br/>" +
              __formatToolTipValue(params, yval, flotItem);
        }
      }
    });

    if (noChartItems) {
      extendsObject(plotOptions.xaxis, {
        min : new Date().getTime()
      });
    }

    extendsObject(plotOptions.xaxis, {
      minTickSize : [1, params.chart.defaultPeriodType]
    });

    return {
      data : plotData, options : plotOptions
    }
  }

  function __pieChartDataToPlotChartData(params) {
    var chartItems = params.chart.items;
    var plotData = [];
    var plotOptions = {};

    for (var i = 0; i < chartItems.length; i++) {
      var pieData = chartItems[i];
      var series;
      if (params.isDisplayAsBars) {
        series = {
          bars : {
            show : true
          },
          data : [[pieData.label, pieData.value]]
        };
      } else {
        series = {label : pieData.label, data : pieData.value};
      }
      if (params.colors.length) {
        series.color = params.colors[i % params.colors.length];
      } else {
        series.color = 0;
      }
      series.srcData = pieData;
      plotData.push(series);
    }

    if (params.isDisplayAsBars) {
      extendsObject(plotOptions, {
        yaxis : {
          min : 0,
          axisLabel : params.chart.axis.y.title},
        xaxis : {
          mode : "categories", tickLength : 0,
          axisLabel : params.chart.axis.x.title},
        series : {
          bars : {
            show : true, barWidth : 0.9, align : "center"
          }
        },
        legend : {
          show : false
        },
        axisLabels : {
          show : true
        },
        tooltip : {
          content : function(label, xval, yval, flotItem) {
            return "<b>" + params.formatToolTipTitle(xval) + "</b><br/>" +
                __formatToolTipValue(params, yval, flotItem);
          }
        }
      });
    } else {
      extendsObject(plotOptions, {
        series : {
          pie : {
            show : true,
            combine : {
              color : '#999',
              threshold : chartPieCombinationThreshold,
              label : ChartBundle.get("chart.other")
            },
            radius : 1,
            label : {
              show : true,
              radius : 3 / 4,
              formatter : function(label, series) {
                return "<div class='chart-pie-label'>" + Math.round(series.percent) + "%</div>";
              }
            }
          }
        },
        legend : {
          show : true, backgroundColor : 'none', backgroundOpacity : 1
        },
        tooltip : {
          content : function(label, xval, yval, flotItem) {
            return "<b>" + params.formatToolTipTitle(label) + "</b><br/>" +
                __formatToolTipValue(params, yval, flotItem);
          }
        }
      });
    }

    return {
      data : plotData, options : plotOptions
    }
  }
})();
