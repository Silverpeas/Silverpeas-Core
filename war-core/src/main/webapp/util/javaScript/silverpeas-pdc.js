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
 * - render the classification on the PdC of a given content in two modes: edition or view mode. In
 * edition mode, a position on the PdC can be added, deleted or modified in the classification.
 * - render an area to create a classification of a resource on the PdC.
 * 
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
 * - axisId: the identifier of the axis to which it belongs.
 * - meaning: the meaning vehiculed by the value. It is either a path of terms in a hierarchic
 * semantic tree or a single term,
 * - synonyms: an array with the synonyms of this value as found in the user thesaurus.
 * 
 * In order to edit or to add a position, the PdC configured for the resource is asked to a web
 * service. The sent back PdC is expected to be formatted in JSON with at least the following
 * attributes:
 * - uri: the URI of the PdC configured for the resource,
 * - axis: an array with the different axis used in the PdC.
 * Each axis is an object that should contain at least:
 * - id: the identifier of the axis,
 * - the localized name of the axis,
 * - originValue: the value used as the origin of the axis,
 * - mandatory: if this axis must be taken into account in a position,
 * - invariantValue: if this axis is already used in a position of the resource content and the axis
 * is marked as invariant (id est it cannot be multi-valued), then the invariantValue is the value
 * used in the positions of the resource on the PdC,
 * - values: an array with the values of the axis.
 * Each value of an axis should be described by the following attributes:
 * - id: the unique identifier of the value in the form of an absolute path relative to its axis,
 * - treeId: the unique identifier of the tree to which the value belongs. The identifier is empty
 * if the value is a single term,
 * - axisId: the identifier of the axis to which it belongs,
 * - term: the localized name of the last node of this value in the tree,
 * - level: the level of this value in the tree from the axis root,
 * - ascendant: if this value is ascendant to the configured origin of the axis to which it belongs,
 * - origin: is this value the configured axis's origin,
 * - synonyms: an array with the synonyms of this value as found in the user thesaurus.
 */
