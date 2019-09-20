package dong.zhi.bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * 算法-位图
 * 参考：https://blog.csdn.net/weixin_40449300/article/details/87620692
 * 基本思想：32位机器上，一个int类型的整数，比如int a=1,在内存中占32bit，这是为了方便计算机的运算，但对于某些场景而言，这属于一种巨大的浪费，因为我们可以用对应的32bit位对应存储十进制的32个数（例如0-31）
 * 这就是bit-map的思想，bit-map利用这种思想可以处理大量数据的排序、查询和去重。
 * bit-map在用户群做交集和并集运算的时候也有极大的便利。
 * 40亿数据存储需要的空间大小：40亿 = 2的32次方 bit = 2的29次方 byte = 2的19次方 kb = 2的9次方 mb=512M
 * 若使用2-bitmap（2bit标志一个数的状态），占用1G空间
 */
public class BitMap {

    /**
     * 以N=100000000一个亿为例，进行排序或查找
     */
    private static final int N  = 100000000;
    /**
     * int数组中的1个int = 4byte = 4*8=32个bit，可存储32个数，N个数共需要 （N/32+1） 个int
     * a[0]--------------------------------> 0-31
     * a[1]--------------------------------> 32-63
     * a[2]--------------------------------> 64-95
     * a[3]--------------------------------> 96-127
     */
    private int[] a = new int[N/32 +1];


    /**
     * 添加数值，将所在的bit位置为1
     * @param n
     */
    public void addValue(int n){

        //求十进制数在数组a中的下标 row= n/32，使用位运算，n>>5，右移5位，相当于除32
        int row = n >> 5;
        //求十进制数在对应数a[row]中的bit位的下标。使用n模32即是bit位的下标。2的幂次方取模运算转换为位运算就是n%length = n&(length-1)
        //所以n%32 = n & 0x1F
        //找到下标后将该位置为1，位运算 1 << (n & 0x1F),即将1左移到该位置上
        // |= ：表示进行或运算后赋值，或运算是为确保a[row]的其他位置上的1不会被覆盖更改，只是将该值对应的该位改为1，其他位的值不动。a[n/32] |= 1 << n % 32 移位操作：a[n>>5] |= 1 << (n & 0x1F)
        a[row] |= 1 << (n & 0x1F);

    }

    /**
     * 判断某一个数字存不存在，即判断所在的bit位是否为1
     * 使用与运算判断位是否为1,例如 00011101 ，判断第二位是否为1，则与 00000010 进行与操作，00011101 & 00000010 = 00000010 只要不为0，即该位置为1.
     * @param n
     * @return
     */
    public  boolean exits(int n){
        int row = n >> 5;
        return (a[row] & (1 << (n & 0x1F))) != 0;
    }

    /**
     * 展示位图
     * 展示某一位，即将该位与1做与运算。将a[row]的末位与1做与运算，而后将a[row]右移一位，使得原第二位与1做与运算，以此类推
     * @param row
     */
    public void display(int row){
        System.out.println("BitMap位图展示");
        for(int i = 0; i < row; i ++){
            List<Integer> list = new ArrayList<>();
            //临时变量，避免更改原数组的数据
            int temp = a[i];
            for(int j = 1; j < 32; j ++){
                list.add(temp & 1);
                //右移后赋值
                temp >>= 1;
            }
            //打印的顺序是0-31
            System.out.println("a["+i+"]" + list);
        }
    }

    public static void main(String[] args) {
        int num[] = {1,5,30,32,64,56,159,120,21,17,35,45};
        BitMap bitMap = new BitMap();
        for(int i = 0; i < num.length; i++){
            bitMap.addValue(num[i]);
        }
        int temp = 120;
        boolean isexits = bitMap.exits(temp);
        System.out.println("temp:" + temp + "is already exits? " + isexits);
        bitMap.display(5);
    }

}
