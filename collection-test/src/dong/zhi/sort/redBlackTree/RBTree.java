package dong.zhi.sort.redBlackTree;

import java.util.Comparator;

/**
 * 红黑树
 * @author dongzhi
 */
public class RBTree<T> {

    private static final boolean BLACK = true;

    private static final boolean RED = false;

    private final Comparator<? super T> comparator;
    /**
     * 树的根节点
     */
    RBNode<T> root;

    public RBTree(){
        comparator = null;
    }

    public RBTree(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    /**
     * 比较两个key
     * @param k1
     * @param k2
     * @return
     */
    final int compare(Object k1, Object k2) {
        return comparator==null ? ((Comparable<? super T>)k1).compareTo((T)k2)
                : comparator.compare((T)k1, (T)k2);
    }
    /**
     * 节点变色为黑色
     * @param node
     */
    public void setBlack(RBNode<T> node){
        node.color = BLACK;
    }

    /**
     * 节点变色为红色
     * @param node
     */
    public void setRed(RBNode<T> node){
        node.color = RED;
    }

    /**
     * 向红黑树中插入节点
     * 和二叉树的插入操作一样，都是先找到插入的位置，然后再将节点插入
     * @param key
     * @return
     */
    public RBNode<T> insertNode(T key) {
        RBNode<T> node = new RBNode<>(RED,key,null,null,null);
        if(node != null){
            insert(node);
        }
        return root;
    }

    private void insert(RBNode<T> node) {
        //标识最后node的父节点
        RBNode<T> current = null;
        //用来向下搜索，从根节点
        RBNode<T> x = this.root;
        //1、找到插入位置,二叉树查找
        while (x != null){
            //最终找到的Current即为要插入节点的父节点
            current = x;
            int cpm = compare(node.key,x.key);
            if(cpm < 0){
                //要插入的节点 《 比较的节点，向左查找
                x = x.left;
            }else {
                x = x.right;
            }
        }

        //找到了当前的位置，将当前Current作为node的父节点
        node.parent = current;
        //2、接下来判断是左子节点还是右子节点
        if(current != null){
            int cmp = compare(node.key,current.key);
            if(cmp < 0){
                current.left = node;
            }else{
                current.right = node;
            }
        }else{
            this.root = node;
        }

        //3、利用旋转操作，将其旋转为一棵红黑树
        insertFixUp(node);
    }

    //TODO
    private void insertFixUp(RBNode<T> node) {

    }

    public class RBNode<T> implements Comparable<T>{

        /**
         * 颜色:黑色为true，红色为false，默认为false
         */
        boolean color;

        /**
         * 关键值
         */
        T key;

        /**
         * 左子节点
         */
        RBNode<T> left;

        /**
         * 右子节点
         */
        RBNode<T> right;

        /**
         * 父节点
         */
        RBNode<T> parent;

        public RBNode(boolean color, T key, RBNode<T> left, RBNode<T> right, RBNode<T> parent){
            this.color = color;
            this.key = key;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }

        /**
         * 获取关键值
         * @return
         */
        public T getKey() {
            return key;
        }

        @Override
        public String toString() {
            return "" + key + (this.color == RED ? "R" : "B");
        }

        @Override
        public int compareTo(T o) {
            return 0;
        }
    }
}
