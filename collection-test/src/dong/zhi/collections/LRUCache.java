package dong.zhi.collections;

/**
 * LinkedHashMap可以用来作缓存，比方说LRUCache.
 * LinkedHashMap可以实现LRU算法的缓存基于两点：
 * 1、LinkedList首先它是一个Map，Map是基于K-V的，和缓存一致
 * 2、LinkedList提供了一个boolean值可以让用户指定是否实现LRU
 * LRUCache就是基于LRU算法的Cache（缓存）.
 * LRU：LRU即Least Recently Used，最近最少使用，也就是说，当缓存满了，会优先淘汰那些最近最不常访问的数据。比方说数据a，1天前访问了；数据b，2天前访问了，缓存满了，优先会淘汰数据b。
 * 而LinkedHashMap的按访问顺序的结构，就是将最近访问的元素移至链表尾部，则头部就是最近最少使用的，换句话说，双向链表最头的那个数据就是要淘汰的数据。
 */
public class LRUCache extends MyLinkedHashMap {

    protected int maxElements;
    public LRUCache(int maxSize){
        super(maxSize,0.75f,true);
        maxElements = maxSize;
    }
    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry eldest)
    {
        return size() > maxElements;
    }
}
