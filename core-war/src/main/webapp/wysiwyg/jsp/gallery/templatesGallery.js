// Register a template definition set named "Octopus".
CKEDITOR.addTemplates('Gallery',
	{
		// The name of the subfolder that contains the preview images of the templates.
		imagesPath: CKEDITOR.getUrl('/silverpeas/wysiwyg/jsp/gallery/icons/'),

		// Template definitions.
		templates:
			[			
			{
				title: 'Galerie Format Carré',
				image: 'galerie-carre.png',
				description: '<br/><strong>[FR]</strong> Les images de format différent du carré seront tronquées. Cette galerie est une liste : appuyez sur Entrée pour ajouter un nouvel élément image.<br/>Utiliser le <strong>"Texte alternatif"</strong> comme description ou titre (visible via diaporama) <br /><br/><strong>[EN]</strong> Images with a non-square format will be cropped. List format: press Enter to add a new image item.<br/>Use the <strong>"Alternative Text"</strong> as a description or title (visible via slideshow)',
				html:'<link href="/silverpeas/wysiwyg/jsp/gallery/css/gallery.css" rel="stylesheet">' +
                    '<script src="/silverpeas/wysiwyg/jsp/gallery/js/gallery.js"></script>' +
                    '<ul class="sp-galleryWysiwyg format1-1 sp-gallery-interaction">'+
					'<li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li>'+
					'<li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/howToHad.png"  /></li>'+
					'</ul><br/>&nbsp;'
			},
			{
				title: 'Galerie Format Paysage',
				image: 'galerie-paysage.png',
				description: '<br/><strong>[FR]</strong> Les images de format différent du 3 / 2 seront tronquées. Cette galerie est une liste : appuyez sur Entrée pour ajouter un nouvel élément image.<br/>Utiliser le <strong>"Texte alternatif"</strong> comme description ou titre (visible via diaporama) <br /><br/><strong>[EN]</strong> Images with a non-square format will be cropped. List format: press Enter to add a new image item.<br/>Use the <strong>"Alternative Text"</strong> as a description or title (visible via slideshow)',
				html:'<link href="/silverpeas/wysiwyg/jsp/gallery/css/gallery.css" rel="stylesheet">' +
                    '<script src="/silverpeas/wysiwyg/jsp/gallery/js/gallery.js"></script>' +
                    '<ul class="sp-galleryWysiwyg format3-2 sp-gallery-interaction">'+
					'<li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li>'+
					'<li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/howToHad.png"  /></li>'+
					'</ul><br/>&nbsp;'
			},
			{
				title: 'Galerie Format Portrait',
				image: 'galerie-portrait.png',
				description: '<br/><strong>[FR]</strong> Les images de format différent du 3 / 4 seront tronquées. Cette galerie est une liste : appuyez sur Entrée pour ajouter un nouvel élément image.<br/>Utiliser le <strong>"Texte alternatif"</strong> comme description ou titre (visible via diaporama) <br /><br/><strong>[EN]</strong> Images with a non-square format will be cropped. List format: press Enter to add a new image item.<br/>Use the <strong>"Alternative Text"</strong> as a description or title (visible via slideshow)',
				html:'<link href="/silverpeas/wysiwyg/jsp/gallery/css/gallery.css" rel="stylesheet">' +
                    '<script src="/silverpeas/wysiwyg/jsp/gallery/js/gallery.js"></script>' +
                    '<ul class="sp-galleryWysiwyg format3-4 sp-gallery-interaction">'+
					'<li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li>'+
					'<li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/howToHad-3-4.png"  /></li>'+
					'</ul><br/>&nbsp;'
			},
			{
				title: 'Galerie Format Patchwork',
				image: 'galerie-patchwork.png',
				description: '<br/><strong>[FR]</strong> Les images seront possibement tronquées. Cette galerie est une liste : appuyez sur Entrée pour ajouter un nouvel élément image.<br/>Utiliser le <strong>"Texte alternatif"</strong> comme description ou titre (visible via diaporama)<br /><br/><strong>[EN]</strong> Images will be possibly cropped. List format: press Enter to add a new image item.<br/>Use the <strong>"Alternative Text"</strong> as a description or title (visible via slideshow)',
				html:'<link href="/silverpeas/wysiwyg/jsp/gallery/css/gallery.css" rel="stylesheet">' +
                    '<script src="/silverpeas/wysiwyg/jsp/gallery/js/gallery.js"></script>' +
                    '<div class="sp-gallery-Patchwork-container"><ul class="sp-gallery-Patchwork sp-gallery-interaction">'+
					'<li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li>'+
					'<li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li><li><img title="" src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li>'+
					'</ul></div><br/>&nbsp;'
			},
			{
				title: 'Galerie Format Polaroid',
				image: 'galerie-polaroid.png',
				description: '<br/><strong>[FR]</strong> Les images gardent leur format. Présentation aléatoire en fonction des formats. Cette galerie est une liste : appuyez sur Entrée pour ajouter un nouvel élément image.<br/>Utiliser le <strong>"Texte alternatif"</strong> comme description ou titre (visible via diaporama)<br /><br/><strong>[EN]</strong> Images keep their format. Random presentation depending on the formats. List format: press Enter to add a new image item.<br/>Use the <strong>"Alternative Text"</strong> as a description or title (visible via slideshow)',
				html:'<link href="/silverpeas/wysiwyg/jsp/gallery/css/gallery.css" rel="stylesheet">' +
                    '<script src="/silverpeas/wysiwyg/jsp/gallery/js/gallery.js"></script>' +
                    '<div class="sp-gallery-polaroid-container"><ul class="sp-gallery-polaroid sp-gallery-interaction">'+
					'<li><img title=""  src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li>'+
					'<li><img title=""  src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li>'+
					'<li><img title=""  src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li>'+
					'<li><img title=""  src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li>'+
					'<li><img title=""  src="/silverpeas/wysiwyg/jsp/gallery/icons/imageDefault.png"  /></li>'+
					'<li><img title=""  src="/silverpeas/wysiwyg/jsp/gallery/icons/howToHad.png"  /></li>'+
					'</ul></div><br/>&nbsp;'
			}
			]
	});


