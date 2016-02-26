/*
Copyright (c) 2003-2012, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/

CKEDITOR.editorConfig = function( config )
{
  // Define changes to default configuration here. For example:
  // config.language = 'fr';
  // config.uiColor = '#AADC6E';

  //config.contentsCss = webContext + '/util/styleSheets/globalSP_SilverpeasV5.css';
  config.baseHref = webContext + '/wysiwyg/jsp/';
  config.filebrowserImageBrowseUrl = config.baseHref+'uploadFile.jsp';
  config.filebrowserFlashBrowseUrl = config.baseHref+'uploadFile.jsp';
  config.filebrowserBrowseUrl = config.baseHref+'uploadFile.jsp';
  config.extraPlugins = 'userzoom,identitycard,allmedias';
  config.allowedContent = true;
  config.toolbarCanCollapse = true;
  //config.forcePasteAsPlainText = true;

  config.stylesSet = [
        {name: 'Titre 1', element: 'h2', attributes : { 'style' : 'border-left: 3px solid #2a2a2a; color: #2a2a2a; font-size: 16px; padding: 0 6px' }},
        {name: 'Titre 2', element: 'h3', attributes : { 'style' : 'border-left: 3px solid #7eb73b; color: #7eb73b; font-size: 14px; padding: 0 6px' }},
        {name: 'Focus', element:'strong', attributes : { 'style' : 'background-color: #2a2a2a; color: #FFF' }},
        {name: 'Paragraphe important', element:'p', attributes : { 'style' : 'background-color: #F5F5F5; border-radius: 0 10px 0 10px; padding: 9.5px; border: 1px solid #ebebeb; font-size: 110%' }}
  ];

config.toolbar_infoLetter = [
  { name: 'document',    items : [ 'Source','-','NewPage','DocProps','Preview','Print','-','Templates' ] },
  { name: 'clipboard',   items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
  { name: 'editing',     items : [ 'Find','Replace','-','SelectAll','-','SpellChecker', 'Scayt' ] },
  '/',
  { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ] },
  { name: 'paragraph',   items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','BidiLtr','BidiRtl' ] },
  { name: 'links',       items : [ 'Link','Unlink','Anchor' ] },
  { name: 'insert',      items : [ 'Image','allmedias','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak' ] },
  '/',
  { name: 'styles',      items : [ 'Styles','Format','Font','FontSize' ] },
  { name: 'colors',      items : [ 'TextColor','BGColor' ] },
  { name: 'tools',       items : [ 'Maximize', 'ShowBlocks','-','About' ] }
];

};