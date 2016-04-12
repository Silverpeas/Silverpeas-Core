/*
Copyright (c) 2003-2012, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/

CKEDITOR.editorConfig = function( config )
{
	// Define changes to default configuration here. For example:
	// config.language = 'fr';
	// config.uiColor = '#AADC6E';

	config.skin = 'kama';
	config.contentsCss = webContext + '/util/styleSheets/globalSP_SilverpeasV5.css';
  config.baseHref = webContext + '/wysiwyg/jsp/';
	config.filebrowserImageBrowseUrl = config.baseHref+'uploadFile.jsp';
	config.filebrowserFlashBrowseUrl = config.baseHref+'uploadFile.jsp';
	config.filebrowserBrowseUrl = config.baseHref+'uploadFile.jsp';
	//config.extraPlugins = 'jwplayer';
	//config.extraPlugins = 'allmedias';

	config.toolbar_Default = [
	       { name: 'document',    items : [ 'Source','-','Save','NewPage','DocProps','Preview','Print','-','Templates' ] },
	       { name: 'clipboard',   items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
	       { name: 'editing',     items : [ 'Find','Replace','-','SelectAll','-','SpellChecker', 'Scayt' ] },
	       { name: 'plugins',     items : [ 'allMedias' ] },
	       //{ name: 'forms',       items : [ 'Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField' ] },
	       '/',
	       { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ] },
	       { name: 'paragraph',   items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','-','BidiLtr','BidiRtl' ] },
	       { name: 'links',       items : [ 'Link','Unlink','Anchor' ] },
	       { name: 'insert',      items : [ 'Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak' ] },
	       '/',
	       { name: 'styles',      items : [ 'Styles','Format','Font','FontSize' ] },
	       { name: 'colors',      items : [ 'TextColor','BGColor' ] },
	       { name: 'tools',       items : [ 'Maximize', 'ShowBlocks','-','About' ] }
	];

	config.toolbar_Light = [
	       { name: 'clipboard',   items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
	       { name: 'links',       items : [ 'Link','Unlink' ] },
	       { name: 'insert',      items : [ 'Table','HorizontalRule','Smiley','SpecialChar' ] },
	       '/',
	       { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ] },
	       { name: 'paragraph',   items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock' ] },
	       { name: 'colors',      items : [ 'TextColor','BGColor' ] },
	       '/',
	       { name: 'styles',      items : [ 'Styles','Format','Font','FontSize' ] }
	];

	config.toolbar_XMLForm = [
	       { name: 'clipboard',   items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
	       { name: 'links',       items : [ 'Link','Unlink' ] },
	       { name: 'insert',      items : [ 'Image','Table','HorizontalRule','Smiley','SpecialChar' ] },
	       '/',
	       { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','Subscript','Superscript','-','RemoveFormat' ] },
	       { name: 'paragraph',   items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote','CreateDiv','-','JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock' ] },
	       { name: 'colors',      items : [ 'TextColor','BGColor' ] },
	       '/',
	       { name: 'styles',      items : [ 'Styles','Format','Font','FontSize' ] }
	];
};
