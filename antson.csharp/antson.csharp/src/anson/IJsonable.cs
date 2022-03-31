using System.IO;

namespace io.odysz.anson
{
    /// <summary>
    /// Interface of types can be serializaed to json. 
    /// </summary>
    public interface IJsonable
    {
        IJsonable ToBlock(Stream stream, JsonOpt opts = null);
    }
}