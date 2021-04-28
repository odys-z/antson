using System.IO;

namespace io.odysz.anson
{
    public interface IJsonable
    {
        Anson ToBlock(Stream stream, JsonOpt opts = null);
    }
}