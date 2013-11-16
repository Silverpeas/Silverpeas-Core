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
 * Assert (Unit Test).
 * User: Yohann Chastagnier
 */

var count;
var TEST_NOT_DEFINED = 'Not defined ...';

/**
 * Centralized assertion method.
 * @param test
 */
function assertThat(test) {
  beforAllTests();

  // Default values
  test = $.extend({title : 'N/A', expected : TEST_NOT_DEFINED, test : TEST_NOT_DEFINED}, test);
  test.title = test.title.replace(/[\n]/g, "<br/>");
  test.expected = test.expected.replace(/[\n]/g, "<br/>");

  // Display
  var $table = $('<div>').addClass('test-table').css('display', 'table');
  $('.test-results').append($table);

  var $title = $('<div>').addClass('test-title').css('display', 'table-row');
  var $expected = $('<div>').addClass('test-expected').css('display', 'table-row');
  var $test = $('<div>').addClass('test-test').css('display', 'table-row');
  $table.append($title).append($expected).append($test);

  var $titleLeft = $('<div>').addClass('test-left').css('display', 'table-cell').html("Title");
  var $titleRight = $('<div>').addClass('test-right').css('display', 'table-cell').html(test.title);
  $title.append($titleLeft).append($titleRight);

  var $expectedLeft = $('<div>').addClass('test-left').css('display',
      'table-cell').html("Expected");
  var $expectedRight = $('<div>').addClass('test-right').css('display',
      'table-cell').html(test.expected);
  $expected.append($expectedLeft).append($expectedRight);

  var $testLeft = $('<div>').addClass('test-left').css('display', 'table-cell').html("Test result");
  var $testRight = $('<div>').addClass('test-right').css('display', 'table-cell').html(test.test);
  $test.append($testLeft).append($testRight);

  // Test
  count.total++;
  if (test.expected == TEST_NOT_DEFINED || test.test == TEST_NOT_DEFINED) {
    $expectedRight.addClass('test-not-defined');
    $testRight.addClass('test-not-defined');
    count.notDefined++;
  } else {
    if (test.expected == test.test) {
      $testRight.addClass('test-success');
      count.success++;
    } else {
      $testRight.addClass('test-failed');
      count.failed++;
    }
  }
  displayCounts();
}

/**
 * Displaying unit test results
 */
function displayCounts() {
  count.$div.html('');
  count.$div.append($('<span>').addClass('label').append('Nb test : ').attr('title',
          'Click : toggle hidden & showed results\nDblClick : show all results').click(function(e) {
        $('.test-table').toggle();
      }).dblclick(function(e) {
        $('.test-table').show();
      }));
  count.$div.append($('<span>').addClass('value').append(count.total));
  count.$div.append($('<span>').addClass('label').append('Nb success : ').attr('title',
          'Click : toggle successful results').click(function(e) {
        $('.test-success').parents('.test-table').toggle();
      }));
  count.$div.append($('<span>').addClass('value').append(count.success));
  count.$div.append($('<span>').addClass('label').append('Nb failed : ').attr('title',
          'Click : toggle failed results').click(function(e) {
        $('.test-failed').parents('.test-table').toggle();
      }));
  count.$div.append($('<span>').addClass('value').append(count.failed));
  count.$div.append($('<span>').addClass('label').append('Nb not defined : ').attr('title',
          'Click : toggle not defined results').click(function(e) {
        $('.test-not-defined').parents('.test-table').toggle();
      }));
  count.$div.append($('<span>').addClass('value').append(count.notDefined));

  // Showong only errors
  count.$div.removeClass('test-not-defined');
  count.$div.removeClass('test-failed');
  count.$div.removeClass('test-success');
  if (count.failed > 0) {
    count.$div.addClass('test-failed');
    $('.test-table').hide();
    $('.test-failed').parents('.test-table').show();
  } else if (count.notDefined > 0) {
    count.$div.addClass('test-not-defined');
    $('.test-table').hide();
    $('.test-not-defined').parents('.test-table').show();
  } else {
    count.$div.addClass('test-success');
  }
}

/**
 * Initialization performed before all tests
 */
function beforAllTests() {
  if (typeof count === 'undefined') {
    count = {
      $div : null,
      total : 0,
      notDefined : 0,
      failed : 0,
      success : 0
    }

    count.$div = $('<div>').addClass('test-right').css('display', 'table-cell');
    $(document.body).append($('<div>').addClass('test-counts').css('display',
            'table').append($('<div>').addClass('test-left').css('display',
            'table-cell')).append(count.$div).append($('<div>')));
    $(document.body).append($('<div>').addClass('test-results'));
  }
}