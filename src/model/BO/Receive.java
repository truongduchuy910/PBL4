package model.BO;

import java.rmi.RemoteException;
import java.util.concurrent.ThreadLocalRandom;

import model.BO.Message.Type;

class Receive extends Thread {
	private Server sender;
	private Message message;
	private int sleep;

	public Receive(Server server, Message message) {

		this.message = message;
		this.sender = server;

		/**
		 * find max and inscrease
		 */
		sender.setTimestamp(Math.max(sender.getTimestamp(), message.getStart()) + 1);
		this.message.setDimension("<-  ");
	}

	public void run() {

		/**
		 * fill info
		 */
		message.setEnd(sender.getTimestamp());
		message.setAt(sender.getTimestamp());

		sender.getReceive().add(message);
		/**
		 * delay simulation
		 */
		sleep = ThreadLocalRandom.current().nextInt(500, 2000);
		try {

			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/**
		 * algorithm
		 */
		switch (message.getType()) {
		case REQ:
			/**
			 * Add server send message to queue
			 */
			sender.getCS().add(message.getFrom());
			/**
			 * REP
			 */
			try {
				sender.incTimestamp();
				model.bean.Server server = sender.getCS().peek();
				if (server != null
//						&& server.getPort() != sender.getPort()
						&& server.getPort() == message.getFrom().getPort()) {
					Message rep = new Message(Type.REP, sender, message.getFrom(), sender.getTimestamp(), -1);
					rep.setSleep(sleep);
					rep.setAt(sender.getTimestamp());
					rep.setDimension("  ->");
					rep.delivery();
					sender.getSend().add(rep);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			break;
		case REL:

			try {
				/**
				 * Remove server send message from queue
				 */
				sender.getCS().remove();
				model.bean.Server server = sender.getCS().peek();
				sender.incTimestamp();
				if (server != null) {
					Message rep = new Message(Type.REP, sender, server, sender.getTimestamp(), -1);
					rep.setSleep(sleep);
					rep.setAt(sender.getTimestamp());
					rep.setDimension("  ->");
					rep.delivery();

					sender.getSend().add(rep);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			try {
				message.getFrom().render();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			break;
		case REP:
			try {
				sender.getIsRep().set(message.getFrom().getIndex(), true);
				checkCS();

			} catch (RemoteException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
		try {
			sender.render();
		} catch (RemoteException e) {
			System.err.println("Render error");
		}

	}

	private void checkCS() throws RemoteException {
		sender.setIsInCS(true);
		for (model.bean.Server orther : sender.getOrthers()) {
			if (sender.getIsRep().get(orther.getIndex()) == false) {
				sender.setIsInCS(false);
			}
		}
	}
}