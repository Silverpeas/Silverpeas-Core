package com.stratelia.webactiv.beans.admin.instance.control;

import java.rmi.RemoteException;

public interface ComponentPasteInterface {

	void paste(PasteDetail pasteDetail) throws RemoteException;
	
}
