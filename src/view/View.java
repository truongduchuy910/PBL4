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
		Console.log("Bye!");
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
					Console.log("You was in CS.");
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
					Console.log("You wasn't in CS.");

				break;

			default:
				Console.log("Command not found");
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

		Console.log();
		int master = server.getIndex();
		String CS = Color.BG_PURPLE + "       " + Color.TEXT_RESET;
		if (server.getIsInCS()) {
			CS = Color.BG_YELLOW + "(IN CS)" + Color.TEXT_RESET;
		}
		Console.log(CS + " SERVER " + master + " at " + server.getTimestamp());
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

			for (Message message : transaction) {
				String dimension = message.getDimension();
				if (dimension.equals("  ->")) {
					dimension = Color.BG_BLUE + dimension + Color.TEXT_RESET;
				} else {
					dimension = Color.BG_RED + dimension + Color.TEXT_RESET;
				}
				Console.log(message.getAt() + " " + dimension + " " + message.getDisplay());
			}
			Console.log(Color.BG_PURPLE, "CS     ");

			for (Server cs : server.getCS()) {
				Console.log("SERVER " + cs.getIndex());
			}

		} catch (Exception e) {
			Console.log("...");
		}

		Console.log();
	}

}
