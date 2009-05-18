package com.stratelia.silverpeas.containerManager;

import java.util.List;

/**
 * This is the interfcae on data structure that the content JSP is going to use (built by the container router)
 * 
 */
public interface ContainerContext
{
		/* Get the URL to get Back on the container */
		public String getReturnURL();

		/* Get the URLIcone corresponding to classify */
		public URLIcone getClassifyURLIcone();

		/* Get the classify URL with parameters to put as link on the Classify Icone */
		public String getClassifyURLWithParameters(String sComponentId, String sSilverContentId);

		/** Find the SearchContext for the given SilverContentId */
		public ContainerPositionInterface getSilverContentIdSearchContext(int nSilverContentId, String sComponentId);

		/* Get All the SilverContentIds corresponding to the given position in the given Components */
		public List getSilverContentIdByPosition(ContainerPositionInterface containerPosition, List alComponentIds);
}
