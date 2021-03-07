package bgu.spl.net.impl.NitzKfitz;

import bgu.spl.net.api.MessageEncoderDecoder;
import javafx.util.Pair;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.HashMap;
public class bguMessageEncoderDecoder implements MessageEncoderDecoder<String> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k`
    private int len;
    private String opcode;
    private byte[] numOfUsersForFollowRequest;
    private Map<String, Pair<String,Integer>> encdoingmap ;
    private Map<String, String> decodingmap;
    private int numOfZeroBytes;
    public bguMessageEncoderDecoder(){
        numOfZeroBytes=0;
        opcode="";
        len=0;
        numOfUsersForFollowRequest = new byte[2];
        encdoingmap=new HashMap<>();
        decodingmap=new HashMap<>();
        encdoingmap.put("1",new Pair<>("REGISTER", 2));
        encdoingmap.put("2",new Pair<>("LOGIN", 2));
        encdoingmap.put("3",new Pair<>("LOGOUT", 1));
        encdoingmap.put("4",new Pair<>("FOLLOW", 1));
        encdoingmap.put("5",new Pair<>("POST", 1));
        encdoingmap.put("6",new Pair<>("PM", 2));
        encdoingmap.put("7",new Pair<>("USERLIST", 1));
        encdoingmap.put("8",new Pair<>("STAT", 1));
        decodingmap.put("NOTIFICATION", "9");
        decodingmap.put("ACK","10");
        decodingmap.put("ERROR","11");
    }
    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
    private byte[] combineTwoBytesArr(byte[] A, byte[] B){
        int oldSize = A.length;
        A = Arrays.copyOf(A, A.length+B.length);
        for(int i=0; i<B.length;i++){
            A[i+oldSize]=B[i];
        }
        return A;
    }
    @Override
    public byte[] encode(String message) {
        String strGiven = message;
        String[] arrOfMessage = strGiven.split(" ");
        String msgOpCode = decodingmap.get(arrOfMessage[0]);
        strGiven = strGiven.replaceFirst(arrOfMessage[0]+" ", "");
        byte[] toReturn = shortToBytes(Short.parseShort(msgOpCode));
        if(msgOpCode.equals("10") || msgOpCode.equals("11")){
            toReturn = combineTwoBytesArr(toReturn, shortToBytes(Short.parseShort(arrOfMessage[1])));
            strGiven = strGiven.replaceFirst(arrOfMessage[1]+" ", "");
            if(arrOfMessage[1].equals("8") && !msgOpCode.equals("11")){
                for(int i=2;i<5;i++)
                    toReturn = combineTwoBytesArr(toReturn, shortToBytes(Short.parseShort(arrOfMessage[i])));
            }
            else if((arrOfMessage[1].equals("7") || arrOfMessage[1].equals("4")) && !msgOpCode.equals("11")){
                toReturn = combineTwoBytesArr(toReturn, shortToBytes(Short.parseShort(arrOfMessage[2])));
                for(int i=3;i<arrOfMessage.length;i++){
                    toReturn = combineTwoBytesArr(toReturn,arrOfMessage[i].getBytes());
                    toReturn = combineTwoBytesArr(toReturn, new byte[]{0});
                }
            }
            else if(arrOfMessage.length>=3){
                toReturn = combineTwoBytesArr(toReturn,shortToBytes(Short.parseShort(arrOfMessage[2])));
                strGiven = strGiven.replaceFirst(arrOfMessage[2]+" ", "");
                toReturn = combineTwoBytesArr(toReturn,strGiven.getBytes());
                toReturn = combineTwoBytesArr(toReturn, new byte[]{0});
            }

        }
        else{
            if(arrOfMessage[1].equals("PM"))
                toReturn = combineTwoBytesArr(toReturn,new byte[]{0});
            else
                toReturn = combineTwoBytesArr(toReturn,new byte[]{1});
            strGiven = strGiven.replaceFirst(arrOfMessage[1]+" ", "");
            toReturn = combineTwoBytesArr(toReturn, arrOfMessage[2].getBytes());
            toReturn = combineTwoBytesArr(toReturn,new byte[]{0});
            strGiven = strGiven.replaceFirst(arrOfMessage[2]+" ", "");
            toReturn = combineTwoBytesArr((toReturn), strGiven.getBytes());
            toReturn = combineTwoBytesArr(toReturn,new byte[]{0});
        }
        return toReturn;
    }
    private String decodeForFollow(byte nextByte){
        if (nextByte == '\0' && len>5) {
            numOfZeroBytes++;
            if(numOfZeroBytes==encdoingmap.get(opcode).getValue())
                return popString();
        }
        if(len==4) {
            this.numOfUsersForFollowRequest[0]=nextByte;
            len++;
        }
        else if(len==5){
            this.numOfUsersForFollowRequest[1]=nextByte;
            short s1 = bytesToShort(numOfUsersForFollowRequest);
            byte[] array = (s1+"").getBytes();
            encdoingmap.put(opcode,new Pair<>("FOLLOW",(int)s1));
            for(int i=0; i<array.length; i++){
                pushByte(array[0]);
            }
            pushByte((byte) 0);
        }
        else if (len==3)
        {
            pushByte((byte)(nextByte+'0'));
        }
        else
            pushByte(nextByte);
        return null;
    }
    public String decodeNextByte(byte nextByte){
        if(len==1)
        {
            pushByte(nextByte);
            opcode=String.valueOf(bytesToShort(bytes));
            pushByte((byte)0);
        }
        else{
            if(opcode.equals("4")){
                return decodeForFollow(nextByte);
            }
            if (nextByte == '\0' && len>0) {
                numOfZeroBytes++;
                if (encdoingmap.get(opcode) != null && numOfZeroBytes == encdoingmap.get(opcode).getValue()) {
                    return popString();
                }
            }
            pushByte(nextByte);
        }
        if(opcode.equals("3") || opcode.equals("7"))
            return popString();
        return null;
    }
    private void pushByte(byte nextByte) {
        if (len >= bytes.length)
            bytes = Arrays.copyOf(bytes, len * 2);
        bytes[len++] = nextByte;
    }
    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 2, len, StandardCharsets.UTF_8);
        result = encdoingmap.get(opcode).getKey() + result.replaceAll("\u0000"," ");
        while (result.endsWith(" "))
            result = result.substring(0, result.length() - 1);
        //clear Data
        numOfZeroBytes=0;
        opcode="";
        len=0;
        bytes= new byte[1 << 10];
        return result;
    }
}