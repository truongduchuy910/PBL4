import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import server.Server;

/**
 * @author Robert
 *
 */
public class Main implements Server {
	Server stub;
	Server schelekon;
	String name = "Main";
	int port = 6789;

	public Main() throws RemoteException {
		super();
		System.out.println("Constructed Main.");
	}

	public void listen() throws NotBoundException {
		final String rmi = "rmi://localhost:" + this.port + "/" + this.name;
		System.out.println("Listeng at " + rmi);
		try {
			Registry registry = LocateRegistry.getRegistry(this.port);
			this.stub = (Server) UnicastRemoteObject.exportObject(this, 0);
			LocateRegistry.createRegistry(this.port);
			registry.rebind(rmi, this.stub);
			this.schelekon = (Server) registry.lookup(rmi);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void test() {
		System.out.println("test");
	}

	public static void main(String args[]) throws RemoteException, NotBoundException {
		System.out.println("Hello!");
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
			System.out.println("New Security Manager.");
		}
		;
		Main main = new Main();
		main.listen();
		if (main.schelekon != null) {
			main.schelekon.test();
		}
		if (main.stub != null) {
			main.stub.test();
		}
	}
}
