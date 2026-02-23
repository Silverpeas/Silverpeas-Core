/**
Copyright (c) 2003-2012, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/

CKEDITOR.editorConfig = function( config )
{
  //config.contentsCss = webContext + '/util/styleSheets/silverpeas-main.css';
  config.baseHref = webContext + '/wysiwyg/jsp/';
  config.filebrowserImageBrowseUrl = config.baseHref+'uploadFile.jsp';
  config.filebrowserFlashBrowseUrl = config.baseHref+'uploadFile.jsp';
  config.filebrowserBrowseUrl = config.baseHref+'uploadFile.jsp';
  config.imageUploadUrl = 'activated';
  config.extraPlugins = 'userzoom,identitycard,autolink,video,html5audio,imageresizerowandcolumn,variables,listblock,floatpanel,richcombo,mediaofcontribution,imagebank,filebank,emoji';
  config.allowedContent = true;
  config.toolbarCanCollapse = true;
  config.disableNativeSpellChecker = false;
  //config.forcePasteAsPlainText = true;

  config.stylesSet = [
    {name: 'Titre 1', element: 'h2', attributes : { 'class' : 'wysiwyg-title1' }},
    {name: 'Titre 2', element: 'h3', attributes : { 'class' : 'wysiwyg-title2' }},
    {name: 'Focus', element:'strong', attributes : { 'class' : 'wysiwyg-focus' }},
    {name: 'Paragraphe important', element:'p', attributes : { 'class' : 'wysiwyg-important' }}
  ];

  config.toolbar_Default = [
    { name: 'document',    items : [ 'Source','-','Save','NewPage','DocProps','Preview','Print','-','Templates' ] },
    { name: 'clipboard',   items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
    { name: 'editing',     items : [ 'Find','Replace','-','SelectAll' ] },
    '/',
    { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ] },
    { name: 'paragraph',   items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock' ] },
    { name: 'links',       items : [ 'Link','Unlink','Anchor' ] },
    { name: 'insert',      items : [ 'Image','mediaofcontribution','imagebank','Video','Html5audio','Iframe','filebank','Table','HorizontalRule','EmojiPanel','SpecialChar','PageBreak', 'identitycard', 'userzoom', 'variables' ] },
    '/',
    { name: 'styles',      items : [ 'Styles','Format','Font','FontSize' ] },
    { name: 'colors',      items : [ 'TextColor','BGColor' ] },
    { name: 'tools',       items : [ 'Maximize', 'ShowBlocks','-','About' ] }
  ];

  config.toolbar_Light = [
    { name: 'clipboard',   items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
    { name: 'links',       items : [ 'Link','Unlink' ] },
    { name: 'insert',      items : [ 'Table','HorizontalRule','EmojiPanel','SpecialChar', 'identitycard', 'userzoom', 'variables' ] },
    '/',
    { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ] },
    { name: 'paragraph',   items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock' ] },
    { name: 'colors',      items : [ 'TextColor','BGColor' ] },
    '/',
    { name: 'styles',      items : [ 'Styles','Format','Font','FontSize' ] }
  ];

  config.toolbar_Basic = [
    { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','-','RemoveFormat' ] },
    { name: 'paragraph',   items : [ 'NumberedList','BulletedList','-','Outdent','Indent' ] }
  ];

  config.toolbar_calendar = config.toolbar_Default;
  config.toolbar_forum = config.toolbar_Light;
  config.toolbar_blog = config.toolbar_Default;
  config.toolbar_almanach = config.toolbar_Default;
  config.toolbar_quickInfo = config.toolbar_Default;
  config.toolbar_XMLForm = config.toolbar_Default;
  config.toolbar_questionReply = config.toolbar_Light;
  config.toolbar_suggestionBox = config.toolbar_Light;
  config.toolbar_userNotification = config.toolbar_Basic;

};
