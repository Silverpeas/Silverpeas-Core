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
 * GNU Affero General Public Liceanse for more details.
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
 * - get the positions that were added through the previous function,
 * - validate the classification of a resource is valid.
 * 
 * The classification is expected to be formatted in JSON as:
 * {
 *   uri: the URI of the classification in the Web,
 *   modifiable: a property indicating if this classification can be edited, 
 *   positions: [ the positions on the PdC ]
 * }
 * Each position of a classification is represented in JSON as:
 * {
 *  uri: the URI of the position on the PdC in the Web,
 *  id: the position unique identifier,
 *  values: [ the position's values on some of the PdC's axis ]
 * }
 * Each value on a PdC's axis is represented in JSON as:
 * {
 *  id: the unique identifier of the value in the form of an absolute path of node identifier in
 *      the sementic tree representing the axis,
 *  treeId: the unique identifier of the sementic tree to which the value belongs (an empty value
 *          means the value is a single one (no hierarchic value representation),
 *  axisId: the unique identifier of the axis to which the value belongs,
 *  meaning: the meaning vehiculed by the value. It is either a path of terms in a hierarchic
 *           sementic tree or a single term (for a single value),
 *  synonyms: [ the synonyms of the value term as strings ]
 * }         
 * 
 * In order to edit or to add a position, the PdC configured for the resource is asked to a web
 * service. The sent back PdC is expected to be formatted in JSON as:
 * {
 *  uri: the URI of the PdC configured for the resource,
 *  axis: [ the different PdC axis used by the component instance to classify its contents ]
 * }
 * Each axis is an object formatted in JSON as:
 * {
 *  id: the identifier of the axis,
 *  name: the localized name of the axis,
 *  originValue: the value used as the origin of the axis,
 *  mandatory: if this axis must be taken into account in a resource classification,
 *  invariant: if the axis is an invariant one (meaning that a classification with this axis can
 *             have only one value, whatever the positions),
 *  invariantValue: if the axis is invariant, the identifier of the invariant value used. If null,
 *                  then the invariant value isn't already set in the classification of the given
 *                  resource,
 *  values: [ the values of this axis ]
 * }
 * Each value of an axis should be described in JSON as:
 * {
 *  id: the unique identifier of the value in the form of an absolute path of node identifier in
 *      the sementic tree representing the axis,
 *  treeId: the unique identifier of the sementic tree to which the value belongs (an empty value
 *          means the value is a single one (no hierarchic value representation),
 *  axisId: the unique identifier of the axis to which the value belongs,
 *  term: the localized name of the value,
 *  level: the level of this value in the hierarchic sementic tree from the axis root,
 *  ascendant: is the value an ascendant one from the axis origin that was configured for the
 *             component instance,
 *  origin: is this value the configured (or the default one) axis origin,
 *  synonyms: [ the synonyms of the value term as strings ]
 * }
 */
(function( $ ){

  /**
   * The parameter settings of the plugin with, for some, the default value.
   */
  var settings = {
    /**
     * The resource for which the classification on the PdC has to be rendered or edited. It is
     * defined by the web application context under which the resource is published, the component
     * instance that handle it and the identifier of the content to classify or being classified.
     * Optionally, the node to which the content belongs can be indicated. For the creation of a
     * new content, the node has to be indicated so that a default classification associated with it
     * can be requested.
     */
    resource: {
      context: '/silverpeas',
      component: '',
      content: '',
      node: ''
    },
    /**
     * The title of the HTML section that will be rendered by this plugin.
     */
    title: 'Classement',
    /**
     * The prefix to use when labelling a given position.
     */
    positionLabel: 'Position',
    /**
     * The label to use for the HTML section into which the positions of the resource will be rendered.
     */
    positionsLabel: 'Positions',
    /**
     * The label to use for the HTML section into which the positions of a predefined classification
     * coming from a parent node will be rendered.
     */
    inheritedPositionsLabel: "Positions du thème parent",
    /**
     * The messages to print when the validation of a classification fails. The messages depend on
     * the type of the validation failure.
     */
    messages: {
      mandatoryMessage: "Le classement est obligatoire pour la création d'une publication. <br />Veuillez sélectionner une position et la valider.",
      contentMustHaveAPosition: "Le contenu doit disposer au moins d'une position avec les axes obligatoires",
      positionAlreayInClassification: "La position existe déjà",
      positionMustBeValued: "Veuillez sélectionner au moins une valeur à la position",
      inheritanceMessage: "Vous avez la possibilité de définir un classement par défaut pour les publications de ce thème. <br />Sinon ce thème utilisera le classement défini pour le thème parent."
    },
    /**
     * The different label to use when rendering the HTML section for updating or adding a position
     * onto the PdC 
     */
    edition: {
      ok: 'Valider',
      cancel: 'Annuler',
      mandatoryLegend: 'Obligatoire',
      mandatoryIcon: '/silverpeas/util/icons/mandatoryField.gif',
      invariantLegend: 'invariantes',
      invariantIcon: '/silverpeas/util/icons/buletColoredGreen.gif',
      mandatoryAxisDefaultValue: 'Veuillez selectionner une valeur'
    },
    /**
     * The attributes of the position adding trigger.
     */ 
    addition: {
      icon: '/silverpeas/pdcPeas/jsp/icons/add.gif',
      title: 'Ajouter une nouvelle position'
    },
    /**
     * The attribute of the position update trigger.
     */
    update: {
      icon: '/silverpeas/util/icons/update.gif',
      title: 'Editer la position'
    },
    /**
     * The attribute of the position deletion trigger with a confirmation message.
     */
    deletion: {
      confirmation: 'Êtes-vous sûr de vouloir supprimer la position ?',
      icon: '/silverpeas/util/icons/delete.gif',
      title: 'Supprimer la position'
    },
    /**
     * The mode under which this plugin has to be ran. By default, in a view mode.
     * The mode can be either 'view' (for a read-only classification rendering) or 'edition'.
     */
    mode: 'view'
  };
  
  var methods = {
    /**
     * Renders an area within which the different axis of the PdC configured for the specified
     * Silverpeas component are presented in order to create a prefefined classification on the PdC
     * for the contents that are published in a given topic of the specified Silverpeas component.
     */
    predefine: function( options ) {
      return this.each(function() {
        var $this = $(this);
        init( options );
        settings.mode = 'predefinition';
        loadPdC($this, function ($this) {
          loadClassification($this, settings.defaultClassificationURI, function($this, uri) {
            var classification = $this.data('classification');
            renderClassificationFrame($this);
            if (isInherited(classification)) {
              $.each(classification.positions, function(index, position) {
                position.id = null;
              });
            }
            renderClassificationEditionFrame($this, []);
            renderPositions($this);
          });
        });
      })
    },
    
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
        loadPdC($this, function($this) {
          loadClassification( $this, settings.defaultClassificationURI, function($this, uri) {
            var classification = $this.data('classification');
            classification.uri = settings.classificationURI;
            renderClassificationFrame($this);
            renderClassificationEditionFrame($this, []);
            if (classification.modifiable) {
              renderPositions($this)
            } else {
              $this.attr('style', 'display: none;')
            }
          });
        })
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
        loadPdC($this, function($this) {
          loadClassification( $this, settings.classificationURI, function($this, uri) {
            var classification = $this.data('classification');
            if (classification.positions.length == 0 && settings.mode == 'view') {
              $this.hide();
            } else {
              renderClassificationFrame($this);
              renderPositions( $this );
            }
          });
        })
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
        loadClassification($this, settings.classificationURI, function($this, uri) {});
      }
      var classification = $this.data('classification');
      if (classification.positions.length == 0) {
        classification = null;
      }
      return classification;
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
    settings.defaultClassificationURI = settings.resource.context + '/services/pdc/' + settings.resource.component + '/classification';
    if (settings.resource.node != null && settings.resource.node.length > 0) {
      settings.defaultClassificationURI += '?nodeId=' + settings.resource.node;
    }
    settings.classificationURI = settings.resource.context + '/services/pdc/' + settings.resource.component + '/';
    if (settings.resource.content != null && settings.resource.node.length > 0) {
      settings.classificationURI += settings.resource.content;
    } else {
      settings.classificationURI += 'new';
    }
    settings.pdcURI = settings.resource.context + '/services/pdc/' + settings.resource.component;
    if (settings.resource.content != null && settings.resource.content.length > 0) {
      settings.pdcURI = settings.pdcURI + '?contentId=' + settings.resource.content;
    }
  }
  
  /**
   * Renders the area into which the classification on the PdC of the resource will be displayed.
   */
  function renderClassificationFrame ( $this ) {
    if (settings.mode == 'predefinition' && settings.resource.node != null && settings.resource.node > 0)
      $('<div>').addClass('inlineMessage').html(settings.messages.inheritanceMessage).appendTo($this);
    var titleTag = $('<div>').append($('<h4>').addClass('clean').html(settings.title));
    if ($this.is('fieldset')) {
      titleTag = $('<legend>').html(settings.title);
    }
    titleTag.addClass('header').appendTo($this);
    if (settings.mode != 'view') {
      var editionBox = $('<div>', {
        id: 'pdc-addition-box'
      }).addClass('fields').appendTo($this);
      if (settings.mode == 'edition') {
        editionBox.attr("style","display: none;");
      }
      $('<div>', {
        id: 'pdc-update-box',
        'style': 'display-none'
      }).addClass('fields').appendTo($this);
    }
    var listOfPositions = $('<div>', {
      id: 'list_pdc_position'
    });
    if (settings.mode != 'view') {
      var classification = $this.data('classification'), positionsLabel = settings.positionsLabel;
      if (isInherited(classification)) {
        positionsLabel = settings.inheritedPositionsLabel;
      }
      listOfPositions.addClass('field').
      append($('<label>', {
        'for': 'allpositions'
      }).html(positionsLabel));
      listOfPositions.append($('<div>', {
        id: 'allpositions'
      }).addClass('champs')).appendTo($('<div>').addClass('fields').appendTo($this));
    } else {
      listOfPositions.append($('<div>', {
        id: 'allpositions'
      })).appendTo($this);
    }
  }

  /**
   * Adds a new position on the PdC in the classification of the resource identified by the url in
   * the settings.
   */
  function addNewPosition( $this, uri ) {
    var selection = [];
    renderClassificationEditionFrame($this, selection);
    $("#pdc-addition-box").dialog({
      width: 640,
      modal: true,
      title: settings.addition.title,
      buttons: [{
        text: settings.edition.ok,
        click: function() {
          var position = aPositionWith(selection);
          if(checkPositionIsValid($this, position)) {
            submitPosition( $this, uri, position );
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
  }
  
  /**
   * Upates the specified position in the classification of the resource identified by the specified
   * url.
   */
  function updatePosition( $this, uri, position ) {
    var selection = [];
    $.each(position.values, function(valindex, value) {
      selection[value.axisId] = value;
    });
    renderClassificationUpdateFrame($this, selection);
    $("#pdc-update-box").dialog({
      width: 640,
      modal: true,
      title: settings.update.title,
      buttons: [{
        text: settings.edition.ok,
        click: function() {
          var updatedPosition = aPositionWith(selection);
          updatedPosition.id = position.id;
          if(checkPositionIsValid($this, updatedPosition)) {
            if (settings.mode == 'predefinition') {
              var classification = $this.data('classification');
              removePosition(position.id, classification);
              classification.positions.push(updatedPosition);
              submitClassification($this, uri);
            } else {
              submitPosition($this, uri, updatedPosition);
            }
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
  }
  
  /**
   * Deletes the specified position in the classification of the resource identified by the url in
   * the settings.
   */
  function deletePosition( $this, uri, positionId ) {
    if (window.confirm( settings.deletion.confirmation )) {
      var uri_parts = uri.match(/[a-zA-Z0-9:=\/]+/gi);
      var uri_position = uri_parts[0] + '/' + positionId + '?' + uri_parts[1];
      $.ajax({
        url: uri_position,
        type: "DELETE",
        success: function() {
          var classification = $this.data('classification');
          removePositionFromClassification($this, classification, positionId);
        },
        error: function(request) {
          if (request.status == 409) {
            alert( settings.messages.contentMustHaveAPosition );
          }
        }
      });
    }
  }
  
  function submitClassification( $this, uri ) {
    var method = 'POST', classification = $this.data('classification');
    if (settings.mode == 'predefinition') {
      if (!isInherited(classification)) {
        method = 'PUT';
      } else {
        if (classification.positions.length == 0) {
          return;
        }
        $.each(classification.positions, function(index, position) {
          position.id = null;
        });
      }
    }
    $.ajax({
      url: uri,
      type: method,
      data: $.toJSON(classification),
      contentType: "application/json",
      dataType: "json",
      cache: false,
      success: function(classification) {
        $("#pdc-update-box").dialog( "destroy" );
        $this.data('classification', classification);
        if (isInherited(classification))
          $('label[for="allpositions"]').html(settings.inheritedPositionsLabel);
        else
          $('label[for="allpositions"]').html(settings.positionsLabel);
        refreshClassification( $this );
      },
      error: function(jqXHR, textStatus, errorThrown) {
        alert(errorThrown);
      }
    });
  }
  
  /**
     * Submit a new or an updated position into the classification on the PdC of the resource
     * identified by the specified URI.
     */
  function submitPosition( $this, uri, position ) {
    if (checkPositionIsValid($this, position)) {
      if (settings.mode == 'edition' || settings.mode == 'predefinition') {
        var method = "POST", uri_position = uri;
        if (position.id != null) {
          var uri_parts = uri.match(/[a-zA-Z0-9:=\/]+/gi);
          uri_position = uri_parts[0] + '/' + position.id + '?' + uri_parts[1];
          method = "PUT";
        }
        $.ajax({
          url: uri_position,
          type: method,
          data: $.toJSON(position),
          contentType: "application/json",
          dataType: "json",
          cache: false,
          success: function(classification) {
            if (settings.mode == 'edition') {
              $("#pdc-addition-box").dialog( "destroy" );
            }
            $("#pdc-update-box").dialog( "destroy" );
            $this.data('classification', classification);
            refreshClassification( $this );
          },
          error: function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
          }
        });
      } else {
        var positions = $this.data('classification').positions;
        for (var ipos = 0; ipos < positions.length; ipos++) {
          if (positions[ipos].id == position.id) {
            positions[ipos] = position;
          }
        }
        $("#pdc-update-box").dialog( "destroy" );
        refreshClassification( $this );
      }
    }
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
          meaning: value.meaning,
          synonyms: value.synonyms
        });
      }
    });
    return position;
  }
  
  function isInherited(classification) {
    return settings.mode == 'predefinition' &&
    escape(classification.uri).search(escape(settings.defaultClassificationURI)) == -1;
  }
  
  /**
     * Checks if the specified position is already defined in the classification.
     */
  function isAlreadyInClassification( thePosition, inClassification) {
    var isFound = false;
    var positions = inClassification.positions;
    for (var p = 0; p < positions.length; p++) {
      if (positions[p].values.length == thePosition.values.length) {
        for (var v = 0; v < thePosition.values.length; v++) {
          if (thePosition.values[v].id == positions[p].values[v].id) {
            isFound = true;
          } else {
            isFound = false;
            break;
          }
        }
        if (isFound) {
          break;
        }
      }
    }
    return isFound;
  }
  
  /**
     * Checks the mandatory axis among the specified axis are valued by the specified position.
     */
  function areMandatoryAxisValued( theAxis, thePosition ) {
    for (var iaxis = 0; iaxis < theAxis.length; iaxis++) {
      if (theAxis[iaxis].mandatory) {
        var isValued = false;
        for (var ival = 0; ival < thePosition.values.length; ival++) {
          if (thePosition.values[ival].axisId == theAxis[iaxis].id) {
            isValued = true;
            break;
          }
        }
        if (!isValued) return false;
      }
    }
    return true;
  }
  
  function checkPositionIsValid( $this, thePosition) {
    var theAxis = $this.data('pdc').axis, classification = $this.data('classification'), isValid = true;
    if (!areMandatoryAxisValued(theAxis, thePosition)) {
      isValid = false;
      alert(settings.messages.contentMustHaveAPosition);
    }
    else if (thePosition.values.length == 0) {
      isValid = false;
      alert(settings.messages.positionMustBeValued);
    } else if (isAlreadyInClassification(thePosition, classification)) {
      isValid = false;
      alert(settings.messages.positionAlreayInClassification);
    }
    return isValid;
  }
   
  /**
     * Removes the specified position from the classification of the resource.
     */
  function removePositionFromClassification($this, classification, positionId ) {
    removePosition(positionId, classification);
    refreshClassification($this);
  }
  
  function removePosition(positionId, classification) {
    var positions = classification.positions;
    for(var i = 0; i < positions.length; i++) {
      if (positions[i].id == positionId) {
        positions.splice(i, 1);
        break;
      }
    }
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
     * Loads the PdC parametrized for the resource refered by in this plugin.
     * Once the PdC correctly get, the function onSuccess is then performed.
     * It is expected the function waits for as parameter $this.
     */
  function loadPdC( $this, onSuccess) {
    $.ajax({
      url: settings.pdcURI,
      dataType: 'json',
      cache: false,
      success: function(pdc) {
        $this.data('pdc', pdc);
        onSuccess( $this );
      },
      error: function(jqXHR, textStatus, errorThrown) {
        var pdc = new Object()
        pdc.axis = []; 
        $this.data('pdc', pdc);
        alert(errorThrown);
      }
    })
  }
  
  /**
     * Loads from the URL defined in the settings the data about the classification on the PdC of the
     * resource. Once the classification loaded, the function onSuccess is then executed with as
     * parameters $this and fromURI.
     */
  function loadClassification( $this, fromURI, onSuccess ) {
    $.ajax({
      url: fromURI,
      dataType: 'json',
      cache: false,
      success: function(classification) {
        $this.data('classification', classification);
        onSuccess( $this, fromURI );
      },
      error: function(jqXHR, textStatus, errorThrown) {
        var classification = new Object();
        classification.positions = []; 
        $this.data('classification', classification);
        alert(errorThrown);
      }
    });
  }
  
  /**
     * Renders the positions of the classification on the PdC of the resource.
     */
  function renderPositions( $this ) {
    var classification = $this.data('classification');
    if (classification.positions.length > 0) {
      $('label[for="allpositions"]').show();
      var positionsSection = $('<ul>').addClass('list_pdc_position').appendTo($('#allpositions'));
      $.each($this.data('classification').positions, function(posindex, position) {
        var posId = posindex + 1, values =  [];
        var currentPositionSection = $('<li>').appendTo(positionsSection);
        var positionLabel = $('<span>').addClass('pdc_position').
        html(settings.positionLabel + ' ' + posId).appendTo(currentPositionSection);

        $.each(position.values, function(valindex, value) {
          values.push(textFrom(value));
        });

        if (settings.mode != 'view') {
          positionLabel.append(
            $('<a>',{
              href: '#', 
              title: settings.update.title + ' ' + posId
            }).addClass('edit').
            append($('<img>', {
              src: settings.update.icon,  
              alt: settings.update.title
            }).click(function () {
              var uri = classification.uri;
              if (settings.mode == 'predefinition') {
                uri = settings.defaultClassificationURI;
              }
              updatePosition($this, uri, position);
            })));
         
          if (!(isInherited(classification) && classification.positions.length == 1)) {
            positionLabel.append($('<a>', {
              href: '#', 
              title: settings.deletion.title + ' ' + posId
            }).addClass('delete').
              append($('<img>', {
                src: settings.deletion.icon,  
                alt: settings.deletion.title
              }).click(function () {
                if (settings.mode == 'edition') {
                  deletePosition($this, classification.uri, position.id);
                } else if (settings.mode == 'predefinition') {
                  if(window.confirm( settings.deletion.confirmation )) {
                    removePosition(position.id, classification);
                    submitClassification($this, settings.defaultClassificationURI);
                  }
                } else
                  removePositionFromClassification($this, classification, position.id);
              })));
          }
        }
          
        currentPositionSection.append($('<ul>').html(values.join('')));
      });
    } else {
      $('label[for="allpositions"]').hide();
    }
    if (settings.mode == 'edition') {
      $('<a>', {
        href: '#'
      }).addClass('add_position').html(settings.addition.title).click(function() {
        addNewPosition($this, classification.uri);
      }).appendTo($('#allpositions'))
    }
  }
  
  function textFrom( value ) {
    var text;
    if (settings.mode == 'view') {
      text = '<li title="' + value.meaning + '">'
      var path = value.meaning.split('/');
      if (path.length > 2) text += path[0] + '/ ... /' + path[path.length -1]; else text += value.meaning;
      text += '</li>';
    } else {
      text = '<li>' + value.meaning + '<i>' + value.synonyms.join(', ') + '</i></li>';
    }
    return text;
  }
  
  /**
     * Renders a frame for editing a classification on the PdC. The frame is for adding new position
     * in the classification.
     */
  function renderClassificationEditionFrame( $this, selectedValues ) {
    $('#pdc-addition-box').children().remove();
    if (hasPdCMandoryAxis($this.data('pdc').axis) && (settings.mode == 'creation')) {
      $('<div>').addClass('inlineMessage').html(settings.messages.mandatoryMessage).appendTo($('#pdc-addition-box'));
    }
    renderPdCAxisFields($this, $this.data('pdc').axis, $('#pdc-addition-box'), selectedValues);
  }
  
  /**
     * Renders a frame for updating an existing position. It is only used in creation mode.
     */
  function renderClassificationUpdateFrame( $this, selectedValues ) {
    $('#pdc-update-box').children().remove();
    renderPdCAxisFields($this, $this.data('pdc').axis, $('#pdc-update-box'), selectedValues);
  }
  
  /**
     * Renders the fields corresponding to each PdC's axis from which a value for a position can be
     * choosen.
     * The fields will be children elements to the specified parent element.
     * The third parameter is an array that can contain the previously values of a position (in the
     * case of a position modification) and that will receive the new values for the position.
     */
  function renderPdCAxisFields( $this, theAxis, axisSection, selectedValues ) {
    var hasMandatoryAxis = false, hasInvariantAxis = false;
    // browse the axis of the PdC and for each of them print out a select HTML element
    $.each(theAxis, function(axisIndex, anAxis) {
      var currentAxisDiv = $('<div>').addClass('champs').appendTo($('<div>').addClass('field').
        append($('<label >', {
          'for': anAxis.id
        }).addClass('txtlibform').html(anAxis.name)).
        appendTo(axisSection));
      var mandatoryField = '';
      if (anAxis.mandatory) {
        mandatoryField = 'mandatoryField'
      }
      var axisValuesSelection = $('<select>', {
        'id': anAxis.id,  
        'name': anAxis.name
      }).addClass(mandatoryField).appendTo(currentAxisDiv).change( function() {
        var theValue = $(this).children(':selected').attr('value');
        if (theValue == 0) {
          selectedValues[anAxis.id] = null;
        } else {
          selectedValues[anAxis.id] = anAxis.values[theValue];
        }
      });
      var path = [];
      
      // browse the values of the current axis and for each of them print out an option HTML element
      // within the select (representing the current axis)
      if (anAxis.mandatory && anAxis.values[anAxis.values.length - 2].ascendant)
        selectedValues[anAxis.id] = anAxis.values[anAxis.values.length - 1];
      $.each(anAxis.values, function(valueIndex, aValue) {
        var level = '';
        path.splice(aValue.level, path.length - aValue.level);
        path[aValue.level] = aValue.term;
        aValue.meaning = path.join(' / ');
        if (aValue.id != '/0/') {
          for (var i = 0; i < aValue.level; i++) {
            level += '&nbsp;&nbsp;';
          }
          var option =
          $('<option>').attr('value', valueIndex).html(level + aValue.term).appendTo(axisValuesSelection);
          if (aValue.ascendant) {
            option.attr('value', 'A').attr('disabled', 'disabled').addClass("intfdcolor51");
          }
          if (anAxis.invariantValue != null && anAxis.invariantValue != aValue.id) {
            selectedValues[anAxis.id] = aValue;
            option.attr('disabled', 'disabled');
          }
          if ((selectedValues[anAxis.id] != null && aValue.id == selectedValues[anAxis.id].id)) {
            option.attr('selected', true);
          }
        }
      });
      
      var option = $('<option>').attr('value', '0').html('&nbsp;').prependTo(axisValuesSelection);
      if (anAxis.mandatory) {
        option.attr('disabled', 'disabled').addClass('emphasis').html(settings.edition.mandatoryAxisDefaultValue);
      }
      if (selectedValues[anAxis.id] == null) {
        option.attr('selected', true);
      }
      if (selectedValues[anAxis.id] != null) {
        $('<span>').html('<i>' + selectedValues[anAxis.id].synonyms.join(', ') + '</i>&nbsp;').appendTo(currentAxisDiv);
      }
      if (anAxis.mandatory) {
        hasMandatoryAxis = true;
        $('<img>', {
          src: settings.edition.mandatoryIcon,
          alt: settings.edition.mandatoryLegend, 
          width: '5px',
          height: '5px'
        }).appendTo(currentAxisDiv.append(' '));
      }
      if (anAxis.invariant) {
        hasInvariantAxis = true;
        $('<img>', {
          src: settings.edition.invariantIcon, 
          alt: settings.edition.invariantLegend,
          width: '10px',
          height: '10px'
        }).appendTo(currentAxisDiv);
      }
    });
    
    axisSection.append($('<br>').attr('clear', 'all'));
    if ((settings.mode == 'creation' || settings.mode == 'predefinition') && axisSection.attr('id') == 'pdc-addition-box') {
      axisSection.append($('<a>').attr('href', '#').
        addClass('valid_position').
        addClass('milieuBoutonV5').
        html(settings.addition.title).click(function() {
          var classification = $this.data('classification');
          var position = aPositionWith(selectedValues);
          if (checkPositionIsValid($this, position)) {
            if (settings.mode == 'creation') {
              position.id = classification.positions.length;
              classification.positions.push(position);
              refreshClassification($this);
            } else if (settings.mode == 'predefinition') {
              if (checkPositionIsValid($this, position)) {
                classification.positions.push(position);
                submitClassification($this, settings.defaultClassificationURI);
              }
            } else {
              submitPosition($this, classification.uri, position);
            }
          }
        }));
    }
    if (theAxis.length > 0) {
      var legende = $('<p>').addClass('legende');
      if (hasMandatoryAxis) {
        legende.append($('<img>', {
          src: settings.edition.mandatoryIcon, 
          alt: settings.edition.mandatoryLegend,
          width: '5px',
          height: '5px'
        })).append($('<span>').html('&nbsp;:' + settings.edition.mandatoryLegend + ' '));
      }
      if (hasInvariantAxis) {
        legende.append(
          $('<img>', {
            src: settings.edition.invariantIcon, 
            alt: settings.edition.invariantLegend,
            width: '10px',
            height: '10px'
          })).
        append($('<span>').html('&nbsp;:' + settings.edition.invariantLegend));
      }
      if (hasMandatoryAxis || hasInvariantAxis) {
        legende.appendTo(axisSection);
      }
    }
  }
})( jQuery );

