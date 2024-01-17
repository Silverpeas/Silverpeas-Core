/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

(function($window, $) {


  if (!$window.PdcSettings) {
    $window.PdcSettings = new SilverpeasPluginSettings();
  }
  if (!$window.PdcBundle) {
    $window.PdcBundle = new SilverpeasPluginBundle();
  }

  var PDC_ELEMENT_ID = $window.PdcSettings.get("pdc.e.i");

  var MANDATORY_AXIS_ERROR = $window.PdcBundle.get("pdc.e.ma");

  var __api = {
    /**
     *
     * Open the PDC classification settings linked to a component instance.
     * @param instanceId an instance identifier.
     */
    openClassificationSettings : function(instanceId) {
      var url = webContext + "/RpdcUtilization/jsp/Main";
      var ajaxConfig = sp.ajaxConfig(url).withParam("ComponentId", instanceId);
      SP_openWindow(ajaxConfig.getUrl(), 'utilizationPdc1', '600', '450',
          'scrollbars=yes, resizable, alwaysRaised');
    },
    /**
     * Gets the position set.
     * @param pdcElementId the HTML element on which the jQuery plugin is initialized.
     */
    getPositions : function(pdcElementId) {
      var $pdcElement = $('#' + pdcElementId);
      if ($pdcElement.pdcClassification && $pdcElement.pdcClassification('isClassification')) {
        return $pdcElement.pdcClassification('positions');
      }
    },
    /**
     * Validate the classifications.
     * @param pdcElementId the HTML element on which the jQuery plugin is initialized.
     */
    validateClassification : function(pdcElementId) {
      var $pdcElement = $('#' + pdcElementId);
      if ($pdcElement.pdcClassification && $pdcElement.pdcClassification('isClassification') &&
          !$pdcElement.pdcClassification('isClassificationValid')) {
        SilverpeasError.add(MANDATORY_AXIS_ERROR);
        return false;
      }
      return true;
    }
  }

  var __templateFileRequestConfig = function(templateFileName) {
    return sp.ajaxConfig(webContext + '/util/javaScript/angularjs/directives/util/' +
        templateFileName);
  }

  /**
   * This directive must be explicitly used only when no one of others are used.
   * Indeed, the others used implicitly the directive and provides the api.
   */
  angular.module('silverpeas.directives').directive('silverpeasPdcClassificationManagement',
      [function() {
        return {
          template : '<div ng-if="false"></div>',
          restrict : 'E',
          scope : {
            api : '=?',
            instanceId : '@'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : [function() {
            this.api = {
              /**
               * Open the PDC classification settings linked to a component instance.
               */
              openClassificationSettings : function() {
                __api.openClassificationSettings(this.instanceId);
              }.bind(this),
              /**
               * Gets the current positions of the PDC.
               */
              getPositions : function() {
                return __api.getPositions(PDC_ELEMENT_ID);
              }.bind(this),
              /**
               * Validate the classifications.
               */
              validateClassification : function() {
                return __api.validateClassification(PDC_ELEMENT_ID);
              }.bind(this)
            };
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasPdcClassificationNew',
      [function() {
        return {
          template : '<silverpeas-pdc-classification-management api="$ctrl.api" instance-id="$ctrl.instanceId"></silverpeas-pdc-classification-management>' +
                     '<div ng-include src="$ctrl.getTemplateUrl()"></div>',
          restrict : 'E',
          scope : {
            api : '=?',
            instanceId : '@'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : [function() {
            //function used on the ng-include to resolve the template
            this.getTemplateUrl = function() {
              var config =  __templateFileRequestConfig('silverpeas-pdc-classification-new-wrapper.jsp');
              config.withParam('instanceId', this.instanceId);
              return config.getUrl();
            }
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasPdcClassificationView',
      [function() {
        return {
          template : '<silverpeas-pdc-classification-management api="$ctrl.api" instance-id="$ctrl.instanceId"></silverpeas-pdc-classification-management>' +
                     '<div ng-include src="$ctrl.getTemplateUrl()"></div>',
          restrict : 'E',
          scope : {
            api : '=?',
            preview : '=',
            instanceId : '@',
            resourceId : '@'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : [function() {
            //function used on the ng-include to resolve the template
            this.getTemplateUrl = function() {
              var config =  __templateFileRequestConfig('silverpeas-pdc-classification-view-wrapper.jsp');
              config.withParam('instanceId', this.instanceId);
              config.withParam('resourceId', this.resourceId);
              config.withParam('mode', this.preview ? 'preview' : undefined);
              return config.getUrl();
            }
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasPdcClassificationEdit',
      [function() {
        return {
          template : '<silverpeas-pdc-classification-management api="$ctrl.api" instance-id="$ctrl.instanceId"></silverpeas-pdc-classification-management>' +
                     '<div ng-include src="$ctrl.getTemplateUrl()"></div>',
          restrict : 'E',
          scope : {
            api : '=?',
            instanceId : '@',
            resourceId : '@'
          },
          controllerAs : '$ctrl',
          bindToController : true,
          controller : [function() {
            //function used on the ng-include to resolve the template
            this.getTemplateUrl = function() {
              var config =  __templateFileRequestConfig('silverpeas-pdc-classification-edit-wrapper.jsp');
              config.withParam('instanceId', this.instanceId);
              config.withParam('resourceId', this.resourceId);
              return config.getUrl();
            }
          }]
        };
      }]);

  angular.module('silverpeas.directives').directive('silverpeasPdcFilter', [function() {
    return {
      templateUrl : webContext + '/util/javaScript/angularjs/directives/util/silverpeas-pdc-filter.jsp',
      restrict : 'E',
      scope : {
        api : '=?',
        instanceIds : '=',
        filterOnChange : '=',
        showCounters : '=',
        onFilter : '&'
      },
      controllerAs : '$ctrl',
      bindToController : true,
      controller : ['$element', '$timeout', function($element, $timeout) {

        this.api = {
          filter : function() {
            if (this.activated) {
              var values = __getJQueryPdcElement().pdc('selectedValues');
              sp.search.on({appId : this.instanceIds, taxonomyPosition : values.flatten()}).then(
                  function(result) {
                    var eventIdsToFilterOn = result.extractElementAttribute('id');
                    this.onFilter({eventIds : eventIdsToFilterOn});
                  }.bind(this));
            }
          }.bind(this),
          refresh : function() {
            if (this.activated) {
              var values = __getJQueryPdcElement().pdc('selectedValues');
              __initJQueryPlugin(values);
            }
          }.bind(this),
          raz : function() {
            if (this.activated) {
              this.onFilter({eventIds : []});
            }
            __initJQueryPlugin([]);
          }.bind(this)
        };

        this.$onInit = function() {
          this.showFilters = false;
          this.activated = false;
        }.bind(this);

        this.$postLink = function() {
          this.api.raz([]);
        }.bind(this);

        function __getJQueryPdcElement() {
          return $('.filters', $element);
        }

        var __initJQueryPlugin = function(selectedValues) {
          var settings = {
            classifiedContentCount : this.showCounters,
            labelInsideSelect : true,
            values : selectedValues,
            workspace : 'current',
            component : this.instanceIds,
            withSecondaryAxis : false,
            onLoaded : function(loadedPdC) {
              __handleSearchAccess();
              if (loadedPdC && loadedPdC.axis.length) {
                $timeout(function() {
                  this.showFilters = true;
                }.bind(this), 0);
              } else {
                $timeout(function() {
                  this.showFilters = false;
                }.bind(this), 0);
              }
            }.bind(this),
            onAxisChanged : function() {
              __handleSearchAccess()
            }
          };
          __getJQueryPdcElement().pdc('used', settings);
        }.bind(this);

        var __handleSearchAccess = function () {
          $timeout(function() {
            $('select', $element).each(function(index, axis) {
              if (axis.value === '-1') {
                axis.classList.add('no-value');
              } else {
                axis.classList.remove('no-value');
              }
            });
          }, 0);
          var $button = $('.filter-button .sp_button', $element);
          var values = __getJQueryPdcElement().pdc('selectedValues');
          if (values.length > 0) {
            this.activated = true;
            $button.removeClass('disabled');
            if (this.filterOnChange) {
              this.api.filter();
            }
          } else {
            if (this.activated) {
              this.onFilter({eventIds : []});
            }
            this.activated = false;
            $button.addClass('disabled');
          }
        }.bind(this);
      }]
    };
  }]);
})(window, jQuery);
