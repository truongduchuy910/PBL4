package model.bean;

import java.rmi.RemoteException;

import model.BO.Message.Type;

public interface Lamport extends Server {
	public void request(String command) throws RemoteException;

	public void broadcast(Type type) throws Exception;
}
