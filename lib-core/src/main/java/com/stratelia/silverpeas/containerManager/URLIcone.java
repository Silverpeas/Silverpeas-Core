package com.stratelia.silverpeas.containerManager;

/**
 * This is the data structure that represents a link links between icone and action (use in JSP ActionBar)
 * 
 */
public class URLIcone
{
		private String sIconePath = "";					// path on the icone to show in the JSP
		private String sAlternateText = "";			// Alternate text on the icone
		private String sActionURL = "";					// URLs on the action to link with the icones
		private boolean bPopUp = true;					// Tells the JSP to open the sActionURL in a new PopUp window or not
		
		public URLIcone()
		{
		}

		public void setIconePath(String sGivenIconePath)
		{
				sIconePath = sGivenIconePath;
		}

		public String getIconePath()
		{
				return sIconePath;
		}

		public void setAlternateText(String sGivenAlternateText)
		{
				sAlternateText = sGivenAlternateText;
		}

		public String getAlternateText()
		{
				return sAlternateText;
		}

		public void setActionURL(String sGivenActionURL)
		{
				sActionURL = sGivenActionURL;
		}

		public String getActionURL()
		{
				return sActionURL;
		}

		public void setPopUp(boolean bGivenPopUp)
		{
				bPopUp = bGivenPopUp;
		}

		public boolean getPopUp()
		{
				return bPopUp;
		}
}
