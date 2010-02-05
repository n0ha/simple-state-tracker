package net.n0ha.sst;

public class ExecutionFailedException extends Exception {

    private static final long serialVersionUID = 2663240175374946810L;

    public ExecutionFailedException(String message) {
    	super(message);
    }
}
