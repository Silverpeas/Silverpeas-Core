/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.statistic.model;

import java.io.Serializable;
import java.util.Date;

import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/*
 * CVS Informations
 * 
 * $Id: HistoryNodePublicationActorDetail.java,v 1.2 2007/06/14 08:37:55 neysseri Exp $
 * 
 * $Log: HistoryNodePublicationActorDetail.java,v $
 * Revision 1.2  2007/06/14 08:37:55  neysseri
 * no message
 *
 * Revision 1.1.1.1.20.1  2007/06/14 08:22:38  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.4  2001/12/26 12:01:47  nchaix
 * no message
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author
 */
public class HistoryNodePublicationActorDetail implements Serializable
{

    private Date          date;
    private String        userId;
    private NodePK        node;
    private PublicationPK pub;

    /**
     * Constructor declaration
     *
     *
     * @param date
     * @param userId
     * @param node
     * @param pub
     *
     * @see
     */
    public HistoryNodePublicationActorDetail(Date date, String userId, NodePK node, PublicationPK pub)
    {
        this.date = date;
        this.userId = userId;
        this.node = node;
        this.pub = pub;
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
    public PublicationPK getPublicationPK()
    {
        return pub;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public NodePK getNodePK()
    {
        return node;
    }

}
