package model.BO;

import java.rmi.RemoteException;

import view.Color;

public class Message implements model.bean.Message {
	public static enum Type {
		REQ, REL, REP
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

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public Type type;

	private int start;
	private model.bean.Server from;

	private int end;
	private model.bean.Server to;

	private int at;
	private int sleep;
	private String dimension;

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public int getSleep() {
		return sleep;
	}

	public void setSleep(int sleep) {
		this.sleep = sleep;
	}

	public int getAtInt() {
		return at;
	}

	public String getAt() {
		if (at < 10)
			return " " + at;
		else
			return Integer.toString(at);
	}

	public void setAt(int at) {
		this.at = at;
	}

	public String getStartString() {
		if (start != -1)
			if (start < 10)
				return "_" + Integer.toString(start);
			else
				return "" + Integer.toString(start);
		else
			return " ?";
	}

	public void setStart(int start) {
		this.start = start;
	}

	public String getEndString() {
		if (end != -1)
			if (end < 10)
				return "_" + Integer.toString(end);
			else
				return "" + Integer.toString(end);
		else
			return " ?";
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getTimestamp() {
		return start;
	}

	public void setTimestamp(int start) {
		this.start = start;
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

	public Message(Type type, model.bean.Server from, model.bean.Server to, int start, int end) {
		this.type = type;
		this.from = from;
		this.to = to;
		this.start = start;
		this.end = end;
	}

	public Message(Server server, String type, int from, int to) throws RemoteException {
		this.type = Type.valueOf(type);
		this.from = server.getServerByPort(from);
		this.to = server.getServerByPort(to);
		this.start = server.getTimestamp();
	}

	public Message(Server server, String command) throws RemoteException, NumberFormatException {
		String[] args = command.split(" ");
		type = Message.Type.valueOf(args[1]);
		start = Integer.parseInt(args[3]);
		sleep = Integer.parseInt(args[4]);
		try {
			from = server.getServerByPort(Integer.parseInt(args[0]));
			to = server.getServerByPort(Integer.parseInt(args[2]));
		} catch (RemoteException e) {
			throw e;
		} catch (NumberFormatException e) {
			throw e;
		}

	}

	public String toString() {
		String content = "";
		try {
			content = from.getPort() + " " + type.toString() + " " + to.getPort() + " " + start + " " + sleep;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	public String getDisplay() {

		try {
			return getTypeString() + " from [s" + from.getIndex() + "] to [s" + to.getIndex() + "] " + this.getSleep();
		} catch (Exception e) {
			return e.toString();
		}

	}

	public void delivery() throws RemoteException {
		String command = this.toString();
		to.receipt(command);
	}

}
