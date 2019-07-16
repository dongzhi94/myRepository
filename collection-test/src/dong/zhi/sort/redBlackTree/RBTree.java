package dong.zhi.sort.redBlackTree;

import java.util.Comparator;

/**
 * 红黑树
 * 特征：1、节点都有颜色。2、在插入和删除的过程中，要遵循保持这些颜色的不通跑咧规则：红-黑规则
 * 红-黑规则：
 * 1、每个节点不是红色就是黑色的。
 * 2、根节点总是黑色的。
 * 3、如果节点是红色的，则它的子节点必须是黑色的（反之不一定），（也就是从给每个叶子到根的所有路径上不能有两个连续的红色节点）
 * 4、从根节点到叶节点或空子节点的每条路径，必须包含相同数目的黑色节点（即相同的黑色高度）。（从根节点到叶节点的路径上的黑色节点的数目成为黑色高度。）
 * 注意：新插入的节点总是红色的，这是因为插入一个红色节点比插入一个黑色节点违背红-黑规则的可能性更小，
 * 原因是插入黑色节点总会改变黑色高度（违背规则4），但是插入红色节点只有一半的机会会违背规则3（因为父节点是黑色的没事，父节点红色才违背规则3）。
 * 另外，违背规则3比违背规则4更容易修正。
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

    /**
     * 红黑树主要通过三种方式对平衡进行修正，1、改变节点颜色。2、左旋。3、右旋。
     * 如果是第一次插入，由于原树为空，直接将根节点涂黑即可，如果插入节点的父节点是黑色的，那不会违背红-黑树规则，什么也不需要做，但遇到如下三种情况时，就需要变色和旋转：
     * 1、插入节点的父节点和其叔叔节点（祖父节点的另一个子节点）均为红色
     * 2、插入节点的父节点是红色的，叔叔节点是黑色的，且插入节点是其父节点的右子节点
     * 3、插入节点的父节点是红色的，叔叔节点是黑色的，且插入节点是其父节点的左子节点
     * @param node
     */
    private void insertFixUp(RBNode<T> node) {
        //定义父节点和祖父节点
        RBNode<T> parent,gparent;
        //需要修正的条件是，父节点存在，且父节点为红色
        while ((parent = parentOf(node)) != null && node.color == RED){
            //父节点是红色，则祖父节点一定存在，且为黑色
            gparent = parentOf(parent);
            //若父节点是祖父节点的左子节点，else相反
            if(parent == gparent.left){
                //获得叔叔节点
                RBNode<T> uncle = gparent.right;
                //case 1: 叔叔节点也是红色
                if(uncle != null && uncle.color == RED){
                    //将父节点和叔叔节点涂黑
                    setBlack(parent);
                    setBlack(uncle);
                    //祖父节点涂红
                    setRed(gparent);
                    //再将当前节点指向祖父节点
                    node = gparent;
                    //再从当前节点开始算法,继续while循环，重新判断
                    continue;
                }

                //case2:叔叔节点是黑色，且当前节点是右子节点
                if(node == parent.right){
                    //将当前节点的父节点作为新的节点，以新的当前节点作为新的节点，做左旋操作。
                    leftRotate(parent);
                    //左旋后，当前节点变为父节点，原父节点变为左节点了，所以将node和parent调换下，为下面右旋做准备
                    RBNode<T> tmp = parent;
                    parent = node;
                    node = tmp;
                }

                //case3:叔叔节点是黑色，且当前节点是左子节点
                //将当前节点的父节点涂黑，祖父节点涂红，在祖父节点为支点做右旋操作，最后把根节点涂黑
                setBlack(parent);
                setRed(gparent);
                rightRotate(gparent);

            }else{
                //若父节点是祖父节点的右子节点，与上面的情况完全相反，但本质相同
                //获得叔叔节点
                RBNode<T> uncle = gparent.left;
                //case 1: 叔叔节点也是红色
                if(uncle != null && uncle.color == RED){
                    //将父节点和叔叔节点涂黑
                    setBlack(parent);
                    setBlack(uncle);
                    //祖父节点涂红
                    setRed(gparent);
                    //再将当前节点指向祖父节点
                    node = gparent;
                    //再从当前节点开始算法,继续while循环，重新判断
                    continue;
                }

                //case2:叔叔节点是黑色，且当前节点是左子节点
                if(node == parent.left){
                    //将当前节点的父节点作为新的节点，以新的当前节点作为新的节点，做右旋操作。
                    rightRotate(parent);
                    //右旋后，当前节点变为父节点，原父节点变为右节点了，所以将node和parent调换下，为下面左旋做准备
                    RBNode<T> tmp = parent;
                    parent = node;
                    node = tmp;
                }

                //case3:叔叔节点是黑色，且当前节点是右子节点
                //将当前节点的父节点涂黑，祖父节点涂红，在祖父节点为支点做左旋操作，最后把根节点涂黑
                setBlack(parent);
                setRed(gparent);
                leftRotate(gparent);
            }

        }
        //将根节点设置为黑色。当前节点为红色，且无父节点时跳出while循环，即当前节点为根节点且为红色时
        setBlack(root);
    }
    /**
     * 左旋示意图：对节点y进行右旋
     *        p                   p
     *       /                   /
     *      y                   x
     *     / \                 / \
     *    x  ry   ----->      lx  y
     *   / \                     / \
     * lx  rx                   rx ry
     * 右旋做了三件事：
     * 1. 将x的右子节点赋给y的左子节点,并将y赋给x右子节点的父节点(x右子节点非空时)
     * 2. 将y的父节点p(非空时)赋给x的父节点，同时更新p的子节点为x(左或右)
     * 3. 将x的右子节点设为y，将y的父节点设为x
     */
    private void rightRotate(RBNode<T> y) {
        //1. 将x的右子节点赋给y的左子节点,并将y赋给x右子节点的父节点(x右子节点非空时):先挪rx
        RBNode<T> x = y.left;
        y.left = x.right;
        if(x.right != null){
            x.right.parent = y;
        }

        //2. 将y的父节点p(非空时)赋给x的父节点，同时更新p的子节点为x(左或右):确定x和p的关系
        x.parent = y.parent;
        if(y.parent == null){
            //y是根节点
            this.root = x;
        }else{
            if(y == y.parent.left){
                //如果y是左节点，则将x也设置为左节点
                y.parent.left = x;
            }else{
                //如果y是右节点，则将x也设置为右节点
                y.parent.right = x;
            }
        }
        //3. 将x的右子节点设为y，将y的父节点设为x
        x.right = y;
        y.parent = x;

    }

    /*************对红黑树节点x进行左旋操作 ******************/
    /**
     * 左旋示意图：对节点x进行左旋
     *     p                       p
     *    /                       /
     *   x                       y
     *  / \                     / \
     * lx  y      ----->       x  ry
     *    / \                 / \
     *   ly ry               lx ly
     * 左旋做了三件事：
     * 1. 将y的左子节点赋给x的右子节点,并将x赋给y左子节点的父节点(y左子节点非空时)
     * 2. 将x的父节点p(非空时)赋给y的父节点，同时更新p的子节点为y(左或右)
     * 3. 将y的左子节点设为x，将x的父节点设为y
     */
    private void leftRotate(RBNode<T> x) {
        //1. 将y的左子节点赋给x的右子节点,并将x赋给y左子节点的父节点(y左子节点非空时) : 先将ly挪过去
        RBNode<T> y = x.right;
        x.right = y.left;
        if(y.left != null){
            y.left.parent = x;
        }
        //2. 将x的父节点p(非空时)赋给y的父节点，同时更新p的子节点为y(左或右) : 将y与p的关系确定
        y.parent = x.parent;
        if(x.parent == null){
            //若x的父节点为空，即x为根节点，则将y设置为根节点
            this.root = y;
        }else{
            //若x是左子节点，则将y设为x父节点p的左子节点
            if(x == x.parent.left){
                x.parent.left = y;
            }else{
                x.parent.right = y;
            }
        }
        //3. 将y的左子节点设为x，将x的父节点设为y
        y.left = x;
        x.parent = y;


    }

    /**
     * 获取父节点
     * @param node
     * @return
     */
    private RBNode<T> parentOf(RBNode<T> node) {
        return node == null ? null : node.parent;
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
