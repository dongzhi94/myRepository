package dong.zhi.sort.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：dongzhi
 * @date ：Created in 2020/3/28 10:03
 * @description：两数之和-简单
 * @modified By：
 * @version: 1.0$
 */
public class Solution {
    public static void main(String[] args) {
        int[] nums = {-1,-2,-3,-4,-5};
        int targer = -8;
//        int[] result = twoSum(nums,targer);
        int[] result = twoSum3(nums,targer);
        System.out.println(result[0]+","+result[1]);
    }

    /**
     * 先排序后相加
     * 时间耗时9ms，内存39.2m,空间复杂度高，需要的额外空间下标数组，数量取决于输入数组的个数
     * 插入排序的时间复杂度：O(n^2)，空间复杂度O(1)
     * @param nums
     * @param target
     * @return
     */
    public static int[] twoSum(int[] nums, int target) {
        //先排序
        int j;
        //记录排序后的原下标数组
        int[] tag = new int[nums.length];
        tag[0] = 0;
        //插入排序
        for(int i = 1; i<nums.length; i++){
            tag[i] = i;
            int tmptag = tag[i];
            int temp = nums[i];
            for(j = i; j > 0 && nums[j-1] > temp ; j--){
                nums[j] = nums[j-1];
                nums[j-1] = temp;
                tag[j]=tag[j-1];
                tag[j-1]=tmptag;
            }
        }
        //相加
        int a = 0;
        int b = nums.length-1;
        int[] result = new int[2];
        while(a < b){
            int tmp = nums[a] + nums[b];
            if(tmp == target){
                result[0] = tag[a];
                result[1] = tag[b];
                return result;
            }
            if(tmp > target){
                b--;
            }else{
                a++;
            }
        }
        return null;
    }

    /**
     * 遍历，每次遍历将元素与其后面的元素全部相加一遍
     * 耗时61ms，内存39.1m
     * 时间复杂度：O(n^2) 。对于每个元素，我们试图通过遍历数组的其余部分来寻找它所对应的目标元素，这将耗费 O(n) 的时间。因此时间复杂度为 O(n^2)
     * 空间复杂度：O(1)
     * @param nums
     * @param target
     * @return
     */
    public static int[] twoSum2(int[] nums, int target) {
        int[] result = new int[2];
        for(int i = 0; i < nums.length ; i++){
            for(int j = i+1;j < nums.length ; j++){
                if(nums[i] + nums[j] == target){
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 两遍哈希法：利用map查找key的特性，保持数组中的每个元素与其索引相互对应的最好方法是什么？哈希表
     * 耗时4ms，内存消耗：41.4m
     * 通过以空间换取速度的方式，我们可以将查找时间从 O(n) 降低到 O(1)。哈希表正是为此目的而构建的，它支持以 近似 恒定的时间进行快速查找。
     * 用“近似”来描述，是因为一旦出现冲突，查找用时可能会退化到 O(n)。但只要你仔细地挑选哈希函数，在哈希表中进行查找的用时应当被摊销为 O(1)。
     * 时间复杂度：O(n)
     * 空间复杂度：O(n)
     * @param nums
     * @param target
     * @return
     */
    public static int[] twoSum3(int[] nums, int target) {
        Map<Integer,Integer> map = new HashMap<>();
        for(int i = 0; i < nums.length; i++){
            //这样存储得保证输入的数组没有重复的值，否则该使用 map.put(i,nums[i]);
            map.put(nums[i],i);
        }
        for(int j = 0; j < nums.length; j ++){
            int tmp = target - nums[j];
            if(map.containsKey(tmp) && map.get(tmp) != j){
                return new int[]{j,map.get(tmp)};
            }
        }
        return null;
    }

    /**
     * 一遍哈希法：在遍历的过程中，既从map中寻找是否存在这个值，又将该值放入map
     * 时间复杂度：O(n)
     * 空间复杂度：O(n)
     * HashMap的containsKey方法，先通过hash值获取到数组得下标，指向数组引用，应为O(1)，未命中时，才会去遍历红黑树，时间复杂度为O(n)
     * @param nums
     * @param target
     * @return
     */
    public int[] twoSum4(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (map.containsKey(complement)) {
                return new int[] { map.get(complement), i };
            }
            map.put(nums[i], i);
        }
        throw new IllegalArgumentException("No two sum solution");
    }

}
