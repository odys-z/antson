using io.odysz.anson;

namespace io.odysz.semantic.jsession
{
    public class SessionInf : Anson
    {
        public string ssid { get; private set; }
        public string device { get; private set; }
        public string uid { get; private set; }
        public string roleId { get; private set; }

        public SessionInf() { }

        public SessionInf(string ssid, string uid, string roleId = null) {
            this.ssid = ssid;
            this.uid = uid;
            this.roleId = roleId;
        }
    }
}
