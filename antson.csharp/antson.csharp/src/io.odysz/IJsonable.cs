using System.IO;

namespace io.odysz.anson
{
    public interface IJsonable
    {
        IJsonable ToBlock(Stream stream, JsonOpt opts = null);
    }
}