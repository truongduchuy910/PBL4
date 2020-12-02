package view;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Stack;

import model.BO.Message;
import model.BO.Message.Type;
import model.bean.Server;

public class View implements model.bean.View {
	private int port = 3000;
	private int start = 3000;
	private int limit = 3;
	model.BO.Server server = new model.BO.Server();
	Scanner scanner = new Scanner(System.in);
	String command;

	public static void main(String[] args) {
		new View();
	}

	public View() {

		initialize();

		do {
			try {
				render();
			} catch (Exception e) {
			}
			command = scanner.nextLine();
			excute(command);

		} while (!command.equals("exit"));
		System.out.println("Bye!");
		System.exit(0);
	}

	private void excute(String command) {
		String syntax[] = command.split(" ");

		try {
			switch (syntax[0]) {
			case "req":
				if (!server.getIsInCS())
					server.broadcast(Type.REQ);
				else
					System.out.println("You was in CS.");
				break;
			case "rel":
				if (server.getIsInCS()) {
					/**
					 * Fill IsRel with false for next REQ
					 */
					server.setIsInCS(false);

					for (Server orther : server.getOrthers()) {
						server.getIsRep().set(orther.getIndex(), false);
					}
					server.broadcast(Type.REL);
				} else
					System.out.println("You wasn't in CS.");

				break;

			default:
				System.out.println("Command not found");
				break;
			}

		} catch (Exception e) {
		}

	}

	private void initialize() {
		try {
			server.setView(this);
			server.setPort(port);
			server.setStart(start);
			server.setLimit(limit);
			server.listen();
			server.autoConnect();
			server.reloadAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void render() throws RemoteException {
		System.out.println();
		int master = server.getIndex();
		if (server.getIsInCS()) {
			System.out.print("(IN CS) ");
		}
		System.out.println("========== SERVER [ s" + master + " at c" + server.getTimestamp() + " ]");
		try {
			for (Server server : server.getOrthers()) {
				int port = server.getIndex();
				System.out.print(" [ s" + port + " at c" + server.getTimestamp() + " ]");
			}
		} catch (Exception e) {
			System.err.println("Cannot get server");

		}
		System.out.println();

		try {
			Stack<Message> transaction = new Stack<Message>();
			transaction.addAll(server.getSend());
			transaction.addAll(server.getReceive());
			Comparator<Message> c = new Comparator<Message>() {
				@Override
				public int compare(Message a, Message b) {
					return a.getAtInt() > b.getAtInt() ? 1 : -1;
				}
			};
			transaction.sort(c);
			System.out.println("Transaction");
			System.out.println("AT     TYPE        FROM          TO");
			for (Message message : transaction) {
				System.out.print(message.getAt() + ". ");
				System.out.print(message.getDimension() + " ");
				System.out.println(message.getDisplay());
			}
			System.out.println("CS");

			for (Server cs : server.getCS()) {
				System.out.println("s" + cs.getIndex());
			}

		} catch (Exception e) {
			System.out.println("...");
		}

		System.out.println();
	}

}
