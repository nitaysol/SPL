package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.impl.NitzKfitz.BidiMessaginProtocolImp;
import bgu.spl.net.impl.NitzKfitz.DataBaseSimulator;
import bgu.spl.net.impl.NitzKfitz.bguMessageEncoderDecoder;
import bgu.spl.net.srv.Server;

public class TPCMain {

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Please provide 1 arguments");
        }
        DataBaseSimulator db = new DataBaseSimulator(); //one shared object

        Server.threadPerClient(
                Integer.parseInt(args[0]), //port
                () ->  new BidiMessaginProtocolImp(db), //protocol factory
                bguMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();


    }
}
