package com.stratelia.silverpeas.containerManager;

import java.sql.Connection;
import java.util.*;

/**
 * The interface for all the containers (PDC, ..)
 * Every container have to implement this interface and declare it in the containerDescriptor (xml)
 */
public interface ContainerInterface
{
		/** Return the parameters for the HTTP call on the classify */
		public String getCallParameters(String sComponentId, String sSilverContentId);

		/** Remove all the positions of the given content */
		public List removePosition(Connection connection, int nSilverContentId) throws ContainerManagerException;

		/** Find the search Context for the given SilverContentId */
		public ContainerPositionInterface getSilverContentIdSearchContext(int nSilverContentId, String sComponentId) throws ContainerManagerException;

		/** Find all the SilverContentId with the given position */
		public List findSilverContentIdByPosition(ContainerPositionInterface containerPosition, List alComponentId) throws ContainerManagerException;
		public List findSilverContentIdByPosition(ContainerPositionInterface containerPosition, List alComponentId, String authorId, String afterDate, String beforeDate) throws ContainerManagerException;
}