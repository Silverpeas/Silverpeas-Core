/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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
 * A JQuery plugin providing some functionalities on the classification of the Silverpeas
 * resources (publication, comments, ...) on the classification plan (named PdC).
 * The supported functionalities are:
 * - render the classification on the PdC of a given content in two modes: edition or view mode,
 * - deletion request of a given position in the classification,
 * - position edition or adding are performed externally by callbacks. Theses callbacks are
 * registered with this plugin settings.
 * The classification is expected to be formatted in JSON with at least the following attributes:
 * - uri: the URI of the classification in the web,
 * - positions: an array with the classsification's positions on the PdC.
 * Each position in a classification should have the at least the following attributes:
 * - uri: the URI of the position on the PdC in the web,
 * - id: the unique identifier of the position,
 * - values: an array with the position's values.
 * Each position's value can be either a single term or a branch of terms in an hierachical semantic
 * tree (for example Geography/France/Isère/Grenoble with a tree representing an hierarchical geographic
 * structuration). A value should contain at least the following attributes:
 * - id: the unique identifier of the value in the form of an absolute path relative to its axis,
 * - treeId: the unique identifier of the tree to which the value belongs. The identifier is empty
 * if the value is a single term,
 * - meaning: the meaning vehiculed by the value. It is either a path of terms in a hierarchic
 * semantic tree or a single term,
 * - synonyms: an array with the synonyms of this value as found in the user thesaurus.
 */
(function( $ ){

  /**
   * The parameter settings of the plugin with, for some, the default value.
   * - url: the base URL from which the classifications on the PdC are located in the web,
   * - title: the title to display with the classification area,
   * - positionLabel: the label to display with the name of a position,
   * - addition: an object about the operation of adding a new position into a given classification.
   * It has the following attributes:
   *    - call: the callback to invoke when the addition of a new position is asked. Returns true if
   *    a new position is added or false if no position is added.
   *    - icon: the icon to display as a new position add invoker,
   *    - altText: an alternative text to print with the add operation invoker.
   * - update: an object about the operation of updating a given position. It has the following
   * attributes:
   *    - call: the callback to invoke when the updating of a new position is asked. Returns true if
   *    the position is updated or false if the position is udpated.
   *    - icon: the icon to display as a new position update invoker,
   *    - altText: an alternative text to print with the update operation invoker.
   * - deletion: an object about the operation of deleting a given position. It has the following
   * attributes:
   *    - call: the callback to invoke when the deleting of a new position is asked,
   *    - icon: the icon to display as a new position deletion invoker,
   *    - altText: an alternative text to print with the deletion operation invoker.
   * - mode: the mode in which the classification on the PdC of a given resource should be rendered.
   * It accepts a value among 'view' or 'edition'. By default, an unknown value is interpreted as
   * a 'view' mode.
   */
  var settings = {
    url: 'http://localhost:8000/silverpeas/services/pdc/',
    title: 'Plan de classement',
    positionLabel: 'Position',
    addition: {
      call: function() {
        return true;
      },
      icon: '/silverpeas/pdcPeas/jsp/icons/add.gif',
      altText: 'Ajouter une nouvelle position'
    },
    update: {
      call: function( positionId ) {
        return true;
      },
      icon: '/silverpeas/pdcPeas/jsp/icons/edit_button.gif',
      altText: 'Editer la position'
    },
    deletion: {
      confirmation: 'Êtes-vous sûr de vouloir supprimer la position ?',
      icon: '/silverpeas/pdcPeas/jsp/icons/delete.gif',
      altText: 'Supprimer la position'
    },
    mode: 'view'
  };

  /**
   * The PdC namespace.
   */
  $.fn.pdc = function( options ) {
    return this.each(function() {
      var $this = $(this);
      init( options );
      renderClassification( $this );
    })
  };
  
  /**
   * Initializes the plugin with some settings passed as arguments.
   */
  function init ( options ) {
    if ( options ) {
      $.extend( true, settings, options );
    }
  }
  
  /**
   * Renders the classification on the PdC of the resource identified by the URL defined in the
   * settings.
   */
  function renderClassification ( $this ) {
    var titleTag = '<span>';
    if ($this.is('fillset')) {
      titleTag = '<legend>';
    }
    $(titleTag).html(settings.title).appendTo($this);
    if (settings.mode == 'edition') {
      $('<a href="#">').append($('<img>').addClass('add').addClass('action').attr( {
        src: settings.addition.icon, 
        alt: settings.addition.altText
      }).click(function () {
        addNewPosition($this);
      })).appendTo($this);
    }
    $('<div>', {
      id: "pdcpositions"
    }).appendTo($this);
    
    loadClassification($this);
  }

 /**
  * Adds a new position on the PdC in the classification of the resource identified by the url in
   * the settings.
  */
  function addNewPosition( $this ) {
    if (settings.addition.call()) {
      refreshClassification($this);
    }
  }
  
  /**
   * Upates the specified position in the classification of the resource identified by the url in
   * the settings.
   */
  function updatePosition( $this, positionId ) {
    if (settings.update.call(positionId)) {
      refreshClassification($this);
    }
  }
  
  /**
   * Deletes the specified position in the classification of the resource identified by the url in
   * the settings.
   */
  function deletePosition( $this, positionId ) {
    if (window.confirm( settings.deletion.confirmation )) {
      $.ajax({
        url: settings.url + "/" + positionId,
        type: "DELETE",
        success: function(data) {
          refreshClassification($this);
        }
      });
    }
  }
  
  /**
   * Refreshs the display of the classification of the resource identified by the url in
   * the settings.
   */
  function refreshClassification( $this ) {
    $('#pdcpositions').children().remove();
    loadClassification($this);
  }
  
  /**
   * Loads from the URL defined in the settings the data about the classification on the PdC of the
   * resource.
   */
  function loadClassification( $this ) {
    $.getJSON(settings.url, function(classification) {
      $.each(classification.positions, function(posindex, position) {
        var posId = posindex + 1, values =  [];
        var htmlPosition =
          $('<div>').addClass('pdcposition' + posId ).append($('<span>').html(settings.positionLabel + ' ' + posId));
            
        $.each(position.values, function(valindex, value) {
          values.push('<li>' + value.meaning + '<i>' + value.synonyms.join(', ') + '</i></li>');
        });
            
        if (settings.mode == 'edition') {
          htmlPosition.append(
          $('<a href="#">').append($('<img>').addClass('update').addClass('action').attr({
              src: settings.update.icon, 
              alt: settings.update.altText
            }).click(function () {
              updatePosition($this, position.id);
            }))).append($('<a href="#">').append(
            $('<img>').addClass('delete').addClass('action').attr({
              src: settings.deletion.icon, 
              alt: settings.deletion.altText
            }).click(function () {
              deletePosition($this, position.id);
            })));
        }
        htmlPosition.append($('<ul>').addClass('pdcvalues').html(values.join(''))).appendTo('#pdcpositions');
      });
    })
  }
})( jQuery );

