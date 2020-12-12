package model.BO;

import java.rmi.RemoteException;
import java.util.ArrayList;

import model.BO.Message.Direction;
import model.BO.Message.Type;
import view.Console;

public class Lamport implements model.bean.Lamport {
	ArrayList<Message> requests = new ArrayList<Message>();

	@Override
	public void request(model.BO.Server sender) throws Exception {
		if (!sender.getIsInCS()) {

			boardcast(sender, Type.REQ);
		} else
			Console.log("You was in CS.");
	}

	@Override
	public void release(model.BO.Server sender) throws Exception {
		if (sender.getIsInCS()) {
			boardcast(sender, Type.REL);
		} else
			Console.log("You wasn't in CS.");
	}

	private void resetCSCheck(model.BO.Server sender) throws RemoteException {
		/**
		 * Fill IsRep with false
		 */

		sender.setIsInCS(false);
//		for (model.bean.Server orther : sender.getTo()) {
//			sender.getIsRep().set(orther.getIndex(), false);
//		}
		sender.getTo().clear();
		sender.getTo().empty();

	}

	@Override
	public void receive(model.BO.Server receiver, String command) throws RemoteException {
		Message message = null;
		try {
			message = new Message(receiver, command);
		} catch (Exception e) {
			Console.log(command);
			e.printStackTrace();
		}
		message.setDirection(Direction.RECEIVE);
		/**
		 * find max and inscrease
		 */
		receiver.setTimestamp(Math.max(receiver.getTimestamp(), message.getSendAt()) + 1);
		message.setReceiveAt(receiver.getTimestamp());

		/**
		 * add message to ArrayList
		 */
		receiver.addMessage(message);

		/**
		 * algorithm
		 */
		switch (message.getType()) {
		case REQ:
			/**
			 * REP
			 */
			model.bean.Server top = receiver.peek();
			if (top != null && top.getIndex() == message.getFrom().getIndex()) {
				receiver.incTimestamp();
				Message rep = new Message(Direction.SEND, Type.REP, receiver, top, receiver.getTimestamp(), -1);
				new Deliver(rep).start();

				/**
				 * Add message to stack
				 */
				receiver.addMessage(rep);
			}

			break;
		case REL:

			/**
			 * Remove server send message from queue
			 */
			receiver.pop(message.getFrom());
			/**
			 * REP
			 */
			model.bean.Server next = receiver.peek();
			if (next != null) {
				receiver.incTimestamp();
				Message rep = new Message(Direction.SEND, Type.REP, receiver, next, receiver.getTimestamp(), -1);
				new Deliver(rep).start();

				/**
				 * Add message to stack
				 */
				receiver.addMessage(rep);
			}
			break;
		case REP:

			receiver.getIsRep().set(message.getFrom().getIndex(), true);
			receiver.checkCS();
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

	public void boardcast(model.BO.Server sender, Type type) throws RemoteException {
		resetCSCheck(sender);

		;
		sender.incTimestamp();
		/**
		 * get all server
		 */
		ArrayList<model.bean.Server> servers = sender.getOrthers();

		for (model.bean.Server receiper : servers) {
			if (type == Type.REQ)
				sender.getTo().add(receiper);

			Message message = new Message(Direction.SEND, type, sender, receiper, sender.getTimestamp(), -1);
			message.setSendAt(sender.getTimestamp());
			new Deliver(message).start();
			/**
			 * add messsage to ArrayList
			 */
			sender.addMessage(message);
		}

	}

}
