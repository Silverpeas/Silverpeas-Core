/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
 * A JQuery plugin to render a breadcrumb trail in Silverpeas web pages.
 * The trail renders each of the nodes in the trail upto the current one. A node in the breadcrumb
 * must be an object with as least the attribute name.
 * It accepts as options:
 * {
 *   root     : the root node at which the breadcrumb trail starts. If not set, the breadcrumb is
 *              not rendered until a root node is passed (through the set method).
 *   css      : a space-separated CSS classes to apply to each rendering nodes.
 *   separator: a separator to use between each nodes in the breadcrumb.
 *   onchange : a function waiting as parameter the current selected node that is called at each
 *              change in the breadcrumb trail (change of the current node in the trail).
 *   oninit   : a function invoked at setup of the breadcrumb. It can be used to perform some
 *              specific tasks with the initialization of the breadcrumb.
 * }
 *
 */
(function( $ ){

  /**
   * The methods the plugin accepts.
   */
  var methods = {
    /**
     * The default method called at plugin invocation.
     */
    init: function( options ) {
      var settings = $.extend(true, {
        root: null,
        css: '',
        separator: ' > ',
        oninit: function() {
        },
        onchange: function(node) {}
      }, options);

      return this.each(function() {
        var $this = $(this), path = [];
        $this.data('breadcrumb-settings', settings);
        $this.data('breadcrumb-trail', path);
        if (settings.root) {
          renderNode($this, settings.root);
        }
        settings.oninit();
      });
    },

    /**
     * Sets the specified node as the current one. The breadcrumb trail is then refreshed.
     * If the specified node is already in the trail, then all nodes after it are cleaned,
     * otherwise it is added in the trail as the current one (one stage added in front of others
     * nodes).
     */
    set: function( node ) {
      return this.each(function() {
        var $this = $(this), path = $this.data('breadcrumb-trail'), settings = $this.data('breadcrumb-settings');
        var position = path.indexOf(node);
        if (position > -1)
          clearNodes($this, position + 1);
        else
          renderNode($this, node)
        settings.onchange(node);
      });
    },

    /**
     * Runs the specified function with the current node in the breadcrumb.
     * It is the latest set in the trail.
     */
    current: function( dosomething ) {
      return this.each(function() {
        var $this = $(this), path = $this.data('breadcrumb-trail');
        dosomething(path[path.length - 1]);
      });
    }
  }

  $.fn.breadcrumb = function( method ) {
    if ( methods[method] ) {
      return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.init.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.breadcrumb' );
    }
  }

  function clearNodes($this, position) {
    var path = $this.data('breadcrumb-trail'), lastPosition = path.length - 1;
    path.splice(position, path.length - position);
    for (var i = lastPosition; i >= position; i--) {
      $('#breadcrumb-node-' + i).remove();
    }
  }

  function renderNode($this, node) {
    var path = $this.data('breadcrumb-trail'), settings = $this.data('breadcrumb-settings');
    var position = path.push(node);
    var $node = $('<span>', {
      id: 'breadcrumb-node-' + (--position)
      });
    if (settings.css)
      $node.attr('class', settings.css);
    if (position > 0)
      $node.append(settings.separator);

    $node.append($('<a>', {
      href: '#'
    }).text(path[position].name).click(function() {
      clearNodes($this, position + 1);
      settings.onchange(node);
    })).appendTo($this);
  }
})( jQuery );
