package dong.zhi.collections;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TestMap {

    public static void main(String[] args){
        //HASHMAP
        /*boolean useAltHashing = sun.misc.VM.isBooted();//VM在启动时将该值赋为true
        System.out.println(useAltHashing);//true

        String altThreshold = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                        "jdk.map.althashing.threshold"));
        System.out.println(altThreshold);//null

        MyHashMap map = new MyHashMap();
        map.put("11",null);*/

        //LinkedHashMap--accessOrder = true;
        //1、LinkedList是有序的
        //2、每次访问一个元素（get或put），被访问的元素都被提到最后面去了
        MyLinkedHashMap<String, String> linkedHashMap =
                new MyLinkedHashMap<String, String>(4, 0.75f, true);
        linkedHashMap.put("111", "111");
        linkedHashMap.put("222", "222");
        linkedHashMap.put("333", "333");
        linkedHashMap.put("444", "444");
        loopLinkedHashMap(linkedHashMap);//111,222,333,444
        linkedHashMap.get("111");
        loopLinkedHashMap(linkedHashMap);//222,333,444,111
        linkedHashMap.put("222", "2222");
        loopLinkedHashMap(linkedHashMap);//333,444,111,222

    }

    public static void loopLinkedHashMap(MyLinkedHashMap<String, String> linkedHashMap)
    {
        Set<Map.Entry<String, String>> set = linkedHashMap.entrySet();
        Iterator<Map.Entry<String, String>> iterator = set.iterator();

        while (iterator.hasNext())
        {
            System.out.print(iterator.next() + "\t");
        }
        System.out.println();
    }
}
