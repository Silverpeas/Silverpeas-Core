package com.stratelia.silverpeas.treeManager.control;

import java.util.List;
import java.sql.Connection;

import com.stratelia.silverpeas.treeManager.model.*;
import com.stratelia.webactiv.searchEngine.model.AxisFilter;

public interface TreeBm {

	public String createRoot(Connection con, TreeNode root) throws TreeManagerException ;

	public void updateNode(Connection con, TreeNode node) throws TreeManagerException ;

	public void updateRoot(Connection con, TreeNode root) throws TreeManagerException ;

	public void deleteTree(Connection con, String treeId) throws TreeManagerException ;

	public void deleteSubTree(Connection con, TreeNodePK rootPK, String treeId) throws TreeManagerException ;

	public List getTree(Connection con, String treeId) throws TreeManagerException ;
	public List getTree(Connection con, String treeId, AxisFilter filter) throws TreeManagerException ;

	public List getSubTree(Connection con, TreeNodePK rootPK, String treeId) throws TreeManagerException ;

	public TreeNode getNode(Connection con, TreeNodePK rootPK, String treeId) throws TreeManagerException ;
	
	public List getNodesByName(Connection con, String nodeName) throws TreeManagerException ;

	public String insertFatherToNode(Connection con, TreeNode nodeToInsert, TreeNodePK refNode, String treeId) throws TreeManagerException ;

	public void moveSubTreeToNewFather(Connection con, TreeNodePK nodeToMovePK, TreeNodePK newFatherPK, String treeId, int orderNumber) throws TreeManagerException ;
	
	public String createSonToNode(Connection con, TreeNode nodeToInsert, TreeNodePK refNode, String treeId) throws TreeManagerException ;

	public List getSonsToNode(Connection con, TreeNodePK treeNodePK, String treeId) throws TreeManagerException ;

	public void deleteNode(Connection con, TreeNodePK treeNodePK, String treeId) throws TreeManagerException;

	public List getFullPath(Connection con, TreeNodePK nodePK, String treeId) throws TreeManagerException ;

	public String getPath(Connection con, TreeNodePK nodePK, String treeId) throws TreeManagerException ;

	public TreeNode getRoot(Connection con, String treeId) throws TreeManagerException ;
	
	public void indexTree(Connection con, int treeId) throws TreeManagerException;
}