package model.BO;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Stack;

import model.BO.Lamport;
import model.BO.Message.Direction;
import model.BO.Message.Type;
import view.View;

public class Server implements model.bean.Server {
	/**
	 * IMPORTANT: master is this server. Type of master is model.bean.Server
	 */
	private int limit;
	private int start;
	private int port;
	private int timestamp;

	/**
	 * Stub of this server
	 */
	private model.bean.Server master;

	/**
	 * Stub of all server
	 */
	private ArrayList<model.bean.Server> orthers = new ArrayList<model.bean.Server>();

	private Stack<model.bean.Server> to = new Stack<model.bean.Server>();
	/**
	 * Flag for receive REP
	 */
	private Stack<Boolean> isRep = new Stack<Boolean>();

	private Boolean isInCS = false;
	private ArrayList<Message> requests = new ArrayList<Message>();
	private ArrayList<Message> messages = new ArrayList<Message>();

	private View view;
	private Lamport lamport = new Lamport();

	/*
	 * START GENERAL GETTER
	 */

	public void addMessage(Message e) {

		messages.add(e);
		messages.sort(Message.byAt);
		if (e.getDirection() == Direction.RECEIVE && e.getType() == Type.REQ) {
			requests.add(e);
			requests.sort(Message.bySendAt);
		}
	}

	public Stack<model.bean.Server> getTo() {
		return to;
	}

	public void setTo(Stack<model.bean.Server> to) {
		this.to = to;
	}

	public model.bean.Server peek() {
		try {
			return requests.get(0).getFrom();
		} catch (Exception e) {
			return null;
		}

	}

	public void pop(model.bean.Server receiver) {
		try {
			if (requests.get(0).getFrom().getIndex() == receiver.getIndex()) {
				requests.remove(0);
			}
		} catch (Exception e) {
		}
	}

	public Stack<Boolean> getIsRep() {
		return isRep;
	}

	public ArrayList<Message> getRequests() {
		return requests;
	}

	public void setRequests(ArrayList<Message> requests) {
		this.requests = requests;
	}

	public ArrayList<Message> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
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

	public View getView() {

		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public ArrayList<model.bean.Server> getOrthers() {
		return orthers;
	}

	public model.bean.Server getMaster() {
		return master;
	}

	public void setMaster(model.bean.Server server) {
		this.master = server;
	}

	public void setOrthers(ArrayList<model.bean.Server> orthers) throws RemoteException {
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

	model.bean.Server getMasterByIndex(int index) {

		for (model.bean.Server server : getOrthers()) {
			try {
				if (server.getIndex() == index) {
					return server;
				}
			} catch (RemoteException e) {
			}
		}
		return null;
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
				getOrthers().add(stub);
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
		lamport.receive(this, command);
	}

	@Override
	public void render() throws RemoteException {
		view.render();
	}

	@Override
	public int getIndex() throws RemoteException {
		return port - start;
	}

	public ArrayList<model.bean.Server> getCS() {
		ArrayList<model.bean.Server> servers = new ArrayList<model.bean.Server>();
		for (Message message : requests) {
			servers.add(message.getFrom());
		}
		return servers;
	}

	public boolean someOneDown() {
		boolean have = false;
		for (int i = 0; i < getOrthers().size(); i++) {
			try {
				getOrthers().get(i).getIndex();
			} catch (Exception e) {
				getOrthers().remove(i);
				have = true;
				break;
			}
		}
		for (int i = 0; i < getRequests().size(); i++) {
			try {
				getRequests().get(i).getFrom().getIndex();
			} catch (Exception e) {
				getRequests().remove(i);

				have = true;
				break;
			}
		}
		for (int i = 0; i < getTo().size(); i++) {
			try {
				getTo().get(i).getIndex();
			} catch (Exception e) {
				getTo().remove(i);
				have = true;
				break;
			}
		}

		if (have)
			try {
				checkCS();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		return have;
	}

	public void checkCS() throws RemoteException {
		setIsInCS(true);
		if (getTo().size() <= 0)
			setIsInCS(false);
		for (model.bean.Server i : getTo()) {
			if (getIsRep().get(i.getIndex()) == false) {
				setIsInCS(false);
			}
		}
	}

}
