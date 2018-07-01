package dong.zhi.collections;

public class TestMap {

    public static void main(String[] args){
        boolean useAltHashing = sun.misc.VM.isBooted();//VM在启动时将该值赋为true
        System.out.println(useAltHashing);//true

        String altThreshold = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                        "jdk.map.althashing.threshold"));
        System.out.println(altThreshold);//null

        MyHashMap map = new MyHashMap();
        map.put("11",null);


    }
}
