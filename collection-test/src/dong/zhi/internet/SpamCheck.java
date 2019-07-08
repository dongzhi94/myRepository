package dong.zhi.internet;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 判断ip是否为垃圾邮件发送者
 * @author zhi.dong
 */
public class SpamCheck {

    public static final String BLACHOLE="sbl.spamhaus.org";

    public static void main(String[] args){
        String params[] = {"207.34.56.23"};
        for(String param : params){
            if(isSpammer(param)){
                System.out.println(param + " is a known spammer.");
            }else{
                System.out.println(param + " appears legitimate.");
            }
        }

    }

    public static boolean isSpammer(String arg){
        try {
            InetAddress address = InetAddress.getByName(arg);
            byte[] quad = address.getAddress();
            String query = BLACHOLE;
            for (byte octet : quad){
                int unsignedByte = octet < 0 ? octet + 256 : octet;
                //逆置这个地址的字节
                query = unsignedByte + "." + query;
            }
            //23.56.34.207.sbl.spamhaus.org
            System.out.println(query);
            InetAddress.getByName(query);
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }
}
