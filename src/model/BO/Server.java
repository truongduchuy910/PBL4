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

import model.BO.Lamport;
import view.View;

public class Server implements model.bean.Server {
	/**
	 * IMPORTANT: master is this server. Type of master is model.bean.Server
	 */
	private int limit;
	private int start;
	private int port;
	private int timestamp;
	private model.bean.Server master;
	private Stack<model.bean.Server> orthers = new Stack<model.bean.Server>();
	private Queue<model.bean.Server> CS = new LinkedList<model.bean.Server>();
	private Boolean isInCS = false;
	private Stack<Message> send = new Stack<Message>();
	private Stack<Message> receive = new Stack<Message>();
	private Stack<Boolean> isRep = new Stack<Boolean>();
	private View view;
	private Lamport lamport = new Lamport();

	/*
	 * START GENERAL GETTER
	 */
	public Stack<Boolean> getIsRep() {
		return isRep;
	}

	public void setIsRep(Stack<Boolean> isRep) {
		this.isRep = isRep;
	}

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

	public View getView() {

		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public Stack<model.bean.Server> getOrthers() {
		return orthers;
	}

	public model.bean.Server getMaster() {
		return master;
	}

	public void setMaster(model.bean.Server server) {
		this.master = server;
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

	/*
	 * END GENERAL GETTER
	 */

	public void listen() throws RemoteException {

		try {
			LocateRegistry.createRegistry(port);
			Registry registry = LocateRegistry.getRegistry(port);
			master = (model.bean.Server) UnicastRemoteObject.exportObject(this, 0);
			registry.rebind(getUri(), master);

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

	model.bean.Server getMasterByIndex(int index) throws RemoteException {
		if (index == getIndex())
			return master;
		for (model.bean.Server server : getOrthers()) {
			if (server.getIndex() == index) {
				return server;
			}
		}
		throw new RemoteException("Not found");
	}

	public void autoConnect() throws Exception {

		for (int p = start; p <= start + limit; p++) {
			connect(getHost(), p, getClassName());
		}

		for (model.bean.Server server : getOrthers()) {
			if (getIndex() != server.getIndex()) {
				server.connect(getHost(), getPort(), getClassName());
			}
		}
	}

	public void reloadAll() {

		try {
			for (model.bean.Server server : getOrthers()) {

				int index = server.getIndex();

				if (index != getIndex())
					server.render();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void incTimestamp() {
		timestamp++;
	}

	@Override
	public void connect(String host, int port, String className) throws RemoteException {
		String uri = "rmi://" + host + ":" + port + "/" + className;
		Registry registry = LocateRegistry.getRegistry(host, port);
		try {
			registry.lookup(uri);
			model.bean.Server stub = (model.bean.Server) registry.lookup(uri);
			if (!getOrthers().contains(stub)

			) {
				getOrthers().push(stub);
			}
			/**
			 * if new server appear declare it in IsRepArray
			 */
			getIsRep().add(false);
		} catch (Exception e) {

		}

	}

	public void send(String command) throws Exception {
		switch (command) {
		case "req":
			lamport.request(this);
			break;
		case "rel":
			lamport.release(this);
			break;
		default:
			break;
		}
	}

	@Override
	public void receive(String command) throws RemoteException {
		try {
			lamport.receive(this, command);
		} catch (RemoteException e) {
			throw new RemoteException("Cannot receive message");
		} catch (NumberFormatException e) {
			throw new RemoteException("Params error: " + command);
		}

	}

	@Override
	public void render() throws RemoteException {
		view.render();
	}

	@Override
	public int getIndex() throws RemoteException {
		return port - start;
	}

}
