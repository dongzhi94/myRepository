package dong.zhi.sort;

import java.util.Arrays;

/**
 * 归并排序算法:采用的是分治策略。使用递归分段
 * 参考：https://www.cnblogs.com/chengxiao/p/6194356.html
 */
public class MergeSort {

    public static void main(String[] args){
        int []arr = {9,8,7,5,2,10,4,3};
        sort(arr);
        System.out.println(Arrays.toString(arr));
    }

    public static void sort(int[] arr){
        //在排序前，先建好一个与原数组长度相同的临时数组，避免递归中频繁开辟空间
        int []temp = new int[arr.length];
        sort(arr,0,arr.length-1,temp);
    }

    /**
     * 分段、合并、排序
     * @param arr 原数组
     * @param left 左序列指针
     * @param right 右序列指针
     * @param temp 临时数组
     */
    public static void sort(int[] arr,int left,int right,int[] temp){
        if(left < right){
            //分成两段
            int mid = (left + right) / 2;
            //左边归并排序，使得左子序列有序
            sort(arr,left,mid,temp);
            //右边归并排序，使得右子序列有序
            sort(arr,mid+1,right,temp);
            merge(arr,left,mid,right,temp);
        }
    }

    /**
     * 两个序列比较合并
     * @param arr
     * @param left
     * @param mid
     * @param right
     * @param temp
     */
    public static void merge(int[] arr,int left,int mid,int right,int[] temp){
        //左序列指针
        int i = left;
        //右系列指针
        int j = mid + 1;
        //临时数组指针
        int t = 0;
        //指针一直向右移动，直至其中一个超出序列尾部指针
        while (i <= mid && j <= right){
            if(arr[i] <= arr[j]){
                //如果左序列数字小，则左序列元素入临时数组,且左序列和临时数组指针向后移
                temp[t++] = arr[i++];
            }else {
                //如果右序列数字小，则右序列元素入临时数组,且右序列和临时数组指针向后移
                temp[t++] = arr[j++];
            }
        }
        //左边若有剩余，则全部拿到临时数组中
        while (i <= mid){
            temp[t++] = arr[i++];
        }
        //右边若有剩余，则全部拿到临时数组中，左序列和右序列只会有一边有剩余
        while (j <= right){
            temp[t++] = arr[j++];
        }
        t=0;
        //将temp数组中的元素全部拷贝至原数组中
        while (left <= right){
            arr[left++] = temp[t++];
        }

    }
}
