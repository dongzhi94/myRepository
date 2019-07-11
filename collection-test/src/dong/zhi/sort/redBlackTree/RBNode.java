package dong.zhi.sort.redBlackTree;

public class RBNode<T> {

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

    public RBNode(boolean color,T key,RBNode<T> left,RBNode<T> right,RBNode<T> parent){
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
        return "" + key + (this.color == false ? "R" : "B");
    }
}
