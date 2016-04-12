


/*************************************************************
                                 CONSTRUCTOR
**************************************************************/
TreeView = function ( id ) {
	this.id = id ;
	this.elements = new Array() ;
	this.width = null ;
	this.height = null ;
	this.elements = new Array() ;
	this.load_all = false ;
	this.root_childrens_always_displayed = false ;

// ==============================================
	this.use_folder_feature = false ;
	this.last_folderId = 0 ;

// ==============================================
	this.use_preloader_feature = false ;
	this.preloader_position = "up";  // top or bottom
	this.preloader_addButton = true;
	this.total_preloaded = 1 ;

// ==============================================
	this.use_link_feature = false ;
	this.link_target = "_self" ;
	this.link_prefix = "" ;
	this.link_suffix = "" ;
	this.link_add_nodeId = false ;

// Added by NEY =================================
	this.verbose = true ;
	}

TreeView.prototype.define = function ( elements_TreeView ) {
	this.elements = elements_TreeView.elements ;
	this.TreeView_elements = elements_TreeView ;
	}



/*************************************************************
                                    GET
**************************************************************/


/*--------------------------Integer---------------------------*/

// TreeView Root   (Id=0)
TreeView.prototype.root = function ( )  {
	for (  var i = 0 ; i < this.elements.length ; i++ ) {
		var one_node = this.elements[i] ;
		if ( ( one_node.E_Num == one_node.E_E_Num ) && ( one_node.E_Num == 0 ) ) return one_node ;
		}
	alert ( "Die: TreeView.prototype.root" ) ;
	}


// Element depth
TreeView.prototype.depth = function ( E_Num , depth ) {
	var j = depth ;
	var depth_max = 0 ;
	var temp = 0 ;
	if ( this.have_childrens ( E_Num ) ) {
		var childrens = this.childrens ( E_Num ) ;
		for (  var i = 0 ; i < childrens.length ; i++ ) {
			var node = childrens[i] ;
			if ( node.Type == "dossier" ) temp = this.depth ( node.E_Num , depth + 1 ) ;
			if ( j > depth_max ) depth_max = j ;
			}
		}
	return ( depth_max >= temp ) ? depth_max : temp ;
	}


// Element depth
TreeView.prototype.childrens_count = function ( E_Num ) {
	var childrens = this.childrens ( E_Num ) ;
	return childrens.length ;
	}



/*--------------------------Array-----------------------------*/

// Children of this Element
TreeView.prototype.childrens = function ( E_Num ) {
	var childrens = new Array ( ) ;
	for (  var k = 0 ; k < this.elements.length ; k++ ) {
		var one_node = this.elements[k] ;
		if ( ( one_node.E_E_Num == E_Num )  && ( one_node.E_Num != 0 ) ) { childrens[childrens.length] = one_node ; }
		}
	return childrens ;
	}


/*--------------------------Element---------------------------*/

// Father of this Element
TreeView.prototype.father = function ( E_Num ) {
	var k , j ;
	var E_E_Num ;
	var childrens = new Array ( ) ;
	j = 0 ;
	for (  var k = 0 ; k < this.elements.length ; k++ ) {
		var one_node = this.elements[k] ;
		if ( one_node.E_Num == E_Num ) E_E_Num = one_node.E_E_Num ;
		}
	for (  var k = 0 ; k < this.elements.length ; k++ ) {
		var one_node = this.elements[k] ;
		if ( one_node.E_Num == E_E_Num ) return one_node ;
		}
	if (this.verbose)
		alert ( "Die: TreeView.prototype.father" ) ;
	}


// Element
TreeView.prototype.node = function ( E_Num ) {
	var k ;
	for (  var k = 0 ; k < this.elements.length ; k++ ) {
		var one_node = this.elements[k] ;
		if ( one_node.E_Num == E_Num ) return one_node ;
		}
	if (this.verbose)
		alert ( "Die: TreeView.prototype.node" ) ;
	}



/*--------------------------Boolean----------------------------*/

// Element have Children ?
TreeView.prototype.have_childrens = function ( E_Num ) {
	var childrens = this.childrens ( E_Num ) ;
	return ( childrens.length >= 1 ) ;
	}


// TreeView have root ?
TreeView.prototype.have_root = function ( )  {
	var m;
	for (  var m = 0 ; m < this.elements.length ; m++ ) {
		var one_node = this.elements[m] ;
		if ( ( one_node.E_Num == one_node.E_E_Num ) && ( one_node.E_Num == 0 ) ) return true;
		}
	return false;
	}



/*************************************************************
                                   VALIDATION
**************************************************************/

TreeView.prototype.validate = function ( )  {
	if ( !this.nodes_valide ( ) ) alert ( "TreeView.elements indefined !" ) ;
	if ( !this.have_root ( ) ) alert ( "TreeView have no root defined ! " ) ;
	}


TreeView.prototype.nodes_valide = function ( )  {
	this.TreeView_elements.validate ( ) ;
	return ( this.elements.length > 0 ) ;
	}


/*************************************************************
                                    DISPLAY
**************************************************************/


/*---------------Display control - Affichage du contrôle------------------------*/

TreeView.prototype.display = function ( )  {
	the_root = this.root ( );
	this.built_control ( the_root ) ;
	if ( this.load_all )  this.built_all ( the_root ) ;
	}


