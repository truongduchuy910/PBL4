package model.bean;

import java.rmi.RemoteException;

public interface View {
	public void render() throws RemoteException;
}
