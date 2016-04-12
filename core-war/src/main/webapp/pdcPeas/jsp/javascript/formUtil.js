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
var selectArray=[]; // tableau qui contiendra toutes les valeurs du tag SELECT

// Cette fonction efface les caracteres blancs de gauche
function ltrim(texte){
	while (texte.substring(0,1) == ' '){
		texte = texte.substring(1, texte.length);
	}

	return texte;
}

// Cette fonction efface les caracteres blancs de droite
function rtrim(texte){
	while (texte.substring(texte.length-1,texte.length) == ' ') {
		texte = texte.substring(0, texte.length-1);
	}

	return texte;
}

// Cette fonction efface les caracteres blancs se trouvant avant et apres du texte
function trim(texte){
	var len = texte.length;
	if (len == 0){
		texte = "";
	}
	else {
		texte = rtrim(texte);
		texte = ltrim(texte);
	}
	return texte;
}

// verifie que le champ requis n'est pas vide
function isEmptyField(texte){
	var isEmpty = false;
	var field = trim(texte);
	if ( field == "" ){
		isEmpty = true;
	}
	return isEmpty;
}


//
// attention, cette fonction javascript ne fonctionne que si le tableau
// selectArray est définie dans la jsp et si lors du OnLoad, on appelle
// une methode qui initialise ce tableau par les valeurs des tags options
//
function highlightItem(selectTag,text){

	// recherche de toutes les valeurs commencant par 'text'

	len = selectTag.length; // le nombre d'éléments contenus dans le tag SELECT
	var subArray=[]; // tableau qui contiendra les valeurs les plus 'proches' de la chaine de caractères rentrée
	var j=0;
	for (i=0;i<len;i++){
		if (text.toLowerCase() <= selectArray[i]){
			subArray[j] = selectArray[i];
			j++;
		}
	}

	// Maintenant, si on a bien un tableau contenant des valeurs,
	// il faut chercher la valeur la plus proche du texte
	// sinon, on sélectionne l'item que l'on veut. Pour moi,
	// c'est le dernier
	if (subArray.length > 0){
		var value=""; // valeur tampon
		var rightValue = subArray[0]; // par defaut, la bonne valeur choisie est la 1ere
		for (k=0;k<j-1;k++){
			// on lit tout le tableau intermediaire
			// et on compare ses elements un a un
			var elt1 = subArray[k];
			var elt2 = subArray[k+1];
			//alert(" les 2 valeurs comparées "+elt1+"  "+elt2);
			if ( elt1 < elt2){
				value = elt1;
			} else {
				value = elt2;
			}
			// une fois la valeur la plus petite entre les deux elements consécutifs du tableau
			// trouvée, on la compare avec la valeur déjà trouvée
			if (value < rightValue){
				rightValue = value;
			}
			//alert(" la valeur gardée "+rightValue);
		}
		// maintenant, je récupère la valeur de l'index correspondant à la valeur sélectionnée
		// pour cela je lis le tableau contenant tous les items du tag SELECT
		// et je sors dès que je trouve l'item correspondant tout en
		// gardant la position
		var index = 0;
		for (i=0;i<len;i++){
			if (selectArray[i] == rightValue){
				index = i;
				break;
			}
		}
		selectTag.selectedIndex = index; // sélectionne l'item trouvé
	} else {
		selectTag.selectedIndex = len-1; // sélectionne par defaut le dernier
	}
	//alert(" la valeur la plus proche de "+text+" est : "+rightValue+" avec pour index "+index);
}


// cette fonction permet de revenir à la page appellante
// remplace le history.go(-1) qui ne doit pas fonctionner sous Netscape
function goBack(){
		history.back();
		//history.go(-1);
}

// fonction qui initialise le tableau selectArray
// dès le chargement de la page
function storeItems(selectTag){
	len = selectTag.length;
	for (i=0;i<len;i++){
		selectArray[i] = (selectTag.options[i].value).toLowerCase();
	}
}
