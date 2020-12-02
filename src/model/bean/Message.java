package model.bean;

public interface Message {
	public String toString();

	public void delivery() throws Exception;
}
