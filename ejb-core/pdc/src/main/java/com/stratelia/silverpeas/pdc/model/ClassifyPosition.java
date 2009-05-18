package com.stratelia.silverpeas.pdc.model;

import java.util.List;

import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;

/**
* @author Nicolas EYSSERIC
*/
public class ClassifyPosition extends com.stratelia.silverpeas.classifyEngine.Position
        implements ContainerPositionInterface, java.io.Serializable
{

    public ClassifyPosition() {
    }

    public ClassifyPosition(List values) {
        super(values);
    }

    public ClassifyPosition(int nPositionId, List values) {
        super(nPositionId, values);
    }

    public String getValueOnAxis(int axisId) {
        List values = getValues();
        ClassifyValue value = null;
        for (int i = 0; i < values.size(); i++) {
            value = (ClassifyValue) values.get(i);
            if (value.getAxisId() == axisId) {
                return value.getValue();
            }
        }
        return null;
    }

    /** Return true if the position is empty */
    public boolean isEmpty() {
        return (getPositionId() == -1 || getValues() == null);
    }

    public String toString() {
        return "ClassifyPosition object :[ positionId=" + getPositionId() + ", " +
                 " value=" + getValues();
    }

    /**
     * Méthodes nécéssaire pour le mapping castor du module importExport.
     * @return
     */
    public List getListClassifyValue() {
    	return super.getValues();
    }
    
    /**
     * Méthodes nécéssaire pour le mapping castor du module importExport.
     * @param values
     */
    public void setListClassifyValue(List values) {
    	super.setValues(values);
    }

}