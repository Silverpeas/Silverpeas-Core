/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.silverstatistics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;


/**
 * Class declaration
 *
 *
 * @author SLR
 */

public class StatisticsConfig
{

    private static final String STATSKEYNAME = "StatsKeysName";
    private static final String STATSKEYTYPE = "StatsKeysType";
    private static final String STATSFAMILYTYPE = "StatsFamily";
    private static final String STATSSEPARATOR = "StatsSeparator";
    private static final String STATSKEYSCUMUL = "StatsKeysCumul";
    private static final String STATSTABLENAME = "StatsTableName";
    private static final String STATSRUN = "StatsRun";
    private static final String STATSASYNCHRON = "StatsAsynchron";
    private static final String STATSPURGE = "StatsPurgeInMonth";
    private static final String STATSMODECUMUL = "StatsModeCumul";

    public static String        MODEADD = TypeStatistics.MODEADD;
    public static String        MODEREPLACE = TypeStatistics.MODEREPLACE;

    private HashMap             allStatisticsConfig;

    private boolean             initGood;

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public StatisticsConfig()
    {
        allStatisticsConfig = new HashMap();
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws SilverStatisticsConfigException
     *
     * @see
     */
    public int init() throws SilverStatisticsConfigException
    {
        java.util.ResourceBundle p = null;
        String                   TokenSeparator;
        String                   configTypeName = "";

        try
        {
            initGood = true;
            p = java.util.ResourceBundle.getBundle("com.stratelia.silverpeas.silverstatistics.SilverStatistics");

            TokenSeparator = p.getString(STATSSEPARATOR);


            StringTokenizer stTypeStats = new StringTokenizer(p.getString(STATSFAMILYTYPE), TokenSeparator);

            while (stTypeStats.hasMoreTokens())
            {
                TypeStatistics currentType = new TypeStatistics();

                currentType.setName(stTypeStats.nextToken());
                configTypeName = currentType.getName();
                currentType.setTableName(p.getString(STATSTABLENAME + currentType.getName()));

                try
                {
                    currentType.setIsRun(Boolean.valueOf(p.getString(STATSRUN + currentType.getName())).booleanValue());
                }
                catch (MissingResourceException e)
                {
                    SilverTrace.info("silverstatistics", "StatisticsConfig.init", "setIsRun", e);
                    //throw new SilverStatisticsConfigException("StatisticsConfig", SilverpeasException.ERROR, "silverstatistics.MSG_KEY_RUN_MISSING", configTypeName, e);
                }

                try
                {
                    currentType.setPurge(Integer.parseInt(p.getString(STATSPURGE + currentType.getName())));
                }
                catch (SilverStatisticsTypeStatisticsException e)
                {
                    SilverTrace.info("silverstatistics", "StatisticsConfig.init", "setPurge", e);
                    //throw new SilverStatisticsConfigException("StatisticsConfig", SilverpeasException.ERROR, "silverstatistics.MSG_PURGE_BAD_VALUE", configTypeName, e);
                }
                catch (MissingResourceException e)
                {
                    SilverTrace.info("silverstatistics", "StatisticsConfig.init", "setPurge", e);
                    //throw new SilverStatisticsConfigException("StatisticsConfig",  SilverpeasException.ERROR, "silverstatistics.MSG_KEY_RUN_MISSING",configTypeName, e);
                }

                try
                {
                    currentType.setIsAsynchron( Boolean.valueOf(p.getString(STATSASYNCHRON+currentType.getName())).booleanValue() );
                }
                catch (MissingResourceException e)
                {
                    SilverTrace.info("silverstatistics", "StatisticsConfig.init", "setIsAsynchron", e);
                    //throw new SilverStatisticsConfigException("StatisticsConfig", SilverpeasException.ERROR, "silverstatistics.MSG_KEY_PURGE_MISSING", configTypeName, e);
                }

                try
                {
                    currentType.setModeCumul(p.getString(STATSMODECUMUL + currentType.getName()));
                }
                catch (SilverStatisticsTypeStatisticsException e)
                {
                    SilverTrace.info("silverstatistics", "StatisticsConfig.init", "setModeCumul", e);
                    //throw new SilverStatisticsConfigException("StatisticsConfig", SilverpeasException.ERROR, "silverstatistics.MSG_PURGE_BAD_VALUE", configTypeName, e);
                }
                catch (MissingResourceException e)
                {
                    SilverTrace.info("silverstatistics", "StatisticsConfig.init", "setModeCumul", e);
                    //throw new SilverStatisticsConfigException("StatisticsConfig", SilverpeasException.ERROR, "silverstatistics.MSG_KEY_PURGE_MISSING", configTypeName, e);
                }

                StringTokenizer stKeyName = new StringTokenizer(p.getString(STATSKEYNAME + currentType.getName()), TokenSeparator);
                StringTokenizer stKeyType = new StringTokenizer(p.getString(STATSKEYTYPE + currentType.getName()), TokenSeparator);
                StringTokenizer stKeyCumul = new StringTokenizer(p.getString(STATSKEYSCUMUL + currentType.getName()), TokenSeparator);


                while (stKeyName.hasMoreTokens())
                {
                    currentType.addKey(stKeyName.nextToken(), stKeyType.nextToken());
                }

                while (stKeyCumul.hasMoreTokens())
                {
                    currentType.addCumulKey(stKeyCumul.nextToken());
                }

                allStatisticsConfig.put(currentType.getName(), currentType);
            }
        }
        catch (SilverStatisticsTypeStatisticsException e)
        {
            initGood = false;
            throw new SilverStatisticsConfigException("StatisticsConfig", SilverpeasException.FATAL, "silverstatistics.MSG_CONFIG_FILE", configTypeName, e);
        }
        catch (MissingResourceException e)
        {
            initGood = false;
            throw new SilverStatisticsConfigException("StatisticsConfig", SilverpeasException.FATAL, "silverstatistics.MSG_CONFIG_FILE_KEY_MISSING", configTypeName, e);
        }

        return 0;
    }

    /**
     * Method declaration
     *
     *
     * @param typeOfStats
     *
     * @return
     *
     * @see
     */
    public Collection getAllKeys(String typeOfStats)
    {
        return ((TypeStatistics) (allStatisticsConfig.get(typeOfStats))).getAllKeys();
    }


    /**
     * Method declaration
     *
     *
     * @param typeOfStats
     * @param keyName
     *
     * @return
     *
     * @see
     */
    public String getKeyType(String typeOfStats, String keyName)
    {
        return ((TypeStatistics) allStatisticsConfig.get(typeOfStats)).getKeyType(keyName);
    }

    /**
     * Method declaration
     *
     *
     * @param typeOfStats
     *
     * @return
     *
     * @see
     */
    public String getTableName(String typeOfStats)
    {
        return ((TypeStatistics) allStatisticsConfig.get(typeOfStats)).getTableName();
    }

    /**
     * Method declaration
     *
     *
     * @param typeOfStats
     *
     * @return
     *
     * @see
     */
    public String getModeCumul(String typeOfStats)
    {
        return ((TypeStatistics) allStatisticsConfig.get(typeOfStats)).getModeCumul();
    }

    /**
     * Method declaration
     *
     *
     * @param typeOfStats
     *
     * @return
     *
     * @see
     */
    public boolean isRun(String typeOfStats)
    {
        return ((TypeStatistics) allStatisticsConfig.get(typeOfStats)).isRun();
    }

    public boolean isAsynchron(String typeOfStats)
    {
        return ((TypeStatistics)allStatisticsConfig.get(typeOfStats)).isAsynchron();
    }

    public int getNumberOfStatsType()
    {
        return allStatisticsConfig.size();
    }

    /**
     * Method declaration
     *
     *
     * @param idFamilyStats
     *
     * @return
     *
     * @see
     */
    public boolean isExist(String idFamilyStats)
    {
        return allStatisticsConfig.containsKey(idFamilyStats);
    }

    /**
     * Method declaration
     *
     *
     * @param idFamilyStats
     *
     * @return
     *
     * @see
     */
    public int getNumberOfKeys(String idFamilyStats)
    {
        return ((TypeStatistics) allStatisticsConfig.get(idFamilyStats)).numberOfKeys();
    }

    /**
     * Method declaration
     *
     *
     * @param typeOfStats
     * @param keyName
     *
     * @return
     *
     * @see
     */
    public boolean isCumulKey(String typeOfStats, String keyName)
    {
        return ((TypeStatistics) allStatisticsConfig.get(typeOfStats)).isCumulKey(keyName);
    }

    /**
     * Method declaration
     *
     *
     * @param StatsType
     * @param keyName
     *
     * @return
     *
     * @see
     */
    public int indexOfKey(String StatsType, String keyName)
    {
        return ((TypeStatistics) allStatisticsConfig.get(StatsType)).indexOfKey(keyName);
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public Collection getAllTypes()
    {
        return allStatisticsConfig.keySet();
    }

    /**
     * Method declaration
     *
     *
     * @param typeOfStats
     *
     * @return
     *
     * @see
     */
    public int getPurge(String typeOfStats)
    {
        return ((TypeStatistics) allStatisticsConfig.get(typeOfStats)).getPurge();
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public boolean isValidConfigFile()
    {
        boolean  valueReturn = true;

        Iterator iteratorTYPE = (allStatisticsConfig.keySet()).iterator();

        while (iteratorTYPE.hasNext())
        {
            String typeCurrent = (String) iteratorTYPE.next();

            if (!initGood)
            {
                valueReturn = false;
            }
            if (!this.isExist(typeCurrent))
            {
                valueReturn = false;
            }
            if (!((TypeStatistics) allStatisticsConfig.get(typeCurrent)).hasGoodCumulKey())
            {
                valueReturn = false;
            }
            if (!((TypeStatistics) allStatisticsConfig.get(typeCurrent)).isDateStatKeyExist())
            {
                valueReturn = false;
            }
        }

        return valueReturn;
    }

    /**
     * Method declaration
     *
     *
     * @param typeOfStats
     * @param dataArray
     *
     * @return
     *
     * @see
     */
    public boolean isGoodDatas(String typeOfStats, ArrayList dataArray)
    {
        boolean valueReturn = false;

        if (initGood)
        {
            if (this.isExist(typeOfStats))
            {
                if (((TypeStatistics) allStatisticsConfig.get(typeOfStats)).hasGoodCumulKey())
                {
                    if (((TypeStatistics) allStatisticsConfig.get(typeOfStats)).isDateStatKeyExist())
                    {
                        if (dataArray.size() == this.getNumberOfKeys(typeOfStats))
                        {
                            valueReturn = true;
                        }
                    }
                }
            }
        }
        return valueReturn;
    }

}


