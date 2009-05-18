/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.statistic.model;

import java.io.Serializable;
import java.util.Date;

import com.silverpeas.util.ForeignPK;

 
/**
 * Class declaration
 *
 *
 * @author
 */
public class HistoryObjectDetail implements Serializable
{

    private Date          date;
    private String        userId;
    private ForeignPK     foreignPK;

    /**
     * Constructor declaration
     *
     *
     * @param date
     * @param userId
     * @param foreignPK
     *
     * @see
     */
    public HistoryObjectDetail(Date date, String userId, ForeignPK foreignPK)
    {
        this.date = date;
        this.userId = userId;
        this.foreignPK = foreignPK;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getUserId()
    {
        return userId;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public ForeignPK getForeignPK()
    {
        return foreignPK;
    }

}
