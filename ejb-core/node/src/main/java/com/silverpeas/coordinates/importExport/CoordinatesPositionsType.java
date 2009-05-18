package com.silverpeas.coordinates.importExport;

import java.util.List;

/**
 * @author dlesimple
 *
 */
public class CoordinatesPositionsType {

	private List coordinatesPositions;
	private boolean createEnable = false;

	/**
	 * @return List of CoordinatesPositions
	 */
	public List getCoordinatesPositions() {
		return coordinatesPositions;
	}

	/**
	 * @param listCoordinatesPositions
	 */
	public void setCoordinatesPositions(List coordinatesPositions) {
		this.coordinatesPositions = coordinatesPositions;
	}

	/**
	 * @return Create mode enabled
	 */
	public boolean getCreateEnable() {
		return createEnable;
	}
	
	/**
	 * @param listCoordinatesPositions
	 */
	public void setCreateEnable(boolean createEnable) {
		this.createEnable = createEnable;
	}
}