(function( $ ){

  /**
   * The parameter settings of the plugin with, for some, the default value.
   * - resource: the resource for which the classification on the PdC has to be rendered or edited.
   * It has the following attributes:
   *    - context: the web context at which the resource is published (generally 'silverpeas'),
   *    - component: the identifier of the Silverpeas component instance that manages the resource.
   *    - content: the identifier of the resource's content that is classified.
   * - url: the base URL from which the classifications on the PdC are located in the web,
   * - title: the title to display with the classification area,
   * - positionLabel: the label to display with the name of a position,
   * - ok: the label to display with an 'Ok' button,
   * - cancel: the label to display with a 'Cancel' button,
   * - mandatory: an object about the information to display when the valuation of an axis is
   * mandatory: It has the following attributes:
   *    - icon: the icon representing a mandatory axis,
   *    - legend: the legend of the icon,
   * invariant: It has the following attributes:
   *    - icon: the icon representing an invariant axis,
   *    - legend: the legend of the icon,
   * - addition: an object about the operation of adding a new position into a given classification.
   * It has the following attributes:
   *    - icon: the icon to display as a new position add invoker,
   *    - title: the text associated with the adding operation.
   * - update: an object about the operation of updating a given position. It has the following
   * attributes:
   *    - icon: the icon to display as a new position update invoker,
   *    - title: the text associated with the update operation.
   * - deletion: an object about the operation of deleting a given position. It has the following
   * attributes:
   *    - icon: the icon to display as a new position deletion invoker,
   *    - title: the text associated with the deletion operation.
   * - creation: an object about the creation of the classification of a given resource. It has the
   * following attributes:
   *    - callback: function to call each time a position is added. It must accept two arguments:
   *    the number of mandatory axis and the position that is added.
   * - mode: the mode in which the classification on the PdC of a given resource should be rendered.
   * It accepts a value among 'view' or 'edition'. By default, an unknown value is interpreted as
   * a 'view' mode.
   */
  var settings = {
    resource: {
      context: '/silverpeas',
      component: '',
      content: ''
    },
    title: 'Classement',
    positionLabel: 'Position',
    positionsLabel: 'Positions',
    edition: {
      ok: 'Valider',
      cancel: 'Annuler',
      mandatoryLegend: 'Obligatoire',
      mandatoryIcon: '/silverpeas/util/icons/mandatoryField.gif',
      invariantLegend: 'invariantes',
      invariantIcon: '/silverpeas/util/icons/buletColoredGreen.gif',
      mandatoryMessage: "Le classement est obligatoire pour la création d'une publication. <br />Veuillez sélectionner une position et la valider."
    },
    addition: {
      icon: '/silverpeas/pdcPeas/jsp/icons/add.gif',
      title: 'Ajouter une nouvelle position'
    },
    update: {
      icon: '/silverpeas/util/icons/update.gif',
      title: 'Editer la position'
    },
    deletion: {
      confirmation: 'Êtes-vous sûr de vouloir supprimer la position ?',
      cannotBeDeleted: "Le contenu doit disposer au moins d'une position avec les axes obligatoires",
      icon: '/silverpeas/util/icons/delete.gif',
      title: 'Supprimer la position'
    },
    mode: 'view'
  };
  
  var methods = {
    /**
     * Renders an area within which the different axis of the PdC configured for the specified
     * Silverpeas component are presented in order to create a new classification of the specified
     * resource content on the PdC. The positions then can be retreived with the function positions
     * of this plugin.
     */
    create: function( options ) {
      return this.each(function() {
        var $this = $(this), classification = new Object();
        init( options );
        settings.mode = 'creation';
        classification.positions = []; 
        $this.data('classification', classification);
        $.getJSON(settings.pdcURI, function(pdc) {
          var selection = [];
          $this.data('pdc', pdc);
          prepareClassificationArea($this);
          renderClassificationEditionBox($this, pdc.axis, selection);
        });
      })
    },
    
    /**
     * Renders an area within which the classification of the specified resource on the PdC is
     * displayed either in a view or an editable mode. In the editable mode, a position can be added,
     * removed or updated.
     */
    open: function( options ) {
      return this.each(function() {
        var $this = $(this);
        init( options );
        if (settings.mode != 'edition') {
          settings.mode = 'view';
        }
        loadClassification( $this );
      })
    },
    
    /**
     * Gets the positions of the resource on the PdC. If no positions were set during the use of
     * this plugin, then fetch them from the remote web service. The positions are sent back as
     * the attribute of an object (a classification): {positions: [...]}. If the resource isn't
     * classified onto the PdC, then null is returned.
     */
    positions: function( options ) {
      var $this = $(this);
      if ($this.data('classification') == null) {
        init( options );
        $.getJSON(settings.classificationURI, function(classification) {
          $this.data('classification', classification);
        })
      }
      var classification = $this.data('classification');
      if (classification.positions.length == 0) {
        classification = null;
      }
      return classification;
    },
    
    /**
     * Gets the axis used in the PdC configured for the resource.
     */
    usedAxis: function( options ) {
      var $this = $(this);
      if ($this.data('pdc') == null) {
        init( options );
        $.getJSON(settings.pdcURI, function(pdc) {
          $this.data('pdc', pdc);
        });
      }
      return $this.data('pdc').axis;
    },
    
    /**
     * Is the classification of the resource valid? The classification is valid if there is at least
     * one position onto the PdC having mandatory axis.
     */
    isClassificationValid: function( ) {
      var $this = $(this), positions = $this.data('classification').positions, axis = $this.data('pdc').axis;
      if (positions.length == 0) {
        return !hasPdCMandoryAxis(axis);
      }
      return true;
    }
  }

  /**
   * The PdC namespace.
   */
  $.fn.pdc = function(method, options ) {
    if ( methods[method] ) {
      return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.init.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.pdc' );
    }
  };
  
  /**
   * Has the classification plan at least one mandatory axis?
   * When an axis is mandatory, the classification position must have a value onto this axis.
   */
  function hasPdCMandoryAxis( axis ) {
    for (var i = 0; i < axis.length; i++) {
      if (axis[i].mandatory) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Initializes the plugin with some settings passed as arguments.
   */
  function init ( options ) {
    if ( options ) {
      $.extend( true, settings, options );
    }
    settings.classificationURI = settings.resource.context + '/services/pdc/' +
    settings.resource.component + '/' + settings.resource.content;
    settings.pdcURI = settings.resource.context + '/services/pdc/' + settings.resource.component;
    if (settings.resource.content != null && settings.resource.content.length > 0) {
      settings.pdcURI = settings.pdcURI + '?contentId=' + settings.resource.content;
    }
  }
  
  /**
   * Prepares the area into which the classification on the PdC of the resource will be rendered.
   */
  function prepareClassificationArea ( $this ) {
    var titleTag = '<span>';
    if ($this.is('fieldset')) {
      titleTag = '<legend>';
    }
    $(titleTag).addClass('title').html(settings.title).appendTo($this);
    if (settings.mode != 'view') {
      var editionBox = $('<div>', {
        id: 'pdc-edition-box'
      }).addClass('fields').appendTo($this);
      if (settings.mode == 'edition') {
        editionBox.attr("style","display: none;");
      }
    }
    $('<div>').addClass('fields').append($('<div>', {
      id: 'list_pdc_position'
    }).addClass('field').
      append($('<label>', {
        'for': settings.positionsLabel
        }).html(settings.positionsLabel)).
      append($('<div>', {
        id: 'allpositions'
      }).addClass('champs'))).
    appendTo($this);
  }

  /**
   * Adds a new position on the PdC in the classification of the resource identified by the url in
   * the settings.
   */
  function addNewPosition( $this ) {
    $.getJSON(settings.pdcURI, function(pdc) {
      var selection = [];
      renderClassificationEditionBox($this, pdc.axis, selection);
      $("#pdc-edition-box").dialog({
        width: 640,
        modal: true,
        title: settings.addition.title,
        buttons: [{
          text: settings.edition.ok,
          click: function() {
            var position = aPositionWith(selection);
            if(isNotAlreadyInClassification(position, $this.data('classification')) &&
              position.values.length > 0) {
              submitPosition( $this, position );
            }
          }
        }, {
          text: settings.edition.cancel,
          click: function() {
            $( this ).dialog( "destroy" );
          }
        }
        ],
        close: function() {
          $( this ).dialog( "destroy" );
        }
      })
    });
  }
  
  /**
   * Upates the specified position in the classification of the resource identified by the url in
   * the settings.
   */
  function updatePosition( $this, positionId ) {
    $.getJSON(settings.pdcURI, function(pdc) {
      var selection = [];
      var positions = $this.data('classification').positions;
      for(var i = 0; i < positions.length; i++) {
        if (positions[i].id == positionId) {
          $.each(positions[i].values, function(valindex, value) {
            selection[value.axisId] = value;
          });
          break;
        }
      }
      renderClassificationEditionBox($this, pdc.axis, selection);
      $("#pdc-edition-box").dialog({
        width: 640,
        modal: true,
        title: settings.addition.title,
        buttons: [{
          text: settings.edition.ok,
          click: function() {
            var position = aPositionWith(selection);
            position.id = positionId;
            submitPosition($this, position);
          }
        }, {
          text: settings.edition.cancel,
          click: function() {
            $( this ).dialog( "destroy" );
          }
        }
        ],
        close: function() {
          $( this ).dialog( "destroy" );
        }
      })
    });
  }
  
  /**
   * Deletes the specified position in the classification of the resource identified by the url in
   * the settings.
   */
  function deletePosition( $this, positionId ) {
    if (window.confirm( settings.deletion.confirmation )) {
      $.ajax({
        url: settings.classificationURI + "/" + positionId,
        type: "DELETE",
        success: function() {
          var positions = $this.data('classification').positions;
          for(var i = 0; i < positions.length; i++) {
            if (positions[i].id == positionId) {
              positions.splice(i, 1);
              break;
            }
          }
          refreshClassification($this);
        },
        error: function(request) {
          if (request.status == 409) {
            alert( settings.deletion.cannotBeDeleted );
          }
        }
      });
    }
  }
  
  /**
   * Submit a new or an updated position into the classification of the resource on the PdC.
   */
  function submitPosition( $this, position ) {
    var method = "POST", uri = settings.classificationURI;
    if (position.id != null) {
      uri = settings.classificationURI + '/' + position.id;
      method = "PUT";
    }
    $.ajax({
      url: uri,
      type: method,
      data: $.toJSON(position),
      contentType: "application/json",
      dataType: "json",
      success: function(classification) {
        $("#pdc-edition-box").dialog( "destroy" );
        $this.data('classification', classification);
        refreshClassification( $this );
      }
    });
  }
  
  /**
   * Creates a position with the specified axis values.
   */
  function aPositionWith( values ) {
    var position = new Object();
    position.values = [];
    $.each(values, function(i, value) {
      if(value != null) {
        position.values.push({
          id: value.id, 
          axisId: value.axisId, 
          treeId: value.treeId,
          meaning: value.term,
          synonyms: value.synonyms
        });
      }
    });
    return position;
  }
  
  /**
   * Checks if the specified position is already defined in the classification.
   */
  function isNotAlreadyInClassification( thePosition, inClassification) {
    var isNotFound = true;
    var positions = inClassification.positions;
    for (var p = 0; p < positions.length; p++) {
      if (positions[p].values.length == thePosition.values.length) {
        for (var v = 0; v < thePosition.values.length; v++) {
          if (thePosition.values[v].id == positions[p].values[v].id) {
            isNotFound = false;
          } else {
            isNotFound = true;
            break;
          }
        }
      }
    }
    return isNotFound;
  }
   
  /**
   * Removes the specified position from the classification of the resource.
   */
  function removePositionFromClassification($this, positionId ) {
    var positions = $this.data('classification').positions;
    for(var i = 0; i < positions.length; i++) {
      if (positions[i].id == positionId) {
        positions.splice(i, 1);
        break;
      }
    }
    refreshClassification($this);
  }
  /**
   * Refreshs the display of the classification of the resource identified by the url in
   * the settings.
   */
  function refreshClassification( $this ) {
    $('#allpositions').children().remove();
    renderPositions( $this );
  }
  
  /**
   * Loads from the URL defined in the settings the data about the classification on the PdC of the
   * resource.
   */
  function loadClassification( $this ) {
    $.getJSON(settings.classificationURI, function(classification) {
      $this.data('classification', classification);
      prepareClassificationArea($this);
      renderPositions( $this );
    })
  }
  
  /**
   * Renders the positions of the resource on the PdC.
   */
  function renderPositions( $this ) {
    var positionsSection = $('<ul>').addClass('list_pdc_position').appendTo($('#allpositions'));
    $.each($this.data('classification').positions, function(posindex, position) {
      var posId = posindex + 1, values =  [];
      var currentPositionSection = $('<li>').appendTo(positionsSection);
      var positionLabel = $('<span>').addClass('pdc_position').
      html(settings.positionLabel + ' ' + posId).appendTo(currentPositionSection);
            
      $.each(position.values, function(valindex, value) {
        values.push('<li>' + value.meaning + '<i>' + value.synonyms.join(', ') + '</i></li>');
      });
            
      if (settings.mode == 'edition') {
        positionLabel.append(
          $('<a>',{
            href: '#', 
            title: settings.update.title + ' ' + posId
            }).addClass('edit').
          append($('<img>', {
            src: settings.update.icon,  
            alt: settings.update.title
            }).click(function () {
            updatePosition($this, position.id);
          }))).append($('<a>', {
          href: '#', 
          title: settings.update.title + ' ' + posId
          }).addClass('delete').
          append($('<img>', {
            src: settings.deletion.icon,  
            alt: settings.deletion.title
            }).click(function () {
            deletePosition($this, position.id);
          })));
      } else  if (settings.mode == 'creation') {
        positionLabel.append($('<a>', {
          href: '#', 
          title: settings.update.title + ' ' + posId
          }).addClass('delete').
          append($('<img>', {
            src: settings.deletion.icon,  
            alt: settings.deletion.title
            }).click(function () {
            var positions = $this.data('classification').positions;
            for(var i = 0; i < positions.length; i++) {
              if (i == posindex) {
                positions.splice(i, 1);
                break;
              }
            }
            refreshClassification($this);
          })));
      }
      currentPositionSection.append($('<ul>').html(values.join('')));
    });
    if (settings.mode == 'edition') {
      $('<a>', { href: '#' }).addClass('add_position').html(settings.addition.title).click(function() {
        addNewPosition($this);
      }).appendTo($('#allpositions'))
    }
  }
  
  /**
   * Renders an area for displaying the dialog box for editing the classification of the resource.
   */
  function renderClassificationEditionBox( $this, theAxis, selectedValues ) {
    $('#pdc-edition-box').children().remove();
    renderPdCAxisFields($this, theAxis, $('#pdc-edition-box'), selectedValues);
  }
  
  /**
   * Renders the fields corresponding to each PdC's axis from which a value for a position can be
   * choosen.
   * The fields will be children elements to the specified parent element.
   * The third parameter is an array that can contain the previously values of a position (in the
   * case of a position modification) and that will receive the new values for the position.
   */
  function renderPdCAxisFields( $this, theAxis, axisSection, selectedValues ) {
    if (hasPdCMandoryAxis(theAxis) && settings.mode == 'creation') {
      $('<div>').addClass('inlineMessage').html(settings.edition.mandatoryMessage).appendTo(axisSection);
    }
    $.each(theAxis, function(axisindex, anAxis) {
      var currentAxisDiv = $('<div>').addClass('champs').appendTo($('<div>').addClass('field').
        append($('<label >', {'for': anAxis.id}).addClass('txtlibform').html(anAxis.name)).
        appendTo(axisSection));
      var axisValuesSelection = $('<select>', { 'id': anAxis.id,  'name': anAxis.name }).appendTo(currentAxisDiv);
      $.each(anAxis.values, function(valueindex, aValue) {
        if (aValue.id != '/0/') {
          var level = '', optionAttr = 'value="' + aValue.id + '"';
          for (var i = 0; i < aValue.level; i++) {
            level = level + '&nbsp;&nbsp;';
          }
          if (aValue.ascendant) {
            optionAttr = 'value="A" class="intfdcolor51" disabled="disabled"';
          }
          if ((selectedValues[anAxis.id] != null && aValue.id == selectedValues[anAxis.id].id) ||
            (aValue.id == anAxis.invariantValue) ||
            (anAxis.mandatory && (aValue.origin || (selectedValues[anAxis.id] == null)))) {
            optionAttr = optionAttr + ' selected="selected"';
            selectedValues[anAxis.id] = aValue;
          }
          $('<option ' + optionAttr + '>').html(level + aValue.term).appendTo(axisValuesSelection).click(function() {
            selectedValues[anAxis.id] = aValue;
          });
        }
      });
      if (selectedValues[anAxis.id] == null) {
        $('<option value="-" selected="selected">').appendTo(axisValuesSelection).click(function() {
          selectedValues[anAxis.id] = null;
        });
      }
      if (selectedValues[anAxis.id] != null) {
        $('<span>').html('<i>' + selectedValues[anAxis.id].synonyms.join(', ') + '</i>').appendTo(currentAxisDiv);
      }
      if (anAxis.mandatory) {
        $('<img>', {
          src: settings.edition.mandatoryIcon,
          alt: settings.edition.mandatoryLegend, 
          width: '5px'
        }).appendTo(currentAxisDiv);
      }
      if (anAxis.invariantValue != null) {
        $('<img>', {
          src: settings.edition.invariantIcon, 
          alt: settings.edition.invariantLegend,
          width: '5px'
        }).appendTo(currentAxisDiv);
      }
    });
    
    if (settings.mode == 'creation') {
      axisSection.append($('<br>').attr('clear', 'all')).
      append($('<a>', {
        'id': 'valid_position', 
        'href': '#'
      }).addClass('add_position').html(settings.edition.ok).click(function() {
        var position = aPositionWith(selectedValues);
        var classification = $this.data('classification');
        if (isNotAlreadyInClassification(position, classification) && position.values.length > 0) {
          classification.positions.push(position);
          refreshClassification($this);
        }
      }));
    }
    if (theAxis.length > 0) {
      $('<p>').addClass('legende').append($('<span>').html('(')).append($('<img>', {
        src: settings.edition.mandatoryIcon, 
        alt: settings.edition.mandatoryLegend,
        width: '5px'
      })).
      append($('<span>').html('&nbsp;:' + settings.edition.mandatoryLegend + ', ')).append(
        $('<img>', {
          src: settings.edition.invariantIcon, 
          alt: settings.edition.invariantLegend,
          width: '10px'
        })).
      append($('<span>').html('&nbsp;:' + settings.edition.invariantLegend + ')')).appendTo(axisSection);
    }
  }
})( jQuery );

