using System.IO;

namespace io.odysz.anson
{
	/**
	 * <h4>This interface is planned to be deprecated in the future.</h4>
	 * <p>Callback to create IJsonable instance from json string.</p>
     * <p>Any class or enum implementing IJsonable must register it's
     * factory to JSONAsonListener through {@link JSONAnsonListener#registFactory(Class, JsonableFactory)}.</p>
     * <p>The AnsT4Enum.Port is a tested example for registering a java enum constructor:<pre>
     public enum Port implements IPort { 
        heartbeat("ping.serv"), session("login.serv11"), dataset("ds.serv11");

        // using static initialized is not always correct
        // - may be there are different implementation of IPort
        static {
            JSONAnsonListener.registFactory(IPort.class, (s) -> {
                return Port.valueOf(s);
            });
        }
     }</pre>
     * </p>
     * @author odys-z@github.com
     */
    public interface JsonableFactory
    {
        IJsonable fromJson(string json);
    }

	/// <summary>
	/// Interface of types can be serializaed to json. 
	/// </summary>
	public interface IJsonable
    {
        IJsonable ToBlock(Stream stream, JsonOpt opts = null);
    }
}