TreeView.prototype.display2 = function ( )  {
	the_root = this.root ( );
	this.built_control2 ( the_root ) ;
	if ( this.load_all )  this.built_all2 ( the_root ) ;
	}



/*---------------Design control - Construction du contrôle----------------------*/

TreeView.prototype.built_control = function ( node ) {
	try {
	var node_is_root = ( 0 == node.E_Num ) ;

	// CREATION DE LA BASE DU CONTRÔLE ET DE LA RACINE
	if ( node_is_root ) {

		document.write ( "<div id='TreeView_" + this.id + "'></div>" )

		this.control = document.getElementById ( "TreeView_" + this.id )
		this.control.className = 'Treeview'

		// DIMENSION
		if ( document.all ) this.width  = parseInt ( this.width ) + 2 + "px"
		if ( this.width != null ) { this.control.style.width = ( this.width.indexOf ( "px" ) != -1 ) ? parseInt ( this.width ) - 2 + "px" : this.width ; }
			else { this.control.style.width = "1000px"; }

		// PRELOADER
		if ( this.preloader_position == "top" ) this.built_preloader ( )

		var TreeView_control = document.createElement ( "ul" )
		var root = document.createElement ( "li" )
		var nobr = document.createElement ( "nobr" )
		var image_plus = document.createElement ( "img" )
		var image_racine = document.createElement ( "img" )
		var root_label = document.createElement ( "font" )
		var childrens_node = document.createElement ( "ul" )


		TreeView_control.id = 'root'

		// DIMENSION
		hauteur_preloader = ( this.use_preloader_feature && this.preloader_addButton ) ? - 30 : ( this.use_preloader_feature && ! this.preloader_addButton ) ? - 10 : -2 ;
		if ( ! document.all ) hauteur_preloader -= 4;
		if ( this.width != null ) 	TreeView_control.style.width = ( this.width.indexOf ( "px" ) != -1 ) ? parseInt ( this.width ) - 2 + "px" : this.width ;
			else { TreeView_control.style.width = "1000px"; }
		if ( this.height != null ) {
			TreeView_control.style.height = ( this.height.indexOf ( "px" ) != -1 ) ? parseInt ( this.height  ) + hauteur_preloader + "px" : this.height ;
			TreeView_control.style.overflow = "auto";
			}
		if ( document.all ) TreeView_control.style.width = parseInt ( TreeView_control.style.width ) - 2 + "px" ;

		root.id = this.id + "_item_" + node.E_Num
		root.className = "element"

		image_plus.src = "treeview/1px.gif"
		image_plus.id = this.id + "_IMAGE_PlusMinus_Folder_" + node.E_Num
		image_plus.TreeViewID = this.id
		image_plus.folderID = node.E_Num
		//image_plus.className = "moins"
		if ( this.all_exist () )  {
			image_plus.onclick = function ( )  { switch_folder_display ( this.TreeViewID , this.folderID ); return true ; };
			image_plus.ondblclick = function ( )  { switch_folder_display ( this.TreeViewID,  this.folderID ); return true ; };
			} else {	image_plus.onmousedown = function ( )  { switch_folder_display ( this.TreeViewID, this.folderID ); return true ; };   }

		image_racine.src = "treeview/1px.gif"
		//image_racine.className =  ( this.use_folder_feature ) ? node.Id_class + "_close"  : node.Id_class + "_open"
		image_racine.id = this.id + "_IMAGE_Folder_" + node.E_Num
		image_racine.TreeViewID = this.id
		image_racine.ondblclick = function ( )  { switch_folder_display ( this.TreeViewID , 0 ) ; return true ;  }

		root_label.id = this.id + "_label_" + node.E_Num
		root_label.className = "label"
		root_label.type = "DOSSIER"
		if ( document.all ) root_label.style.marginLeft = "-3px"
		this.add_feature ( this.id , node , root_label )

		childrens_node.id = this.id + "_childrens_Folder_" + node.E_Num
		childrens_node.className = "childrens"

		nobr.appendChild ( image_plus )
		nobr.appendChild ( image_racine )
		nobr.appendChild ( root_label )

		root.appendChild ( nobr )
		root.appendChild ( childrens_node )

		TreeView_control.appendChild ( root )
		this.objet ( "TreeView_" + this.id ).appendChild ( TreeView_control )

		// PRELOADER
		if ( this.preloader_position == "bottom" ) this.built_preloader ( )

		// CREATION DE LA BASE D'UN ITEM DU CONTRÔLE
		} else {
			var childrens_node = document.createElement ( "ul" )
			childrens_node.id = this.id + "_childrens_Folder_" + node.E_Num
			childrens_node.className = "childrens"
			}

	this.built_childrens ( childrens_node , node )
	return ( node_is_root ) ? "Done" : childrens_node

	}
	catch ( e )
	{
		if (this.verbose)
			alert ( "TreeView.prototype.built_control = function ( node )\n" + e.description ) ; }
	}


