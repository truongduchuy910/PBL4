package model.BO;

import java.rmi.RemoteException;
import java.util.Stack;

import model.BO.Message.Direction;
import model.BO.Message.Type;
import model.bean.Server;
import view.Console;

public class Lamport implements model.bean.Lamport {
	@Override
	public void request(model.BO.Server sender) throws Exception {
		if (!sender.getIsInCS()) {
			resetCSCheck(sender);
			boardcast(sender, Type.REQ);
		} else
			Console.log("You was in CS.");
	}

	@Override
	public void release(model.BO.Server sender) throws Exception {
		if (sender.getIsInCS()) {
			resetCSCheck(sender);
			boardcast(sender, Type.REL);
		} else
			Console.log("You wasn't in CS.");
	}

	private void resetCSCheck(model.BO.Server sender) throws RemoteException {
		/**
		 * Fill IsRep with false
		 */
		sender.setIsInCS(false);
		for (model.bean.Server orther : sender.getOrthers()) {
			sender.getIsRep().set(orther.getIndex(), false);
		}

	}

	@Override
	public void receive(model.BO.Server receiver, String command) throws RemoteException {
		Message message = new Message(receiver, command);
		message.setDirection(Direction.RECEIVE);
		/**
		 * find max and inscrease
		 */
		receiver.setTimestamp(Math.max(receiver.getTimestamp(), message.getSendAt()) + 1);
		message.setReceiveAt(receiver.getTimestamp());

		/**
		 * add message to Stack
		 */
		receiver.getReceive().add(message);

		/**
		 * algorithm
		 */
		switch (message.getType()) {
		case REQ:

			/**
			 * Add server send message to queue
			 */
			receiver.getCS().add(message.getFrom());
			/**
			 * REP
			 */
			model.bean.Server top = receiver.getCS().peek();
			if (top != null && top.getIndex() == message.getFrom().getIndex()) {
				receiver.incTimestamp();
				Message rep = new Message(Direction.SEND, Type.REP, receiver, top, receiver.getTimestamp(), -1);
				new Deliver(rep).start();

				/**
				 * Add message to stack
				 */
				receiver.getSend().add(rep);
			}

			break;
		case REL:

			/**
			 * Remove server send message from queue
			 */
			receiver.getCS().remove();
			/**
			 * REP
			 */
			model.bean.Server next = receiver.getCS().peek();
			if (next != null) {
				receiver.incTimestamp();
				Message rep = new Message(Direction.SEND, Type.REP, receiver, next, receiver.getTimestamp(), -1);
				new Deliver(rep).start();

				/**
				 * Add message to stack
				 */
				receiver.getSend().add(rep);
			}
			break;
		case REP:

			receiver.getIsRep().set(message.getFrom().getIndex(), true);
			checkCS(receiver);
			break;
		default:
			break;
		}

		try {
			receiver.render();
		} catch (RemoteException e) {
			System.err.println("Render error");
		}
	}

	public void boardcast(model.BO.Server sender, Type type) {
		sender.incTimestamp();
		/**
		 * get all server
		 */
		Stack<model.bean.Server> servers = sender.getOrthers();

		for (model.bean.Server receiper : servers) {
			Message message = new Message(Direction.SEND, type, sender, receiper, sender.getTimestamp(), -1);
			message.setSendAt(sender.getTimestamp());
			new Deliver(message).start();
			/**
			 * add messsage to Stack
			 */
			sender.getSend().add(message);

		}

	}

	private void checkCS(model.BO.Server server) throws RemoteException {
		server.setIsInCS(true);
		for (model.bean.Server orther : server.getOrthers()) {
			if (server.getIsRep().get(orther.getIndex()) == false) {
				server.setIsInCS(false);
			}
		}
	}
}
//
//class Receive extends Thread {
//	private model.BO.Server server;
//	private Message message;
//
//	public Receive(model.BO.Server server, String command) throws NumberFormatException, RemoteException {
//		this.server = server;
//		this.message = new Message(server, command);
//	}
//
//	public void run() {
//
//		
//
//	}
//
//	private void checkCS() throws RemoteException {
//		message.getFrom().setIsInCS(true);
//		for (model.bean.Server orther : message.getFrom().getOrthers()) {
//			if (message.getFrom().getIsRep().get(orther.getIndex()) == false) {
//				message.getFrom().setIsInCS(false);
//			}
//		}
//	}
//}
//	public void receive(String command) throws RemoteException {
//		Message message = new Message(this, command);
//		try {
//			Receive receive = new Receive(this, message);
//			receive.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void broadcast(Type type) throws Exception {
//		try {
//			Broadcast broadcast = new Broadcast(this, type);
//			broadcast.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	try {
//		model.bean.Server top = sender.getCS().peek();
//		if (top != null && top.getIndex() == sender.getFrom().getIndex()) {
//			incTimestamp();
//			Message rep = new Message(Type.REP, getServer(), message.getFrom(), getTimestamp(), -1);
//			rep.setDimension("  ->");
//			rep.delivery();
//			/**
//			 * Add message to stack
//			 */
//			getSend().add(rep);
//		}
//	} catch (RemoteException e) {
//		e.printStackTrace();
//	}

//		if (getIsInCS()) {
//			/**
//			 * Fill IsRel with false for next REQ
//			 */
//			setIsInCS(false);
//
//			for (model.bean.Server orther : getOrthers()) {
//				getIsRep().set(orther.getIndex(), false);
//			}
//			broadcast(Type.REL);
//		} else
//			Console.log("You wasn't in CS.");
