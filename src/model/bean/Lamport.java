package model.bean;

public interface Lamport {
	public void request(model.BO.Server sender) throws Exception;

	public void release(model.BO.Server sender) throws Exception;

	public void receive(model.BO.Server receiver, String command) throws Exception;
}
