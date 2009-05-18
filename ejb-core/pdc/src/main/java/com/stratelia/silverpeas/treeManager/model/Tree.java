/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.treeManager.model;

import java.util.List;
import java.util.ArrayList;

/*
 * CVS Informations
 * 
 * $Id: Tree.java,v 1.1.1.1 2002/08/06 14:47:53 nchaix Exp $
 * 
 * $Log: Tree.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.2  2002/02/12 11:40:29  neysseri
 * no message
 *
 * Revision 1.1  2002/02/08 14:28:22  neysseri
 * no message
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author
 */
public class Tree implements java.io.Serializable
{

    private List tree = null;

    /**
     * Constructor declaration
     *
     *
     * @param treeNodes
     *
     * @see
     */
    public Tree(List treeNodes)
    {
        tree = treeNodes;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public List getTree()
    {
        return (this.tree);
    }

    /**
     * Method declaration
     *
     *
     * @param treeNodes
     *
     * @see
     */
    public void setTree(List treeNodes)
    {
        this.tree = treeNodes;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public int getDepth()
    {
        if (getTree() != null && getTree().size() > 0)
        {
            ArrayList tree = (ArrayList) getTree();
            int       maxLevel = 0;
            int       rootLevel = 0;
			int		  level = 0;
            TreeNode  node = null;

            for (int i = 0; i < tree.size(); i++)
            {
                node = (TreeNode) tree.get(0);
                level = node.getLevelNumber();
                if (i == 0)
                {
                    rootLevel = level;
                }
                if (level > maxLevel)
                {
                    maxLevel = level;
                }
            }
            return maxLevel - rootLevel;
        }
        return 0;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public TreeNode getRoot()
    {
        if (getTree() != null && getTree().size() > 0)
        {
            return (TreeNode) getTree().get(0);
        }
        return null;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String toString()
    {
        return "(tree = " + getTree() + ")";
    }

}
