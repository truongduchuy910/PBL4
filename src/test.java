import java.util.LinkedList;
import java.util.Queue;

import view.Console;

public class test {
	public static void main(String agrs[]) {
		Queue<Integer> fifo = new LinkedList<Integer>();
		fifo.add(1);
		fifo.add(2);
		fifo.add(3);
		for (int a : fifo) {
			Console.log(a + "");
		}
		Console.log(fifo.peek() + "");

	}
}
