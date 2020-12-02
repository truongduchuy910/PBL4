package model.BO;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import model.BO.Message.Type;

import model.bean.View;

public class Server implements model.bean.Server, model.bean.Lamport {
	private int limit;
	private int start;
	private int port;
	private int timestamp;

	public Stack<Boolean> getIsRep() {
		return isRep;
	}

	public void setIsRep(Stack<Boolean> isRep) {
		this.isRep = isRep;
	}

	model.bean.Server server;
	Stack<model.bean.Server> orthers = new Stack<model.bean.Server>();

	Queue<model.bean.Server> CS = new LinkedList<model.bean.Server>();
	Boolean isInCS = false;

	public Boolean getIsInCS() {
		return isInCS;
	}

	public void setIsInCS(Boolean isInCS) {
		this.isInCS = isInCS;
	}

	public Queue<model.bean.Server> getCS() {
		return CS;
	}

	public void setCS(Queue<model.bean.Server> CS) {
		this.CS = CS;
	}

	Stack<Message> send = new Stack<Message>();
	Stack<Message> receive = new Stack<Message>();
	Stack<Boolean> isRep = new Stack<Boolean>();

	View view;

	public View getView() {

		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	// GENERAL GETTER
	public Stack<model.bean.Server> getOrthers() {
		return orthers;
	}

	public model.bean.Server getServer() {
		return server;
	}

	public void setServer(model.bean.Server server) {
		this.server = server;
	}

	public void setOrthers(Stack<model.bean.Server> orthers) throws RemoteException {
		this.orthers = orthers;
	}

	public int getLimit() throws RemoteException {
		return limit;
	}

	public void setLimit(int limit) throws RemoteException {
		this.limit = limit;
	}

	public int getStart() throws RemoteException {
		return start;
	}

	public void setStart(int start) throws RemoteException {
		this.start = start;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) throws RemoteException {
		this.port = port;
	}

	public Stack<Message> getSend() {
		return send;
	}

	public void setSend(Stack<Message> send) {
		this.send = send;
	}

	public Stack<Message> getReceive() {
		return receive;
	}

	public void setReceive(Stack<Message> receive) {
		this.receive = receive;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public int getIndex() throws RemoteException {
		return port - start;
	}

	public void listen() throws Exception {

		try {
			LocateRegistry.createRegistry(port);
			Registry registry = LocateRegistry.getRegistry(port);
			server = (model.bean.Server) UnicastRemoteObject.exportObject(this, 0);
			registry.rebind(getUri(), server);

		} catch (Exception e) {
			if (port < start + limit) {
				port++;
				listen();
			} else
				throw e;
		}

	}

	public String getHost() throws RemoteException {
		String host = "localhost";
		try {
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			for (; n.hasMoreElements();) {
				NetworkInterface e = n.nextElement();

				if (e.getName().compareTo("wlp2s0") == 0) {

					Enumeration<InetAddress> a = e.getInetAddresses();
					for (; a.hasMoreElements();) {
						InetAddress addr = a.nextElement();
						host = addr.getHostAddress();
					}
				}
			}

		} catch (Exception e) {
			host = "localhost";
		}
		return host;
	}

	public String getClassName() throws RemoteException {
		return this.getClass().getSimpleName();
	}

	String getUri() throws RemoteException {

		return "rmi://" + getHost() + ":" + port + "/" + getClassName();

	}

	public void connect(String host, int port, String className) throws Exception {
		String uri = "rmi://" + host + ":" + port + "/" + className;
		Registry registry = LocateRegistry.getRegistry(host, port);
		try {
			registry.lookup(uri);
			model.bean.Server stub = (model.bean.Server) registry.lookup(uri);
			if (!getOrthers().contains(stub)
//					&& port != getPort()
			) {
				getOrthers().push(stub);
			}
			/**
			 * declare it in IsRepArray
			 */
			getIsRep().add(false);
		} catch (Exception e) {

		}

	}

	model.bean.Server getServerByPort(int port) throws RemoteException {
		if (port == getPort())
			return server;
		for (model.bean.Server server : getOrthers()) {
			if (server.getPort() == port) {
				return server;
			}
		}
		throw new RemoteException("Not found");
	}

	public void request(String command) throws RemoteException {

	}

	public void autoConnect() throws Exception {

		for (int p = start; p <= start + limit; p++) {
			connect(getHost(), p, getClassName());
		}

		for (model.bean.Server server : getOrthers()) {
			if (getPort() != server.getPort()) {
				server.connect(getHost(), getPort(), getClassName());
			}
		}
	}

	public void reloadAll() {

		try {
			for (model.bean.Server server : getOrthers()) {

				int port = server.getPort();

				if (port != getPort())
					server.render();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void receipt(String command) throws RemoteException {
		Message message = new Message(this, command);
		receipt(message);
	}

	@Override
	public void render() throws RemoteException {
		view.render();

	}

	public void incTimestamp() {
		timestamp++;
	}

	public void receipt(Message message) throws RemoteException {
		try {
			Receive receive = new Receive(this, message);
			receive.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void broadcast(Type type) throws Exception {
		try {
			Broadcast broadcast = new Broadcast(this, type);
			broadcast.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String toView() throws RemoteException {
		return "[s" + getIndex() + " at c" + getTimestamp() + "]";
	}
}
