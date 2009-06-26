package com.stratelia.webactiv.util.repositoryImport;
/*
 * Created by IntelliJ IDEA.
 * User: Mikhail_Nikolaenko
 * Date: Jun 28, 2002
 * Time: 2:10:30 PM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */

public class PropReader
{
    public PropReader()
    {
    }

    static
    {
           System.loadLibrary("propdll");
    }

    public native String[] launch(String txt) throws Exception;
}

