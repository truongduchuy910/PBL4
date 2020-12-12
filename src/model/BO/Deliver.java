package model.BO;

import java.rmi.RemoteException;
import java.util.concurrent.ThreadLocalRandom;

public class Deliver extends Thread {
	private Message message;

	public Deliver(Message message) {
		this.message = message;
	}

	@Override
	public void run() {

		/**
		 * delay simulation
		 */
		message.setDuration(ThreadLocalRandom.current().nextInt(500, 2000));
		try {
			Thread.sleep(message.getDuration());
		} catch (InterruptedException e) {
		}

		/**
		 * delivery
		 */

		try {
			message.getFrom().someOneDown();
			message.getTo().receive(message.toCommand());
		} catch (RemoteException e) {
			try {
				message.getFrom().someOneDown();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}

	}
}
