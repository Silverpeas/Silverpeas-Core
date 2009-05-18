package com.stratelia.silverpeas.contentManager;

import com.silverpeas.util.i18n.AbstractI18NBean;
import com.silverpeas.util.i18n.I18NHelper;

public class SilverContent extends AbstractI18NBean implements SilverContentInterface
{
		private String silverContentName;
		private String silverContentDescription;
		private String silverContentURL;

		public SilverContent(String name, String description, String url) {
				setName(name);
				setDescription(description);
				setURL(url);
		}

		public String getName()
		{
				return silverContentName;
		}
		
		public String getName(String language)
		{
			if (!I18NHelper.isI18N)
				return getName();
		  	
			SilverContentI18N p = (SilverContentI18N) getTranslations().get(language);
		  	if (p == null)
		  		p = (SilverContentI18N) getNextTranslation();
		  	
		  	return p.getName();
		}

		public String getDescription()
		{
				return silverContentDescription;
		}
		
		public String getDescription(String language)
		{
			if (!I18NHelper.isI18N)
				return getDescription();
		  	
			SilverContentI18N p = (SilverContentI18N) getTranslations().get(language);
		  	if (p == null)
		  		p = (SilverContentI18N) getNextTranslation();
		  	
		  	return p.getDescription();
		}

		public String getURL()
		{
				return silverContentURL;
		}

		public void setName(String name)
		{
				silverContentName = name;
		}

		public void setDescription(String description)
		{
				silverContentDescription = description;
		}

		public void setURL(String url)
		{
				silverContentURL = url;
		}

		public String getId()
		{
			return "unknown";
		}

		public String getInstanceId()
		{
			return "unknown";
		}

        public String getTitle()
		{
			return getName();
		}

        public String getDate() 
		{
			return "unknown";
		}
		
		public String getSilverCreationDate() 
		{
			return "unknown";
		}

        public String getIconUrl()
		{
			return "unknown";
		}

		public String getCreatorId()
		{
			return "unknown";
		}

		public String toString() {
			return ("silverContent contains Name = "+getName()+", Description = "+getDescription()+", Url = "+getURL());
		}

}