// Childrens Nodes design - Construction des noeuds enfants
TreeView.prototype.built_childrens = function ( parentNode , node ) {
	if ( this.have_childrens ( node.E_Num )  ) {
		var childrens = this.childrens ( node.E_Num ) ;
		var childrens_count = childrens.length ;

		for ( var i = 0 ; i < childrens_count ; i++ ) {
			var node = childrens [i] ;

			// Recherche : type du noeud ?
			if ( i != childrens_count - 1 ) { if ( node.Type == "dossier" )  var cas = ( ! this.have_childrens ( node.E_Num ) ) ? 1 : 2 ; else cas = 3 ;	}
				else { if ( node.Type == "dossier" )  var cas = ( ! this.have_childrens ( node.E_Num ) ) ? 4 : 5 ;  else cas = 6 ; }

			switch ( cas ) {
				//  Pas dernier fils
				case 1:	this.built_node_folder_without_children ( parentNode , node )
					break		// Fils = dossier sans fils
				case 2:	this.built_node_folder_with_children ( parentNode , node )
					break		// Fils = dossier avec fils
				case 3:	this.built_node_file ( parentNode , node )
					break		// Fils = fichier
				// Dernier fils
				case 4: 	this.built_last_node_folder_without_children ( parentNode , node )
					break		// Fils = dossier sans fils
				case 5:	this.built_last_node_folder_with_children ( parentNode , node )
					break		// Fils = dossier avec fils
				case 6:	this.built_last_node_file ( parentNode , node )
					break		// Fils = fichier
				default : alert(" Cas indéfini : " + cas )
				}

			// PRECHARGEMENT
			if ( this.use_preloader_feature ) {
				this.total_preloaded++
				var pourcentage = parseInt ( 100 * this.total_preloaded / this.elements.length )
				this.objet ( "preload_" + this.id ).style.width =  pourcentage + '%'
				}
			}
		}
	}


// Children Node design  - Construction d'un noeud enfant
TreeView.prototype.built_node_folder_without_children = function ( parentNode , node ) {
	var children = document.createElement ( "li" ) ;
	var nobr = document.createElement ( "nobr" ) ;
	var image_branche = document.createElement ( "img" ) ;
	var image_dossier = document.createElement ( "img" ) ;
	var children_label = document.createElement ( "font" )  ;
	children.className = "children"
	image_branche.src = "treeview/1px.gif"
	image_branche.className = "branche"
	image_dossier.src = "treeview/1px.gif"
	image_dossier.className = node.Id_class + "_close"
	image_dossier.id = this.id + "_IMAGE_Folder_" + node.E_Num
	children_label.id = this.id + "_label_" + node.E_Num
	children_label.className = "label"
	children_label.type = "DOSSIER"
	this.add_feature ( this.id , node , children_label )
	nobr.appendChild ( image_branche ) ;
	nobr.appendChild ( image_dossier ) ;
	if ( document.all ) children_label.style.marginLeft = "-3px" ;
	nobr.appendChild(children_label);
	children.appendChild ( nobr ) ;
	parentNode.appendChild ( children ) ;
	}


TreeView.prototype.built_node_folder_with_children = function ( parentNode , node ) {
	var children = document.createElement ( "li" )
	var nobr = document.createElement ( "nobr" ) ;
	var image_plus = document.createElement ( "img" )
	var image_dossier = document.createElement ( "img" )
	var children_label = document.createElement ( "font" )
	children.className = "children";
	children.id = this.id + "_item_" + node.E_Num
	children.loaded = false

	image_plus.src = "treeview/1px.gif"
	image_plus.id = this.id + "_IMAGE_PlusMinus_Folder_" + node.E_Num
	image_plus.TreeViewID = this.id
	image_plus.folderID = node.E_Num
	image_plus.className = "plus"
	if ( this.all_exist ( ) )  {
		image_plus.onclick = function ( )  { display_folder ( this.TreeViewID , this.folderID ); }
		image_plus.ondblclick = function ( )  { display_folder ( this.TreeViewID , this.folderID ); }
		} else {
			image_plus.onmousedown = function ( )  { display_folder ( this.TreeViewID , this.folderID ); }
			}

	image_dossier.src = "treeview/1px.gif"
	image_dossier.id = this.id + "_IMAGE_Folder_" + node.E_Num
	image_dossier.TreeViewID = this.id
	image_dossier.folderID = node.E_Num
	image_dossier.className = node.Id_class + "_close"
	image_dossier.ondblclick = function ( )  { display_folder ( this.TreeViewID , this.folderID ); }
	children_label.id = this.id + "_label_" + node.E_Num
	children_label.className = "label"
	children_label.type = "DOSSIER"
	if ( document.all ) children_label.style.marginLeft = "-3px"
	this.add_feature ( this.id  , node , children_label )
	nobr.appendChild ( image_plus )
	nobr.appendChild ( image_dossier )
	nobr.appendChild ( children_label )
	children.appendChild ( nobr ) ;
	parentNode.appendChild ( children )
	}


TreeView.prototype.built_node_file = function ( parentNode , node ) {
	var children = document.createElement ( "li" ) ;
	var image_branche = document.createElement ( "div" )
	var image_fichier = document.createElement ( "div" ) ;
	var children_label = document.createElement ( "font" )  ;
	children.className = "children";
	image_branche.className = "branche";
	image_fichier.className = node.Id_class;
	children_label.id = this.id + "_label_" + node.E_Num
	children_label.className = "label";
	children_label.type = "FICHIER";
	if ( document.all ) children_label.style.marginLeft = "-3px" ;
	this.add_feature ( this.id , node , children_label );
	children.appendChild ( image_branche ) ;
	children.appendChild ( image_fichier ) ;
	children.appendChild ( children_label ) ;
	parentNode.appendChild ( children ) ;
	}


