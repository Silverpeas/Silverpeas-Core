/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

(function() {
  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/util/javaScript/vuejs/components/comments/silverpeas-comments-templates.jsp');

  const cache = new SilverpeasSessionCache('silverpeas-comments');

  /**
   * Main component handling the different actions on the comments:
   * - creation of a new comment,
   * - update of the text of an existing comment,
   * - delete an existing comment.
   * The rendition of each comment is done by a dedicated child component which delegates the update
   * and the deletion to this component.
   * The edition of a new comment is done directly within this component.
   */
  SpVue.component('silverpeas-comments',
      templateRepository.get('comments', {
        mixins: [VuejsI18nTemplateMixin],
        emits : ['deletion', 'listing', 'addition', 'update'],
        props: {
          componentId: {
            'type': String,
            'mandatory': true
          },
          resourceId: {
            'type': String,
            'mandatory': true
          },
          resourceType: {
            'type': String,
            'mandatory': true
          },
          fromComponentId: {
            'type': String,
            'mandatory': true
          },
          indexed: {
            'type': Boolean,
            'default': true
          },
          user: {
            'type': Object,
            'mandatory': true
          }
        },
        data : function() {
          return {
            comments: [],
            url: undefined,
            updatePopin: undefined,
            commentText: '',
            updatedCommentText: ''
          };
        },
        created : function() {
          sp.ajaxRequest(webContext +
              '/services/bundles/settings/org/silverpeas/util/comment/Comment')
              .send()

          this.url = webContext + '/services/comments/' + this.componentId + '/' +
              this.resourceType + '/' + this.resourceId;
          this.loadComments();
        },
        mounted: function() {
          whenSilverpeasEntirelyLoaded(function() {
            let element = cache.get(this.contributionId.asString());
            if (element && (typeof element === 'string')) {
              let $element = this.$el.querySelector(element);
              if ($element) {
                cache.remove(this.contributionId.asString());
                sp.element.scrollToWhenSilverpeasEntirelyLoaded($element);
              } else {
                sp.log.error('No such HTML element', $element);
              }
            }
          }.bind(this));
        },
        methods : {
          goToLoginPage: function() {
            cache.put(this.contributionId.asString(), '.commentsList');
            const queryParams = {
              forceToLogin : true
            };
            if (this.componentId !== this.fromComponentId) {
              // alias case
              queryParams.ComponentId = this.fromComponentId;
            }
            top.location.href = sp.url.format(webContext + '/Contribution/' + this.contributionId.asBase64(), queryParams);
          },
          /**
           * Validates the specified text satisfies the requirement to be used as a comment's
           * content.
           * @param text {String} the text to validate.
           * @returns {boolean} true if the text is valid, false otherwise.
           */
          validateText : function(text) {
            if (StringUtil.isNotDefined(text)) {
              SilverpeasError.add(this.messages.commentErrorSingleCharAtLeast);
            } else if (!isValidTextArea(text)) {
              SilverpeasError.add(this.messages.commentErrorFieldTooLong);
            }
            return !SilverpeasError.show();
          },
          /**
           * Loads all the comments on the given Silverpeas resource from the server.
           * @returns {Promise} the promise of an HTTP response.
           */
          loadComments: function() {
            return sp.ajaxRequest(this.url)
                .sendAndPromiseJsonResponse()
                .then(function(comments) {
                  this.comments = comments;
                  this.emitChange('listing', comments);
                }.bind(this));
          },
          /**
           * Creates a new comment on the given Silverpeas resource for the current user from the
           * text written in the corresponding text area.
           */
          createComment: function() {
            if (this.validateText(this.commentText)) {
              const comment = {
                author : {
                  id : this.user.id
                },
                componentId : this.componentId,
                resourceId : this.resourceId,
                resourceType : this.resourceType,
                indexed : this.indexed,
                text : this.commentText
              }
              sp.ajaxRequest(this.url)
                  .byPostMethod()
                  .sendAndPromiseJsonResponse(comment)
                  .then(function(newComment) {
                    this.commentText = '';
                    this.emitChange('addition', newComment);
                    return this.loadComments();
                  }.bind(this));
            }
          },
          /**
           * Updates the content of specified existing comment on the given Silverpeas resource for
           * the current user from the text written in the corresponding text area displayed in a
           * dedicated popup. The opening of the popup is performed by this method.
           * @param comment {Object} the comment to update.
           */
          updateComment: function(comment) {
            this.updatedCommentText = comment.text;
            let $textarea = this.$el.querySelector('#comment-update-text');
            jQuery($textarea).autoResize();
            this.updatePopin.open({
              callback: function() {
                const commentWithUpdatedText = extendsObject({}, comment, {
                  text: this.updatedCommentText
                })
                if (this.validateText(commentWithUpdatedText.text)) {
                  return sp.ajaxRequest(this.url + '/' + commentWithUpdatedText.id)
                      .byPutMethod()
                      .sendAndPromiseJsonResponse(commentWithUpdatedText)
                      .then(function(updatedComment) {
                        this.emitChange('update', updatedComment);
                        return this.loadComments();
                      }.bind(this));
                }
                return false;
              }.bind(this)
            });
          },
          /**
           * Deletes the specified existing comment on the given Silverpeas resource for
           * the current user.
           * @param comment {Object} the comment to delete.
           */
          deleteComment: function(comment) {
            sp.popup.confirm(this.messages.commentDeletionConfirmation, function() {
              return sp.ajaxRequest(this.url + '/' + comment.id)
                  .byDeleteMethod()
                  .send()
                  .then(function() {
                    this.emitChange('deletion', comment);
                    return this.loadComments();
                  }.bind(this));
            }.bind(this));
          },
          emitChange: function(type, comments) {
            this.$emit(type, Array.isArray(comments) ? comments : Array(comments));
          },
        },
        computed : {
          infoNbOfComments: function() {
            let msg;
            if (this.comments.length === 0) {
              msg = this.messages.textNoComment;
            } else if (this.comments === 1) {
              msg = '1' + this.messages.textComment;
            } else {
              msg = this.comments.length + ' ' + this.messages.textComments;
            }
            return msg;
          },
          contributionId : function() {
            return sp.contribution.id.from(this.componentId, this.resourceType, this.resourceId);
          }
        }
      }));

  SpVue.component('silverpeas-comment-edition',
      templateRepository.get('comment-edition', {
        emits : ['update:modelValue', 'comment-adding'],
        model : {
          prop : 'modelValue',
          event : 'update:modelValue'
        },
        props : {
          currentUser: {
            'type': Object,
            'mandatory': true
          },
          modelValue : {
            'type' : String
          }
        },
        mounted : function() {
          let $textarea = this.$el.querySelector('#comment-edition-text');
          jQuery($textarea).autoResize();
        },
        data: function() {
          return {
            comment: ''
          }
        }
      }));

  SpVue.component('silverpeas-comment',
      templateRepository.get('comment', {
        emits : ['comment-update', 'comment-deletion'],
        props: {
          currentUser: {
            'type': Object,
            'mandatory': true
          },
          comment: {
            'type': Object,
            'mandatory': true
          },
          readonly: {
            'type': Boolean,
            'default': false
          }
        },
        mounted: function() {
          activateUserZoom();
        },
        computed : {
          commentText : function() {
            return this.comment.text.noHTML().convertNewLineAsHtml();
          },
          displayUserZoom: function() {
            return this.currentUser.id !== this.comment.author.id && !this.currentUser.anonymous;
          },
          canBeUpdated : function() {
            return !this.readonly &&
                (this.currentUser.canUpdateAll || this.comment.author.id === this.currentUser.id);
          },
          canBeDeleted: function() {
            return !this.readonly &&
                (this.currentUser.canUpdateAll || this.comment.author.id === this.currentUser.id);
          }
        }
      }));
})();