package bgu.spl.mics.application.passiveObjects;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Serializing an object into a file(Not validating that it implements Serializable)
 */
public class PrintToFile {
    public static void printToFile(String fileNameToOutput,Object OutputObject){
        try {
            FileOutputStream fileOut = new FileOutputStream(fileNameToOutput);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(OutputObject);
            out.close();
            fileOut.close();
        } catch (IOException i) { }
    }
}
