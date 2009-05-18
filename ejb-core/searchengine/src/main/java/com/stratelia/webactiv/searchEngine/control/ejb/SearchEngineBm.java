package com.stratelia.webactiv.searchEngine.control.ejb;

import javax.ejb.EJBObject;

/**
 * A SearchEngineBm search the web'activ index
 * and give access to the retrieved index entries.
 */
public interface SearchEngineBm extends EJBObject,
                                        SearchEngineBmBusinessSkeleton
{
}
