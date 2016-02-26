/**
 * @license Copyright (c) 2003-2013, webmote - codeex.cn. All rights reserved.
 * For licensing, see http://codeex.cn/
 * 2013-2-18 v1.0
 */

(function() {
	//var allMediasFilenameRegex = /\.(avi|asf|fla|flv|mov|rm|rmvb|ra|mp3|mp4|mpg|mpeg|qt|wma|wmv)(?:$|\?)/i;

	function isallMediasEmbed( element ) {
		var attributes = element.attributes;
		return ( attributes.mtype == 'allmedias'); // || allMediasFilenameRegex.test( attributes.src || '' ) );
	}

	function createFakeElement( editor, realElement ) {
		return editor.createFakeParserElement( realElement, 'cke_allMedias', 'allmedias', true );
	}

	CKEDITOR.plugins.add( 'allmedias', {
		requires: 'dialog,fakeobjects',
		//langEntries: 'en,zh-cn,zh', // %REMOVE_LINE_CORE%
		lang: ['en', 'zh-cn', 'zh', 'fr'],
		icons: 'allmedias', // %REMOVE_LINE_CORE%
		onLoad: function() {
			CKEDITOR.addCss( 'img.cke_allMedias' +
				'{' +
					'background-image: url(' + CKEDITOR.getUrl( this.path + 'images/placeholder.png' ) + ');' +
					'background-position: center center;' +
					'background-repeat: no-repeat;' +
					'border: 1px solid #a9a9a9;' +
					'width: 80px;' +
					'height: 80px;' +
				'}'
				);
			//CKEDITOR.scriptLoader.load( 'plugins/allmedias/jwplayer.js' );

		},
		init: function( editor ) {
			editor.addCommand( 'allmedias', new CKEDITOR.dialogCommand( 'allmedias' ) );
			editor.ui.addButton && editor.ui.addButton( 'allmedias', {
				label: editor.lang.allmedias.allmedias,
				command: 'allmedias',
				toolbar: 'insert'
			});
			CKEDITOR.dialog.add( 'allmedias', this.path + 'dialogs/allMedias.js' );

			// If the "menu" plugin is loaded, register the menu items.
			if ( editor.addMenuItems ) {
				editor.addMenuGroup( 'mediagroup' );
				editor.addMenuItems({
					mediamenu: {
						label: editor.lang.allmedias.properties,
						command: 'allmedias',
						group: 'mediagroup',
						icons:  this.icons
					}
				});
			}

			editor.on( 'doubleclick', function( evt ) {
				var element = evt.data.element;

				if ( element.is( 'img' ) && element.data( 'cke-real-element-type' ) == 'allmedias' )
					evt.data.dialog = 'allmedias';
			});

			// If the "contextmenu" plugin is loaded, register the listeners.
			if ( editor.contextMenu ) {
				editor.contextMenu.addListener( function( element, selection ) {
					if ( element && element.is( 'img' ) && !element.isReadOnly() && element.data( 'cke-real-element-type' ) == 'allMedias' )
						return { mediamenu: CKEDITOR.TRISTATE_OFF };
				});
			}
		},

		afterInit: function( editor ) {
			var dataProcessor = editor.dataProcessor,
				dataFilter = dataProcessor && dataProcessor.dataFilter;
				htmlFilter = dataProcessor && dataProcessor.htmlFilter;

			if ( dataFilter ) {
				dataFilter.addRules({
					elements: {
						'cke:object': function( element ) {
							var attributes = element.attributes;
								//classId = attributes.classid && String( attributes.classid ).toLowerCase();

							if ( !isallMediasEmbed( element ) ) {
								// Look for the inner <embed>
								for ( var i = 0; i < element.children.length; i++ ) {
									if ( element.children[ i ].name == 'cke:embed' ) {
										if ( !isallMediasEmbed( element.children[ i ] ) )
											return null;

										return createFakeElement( editor, element );
									}
								}
								return null;
							}
							else{
								return createFakeElement( editor, element );
							}
						},

						'cke:embed': function( element ) {
							if ( !isallMediasEmbed( element ) )
								return null;

							return createFakeElement( editor, element );
						}
					}
				}, 1 );
			}
			/*
			if ( htmlFilter ) {
				htmlFilter.addRules({
					elements: {
						'cke:object': function( element ) {
							if ( element.attributes && element.attributes[ 'mtype' ] )
								delete element.name;
						}
					}
				});
			}
			*/
		}
	});
})();

CKEDITOR.tools.extend( CKEDITOR.config, {
	/**
	 * Save as `<embed>` tag only. This tag is unrecommended.
	 *
	 * @cfg {Boolean} [allMediasEmbedTagOnly=false]
	 * @member CKEDITOR.config
	 */
	allMediasEmbedTagOnly: false,

	/**
	 * Add `<embed>` tag as alternative: `<object><embed></embed></object>`.
	 *
	 * @cfg {Boolean} [allMediasAddEmbedTag=false]
	 * @member CKEDITOR.config
	 */
	allMediasAddEmbedTag: true,

	/**
	 * Use {@link #allMediasEmbedTagOnly} and {@link #allMediasAddEmbedTag} values on edit.
	 *
	 * @cfg {Boolean} [allMediasConvertOnEdit=false]
	 * @member CKEDITOR.config
	 */
	allMediasConvertOnEdit: false
});
