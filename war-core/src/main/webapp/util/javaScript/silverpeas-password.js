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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

/**
 * Silverpeas plugin build upon JQuery to manage passwords.
 */
(function($) {

  $.password = {
    webServiceContext: webContext + '/services',
    rules: null,
    nbMatchingCombinedRules: null,
    combinedRules: null,
    extraRuleMessage: null,
    doInitialize : function() {
      if ($.password.rules == null) {
        __getJSonData($.password.webServiceContext + '/password/policy').then(function(policy) {
          $.password.rules = policy.rules;
          $.password.nbMatchingCombinedRules = policy.nbMatchingCombinedRules;
          $.password.combinedRules = policy.combinedRules;
          $.password.extraRuleMessage = $.trim(policy.extraRuleMessage);
        });
      }
    }
  };

  /**
   * The different password methods handled by the plugin.
   */
  var methods = {
    /**
     * Prepare UI and behavior
     */
    init: function() {
      return __init($(this));
    },
    /**
     * Verifies the entered password :
     * - displaying rules
     * - showing to user the validated and unvalidated rules
     */
    verify: function(options) {
      return __verify($(this), options)
    }
  };

  /**
   * The password Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   *
   * Here the password namespace in JQuery.
   */
  $.fn.password = function(method) {
    $.password.doInitialize();
    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.init.apply(this, arguments);
    } else {
      $.error('Method ' + method + ' does not exist on jQuery.password');
    }
    return false;
  };

  /**
   * Private method that handles the verification of a password
   * @param $targets
   * @param options
   * @private
   */
  function __verify($targets, options) {

    if (!$targets.length) {
      return $targets;
    }

    return $targets.each(function() {
      var $this = $(this);

      // Root id
      var infoBoxId = __getInfoBoxId($this);

      // Clean if necessary
      var $box = $('#' + infoBoxId);
      if ($box.length === 0) {
        $box = __prepareUI($this);
      }

      // Checking
      return __checking($this, $box, options);
    });
  }

  /**
   * Private method that handles the verification of a password
   * @param $target
   * @param $box
   * @param options
   * @private
   */
  function __checking($target, $box, options) {
    var deferred = new $.Deferred();

    // Checking
    __postJSonData($.password.webServiceContext + '/password/policy/checking',
        {value : $target.val()}).then(function(passwordCheck) {

          // Ajax concurrency
          $(document).ready(function() {

            // All rules are verified by default
            $box.find('li').each(function() {
              var $rule = $(this);
              if ($rule.attr('id')) {
                if (!$rule.hasClass('combined')) {
                  __switchStatusStyleClass($rule, 'success');
                } else {
                  if ($.inArray($rule.attr('id'), passwordCheck.combinedRuleIdsInError) < 0) {
                    __switchStatusStyleClass($rule, 'success');
                  } else {
                    __switchStatusStyleClass($rule, 'error');
                  }
                }
              }
            });

            try {
              if (passwordCheck.isCorrect) {
                if (options.onSuccess) {
                  options.onSuccess.call(this);
                }
              } else {
                // Indicate the rules in error
                $.each(passwordCheck.requiredRuleIdsInError, function(index, value) {
                  $box.find('li[id="' + value + '"]').each(function() {
                    var $rule = $(this);
                    if (!$rule.hasClass('combined')) {
                      __switchStatusStyleClass($rule, 'error');
                    }
                  });
                });
                if (!passwordCheck.isRuleCombinationRespected) {
                  __switchStatusStyleClass($box.find('li[id="' + __getCombinedRuleId() + '"]'),
                      'error');
                }
                if (options.onError) {
                  options.onError.call(this);
                }
                $box.show();
              }
            } catch (e) {
              alert("Silverpeas JQuery password plugin error ...");
              if (options.onError) {
                options.onError.call(this);
              }
              $box.show();
            } finally {
              deferred.resolve()
            }
          });
        });
    return deferred.promise();
  }

  /**
   * Private method that prepares UI and behavior.
   */
  function __init($targets) {

    if (!$targets.length) {
      return $targets;
    }

    return $targets.each(function() {
      __prepareUI($(this));
    });
  }

  /**
   * Private method that prepares UI and behavior.
   */
  function __prepareUI($target) {

    //  <div id="...">
    //    <span>[title]</span>
    //      <ul>
    //      <li id="[rule name]" class="[status style class]">[description]</li>
    //      ...
    //    </ul>
    //    <div class"extraRules">
    //      [extra rule massage from StringTemplate]
    //    </div>
    //  </div>

    // Root id
    var infoBoxId = __getInfoBoxId($target);

    // Clean if necessary
    var $box = $('#' + infoBoxId);
    if ($box.length > 0) {
      $box.remove();
    }

    // Common body
    $box = $('<div>').css('display', 'none').css('z-index', '100000').attr('id',
            infoBoxId).addClass('password-box-info');
    $box.append($('<span>').append(__getFromBundleKey('password.rule.requirements')));
    var $rules = $('<ul>');
    $box.append($rules);

    // Rules
    $.each($.password.rules, function(name, rule) {
      $rules.append($('<li>').attr('id', name).addClass('info').append(rule.description));
    });

    // Combined rules
    if (!$.isEmptyObject($.password.combinedRules)) {
      $rules.append($('<li>').attr('id',
              __getCombinedRuleId()).addClass('info').append(__getFromBundleKey('password.rule.combination',
              $.password.nbMatchingCombinedRules)));
      var $combinedRule = $('<li>');
      var $combinedRuleDetails = $('<ul>');
      $combinedRule.append($combinedRuleDetails);
      $.each($.password.combinedRules, function(name, rule) {
        $combinedRuleDetails.append($('<li>').attr('id',
                name).addClass('combined').append(rule.description));
      });
      $rules.append($combinedRule);
    }

    // Extra rules
    if ($.password.extraRuleMessage) {
      $box.append($('<div>').addClass('extraRules').append($.password.extraRuleMessage));
    }

    // Events
    $target.keyup(function(event) {
      var deferred = new $.Deferred();
      // Toggle info box display on 'Escape'
      if (event.keyCode === 27) {
        $box.toggle();
      }
      // Checking password if box displayed
      // and no 'shift' (16) key and no 'tab' key (9) and no 'Escape' and no 'Print Screen'
      if ((event.keyCode !== 44 && event.keyCode !== 27 && event.keyCode !== 9 &&
              event.keyCode !== 16) && $box.css('display') !== 'none') {
        __checking($target, $box, {}).then(function() {
          deferred.resolve();
        });
      } else {
        deferred.resolve();
      }

      deferred.then(function() {
        var deferred2 = new $.Deferred();
        // Position
        if ($box.css('display') !== 'none') {
          // Checking when displaying the box from 'Escape' key event
          if (event.keyCode === 27 && $target.val()) {
            __checking($target, $box, {}).then(function() {
              deferred2.resolve();
            });
          } else {
            deferred2.resolve();
          }
          deferred2.then(function() {
            __setBoxInfoPosition($target, $box);
          });
        }
      });
    });

    $(window).resize(function() {
      // Position
      if ($box.css('display') !== 'none') {
        __setBoxInfoPosition($target, $box);
      }
    });
    $target.focus(function() {
      var deferred = new $.Deferred();
      if ($target.val()) {
        __checking($target, $box, {}).then(function() {
          deferred.resolve();
        });
      } else {
        deferred.resolve();
      }
      deferred.then(function() {
        $box.show();
        __setBoxInfoPosition($target, $box);
      });
      return true;
    });
    $target.blur(function() {
      $box.hide();
      return true;
    });
    $box.click(function() {
      $box.hide();
      return true;
    });

    // Result : the password box info
    $box.appendTo(document.body);
    __setBoxInfoPosition($target, $box);
    return $box;
  }

  /**
   * Private method that returns the combined rule id.
   * @return {string}
   * @private
   */
  function __getCombinedRuleId() {
    return 'COMBINED_RULES';
  }

  /**
   * Private method to calculate position of the given box
   * @param $target
   * @param $box
   * @private
   */
  function __setBoxInfoPosition($target, $box) {
    var top = $target.offset().top + $target.outerHeight(true) + 8;
    var left = $target.offset().left - ($box.outerWidth(true) / 2) + ($target.outerWidth(true) / 2);
    $box.offset({top: top, left: left});
  }

  /**
   * Private method to switch between style class that represents the status of a rule.
   * @param $htmlTag
   * @param styleClass
   * @private
   */
  function __switchStatusStyleClass($htmlTag, styleClass) {
    $htmlTag.removeClass('info');
    $htmlTag.removeClass('success');
    $htmlTag.removeClass('error');
    $htmlTag.addClass(styleClass);
  }

  /**
   * Private method that centralizes the build of the password info box.
   * @param $htmlTag
   * @return {string}
   * @private
   */
  function __getInfoBoxId($htmlTag) {
    return $htmlTag.attr('id') + '_pwdInfoBox';
  }

  /**
   * Centralizes synchronous ajax request for json response.
   * @param url
   * @return {*}
   * @private
   */
  function __getJSonData(url) {
    return __performAjaxRequest({url : url, type : 'GET', dataType : 'json', async : false});
  }

  /**
   * Centralizes synchronous ajax request for json response.
   * @param url
   * @param data
   * @return {*}
   */
  function __postJSonData(url, data) {
    return __performAjaxRequest({
      url: url,
      type: 'POST',
      dataType: 'json',
      data: $.toJSON(data),
      contentType: "application/json"
    });
  }

  /**
   * Private function that performs an ajax request.
   */
  function __performAjaxRequest(settings) {
    var deferred = new $.Deferred();

    // Default options.
    // url, type, dataType are missing.
    var options = {
      cache : false,
      success : function(data) {
        deferred.resolve(data);
      },
      error : function(jqXHR, textStatus, errorThrown) {
        window.console &&
        window.console.log('Silverpeas Check Password Request - ERROR - ' + errorThrown);
        deferred.reject();
      }
    };

    // Adding settings
    options = $.extend(options, settings);

    // Ajax request
    $.ajax(options);
    return deferred.promise();
  }

// Initialization indicator.
  var __i18nInitialized = false;

  /**
   * Private method that handles i18n.
   * @param key
   * @param params
   * @return message
   * @private
   */
  function __getFromBundleKey(key, params) {
    if (webContext) {
      if (!__i18nInitialized) {
        $.i18n.properties({
          name: 'passwordBundle',
          path: webContext + '/services/bundles/org/silverpeas/password/multilang/',
          language: '$$', /* by default the language of the user in the current session */
          mode: 'map'
        });
        __i18nInitialized = true;
      }
      return $.i18n.prop(key, params);
    }
    return key;
  }

})(jQuery);