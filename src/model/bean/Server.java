package model.bean;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {

	public void connect(String host, int port, String className) throws Exception;

	public void render() throws RemoteException;

	public void receipt(String command) throws RemoteException;

	public int getLimit() throws RemoteException;

	public int getStart() throws RemoteException;

	public int getPort() throws RemoteException;

	public int getTimestamp() throws RemoteException;

	public String toView() throws RemoteException;

	public int getIndex() throws RemoteException;

	public String getHost() throws RemoteException;

	public String getClassName() throws RemoteException;
}
