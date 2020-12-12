package view;

import java.util.Scanner;

import model.BO.Message;
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
				Console.log("Command not found.");
				break;
			}

		} catch (Exception e) {
			Console.log("Error.");
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
		Console.clear();
		try {
			Console.log();
			for (Message message : server.getMessages()) {
				Console.log(message.toString());
			}
			/**
			 * DISPLAY CS
			 */
			String CS = Color.BG_PURPLE + "        " + Color.TEXT_RESET;
			if (server.getIsInCS()) {
				CS = Color.BG_YELLOW + " IN CS  " + Color.TEXT_RESET;
			}
			int master = -1;
			master = server.getIndex();
			Console.log(CS + " SERVER " + master + " at " + server.getTimestamp());

			for (Server server : server.getOrthers()) {
				Console.print("S" + server.getIndex() + " ");
			}
			Console.log();

			for (Server isRep : server.getTo()) {
				Console.print(" " + server.getIsRep().get(isRep.getIndex()) + " ");
			}
			Console.log();

			for (Server cs : server.getCS()) {
				Console.log("SERVER " + cs.getIndex());
			}
			Console.log();
		} catch (Exception e) {
			if (server.someOneDown()) {
				render();
			}
		}
		System.out.print(Color.TEXT_CYAN + "➜ " + Color.TEXT_RESET);
	}

}
