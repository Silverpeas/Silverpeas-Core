package com.stratelia.silverpeas.classifyEngine;

import com.stratelia.webactiv.util.exception.*;

public class Value extends Object implements java.io.Serializable
{
    	private int nAxisId = -1;
    	private int physicalAxisId = -1;
		private String sValue = null;
		
		// Constructor
		public Value(int nGivenAxisId, String sGivenValue)
    {
				nAxisId = nGivenAxisId;
				sValue = sGivenValue;
    }

		public Value()
    {
    }

		public void setAxisId(int nGivenAxisId)
		{
				nAxisId = nGivenAxisId;
		}

		public int getAxisId()
		{
				return nAxisId;
		}

		public void setValue(String sGivenValue)
		{
				sValue = sGivenValue;
		}

		public String getValue()
		{
				return sValue;
		}

		public void checkValue() throws ClassifyEngineException
		{
				// Check the axisId
				if(this.getAxisId() < 0)
            throw new ClassifyEngineException("Value.checkValue",SilverpeasException.ERROR,"classifyEngine.EX_INCORRECT_AXISID_VALUE");
		}
		
		public void setPhysicalAxisId(int id)
		{
			physicalAxisId = id;
		}
		
		public int getPhysicalAxisId()
		{
			return physicalAxisId;
		}
}