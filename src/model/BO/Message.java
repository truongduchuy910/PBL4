package model.BO;

import java.rmi.RemoteException;

import view.Color;
import view.Console;

public class Message {
	public static enum Type {
		REQ, REL, REP
	}

	public static enum Direction {
		SEND, RECEIVE
	}

	private Direction direction;
	private Type type;

	private model.bean.Server from;
	private int sendAt;
	private model.bean.Server to;
	private int receiveAt;
	private int duration;

	public int getSendAt() {
		return sendAt;
	}

	public void setSendAt(int sendAt) {
		this.sendAt = sendAt;
	}

	public int getReceiveAt() {
		return receiveAt;
	}

	public void setReceiveAt(int receiveAt) {
		this.receiveAt = receiveAt;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public int getAt() {
		if (direction == Direction.SEND)
			return sendAt;
		else
			return receiveAt;
	}

	public String getAtString() {
		return space(getAt());
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public model.bean.Server getFrom() {
		return from;
	}

	public void setFrom(model.bean.Server from) {
		this.from = from;
	}

	public model.bean.Server getTo() {
		return to;
	}

	public void setTo(model.bean.Server to) {
		this.to = to;
	}

	public Message(Direction direction, Type type, Server sender, model.bean.Server receiver, int sendAt,
			int receiveAt) {
		this.direction = direction;
		this.type = type;
		this.from = sender;
		this.to = receiver;
		this.sendAt = sendAt;
		this.receiveAt = receiveAt;
	}

	public Message(Direction direction, Type type, Server sender, int receiver, int sendAt, int receiveAt)
			throws RemoteException {
		this.direction = direction;
		this.type = type;
		this.from = sender;
		this.to = sender.getMasterByIndex(receiver);
		this.sendAt = sendAt;
		this.receiveAt = receiveAt;
	}

	public Message(Server server, String command) {
		try {
			String[] args = command.split("&");
			this.direction = Message.Direction.valueOf(args[0]);
			this.type = Message.Type.valueOf(args[1]);
			this.from = server.getMasterByIndex(Integer.parseInt(args[2]));
			this.to = server.getMasterByIndex(Integer.parseInt(args[3]));
			this.sendAt = Integer.parseInt(args[4]);
			this.receiveAt = Integer.parseInt(args[5]);
			this.duration = Integer.parseInt(args[6]);
		} catch (Exception e) {
			Console.log(Color.TEXT_RED, command);
		}
	}

	public String toCommand() {
		try {
			return direction.toString() + "&" + type.toString() + "&" + from.getIndex() + "&" + to.getIndex() + "&"
					+ sendAt + "&" + receiveAt + "&" + duration;
		} catch (Exception e) {
			return "";
		}
	}

	public String getDirectionString() {
		switch (direction) {
		case SEND:
			return Color.BG_BLUE + " >>> " + Color.TEXT_RESET;
		case RECEIVE:
			return Color.BG_RED + " <<< " + Color.TEXT_RESET;
		default:
			return "   ";
		}
	}

	public String getTypeString() {
		switch (this.type) {
		case REQ:
			return Color.TEXT_GREEN + "REQUEST" + Color.TEXT_RESET;
		case REP:
			return Color.TEXT_YELLOW + "REPLY  " + Color.TEXT_RESET;
		case REL:
			return Color.TEXT_BLUE + "RELEASE" + Color.TEXT_RESET;
		default:
			return "       ";
		}
	}

	@Override

	public java.lang.String toString() {
		try {
			return getAtString() + " " + getDirectionString() + " " + getTypeString() + " from [s"
					+ getFrom().getIndex() + "] to [s" + getTo().getIndex() + "] " + getDuration() + " ms";
		} catch (RemoteException e) {
			return e.toString();
		}
	}

	String space(int number) {
		if (number < 10)
			return " " + number;
		else
			return Integer.toString(number);
	}
}
