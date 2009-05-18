package com.stratelia.webactiv.organization;

import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;

public class OrganizationSchema extends Schema
{
    public OrganizationSchema(int cl) throws UtilException
    {
        super(cl);
        init();
    }
    protected String getJNDIName() 
    {
        return JNDINames.ADMIN_DATASOURCE;
    }

    public void init()
    {
        domain = new DomainTable(this);
		keyStore = new KeyStoreTable(this);
        user = new UserTable(this);
        group = new GroupTable(this);
        space = new SpaceTable(this);
        spaceI18N = new SpaceI18NTable(this);
        instance = new ComponentInstanceTable(this);
        instanceI18N = new ComponentInstanceI18NTable(this);
        instanceData = new InstanceDataTable(this);
        userRole = new UserRoleTable(this);
        spaceUserRole = new SpaceUserRoleTable(this);
        userSet = new UserSetTable(this);
        accessLevel = new AccessLevelTable(this);
        groupUserRole = new GroupUserRoleTable(this);
    }

    public DomainTable domain = null;
	public KeyStoreTable keyStore = null;
    public UserTable user = null;
    public GroupTable group = null;
    public SpaceTable space = null;
    public SpaceI18NTable spaceI18N = null;
    public ComponentInstanceTable instance = null;
    public ComponentInstanceI18NTable instanceI18N = null;
    public InstanceDataTable instanceData = null;
    public UserRoleTable userRole = null;
    public SpaceUserRoleTable spaceUserRole = null;
    public UserSetTable userSet = null;
    public AccessLevelTable accessLevel = null;
    public GroupUserRoleTable groupUserRole = null;
}
