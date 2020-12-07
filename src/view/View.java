package view;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Stack;

import model.BO.Message;
import model.BO.Message.Type;
import model.bean.Server;

public class View {
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
			System.out.print(Color.TEXT_CYAN + "➜ " + Color.TEXT_RESET);
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
				server.send("req");
				break;
			case "rel":
				server.send("rel");
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
			Console.log(Color.TEXT_RED, "Cannot initialize");
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void render() {

		Console.log();
		int master = -1;
		try {
			master = server.getIndex();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		String CS = Color.BG_PURPLE + "        " + Color.TEXT_RESET;
		if (server.getIsInCS()) {
			CS = Color.BG_YELLOW + " IN CS  " + Color.TEXT_RESET;
		}
		Console.log(CS + " SERVER " + master + " at " + server.getTimestamp());
		try {
			Stack<Message> transaction = new Stack<Message>();
			transaction.addAll(server.getSend());
			transaction.addAll(server.getReceive());
			Comparator<Message> c = new Comparator<Message>() {
				@Override
				public int compare(Message a, Message b) {
					return a.getAt() > b.getAt() ? 1 : -1;
				}
			};
			transaction.sort(c);

			for (Message message : transaction) {
				Console.log(message.toString());
			}
			Console.log(Color.BG_PURPLE, " LIST   ");

			for (Server cs : server.getCS()) {
				Console.log("SERVER " + cs.getIndex());
			}

		} catch (Exception e) {
			Console.log("...");
		}

		Console.log();
	}

}
