/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

// Gauge centralizations.

/**
 * Create a gauge
 * @param [options.containerId='gauge'] the id of the container to transform into gauge
 * @param [options.currentValue=0] the current value of the gauge
 * @param [options.currentValueLabel=''] the label of the current value of the gauge
 * @param [options.minValue=0] the minimum value of the gauge
 * @param [options.maxValue=100] the maximum value of the gauge
 * @param [options.title=''] the title of the gauge
 * @param [options.refreshCallback] the callback to execute to refresh the gauge
 * @param [options.refreshTime=5000] the time to wait before calling the gauge refresh
 * @returns {JustGage}
 */
function createGauge(options) {
  jQuery(document).ready(function() {
    var params = {};
    params.id = !options.containerId ? 'gauge' : options.containerId;
    params.title = !options.title ? '' : options.title;
    params.value = !options.currentValue ? 0 : options.currentValue;
    params.label = !options.currentValueLabel ? '' : options.currentValueLabel;
    params.min = !options.minValue ? 0 : options.minValue;
    params.max = !options.maxValue ? 100 : options.maxValue;

    // Initializing the gauge
    var gauge = new JustGage(params);

    // Adding refresh callback if any
    if (jQuery.isFunction(options.refreshCallback)) {
      var refreshTime = !options.refreshTime ? 5000 : options.refreshTime;
      setInterval(function() {
        gauge.refresh(options.refreshCallback.call(this));
      }, refreshTime);
    }
  });
}
