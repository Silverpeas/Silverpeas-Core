/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/*************************************************************
                                 CONSTRUCTOR
**************************************************************/
function TreeViewElements() {
	this.elements = new Array();
	}


/*************************************************************
                                   SET / ADD
**************************************************************/
TreeViewElements.prototype.addElement = function ( Libelle , Id , Id_pere , Type , Id_class , Lien  ) {
	element = new Object();
	element.Libelle = Libelle;
	element.E_Num = Id;
	element.E_E_Num = Id_pere;
	element.Type = Type;
	element.Id_class = Id_class;
	element.Lien = Lien;
	this.elements[this.elements.length] = element;
	}


/*************************************************************
                                   GET
**************************************************************/

/*-------------------------Properties--------------------------------------*/
TreeViewElements.prototype.attribute = function ( Id , Attribute ) {
	for ( m=0 ; m<this.elements.length ; m++ ) {
		l_element = this.elements[m];
		if ( l_element.E_Num == Id ) return eval("l_element."+Attribute);
		}
	alert("Die : TreeViewElements.prototype.attribute");
	}

/*-------------------------Boolean--------------------------------------*/
TreeViewElements.prototype.est_fichier = function ( Id ) {
	for ( m=0 ; m<this.elements.length ; m++ ) {
		l_element = this.elements[m];
		if ( l_element.E_Num == Id)  return (l_element.Type=="fichier");
		}
	alert("Die : TreeViewElements.prototype.est_fichier");
	}

TreeViewElements.prototype.est_dossier = function ( Id ) {
	for ( m=0 ; m<this.elements.length ; m++ ) {
		l_element = this.elements[m];
		if ( l_element.E_Num == Id)  return (l_element.Type=="dossier");
		}
	alert("Die : TreeViewElements.prototype.est_dossier");
	}


/*************************************************************
                                   ORDER BY
**************************************************************/
TreeViewElements.prototype.orderBy_Type = function ( mode ) {
	var tempDossier = new Array();
	var tempFichier = new Array();
	for ( var i=0; i<this.elements.length; i++ ) {
		var element = this.elements[i];
		if (element.Type=="dossier") tempDossier[tempDossier.length]=element;
		if (element.Type=="fichier") tempFichier[tempFichier.length]=element;
		}
	tempDossier = this.trier(tempDossier);
	tempFichier = this.trier(tempFichier);
	this.elements = tempDossier.concat(tempFichier);
	if (mode=="DESC") this.elements.reverse();
	}

TreeViewElements.prototype.orderBy_Libelle = function ( mode ) {
	this.elements = this.trier(this.elements);
	if (mode=="DESC") this.elements.reverse();
	}

TreeViewElements.prototype.sort = function ( a , b ) {
	/*
	This function must return :
		- A negative value if the first argument is smaller than the second.
		- Zero if the 2 argument are equivalent.
		- A positive value if the first argument is bigger than the second.
	*/
	return a<b;
	}

TreeViewElements.prototype.trier = function ( tableau ) {
	if (tableau.length>0) {
		var temp = new Array();
		var temp_tab = new Array();
		for ( var i=0; i<tableau.length; i++ ) temp_tab[i] = tableau[i];
		for ( var i=0; i<temp_tab.length; i++ ) {
			var plus_petit = temp_tab[i];
			var pos = i;
			for ( var j=0; j<tableau.length; j++ )  {
								//     ORDER WITH "<"
				if ((j>i) && this.sort(tableau[j].Libelle, plus_petit.Libelle)) {
					plus_petit = tableau[j];
					pos = j;
					}
				}
			temp[i] = plus_petit;
			tableau[pos] = temp_tab[i];
			temp_tab[pos] = temp_tab[i];
			}
		return temp;
		}
	alert("Die: TreeViewElements.prototype.trier");
	}


/*************************************************************
                                   VALIDATION
**************************************************************/
TreeViewElements.prototype.validate = function ( ) {
	if (this.elements.length>0) {
		if (!this.type_OK()) alert("TreeViewElements : element.Type incorrecte !");
		if (!this.a_une_racine()) alert("TreeViewElements : pas de racine définie !");
		if (!this.unicite_racine()) alert("TreeViewElements : plusieurs racines définie !");
		if (!this.elements_ont_pere()) alert("TreeViewElements : Des éléments n'ont pas de père !");
		} else alert("TreeViewElements : Aucun élément défini !");
	}


TreeViewElements.prototype.type_OK = function ( ) {
	for ( m=0 ; m<this.elements.length ; m++ ) {
		l_element = this.elements[m];
		if (!( l_element.Type == "dossier" || l_element.Type == "fichier" )) {
			alert("TreeViewElements : Type élément '"+l_element.Type+"' incorrecte !");
			return false;
			}
		}
	return true;
	}

/*-------------------------Racine------------------------------*/
TreeViewElements.prototype.a_une_racine = function ( ) {
	var m;
	for ( m=0 ; m<this.elements.length ; m++ ) {
		l_element = this.elements[m];
		if ( l_element.E_Num == l_element.E_E_Num && l_element.E_Num==0 ) return true;
		}
	return false;
	}

TreeViewElements.prototype.unicite_racine = function ( ) {
	var m;
	var nb_E_Num_egal_a_0=0;
	var nb_E_Num_egal_a_E_E_Num=0;
	for ( m=0 ; m<this.elements.length ; m++ ) {
		var l_element = this.elements[m];
		if ( l_element.E_Num == l_element.E_E_Num ) nb_E_Num_egal_a_E_E_Num++;
		if ( l_element.E_Num==0 ) nb_E_Num_egal_a_0++;
		}
	if ( nb_E_Num_egal_a_E_E_Num==0 ) alert("TreeViewElements.validate\nPas de racine définie. \n Une racine à pour attribut identifiant Id=0 et pour père Id_Pere=0.");
	if ( nb_E_Num_egal_a_E_E_Num>1 ) alert("TreeViewElements.validate\nPlusieurs éléments ont pour père soit-même.");
	if ( nb_E_Num_egal_a_0>1 ) alert("TreeViewElements.validate\nPlusieurs élément ont pour attribut Id=0.");
	return true;
	}

/*-------------------------Fils-------------------------------*/
TreeViewElements.prototype.elements_ont_pere = function ( ) {
	nb_elements = this.elements.length;
	for ( i=0 ; i<nb_elements ; i++ ) {
		trouve = false;
		l_element = this.elements[i];
		for ( j=0 ; j<nb_elements && !trouve ; j++ ) {
			un_element = this.elements[j];
			if (l_element.E_E_Num==un_element.E_Num) trouve = true;
			}
		if (!trouve) alert("TreeViewElements.validate\nPas de père trouvé pour l'élément E_Num="+l_element.E_Num);
		}
	return true;
	}