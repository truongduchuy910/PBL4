package view;

public class Console {
	public static void clear() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		System.out.println();
	}

	public static void log() {
		System.out.println();
	}

	public static void print(String c1) {
		System.out.print(c1 + Color.TEXT_RESET);
	}

	public static void log(String c1) {
		System.out.println(c1 + Color.TEXT_RESET);
	}

	public static void log(String c1, String c2) {
		System.out.println(c1 + c2 + Color.TEXT_RESET);
	}

	public static void log(String c1, String c2, String c3) {
		System.out.println(c1 + c2 + c3 + Color.TEXT_RESET);
	}

}