TreeView.prototype.built_last_node_folder_without_children = function ( parentNode , node ) {
	var last_children = document.createElement ( "li" ) ;
	var nobr = document.createElement ( "nobr" ) ;
	var image_branche = document.createElement ( "img" )
	var image_dossier = document.createElement ( "img" ) ;
	var children_label = document.createElement ( "font" )  ;
	last_children.className = "last_children";

	image_branche.src = "treeview/1px.gif"
	image_branche.className = "branche";

	image_dossier.src = "treeview/1px.gif"
	image_dossier.className = node.Id_class+"_close";
	image_dossier.id = this.id + "_IMAGE_Folder_" + node.E_Num ;

	children_label.id = this.id + "_label_" + node.E_Num
	children_label.className = "label";
	children_label.type = "DOSSIER";
	if ( document.all ) children_label.style.marginLeft = "-3px" ;
	this.add_feature ( this.id , node , children_label );

	nobr.appendChild ( image_branche ) ;
	nobr.appendChild ( image_dossier ) ;
	nobr.appendChild ( children_label ) ;
	last_children.appendChild ( nobr ) ;

	parentNode.appendChild ( last_children ) ;
	}


TreeView.prototype.built_last_node_folder_with_children = function ( parentNode , node ) {
	var last_children = document.createElement ( "li" ) ;
	var nobr = document.createElement ( "nobr" ) ;
	var image_plus = document.createElement ( "img" )
	var image_dossier = document.createElement ( "img" ) ;
	var children_label = document.createElement ( "font" )  ;

	last_children.className = "last_children"
	last_children.id = this.id + "_item_" + node.E_Num;
	last_children.loaded = false

	image_plus.src = "treeview/1px.gif"
	image_plus.id = this.id + "_IMAGE_PlusMinus_Folder_" + node.E_Num
	image_plus.TreeViewID = this.id
	image_plus.folderID = node.E_Num
	image_plus.className = "plus"
	if ( this.all_exist ( ) )  {
		image_plus.onclick = function ( )  { display_folder ( this.TreeViewID , this.folderID ); };
		image_plus.ondblclick = function ( )  { display_folder ( this.TreeViewID , this.folderID ); };
		} else {
			image_plus.onmousedown = function ( )  { display_folder ( this.TreeViewID , this.folderID ); }
			}

	image_dossier.src = "treeview/1px.gif"
	image_dossier.id = this.id + "_IMAGE_Folder_" + node.E_Num ;
	image_dossier.TreeViewID = this.id ;
	image_dossier.folderID = node.E_Num ;
	image_dossier.className = node.Id_class + "_close" ;
	image_dossier.ondblclick = function ( )  { display_folder ( this.TreeViewID , this.folderID ); }

	children_label.id = this.id + "_label_" + node.E_Num
	children_label.className = "label";
	children_label.type = "DOSSIER";
	if ( document.all ) children_label.style.marginLeft = "-3px" ;
	this.add_feature ( this.id , node , children_label );

	nobr.appendChild ( image_plus ) ;
	nobr.appendChild ( image_dossier ) ;
	nobr.appendChild ( children_label ) ;

	last_children.appendChild( nobr );

	parentNode.appendChild ( last_children ) ;
	}


TreeView.prototype.built_last_node_file = function ( parentNode , node ) {
	var last_children = document.createElement ( "li" )
	var image_branche = document.createElement ( "div" )
	var image_fichier = document.createElement ( "div" )
	var children_label = document.createElement ( "font" )
	image_branche.className = "branche"
	image_fichier.className = node.Id_class
	children_label.id = this.id + "_label_" + node.E_Num
	children_label.className = "label"
	children_label.type = "FICHIER"
	if ( document.all ) children_label.style.marginLeft = "-3px"
	this.add_feature ( this.id , node , children_label )
	last_children.className = "last_children"
	last_children.appendChild ( image_branche )
	last_children.appendChild ( image_fichier )
	last_children.appendChild ( children_label )
	parentNode.appendChild ( last_children )
	}




TreeView.prototype.built_all = function ( node ) {
	try {
	this.control.style.display = "none" ;
	var node_is_root = ( 0 == node.E_Num ) ;
	// L'ITEM A DES FILS
	if ( this.have_childrens ( node.E_Num )  ) {
		var childrens = this.childrens ( node.E_Num ) ;
		var childrens_count = childrens.length ;
		// PARCOURS SEQUENTIEL DES FILS
		for ( var i = 0 ; i < childrens_count ; i++ ) {
			var one_children = childrens [i] ;
			var E_Num = one_children.E_Num ;
			var Type = one_children.Type ;
			if ( i != childrens_count - 1 )
				{ if ( Type == "dossier" )  cas = ( ! this.have_childrens ( E_Num ) ) ? 1 : 2 ;  else cas = 3 ;	}
			else
				{ if ( Type == "dossier" )  cas = ( ! this.have_childrens ( E_Num ) ) ? 4 : 5 ;  else cas = 6 ;	}
			if ( ( cas == 2 ) || ( cas == 5 ) ) {
				children = this.objet ( this.id + "_item_" + E_Num ) ;
				if ( ! children.loaded ) {
					var folder_childrens = this.built_control ( one_children ) ;
					children.appendChild ( folder_childrens ) ;
					children.loaded = true ;
					this.built_all ( one_children ) ;
					this.display_imageMinus ( E_Num ) ;
					if ( ! this.use_folder_feature ) this.display_imageDossier_open ( E_Num ) ;
					} else this.built_all ( one_children ) ;
				}

			}
		}
		this.control.style.display = ""
		}
		catch ( e )
		{
			if (this.verbose)
				alert ( "TreeView.prototype.built_all = function ( node )\n" + e.description ) ;
		}
	}


