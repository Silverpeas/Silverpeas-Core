/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.silverpeas.util.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.exception.UtilTrappedException;


public class CSVWriter extends SilverpeasSettings
{
    protected int       m_nbCols = 0;
    protected String[]  m_colNames;
    protected String[]  m_colTypes;
    protected String[]  m_colDefaultValues;
    protected String    m_separator;
    protected ResourceLocator m_utilMessages;
    
    //properties spécifiques éventuellement en plus
    protected int       m_specificNbCols = 0;
    protected String[]  m_specificColNames;    
    protected String[]  m_specificColTypes;
    protected String[]  m_specificParameterNames;
    
    
    /**
     * Constructeur
     */
    public CSVWriter(String language) {
        m_utilMessages = new ResourceLocator("com.silverpeas.util.multilang.util", language);
    }
    
    public void initCSVFormat(String propertiesFile, String rootPropertyName, String separator)
    {
        ResourceLocator rs = new ResourceLocator(propertiesFile, "");

        m_colNames = readStringArray(rs, rootPropertyName, ".Name", -1);
        m_nbCols = m_colNames.length;
        m_colTypes = readStringArray(rs, rootPropertyName, ".Type", m_nbCols);
        m_colDefaultValues = readStringArray(rs, rootPropertyName, ".Default", m_nbCols);
        m_separator = separator;
    }
    
    public void initCSVFormat(String propertiesFile, String rootPropertyName, String separator, 
    						String specificPropertiesFile, String specificRootPropertyName)
    {
        ResourceLocator rs = new ResourceLocator(propertiesFile, "");

        m_colNames = readStringArray(rs, rootPropertyName, ".Name", -1);
        m_nbCols = m_colNames.length;
        m_colTypes = readStringArray(rs, rootPropertyName, ".Type", m_nbCols);
        m_colDefaultValues = readStringArray(rs, rootPropertyName, ".Default", m_nbCols);
        m_separator = separator;
        
        ResourceLocator specificRs = new ResourceLocator(specificPropertiesFile, "");
        m_specificColNames = readStringArray(specificRs, specificRootPropertyName, ".Name", -1);
        m_specificNbCols = m_specificColNames.length;
        
        m_specificColTypes = readStringArray(specificRs, specificRootPropertyName, ".Type", m_specificNbCols);
        for(int i=0; i<m_specificNbCols; i++) {
        	if(! Variant.TYPE_STRING.equals(m_specificColTypes[i]) &&
        	   ! Variant.TYPE_INT.equals(m_specificColTypes[i]) &&
        	   ! Variant.TYPE_BOOLEAN.equals(m_specificColTypes[i]) &&
        	   ! Variant.TYPE_FLOAT.equals(m_specificColTypes[i]) &&
        	   ! Variant.TYPE_DATEFR.equals(m_specificColTypes[i]) &&
        	   ! Variant.TYPE_DATEUS.equals(m_specificColTypes[i]) &&
        	   ! Variant.TYPE_STRING_ARRAY.equals(m_specificColTypes[i]) &&
        	   ! Variant.TYPE_LONG.equals(m_specificColTypes[i])) {
        		
        		m_specificColTypes[i] = Variant.TYPE_STRING;	
        	}
        }
        m_specificParameterNames = m_specificColNames;
    }
    
    
    
    /**
     * @return Returns the m_specificNbCols.
     */
    public int getM_specificNbCols() {
        return m_specificNbCols;
    }
    
    /**
     * @param cols The m_specificNbCols to set.
     */
    public void setM_specificNbCols(int cols) {
    	m_specificNbCols = cols;
    }
    
}
