import java.util.LinkedList;
import java.util.Queue;

public class test {
	public static void main(String agrs[]) {
		Queue<Integer> fifo = new LinkedList<Integer>();
		fifo.add(1);
		fifo.add(2);
		fifo.add(3);
		System.out.println(fifo.peek());
		System.out.println(fifo.peek());
		fifo.remove();
		System.out.println(fifo.peek());
	}
}
