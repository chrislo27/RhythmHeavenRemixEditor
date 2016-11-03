package ionium.registry.handler;


public interface IErrorLogWriter {
	
	public void appendToStart(StringBuilder builder);
	
	public void appendToEnd(StringBuilder builder);
	
}
