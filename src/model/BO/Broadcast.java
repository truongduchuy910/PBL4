package model.BO;

import java.util.Stack;

import model.BO.Message.Type;

class Broadcast extends Thread {
	Server sender;
	Type type;

	public Broadcast(Server server, Type type) {

		this.sender = server;
		this.type = type;
	}

	@Override
	public void run() {

		try {
			sender.incTimestamp();
			/**
			 * if server broadcast REQ for orthers server Add itself to its CS;
			 */
//			if (type == Type.REQ) {
//				sender.getCS().add(sender);
//			}
//			if (type == Type.REL) {
//
//			}
			/**
			 * BROADCAST
			 */
			Stack<model.bean.Server> servers = sender.getOrthers();

			for (model.bean.Server receiper : servers) {
				Message message = new Message(type, sender.getServer(), receiper, sender.getTimestamp(), -1);
				message.delivery();
				message.setAt(sender.getTimestamp());
				message.setDimension("-->");
				sender.getSend().add(message);

			}
		} catch (Exception e) {
		}
	}
}