TreeView.prototype.destroy_all = function ( node ) {
	try {
	this.control.style.display = "none"
	var node_is_root = ( 0 == node.E_Num )
	// L'ITEM A DES FILS
	if ( this.have_childrens ( node.E_Num )  ) {
		var childrens = this.childrens ( node.E_Num )
		var childrens_count = childrens.length
		// PARCOURS SEQUENTIEL DES FILS
		for ( var i = 0 ; i < childrens_count ; i++ ) {
			var one_children = childrens[i]
			var E_E_Num = one_children.E_E_Num
			var E_Num = one_children.E_Num
			var Type = one_children.Type
			if ( i != childrens_count - 1 ) {
				if ( Type == "dossier" )  cas = ( ! this.have_childrens(E_Num) ) ? 1 : 2 ; else cas = 3 ;
				} else {
					if ( Type == "dossier" )  cas = ( ! this.have_childrens(E_Num) ) ? 4 : 5 ;  else cas = 6 ;
					}
			if ( ( cas == 2 ) || ( cas == 5 ) ) {
				var children = this.objet ( this.id + "_item_" + E_Num)
				if ( children.loaded ) {
					this.destroy_all ( one_children )
					children.loaded = false
					maref = children.removeChild ( this.objet (  this.id + "_childrens_Folder_" + E_Num ) )
					this.total_preloaded = this.total_preloaded - this.childrens_count ( E_Num )
					var pourcentage = parseInt ( 100 * this.total_preloaded / this.elements.length )
					this.objet ( "preload_" + this.id ).style.width = pourcentage + "%"
					this.display_imagePlus ( E_Num )
					this.display_imageDossier_close ( E_Num )
					}
				}
			}
		}
	this.control.style.display="";
		}
		catch (e)
		{
			if (this.verbose)
				alert ("TreeView.prototype.destroy_all = function ( node )\n" + e.description ) ;
		}
	}


TreeView.prototype.built_preloader = function ( ) {
	try {

	if ( this.use_preloader_feature ) {
		this.preloader = document.createElement ( "div" )
		this.preloader.className = "preloader"
		this.preloader.id = "preloader"
		if ( this.preloader_addButton ) {
			this.button_expandAll = document.createElement ( "div" )
			this.button_reduceAll = document.createElement ( "div" )
			this.button_builtAll = document.createElement ( "div" )
			this.button_destroyAll = document.createElement ( "div" )
		// BOUTON TOUT OUVRIR -----------------------------------------------------
			this.button_expandAll.className = "button_expandAll"
			this.button_expandAll.TreeViewID = this.id
			this.button_expandAll.style.backgroundColor = ""
			this.button_expandAll.action =  function ( )  {  eval ( this.TreeViewID + ".expand_all ( " + this.TreeViewID + ".root () );" ) ; this.onmouseout(); return true ; };
			this.button_expandAll.onmousedown = function ( )  { this.action(); }
			this.button_expandAll.onmouseover = function ( )  { this.style.backgroundColor = "#669900"; }
			this.button_expandAll.onmouseout = function ( )  { this.style.backgroundColor = ""; }
		// BOUTON TOUT FERMER -----------------------------------------------------
			this.button_reduceAll.className = "button_reduceAll"
			this.button_reduceAll.TreeViewID = this.id
			this.button_reduceAll.style.backgroundColor = ""
			this.button_reduceAll.action =  function ( )  {  eval ( this.TreeViewID + ".reduce_all ();" ) ; this.onmouseout(); return true ; }
			this.button_reduceAll.onmousedown = function ( )  { this.action(); }
			this.button_reduceAll.onmouseover = function ( )  { this.style.backgroundColor = "#669900"; }
			this.button_reduceAll.onmouseout = function ( )  { this.style.backgroundColor = ""; }
		// BOUTON TOUT CONSTRUIRE ----------------------------------------------------
			this.button_builtAll.className = "button_builtAll"
			this.button_builtAll.TreeViewID = this.id
			this.button_builtAll.style.backgroundColor = ""
			this.button_builtAll.action =  function ( )  {  eval ( this.TreeViewID + ".built_all ( " + this.TreeViewID + ".root() );" ) ; this.onmouseout(); return true ; }
			this.button_builtAll.onmousedown = function ( )  { this.action(); }
			this.button_builtAll.onmouseover = function ( )  { this.style.backgroundColor = "#669900"; }
			this.button_builtAll.onmouseout = function ( )  { this.style.backgroundColor = ""; }
		// BOUTON TOUT DETRUIRE -----------------------------------------------------
			this.button_destroyAll.className = "button_destroyAll"
			this.button_destroyAll.TreeViewID = this.id
			this.button_destroyAll.style.backgroundColor = ""
			this.button_destroyAll.action =  function ( )  {  eval ( this.TreeViewID + ".destroy_all (" + this.TreeViewID + ".root() );" ) ; this.onmouseout(); return true ; }
			this.button_destroyAll.onmousedown = function ( )  { this.action(); }
			this.button_destroyAll.onmouseover = function ( )  { this.style.backgroundColor = "#669900"; }
			this.button_destroyAll.onmouseout = function ( )  { this.style.backgroundColor = ""; }
		//
			this.preloader.appendChild ( this.button_destroyAll )
			this.preloader.appendChild ( this.button_builtAll )
			this.preloader.appendChild ( this.button_reduceAll )
			this.preloader.appendChild ( this.button_expandAll )
			this.preloader.style.height = "26px"
			} else this.preloader.style.height = "6px"
		// BARRE DE PRECHARGEMENT-----------------------------------------------------
			this.preload_container = document.createElement ( "div" )
			this.preload = document.createElement ( "div" )
			this.preload_container.id ="preloadContainer_" + this.id
			this.preload_container.className = "preload_container"
			this.preload.id = "preload_" + this.id
			this.preload.className = "preload"
			this.preload.style.width = "1%"

			this.objet ( "TreeView_" + this.id ).appendChild ( this.preload_container )
			this.preloader.appendChild ( this.preload_container )
			this.objet ( "TreeView_" + this.id ).appendChild ( this.preloader )
			this.preload_container.appendChild ( this.preload )
			}
		return true
		}
		catch (e)
		{
			if (this.verbose)
				alert ("TreeView.prototype.built_preloader = function ( node )\n" + e.description ) ;
		}
	}


