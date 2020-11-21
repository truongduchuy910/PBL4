package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {

	void test() throws RemoteException;
}
