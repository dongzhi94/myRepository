package dong.zhi.sort;

import java.util.Arrays;

/**
 * 归并排序算法:采用的是分治策略。使用递归分段
 * 参考：https://www.cnblogs.com/chengxiao/p/6194356.html
 * 这段代码的思想像一个二叉树，最终的叶子结点都只有一个元素，永远先排序左子树，之后右子树，之后合并到父节点。一路向左，逐层向右。
 */
public class MergeSort {

    public static void main(String[] args){
        /*
        排序合并的元素：[8, 9, 0, 0, 0, 0, 0, 0, 0]
        排序合并的元素：[7, 8, 9, 0, 0, 0, 0, 0, 0]
        排序合并的元素：[2, 5, 0, 0, 0, 0, 0, 0, 0]
        排序合并的元素：[2, 5, 7, 8, 9, 0, 0, 0, 0]
        排序合并的元素：[4, 10, 0, 0, 0, 0, 0, 0, 0]
        排序合并的元素：[1, 3, 0, 0, 0, 0, 0, 0, 0]
        排序合并的元素：[1, 3, 4, 10, 0, 0, 0, 0, 0]
        排序合并的元素：[1, 2, 3, 4, 5, 7, 8, 9, 10]
        [1, 2, 3, 4, 5, 7, 8, 9, 10]
        元素个数为单数的情况下，左比右多一个元素，树高度多一层
         */
        int []arr = {9,8,7,5,2,10,4,3,1};
        sort(arr);
        System.out.println(Arrays.toString(arr));
    }

    public static void sort(int[] arr){

        /*
        若temp放于此处，则每次递归排序的结果如下，看起来不太符合归并排序算法，每次排序合并的元素数组，不应有上次递归的结果，虽不影响最终结果，但容易混淆
        所以每次递归后清空结果最好。每次递归前都是空的。
        排序合并的元素：[8, 9, 0, 0, 0, 0, 0, 0]
        排序合并的元素：[5, 7, 0, 0, 0, 0, 0, 0]
        排序合并的元素：[5, 7, 8, 9, 0, 0, 0, 0]
        排序合并的元素：[2, 10, 8, 9, 0, 0, 0, 0] //右序列第一次排序，只排2,10
        排序合并的元素：[3, 4, 8, 9, 0, 0, 0, 0]  //右序列第二次排序，只排3,4
        排序合并的元素：[2, 3, 4, 10, 0, 0, 0, 0]
        排序合并的元素：[2, 3, 4, 5, 7, 8, 9, 10]
        [2, 3, 4, 5, 7, 8, 9, 10]
        */
        //在排序前，先建好一个与原数组长度相同的临时数组，避免递归中频繁开辟空间，每一次递归创建新的，保证temp只是每次递归的最终结果，不存留上次递归的结果
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
            //temp不能放于此，虽每次递归前都是新的，但每次递归都要开辟空间
//            int[] temp = new int[arr.length];
            //分成两段，用 (left + right) / 2 取left和right中间元素的下标
            int mid = (left + right) / 2;
            //左边归并排序，使得左子序列有序
            sort(arr,left,mid,temp);
            //右边归并排序，使得右子序列有序
            sort(arr,mid+1,right,temp);
            //第一次跳出递归时，left=0,mid=0,right=1,即开始排序arr[0],arr[1]并合并。下一次跳出是排序arr[2],arr[3]
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
        /*
        排序合并的元素：[8, 9, 0, 0, 0, 0, 0, 0]
        排序合并的元素：[5, 7, 0, 0, 0, 0, 0, 0]
        排序合并的元素：[5, 7, 8, 9, 0, 0, 0, 0]
        排序合并的元素：[2, 10, 0, 0, 0, 0, 0, 0]
        排序合并的元素：[3, 4, 0, 0, 0, 0, 0, 0]
        排序合并的元素：[2, 3, 4, 10, 0, 0, 0, 0]
        排序合并的元素：[2, 3, 4, 5, 7, 8, 9, 10]
        [2, 3, 4, 5, 7, 8, 9, 10]
         */
        System.out.println("排序合并的元素："+ Arrays.toString(temp));
        //将temp数组中的元素全部拷贝至原数组中
        while (left <= right){
            arr[left++] = temp[t];
            //每次递归后清空
            temp[t] = 0;
            t++;

        }

    }
}