// Features Add - Ajout de fonctionnalités
TreeView.prototype.add_feature = function ( TreeViewID , node, children_label ) {
	try {
	var E_Num = node.E_Num
	var Libelle = node.Libelle
	var Id_class = node.Id_class
	var Lien = ( this.link_add_nodeId ) ?  this.link_prefix + node.Lien + this.link_suffix + E_Num : this.link_prefix + node.Lien + this.link_suffix ;
	// LIEN HYPERTEXTE
	children_label.lien = Lien;
	children_label.link_target = this.link_target;
	children_label.title = Libelle;
	children_label.name = Libelle;
	children_label.innerHTML = Libelle ;
	children_label.TreeViewID = TreeViewID ;
	children_label.folderID = E_Num ;
	if ( this.use_link_feature ) {
		if ( this.all_exist ( ) )  {
			children_label.onclick = function ( )  { goto_link ( this.TreeViewID , this.folderID , this.link_target , this.lien ) ; };
			children_label.ondblclick = function ( )  { goto_link ( this.TreeViewID , this.folderID , this.link_target , this.lien ) ; };
			} else {	children_label.onmousedown = function ( )  { goto_link ( this.TreeViewID , this.folderID , this.link_target , this.lien ) ; }; }
		}
		}
		catch (e)
		{
			if (this.verbose)
				alert ("TreeView.prototype.add_feature = function ( TreeViewID="+TreeViewID+" , node, children_label )\n" + e.description ) ;
		}
	}



/*------------------------Image-----------------------------------------------*/

//   Switch folder image - Change l'image d'un dossier
TreeView.prototype.switch_imageDossier = function (folderID ) {
	try {
		var imageDossier = this.objet ( this.id + "_IMAGE_Folder_" + folderID ) ;
		var folder = this.node ( folderID ) ;
		var Id_class = folder.Id_class ;
		if ( imageDossier.className == Id_class + "_open" ) imageDossier.className = Id_class + "_close" ;
			else imageDossier.className = Id_class + "_open" ;
		return true ;
		}
		catch (e)
		{
			if (this.verbose)
				alert ("TreeView.prototype.switch_imageDossier = function ( folderID="+folderID+" )\n" + e.description ) ;
		}
	}


TreeView.prototype.display_imageDossier_open = function ( folderID ) {
	try {
		var imageDossier = this.objet ( this.id + "_IMAGE_Folder_" + folderID ) ;
		var folder = this.node ( folderID ) ;
		var Id_class = folder.Id_class ;
		imageDossier.className = Id_class + "_open" ;
		return true ;
	}
	catch (e)
	{
		if (this.verbose)
			alert ("TreeView.prototype.display_imageDossier_open = function ( folderID="+folderID+" )\n" + e.description ) ;
	}
}


TreeView.prototype.display_imageDossier_close = function ( folderID ) {
	try {
		var imageDossier = this.objet ( this.id + "_IMAGE_Folder_" + folderID ) ;
		var folder = this.node ( folderID ) ;
		var Id_class = folder.Id_class ;
		imageDossier.className = Id_class + "_close" ;
		return true ;
		}
		catch (e)
		{
			if (this.verbose)
				alert ("TreeView.prototype.display_imageDossier_close = function ( folderID="+folderID+" )\n" + e.description ) ;
		}
	}


