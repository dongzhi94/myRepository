package dong.zhi.internet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class InternetTest {
    public static void main(String[] args){
//        testInetAddress();
//        testAddress();
        testNetWorkInterface();
    }

    /**
     * 获取主机名和ip地址
     */
    public static void testInetAddress(){
        InetAddress address = null;
        InetAddress localAddress = null;
        try {

            //1.通过主机名获取InetAddress对象
            address = InetAddress.getByName("www.baidu.com");
            //www.baidu.com/182.61.200.7
            System.out.println(address);
            //2.通过地址获取InetAddress对象 /182.61.200.7
            address = InetAddress.getByName("182.61.200.7");
            //获取对象的主机名，获取不到，返回四段式ip地址 182.61.200.7
            System.out.println(address.getHostName());
            //3.返回当前主机的地址 LAPTOP-LP4067O1/192.168.99.1
            localAddress = InetAddress.getLocalHost();
            System.out.println(localAddress);
            String localhost = localAddress.getHostName();
            String localhost1 = localAddress.getCanonicalHostName();
            //可以根据getAddress方法返回的数组得大小，可判断是IPV4（长度为4）还是IPV6（长度为16）。
            byte[] add = localAddress.getAddress();
            String hostAddress = localAddress.getHostAddress();
            System.out.println("localhost:"+localhost + ",localhost1:"+localhost1+",add:"+add+",hostAddress:"+hostAddress);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断地址是否符合其中某个标准
     */
    public static void testAddress(){
        try {
            //通配地址：通配地址可以匹配本地系统中的任何位置。在ipv4中，通配地址是0.0.0.0.ipv6中通配地址是0:0:0:0:0:0:0:0(又写作::)
            InetAddress address1 = InetAddress.getByName("0.0.0.0");
            System.out.println(address1.isAnyLocalAddress());
            //回送地址：在ipv4中，回送地址是127.0.0.1.ipv6中通配地址是0:0:0:0:0:0:0:1(又写作::1)
            InetAddress address2 = InetAddress.getByName("127.0.0.1");
            System.out.println(address2.isLoopbackAddress());
            //如果地址是一个ipv6本地链接地址，isLinkLocalAddress会返回true
            InetAddress address3 = InetAddress.getByName("FE80:0:0:0:0:0:0:1");
            System.out.println(address3.isLinkLocalAddress());
            //组播地址：会将内容广播给所有预定的计算机，而不是某一台计算机。ipv4中，组播地址都在224.0.0.0到239.225.225.225范围内。ipv6中，组播地址都以FE字母开头
            InetAddress address4 = InetAddress.getByName("224.0.0.0");
            System.out.println(address4.isMulticastAddress());
            //InetAddress有两个isReachable方法，测试一个特定节点到当前主机是否可达（能否建立一个连接）
            //这些方法尝试使用traceroute，确切的说，就是ICMP echo请求，查看地址是否可达，如果主机在timeout毫秒内响应，则方法返回true
            InetAddress address5 = InetAddress.getByName("www.baidu.com");
            System.out.println(address5.isReachable(300));

            //测试两个对象的equals：两个InetAddress对象有相同的ip地址，只有这时两个对象相等
            InetAddress address6 = InetAddress.getByName("www.baidu.com");
            InetAddress address7 = InetAddress.getByName("182.61.200.61");
            System.out.println(address6.equals(address7));
            System.out.println(address6.getHostAddress()+" "+address7.getHostAddress());


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testNetWorkInterface(){
        try {
            //获取接口
            NetworkInterface ni = NetworkInterface.getByName("eth1");
            if(ni == null){
                //name:eth0 (VirtualBox Host-Only Ethernet Adapter #2)
                //name:eth1 (WAN Miniport (Network Monitor))
                System.out.println("No such interface : eth1");
            }else{
                System.out.println("ni"+ni);
            }

            //获取和ip绑定的网络接口
            InetAddress local = InetAddress.getByName("127.0.0.1");
            NetworkInterface ni1 = NetworkInterface.getByInetAddress(local);
            //name:lo (Software Loopback Interface 1)
            System.out.println("ni1:"+ni1);
            //NetworkInterface的getNetworkInterfaces放回一个enumeration，列出所有的网络接口
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()){
                NetworkInterface ni2 = enumeration.nextElement();
                //ni2-name:lo,ni2-index:1
                //ni2-name:net0,ni2-index:2
                //ni2-name:net1,ni2-index:3
                System.out.println("ni2-name:"+ni2.getName()+",ni2-index:"+ni2.getIndex());
                Enumeration<InetAddress> addresses = ni2.getInetAddresses();
                while (addresses.hasMoreElements()){
                    //回送地址：ni2-name:lo,ni2-index:1
                    //  ni2-inetaddress:/127.0.0.1
                    //  ni2-inetaddress:/0:0:0:0:0:0:0:1
                    //物理网卡：ni2-name:eth0,ni2-index:4
                    //  ni2-inetaddress:/192.168.99.1
                    //  ni2-inetaddress:/fe80:0:0:0:9049:97bf:b035:dc7e%4
                    System.out.println("    ni2-inetaddress:"+addresses.nextElement());
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
