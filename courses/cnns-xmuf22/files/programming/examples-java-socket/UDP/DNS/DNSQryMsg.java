public class DNSQryMsg extends DNSMsg {

    private state final HashMap<String, int> types;

    public static void init() {
        // http://www.iana.org/assignments/dns-parameters/dns-parameters.xhtml
        types = HashMap<String, int>();
        types.put("A",   1);
        types.put("MX",  15);
        types.put("TXT", 16);
    }

    public DNSQryMsg(String type, String domainName) {
        short id = Random.();
        

    } // end of constructor

}