//   Switch (+/-) image  - Change l'image (+/-)
TreeView.prototype.switch_imagePlusMinus = function ( folderID ) {
	try {
		imagePlusMinus = this.objet ( this.id + "_IMAGE_PlusMinus_Folder_" + folderID ) ;
		if ( imagePlusMinus.className == "plus" ) imagePlusMinus.className = "moins" ;
			else imagePlusMinus.className = "plus" ;
		return true ;
		}
		catch (e)
		{
			if (this.verbose)
				alert ("TreeView.prototype.switch_imagePlusMinus = function ( folderID="+folderID+" )\n" + e.description ) ;
		}
	}


TreeView.prototype.display_imagePlus = function ( folderID ) {
	try {
		imagePlusMinus = this.objet ( this.id + "_IMAGE_PlusMinus_Folder_" + folderID ) ;
		imagePlusMinus.className = "plus" ;
		return true ;
		} catch (e)
		{
			if (this.verbose)
				alert ("TreeView.prototype.display_imagePlus = function ( folderID="+folderID+" )\n" + e.description ) ;
		}
	}


TreeView.prototype.display_imageMinus = function ( folderID ) {
	try {
		imagePlusMinus = this.objet ( this.id + "_IMAGE_PlusMinus_Folder_" + folderID ) ;
		imagePlusMinus.className = "moins" ;
		return true ;
		} catch (e)
		{
			if (this.verbose)
				alert ("TreeView.prototype.display_imageMinus = function ( folderID="+folderID+" )\n" + e.description ) ;
		}
	}




/*************************************************************
                                    INTERACTION
**************************************************************/

/*------------------------Object/Navigator- Objet/Navigateur-----------------------------------------------------------*/

TreeView.prototype.objet = function ( id ) { try {	return document.getElementById ( id ) ; } catch (e) { alert ("TreeView.prototype.objet = function ( id="+id+" ) "); } }
TreeView.prototype.all_exist = function ( )  { return !( navigator.appName.indexOf ( "Microsoft" , 0) == -1 ) ; }


/*----------------------- Alternate Reduce all folder & Collapse all folder  - Réduit tous les dossier ------------------------------*/

TreeView.prototype.switch_all_display = function ( )  {
	try {
		var childrens = this.childrens ( 0 ) ;
		var some_childrens_are_displayed = true ;
		for ( var i = 0 ; i < childrens.length ; i++ ) {
			var folderID = childrens[i].E_Num ;
			if ( childrens[i].Type == "dossier" && this.have_childrens ( folderID ) ) {
				try {
					childrens_DIV = this.objet ( this.id + "_childrens_Folder_" + childrens[i].E_Num ) ;
					some_childrens_are_displayed = ( some_childrens_are_displayed && childrens_DIV.style.display == "" )
					} catch (e) { }
				}
			}
		if ( some_childrens_are_displayed ) this.reduce_all ( ) ; else this.expand_all ( ) ;
		return true;
		} catch (e)
		{
			if (this.verbose)
				alert ( "TreeView.prototype.switch_all_display = function ( )\n" + e.description ) ;
		}
	}



/*----------------------- Reduce all folder - Réduit tous les dossier ------------------------------*/

TreeView.prototype.reduce_all = function ( )  {
	try {
		this.control.style.display="none";
		for ( var i = 0 ; i < this.elements.length ; i++ ) {
			var folderID = this.elements[i].E_Num ;
			var type = this.elements[i].Type ;
			if (  type == "dossier" && folderID != 0  ) { // || ( ! this.root_childrens_always_displayed && folderID == 0 )
				if ( this.have_childrens ( folderID ) ) {
					try {
						var folder = this.objet ( this.id + "_item_" + folderID );
						if ( folder.loaded ) {
							this.mask_childrens ( folderID ) ;
							this.display_imagePlus ( folderID ) ;
							if ( !this.use_folder_feature ) this.display_imageDossier_close ( folderID );
							}
						} catch (e) { }
					}
				}
			}
		this.control.style.display="";
		return true;
		} catch (e)
		{
			if (this.verbose)
				alert ( "TreeView.prototype.reduce_all = function ( )\n" + e.description ) ;
		}
	}



/*----------------------- Expand all folder - Agrandit tous les dossier ----------------------------*/

TreeView.prototype.expand_all = function ( )  {
	try {
		this.control.style.display = "none" ;
		this.display_childrens ( 0 ) ;
		this.display_imageMinus ( 0 ) ;
		if ( this.load_all )  this.built_all ( this.root ( ) ) ;  // (*)
		for ( var i = 0 ; i < this.elements.length ; i++ ) {
			var folderID = this.elements[i].E_Num ;
			var type = this.elements[i].Type ;
			if ( ( type == "dossier" && folderID != 0 ) ) {
				if ( this.have_childrens ( folderID ) ) {
					try {
						var folder = this.objet ( this.id + "_item_" + folderID ) ;
						if ( folder.loaded ) {
							this.display_childrens ( folderID ) ;
							this.display_imageMinus ( folderID ) ;
							if ( !this.use_folder_feature ) this.display_imageDossier_open ( folderID ) ;
							}
						} catch (e) { }
					}
				}
			}
		this.control.style.display = "" ;
		return true ;
		} catch (e)
		{
			if (this.verbose)
				alert ( "TreeView.prototype.expand_all = function ( )\n" + e.description ) ;
		}
	}



