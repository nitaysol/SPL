
package bgu.spl.net.impl.BGSServer;
import bgu.spl.net.impl.NitzKfitz.BidiMessaginProtocolImp;
import bgu.spl.net.impl.NitzKfitz.DataBaseSimulator;
import bgu.spl.net.impl.NitzKfitz.bguMessageEncoderDecoder;
import bgu.spl.net.srv.Server;


public class ReactorMain {

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Please provide 2 arguments");
        }
        DataBaseSimulator db = new DataBaseSimulator();

    Server.reactor(
            Integer.parseInt(args[1]),
            Integer.parseInt(args[0]), //port
            () ->  new BidiMessaginProtocolImp(db), //protocol factory
            bguMessageEncoderDecoder::new //message encoder decoder factory
    ).serve();

    }
}
