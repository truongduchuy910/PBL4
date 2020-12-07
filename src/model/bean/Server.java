package model.bean;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {
	/**
	 * FOR RMI
	 */
	public void listen() throws RemoteException;

	public void connect(String host, int port, String className) throws RemoteException;

	public void receive(String command) throws RemoteException;

	public void render() throws RemoteException;

	public int getIndex() throws RemoteException;

	public int getTimestamp() throws RemoteException;

}
