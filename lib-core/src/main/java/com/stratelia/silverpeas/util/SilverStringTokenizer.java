package com.stratelia.silverpeas.util;

import java.util.ArrayList;
import java.util.List;

/**
* Cette classe permet de récupérer des mots
* d'une chaine de caractères en fonction d'un séparateur.
* Ce séparateur peut être un caractère ou bien une chaine de caractères
*/
public class SilverStringTokenizer {
		
		/**
		* Contient tous les mots de la chaine de caracteres qui ne sont pas les
		* motifs
		*/
		private List<String>tokens = new ArrayList<String>();

		/**
		* Recherche le motif dans la pattern.
		* Récupère les mots de la pattern qui ne sont pas les motifs
		* puis les ajoute dans la liste.
		*/
		private void parse(String pattern, String motif){
			if ( (pattern != null) && (motif != null) ){
				int motif_idx = pattern.indexOf(motif);
				int motif_len = motif.length();
				while(motif_idx != -1){
				  String token = pattern.substring(0,motif_idx);
					if (!"".equals(token)){
						tokens.add(token);
					}
					pattern = pattern.substring(motif_idx+motif_len);
					motif_idx = pattern.indexOf(motif);
				}
				if (tokens.size() == 0){
					// le motif n'apparait pas dans le pattern
					// on rajoute alors le pattern complet
					tokens.add(pattern);
				}				
				if ( (pattern.length() > 0) && (motif_idx == -1) ){
					// il reste donc encore un mot
					// on le rejoute
					tokens.add(pattern);
				}
			}
		}

		public SilverStringTokenizer(String pattern){
			parse(pattern," ");
		}

		public SilverStringTokenizer(String pattern, String motif){
			parse(pattern,motif);
		}

		/**
		* Calcul et retourne la taille de la liste 
		* @return le nombre d'élément qui sont dans la liste
		*/
		public int countTokens(){
			return tokens.size();
		}
	
		/**
		* Détermine s'il y a des mots dans la pattern qui ne sont pas dans la liste
		* @return vrai s'il existe de mots sinon faux
		*/
		public boolean hasMoreTokens(){
			return (tokens.size()>0)?true:false;
		}
	
		/**
		* enleve le 1er mot de la liste
		* @return le 1er mot de la liste
		*/
		public String nextToken(){
			return (String)tokens.remove(0);
		}
}