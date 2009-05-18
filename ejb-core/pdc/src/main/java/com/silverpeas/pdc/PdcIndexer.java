package com.silverpeas.pdc;

import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;

public class PdcIndexer {
   
   	PdcBm 	pdc 	= new PdcBmImpl();
   	
    public void index() throws Exception {
		pdc.indexAllAxis();
    }

}