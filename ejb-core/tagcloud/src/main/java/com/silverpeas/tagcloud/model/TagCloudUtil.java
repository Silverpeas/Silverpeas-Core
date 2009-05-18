package com.silverpeas.tagcloud.model;

/**
 * Utilities class.
 * Used to generate the correct spelling for the tag of a tagcloud.
 */
public class TagCloudUtil
{
	
	// List of characters used to generate a correct tag.
	// For each line, the characters contained in the first string has to be replaced by the
	// character of the second string.
	private static final String[][] TAG_DATA = {
	    {"(Ç)", 			"C"},
	    {"(À|Á|Â|Ã|Ä|Å|Æ)", "A"},
	    {"(È|É|Ê|Ë)", 		"E"},
	    {"(Ì|Í|Î|Ï)", 		"I"},
	    {"(Ò|Ó|Ô|Õ|Ö)", 	"O"},
	    {"(Ù|Ú|Û|Ü)", 		"U"},
	    {"(Ý)", 			"Y"}
	};
	
	/**
	 * @param s The string to convert into a valid tag.
	 * @return The tag corresponding to the string given as parameter.
	 */
	public static String getTag(String s)
	{
		s = s.toUpperCase();
		for (int i = 0, n = TAG_DATA.length; i < n; i++)
		{
			s = s.replaceAll(TAG_DATA[i][0], TAG_DATA[i][1]);
		}
	    return s;
	}
	
}