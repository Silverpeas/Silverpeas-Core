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
 * This JQuery plugin is developed for the Silverpeas application.
 * It provides functions to:
 * - print an area for editing new comments,
 * - print a list of comments,
 * - and update or delete a given comment.
 * The comments are expected to be formatted in JSON with at least the following attributes:
 * - uri: the URI of the comment in the web,
 * - id: the unique identifier of the comment in the server from which it is fetched,
 * - creationDate: the date at which the comment was created,
 * - modificationDate: the date at which the comment was lastly updated,
 * - text: the text of the comment,
 * - indexed: a flag indicating whether the comment is indexed or should be indexed,
 * - author: the user that has written the comment with at least the following attributes:
 *    - id: the unique identifier of the user in the server from which the comment has been fetched,
 *    - fullName: the full name (first and last names) of the user,
 *    - avatar: the URI of its avatar.
 */
(function( $ ){

  /**
   * The parameter settings of the plugin with, for some, the default value.
   * - uri: the URI at which the comments are located in the web. To access or to update one
   * comment,
   * - update: an object about the update information on a given comment having the following
   * attributes:
   *    - activated: a function that accepts as argument a comment and that should return a boolean
   * value indicating if that comment can be updated. By default, return false,
   *    - icon: the URL of the icon representing the operation of updating a given comment,
   *    - altText: the alternative text for the comment update operation,
   * - deletion: an object about the deletion information on a given comment having the same
   *  attributes than the object about update information.
   * - updateBox: the modal box in which the text of a given comment can be updated. It has the
   * following attributes:
   *    - title: the title of the modal box,
   *    - ok: the text of the ok button,
   *    - cancel: the text of the cancel button,
   * - editionBox: the box in which a new comment can be edited. It is not a dialog box, but a
   * an area dedicated to edit a text and it can be placed after or before the list of comments.
   * It has the following attributes:
   *    - title: the title of the box,
   *    - ok: the text of the ok button,
   * - validate: a callback function to assert the text of a comment is valid. The function can
   * be display a message to inform in what the comment isn't valid.
   * - callback: a function that will be called by the plugin at comments listing or at comment
   * deletion, updating or  adding. The user can then perform additional operations according the
   * events. It accepts as argument the event for which it was called. The event object has two
   * attributes: the event type (a value among 'addition', 'update', 'deletion' or 'listing') and
   * the object on which the event is about (a list of comments).
   * - mandatory: the symbol indicating the text of the comment is mandatory. Can be the path of
   * an icon.
   * - mandatoryText: the text to display to indicate that the presence of the mandatory symbol
   * means the field is mandatory.
   */
  var settings = {
    url: 'http://localhost/comments',
    update: {
      activated: function( comment ) {
        return false;
      },
      icon: '',
      altText: 'update'
    },
    deletion: {
      activated: function( comment ) {
        return false;
      },
      confirmation: 'Are your sure?',
      icon: '',
      altText: 'delete'
    },
    updateBox: {
      title: 'The comment:',
      ok: 'Update',
      cancel: 'Cancel'
    },
    editionBox: {
      title: 'New comment:',
      ok: 'Save'
    },
    validate: function( text ) {
      if (text != null && text.length > 0)
        return true
      else
        return false;
    },
    callback: function( event ) {},
    mandatory: '*',
    mandatoryText: 'mandatory'
  };

  /**
   * The different methods on comments published by the plugin.
   */
  var methods = {
    /**
     * Sets up the JQuery comment plugin with the specified options. The options defines the settings
     * to apply to the comment plugin. Please see the settings declaration to have a glance at
     * the different settigns parameters.
     */
    init : function( options ) {

      if ( options ) {
        $.extend( true, settings, options );
      }

      return this.each(function() {
        var $this = $(this), data = $this.data('comments');
        if (!data) {
          $this.data('comments', {
            comments: new Array(),
            commentsById: new Array()
          });
        }
      })
    },

    /**
     * Lists all the comments available at the specified URL.
     * The list of comments will be printed into a div element within the HTML element on which the
     * method is applied. The div element's class is 'list-box'.
     * Each comment will be placed into a div element with as id their identifier prefixed by 'comment'
     * (for example 'comment2' for the comment with as identifier 2).
     * For each comment:
     * - the avatar of the author will be placed into a div element with as class 'avatar',
     * - the author name will be placed into a p element with as class 'author' and will also contain
     * the date at which the comment was written into a span element with as class 'date'.
     * - the text of the comment will be placed into a p element with as class 'text'.
     * This method prepares also the update box for updating comments; the update box will contain
     * a textarea with as class comment-text for updating the text a comment .
     */
    list : function() {
      return this.each(function() {
        var $this = $(this), comments = $this.data('comments');
        $this.append(
          $("<div id='comments-update-box'>").attr("style","display: none;").append(
            $("<div>").addClass("mandatoryField").append(
              $("<textarea>").addClass("text")).append(
              $("<span>").html("&nbsp;")).append(
              $("<img>").attr("src", settings.mandatory).attr("alt", settings.mandatory)).append(
              $("<div>").addClass("legende").append(
                $("<img>").attr("src", settings.mandatory).attr("alt", settings.mandatory)).append(
                $("<span>").html(":&nbsp;" + settings.mandatoryText))))).append(
          $("<div id='list-box'>"));

        $.getJSON(settings.uri, function( arrayOfComments ) {
          comments.comments = arrayOfComments;
            for (var x = 0; x < arrayOfComments.length; x++) {
              comments.commentsById[arrayOfComments[x].id] = arrayOfComments[x];
              __printComment( $this, arrayOfComments[x], 'bottom' );
            }
            settings.callback( { type: 'listing', comments: arrayOfComments } );
        });
      })
    },

    /**
     * Renders a box in which users can edit new comments.
     * The method accepts as argument a function from which a new well-prepared comment can be get.
     * The box is a div with as id edition-box and it contains a:
     * - a p element with as class title and displaying the title of the edition box,
     * - a textarea element with as class update-text,
     * - a button element with as class comment-button with which the new comment can be added by using the
     * URI passed in the plugin options.
     */
    edition : function( commentCreation ) {
      return this.each(function() {
        var $this = $(this), edition = settings.editionBox;
        $this.append(
          $("<div id='edition-box'>").addClass("mandatoryField").append(
            $("<p>").addClass("title").text(edition.title)).append(
            $("<textarea>").addClass("text")).append(
            $("<span>").html("&nbsp;")).append(
            $("<img>").attr("src", settings.mandatory).attr("alt", settings.mandatory)).append(
            $("<div>").addClass("legende").append(
              $("<img>").attr("src", settings.mandatory).attr("alt", settings.mandatory)).append(
              $("<span>").html("&nbsp;:&nbsp;" + settings.mandatoryText))).append(
            $("<div>").addClass("buttons").append(
              $("<button>").addClass("button").text(edition.ok).click(function() {
                __addComment( $this, commentCreation );}))));
      })
    }
  };

  /**
   * The comment namespace in JQuery in which methods on comments are provided.
   */
  $.fn.comment = function( method ) {

    if ( methods[method] ) {
      return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.init.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.comment' );
    }

  };

  /**
   * A private method to print the specified comment.
   */
  function __printComment( $this, comment, position ) {
    var update = settings.update, deletion = settings.deletion, comments = $this.data('comments'),
      commentBox, actionsPane = $("<div>").addClass("action"),
    commentDescription = $("<div id='comment" + comment.id + "'>").addClass("oneComment");

    if (position === 'top' && comments.comments.length > 0) {
      commentBox = commentDescription.insertBefore($('#comment' + comments.comments[0].id));
    } else {
      commentBox = commentDescription.appendTo($("#list-box"));
    }
    commentBox.append($("<div>").append(
      actionsPane).append(
      $("<div>").addClass("avatar").append(
        $("<img>").attr("src", comment.author.avatar))).append(
      $("<p>").addClass("author").text(comment.author.fullName).append(
        $("<span>").addClass("date").text(" - " + comment.creationDate))).append(
      $("<pre>").addClass("text").append(comment.text.replace(/\n/g, '<br/>'))));
    if (update['activated']( comment )) {
      $("<img>").attr("src", update.icon).attr("alt", update.altText).click(function() {
        __updateComment($this, comment.id)
      }).appendTo(actionsPane);
    }
    $("<span>").html("&nbsp").appendTo(actionsPane);
    if (deletion.activated( comment )) {
      $("<img>").attr("src", deletion.icon).attr("alt", deletion.altText).click(function() {
        __deleteComment($this, comment.id)
      }).appendTo(actionsPane);
    }
  }

  /**
   * A private method to update the specified comment with a new text.
   * The remote web service is invoked to persist the update.
   */
  function __updateComment( $this, commentId ) {
    var comments = $this.data('comments'), comment = comments.commentsById[commentId],
      updateBox = settings.updateBox;
    $("#comments-update-box").find("textarea").val(comment.text);
    $("#comments-update-box").dialog({
      width: 640,
      modal: true,
      title: updateBox.title,
      buttons: [{
          text: updateBox.ok,
          click: function() {
          var text = $("#comments-update-box").find("textarea").val();
          if (settings.validate(text)) {

            var commentToSend = comment;
            commentToSend.text = text;
            $.ajax({
              url: settings.uri + "/" + commentId,
              type: "PUT",
              data: $.toJSON(comment),
              contentType: "application/json",
              dataType: "json",
              success: function(data) {
                comment.text = data.text;
                $("#comment" + commentId).find('pre.text').text('').append(data.text.replace(/\n/g, '<br/>'));
                comments.commentsById[commentId] = data;
                settings.callback( { type: 'update', comments:  new Array( data ) } );
              }
            });
            $( this ).dialog( "destroy" );
          }
        }
      }, {
        text: updateBox.cancel,
        click: function() { $( this ).dialog( "destroy" ); }
      }],
      close: function() { $( this ).dialog( "destroy" );
	  }
    })
  }

  /**
   * A private method to delete the specified comment with a new text.
   * The remote web service is invoked to delete effectivly the comment.
   */
  function __deleteComment( $this, commentId ) {
    var comments = $this.data('comments'), msg = settings.deletion.confirmation;
    if (window.confirm(msg)) {
      $.ajax({
        url: settings.uri + "/" + commentId,
        type: "DELETE",
        success: function(data) {
          var comment = $.extend(true, {}, comments.commentsById[commentId]);
          if (comments.comments[0].id === commentId) {
            comments.comments.shift();
          }
          delete comments.commentsById[commentId];
          $("#comment" + commentId).hide('slow');
          settings.callback( { type: 'deletion', comments:  new Array( comment ) } );
        }
      });
    }
  }

  function __addComment( $this, commentCreation ) {
    var comments = $this.data('comments');
    var comment = commentCreation();
    comment.text = $("#edition-box").find("textarea").val();
    if (settings.validate(comment.text)) {
      $.ajax({
        url: settings.uri,
        type: "POST",
        data: $.toJSON(comment),
        contentType: "application/json",
        dataType: "json",
        success: function(data) {
          __printComment( $this, data, 'top' );
          comments.comments.unshift(data);
          comments.commentsById[data.id] = data;
          $("#edition-box").find("textarea").val("");
          settings.callback( { type: 'addition', comments:  new Array( data ) } );
        }
      });
    }
  }
})( jQuery );