/*----------------------Display folder childrens - Affiche les enfants d'un dossier-------------------*/

TreeView.prototype.switch_childrens_display = function ( folderID ) {
	try {
		childrens = this.objet ( this.id + "_childrens_Folder_" + folderID ) ;
		childrens_are_displayed =  (childrens.style.display == '' ) ;
		if ( childrens_are_displayed ) childrens.style.display = 'none' ;
			else childrens.style.display = '';
		return true ;
		} catch (e)
		{
			if (this.verbose)
				alert ( "TreeView.prototype.switch_childrens_display = function ( folderID="+folderID+" )\n" + e.description ) ;
		}
	}


TreeView.prototype.display_childrens = function ( folderID ) {
	try {
		childrens = this.objet ( this.id + "_childrens_Folder_" + folderID ) ;
		childrens.style.display = '' ;
		return true ;
		} catch (e)
		{
			if (this.verbose)
				alert ( "TreeView.prototype.display_childrens = function ( folderID="+folderID+" )\n" + e.description ) ;
		}
	}


TreeView.prototype.mask_childrens = function ( folderID ) {
	try {
		childrens = this.objet ( this.id + "_childrens_Folder_" + folderID ) ;
		childrens.style.display = 'none' ;
		return true ;
		} catch (e)
		{
			if (this.verbose)
				alert ( "TreeView.prototype.mask_childrens = function ( folderID="+folderID+" )\n" + e.description ) ;
		}
	}



/*----------------------Display folder "files" - Affiche "les fichiers" d'un dossier-------------------*/

switch_folder_display = function ( TreeViewID , folderID ) {
	try {
		// alert ( "TreeViewID:" + TreeViewID + " | folderID:" + folderID ) ;
		var TreeView = eval ( TreeViewID );
		TreeView.control.style.visibility="hidden";
		if ( TreeView.use_folder_feature ) {
				eval ( TreeViewID + ".switch_childrens_display (" + folderID + ");") ;
				eval ( TreeViewID + ".switch_imagePlusMinus (" + folderID + ");") ;
			} else {
				eval ( TreeViewID + ".switch_childrens_display (" + folderID + ");") ;
				eval ( TreeViewID + ".switch_imageDossier (" + folderID + ");" ) ;
				eval ( TreeViewID + ".switch_imagePlusMinus (" + folderID + ");") ;
				}
		TreeView.control.style.visibility="visible";
		return true ;
		} catch (e)
		{
			if (this.verbose)
				alert ( "switch_folder_display = function ( TreeViewID="+TreeViewID+" , folderID="+folderID+" )\n" + e.description ) ;
		}
	}


display_folder = function ( TreeViewID , folderID ) {
	try {
		var TreeView = eval ( TreeViewID ) ;
		var folder = TreeView.objet ( TreeViewID + "_item_" + folderID );
		if ( folder.loaded ) switch_folder_display ( TreeViewID , folderID ) ;
			else {
				var folder_childrens = TreeView.built_control ( TreeView.node ( folderID ) ) ;
				folder.appendChild ( folder_childrens ) ;
				folder.loaded = true;
				switch_folder_display ( TreeViewID , folderID ) ;
				TreeView.display_childrens ( folderID) ;
				}
		return true ;
		} catch ( e )
		{
			if (this.verbose)
				alert ( "display_folder = function ( TreeViewID="+TreeViewID+" , folderID="+folderID+" )\n" + e.description ) ;
		}
	}



/*----------------------Ouvre le lien d'un élément du contrôle-------------------*/

goto_link = function ( TreeViewID , E_Num , link_target , lien ) {
	try {
		var TreeView = eval ( TreeViewID )
		var Type = TreeView.node ( E_Num ).Type

		/* SELECTED LABEL  */
		var IdLastFile = ( TreeView.last_folderId == 0 ) ? 0 : TreeView.IdLastFile  ;
		window.status = document.getElementById ( TreeViewID + "_label_" + E_Num ).className ;
		document.getElementById ( TreeViewID + "_label_" + IdLastFile ).className =  "label"
		document.getElementById ( TreeViewID + "_label_" + E_Num ).className = "label_selected"
		TreeView.IdLastFile = E_Num ;

		/* TREEVIEW FEATURE */
		if ( TreeView.use_folder_feature && Type == "dossier" ) {
			TreeView.display_imageDossier_close ( TreeView.last_folderId );
			TreeView.display_imageDossier_open ( E_Num );
			}
		if ( Type == "dossier" ) TreeView.last_folderId = E_Num ;

		/* REDIRECTION */
		switch ( link_target ) {
			case "_blank": new_window = window.open ( lien ); return true ;
				break;
			case "_parent": window.parent.location.href = lien ; return true ;
				break;
			case "_self": self.location.href = lien ; return true ;
				break;
			case "_top": top.location.href = lien ; return true ;
				break;
			default : setTimeout ( "top.frames." + link_target + ".location.href = '" + lien +"'" , 10 ) ; return true ;
			}
		} catch ( e )
		{
			if (this.verbose)
				alert ( "goto_link = function ( TreeViewID="+TreeViewID+" , E_Num="+E_Num+" , link_target="+link_target+" , lien="+lien+" )\n" + e.description ) ;
		}
	}