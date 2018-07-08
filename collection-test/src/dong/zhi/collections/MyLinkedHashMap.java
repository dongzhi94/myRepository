package dong.zhi.collections;

import java.util.*;

/**
 * 通过维护一个运行于所有条目的双向链表，保证了元素的迭代顺序，该迭代顺序可以是插入顺序，也可以是访问顺序
 * key,value都允许为空
 * 有序
 * 非线程安全
 * 可以看成是HashMap+LinkedList
 * @param <K>
 * @param <V>
 */
public class MyLinkedHashMap<K,V> extends MyHashMap<K,V> implements Map<K,V>{
    /**
     * The head of the doubly linked list.
     * 双向链表的头结点
     */
    private transient Entry<K,V> header;

    /**
     * The iteration ordering method for this linked hash map: <tt>true</tt>
     * for access-order, <tt>false</tt> for insertion-order.
     * true表示按照访问顺序迭代（最少使用次序？），false表示按照插入顺序迭代
     * 如果是访问顺序，则最久访问的节点在双向链表的头部，最近访问的节点放在尾部
     * 如果是插入顺序，则最先插入的节点在头部，最近插入的节点在尾部
     * @serial
     */
    private final boolean accessOrder;

    /**
     * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
     * with the specified initial capacity and load factor.
     * 構造方法，基于HashMap的构造方法。
     * 都是按照插入顺序
     */
    public MyLinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        accessOrder = false;
    }

    public MyLinkedHashMap(int initialCapacity) {
        super(initialCapacity);
        accessOrder = false;
    }

    public MyLinkedHashMap() {
        super();
        accessOrder = false;
    }

    public MyLinkedHashMap(Map<? extends K, ? extends V> m) {
        super(m);
        accessOrder = false;
    }

    public MyLinkedHashMap(int initialCapacity,
                         float loadFactor,
                         boolean accessOrder) {
        super(initialCapacity, loadFactor);
        this.accessOrder = accessOrder;
    }

    /**
     * Called by superclass constructors and pseudoconstructors (clone,
     * readObject) before any entries are inserted into the map.  Initializes
     * the chain.
     */
    @Override
    void init() {
        //在LinkedHashMap初始化的时候调用，hash为-1，其余都为空，说明是不存储任何值的空节点，只是作为双向链表的入口
        header = new Entry<>(-1, null, null, null);
        header.before = header.after = header;//初始化时，前后节点都指向自己
    }

    /**
     * Transfers all entries to new table array.  This method is called
     * by superclass resize.  It is overridden for performance, as it is
     * faster to iterate using our linked list.
     */
    @Override
    void transfer(MyHashMap.Entry[] newTable, boolean rehash) {
        int newCapacity = newTable.length;
        for (Entry<K,V> e = header.after; e != header; e = e.after) {
            if (rehash)
                e.hash = (e.key == null) ? 0 : hash(e.key);
            int index = indexFor(e.hash, newCapacity);
            e.next = newTable[index];
            newTable[index] = e;
        }
    }


    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value
     */
    public boolean containsValue(Object value) {
        // Overridden to take advantage of faster iterator
        if (value==null) {
            for (Entry e = header.after; e != header; e = e.after)
                if (e.value==null)
                    return true;
        } else {
            for (Entry e = header.after; e != header; e = e.after)
                if (value.equals(e.value))
                    return true;
        }
        return false;
    }

    /**
     * 重写了父类的get方法
     *
     */
    public V get(Object key) {
        //调用父类的getEntry()获取元素
        Entry<K,V> e = (Entry<K,V>)getEntry(key);
        if (e == null)
            return null;
        //调用recordAccess，如果是按访问顺序，则将该节点从当前位置山区，添加到链表的尾部
        e.recordAccess(this);
        return e.value;
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        super.clear();
        header.before = header.after = header;
    }

    /**
     * MyLinkedHashMap entry.
     * 从HashMap中继承过来的属性
     * 1、K key
     * 2、V value
     * 3、Entry<K, V> next  维护HashMap中桶（table）连接的entry的顺序
     * 4、int hash
     *
     */
    private static class Entry<K,V> extends MyHashMap.Entry<K,V> {
        // These fields comprise the doubly linked list used for iteration.
        //用于维护entry的插入顺序。LinkedList的顺序
        Entry<K,V> before, after;

        Entry(int hash, K key, V value, MyHashMap.Entry<K,V> next) {
            super(hash, key, value, next);
        }

        /**
         * Removes this entry from the linked list.
         */
        private void remove() {
            //删除当前节点，当前节点的前一个节点的后续节点，指向当前节点的后续节点
            before.after = after;
            //当前节点的后续节点的前序节点指向当前节点的前序节点
            after.before = before;
        }

        /**
         * Inserts this entry before the specified existing entry in the list.
         * 将节点插入到已存在的节点之前
         */
        private void addBefore(Entry<K,V> existingEntry) {
            after  = existingEntry;
            before = existingEntry.before;
            before.after = this;
            after.before = this;
        }

        /**
         * This method is invoked by the superclass whenever the value
         * of a pre-existing entry is read by Map.get or modified by Map.set.
         * If the enclosing Map is access-ordered, it moves the entry
         * to the end of the list; otherwise, it does nothing.
         * 没有重写put方法，但是重写了put中需要调用的recordAccess()方法
         * 当节点被访问或被修改后调用该方法，如果链表迭代顺序为“访问顺序”，则将该节点插入到双向链表的头部的前置节点
         * （由于链表是双向的，头结点的前置节点即为尾节点）
         */
        void recordAccess(MyHashMap<K,V> m) {
            MyLinkedHashMap<K,V> lm = (MyLinkedHashMap<K,V>)m;
            if (lm.accessOrder) {
                lm.modCount++;
                remove();//先将节点从当前位置上删去
                addBefore(lm.header);
            }
        }

        void recordRemoval(HashMap<K,V> m) {
            remove();
        }
    }

    private abstract class LinkedHashIterator<T> implements Iterator<T> {
        Entry<K,V> nextEntry    = header.after;
        Entry<K,V> lastReturned = null;

        /**
         * The modCount value that the iterator believes that the backing
         * List should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        int expectedModCount = modCount;

        public boolean hasNext() {
            return nextEntry != header;
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            MyLinkedHashMap.this.remove(lastReturned.key);
            lastReturned = null;
            expectedModCount = modCount;
        }

        Entry<K,V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (nextEntry == header)
                throw new NoSuchElementException();

            Entry<K,V> e = lastReturned = nextEntry;
            nextEntry = e.after;
            return e;
        }
    }

    private class KeyIterator extends LinkedHashIterator<K> {
        public K next() { return nextEntry().getKey(); }
    }

    private class ValueIterator extends LinkedHashIterator<V> {
        public V next() { return nextEntry().value; }
    }

    private class EntryIterator extends LinkedHashIterator<Map.Entry<K,V>> {
        public Map.Entry<K,V> next() { return nextEntry(); }
    }

    // These Overrides alter the behavior of superclass view iterator() methods
    Iterator<K> newKeyIterator()   { return new KeyIterator();   }
    Iterator<V> newValueIterator() { return new ValueIterator(); }
    Iterator<Map.Entry<K,V>> newEntryIterator() { return new EntryIterator(); }

    /**
     * This override alters behavior of superclass put method. It causes newly
     * allocated entry to get inserted at the end of the linked list and
     * removes the eldest entry if appropriate.
     * 没有重写put方法，但是重写了put中需要调用的addEntry()方法
     */
    void addEntry(int hash, K key, V value, int bucketIndex) {
        super.addEntry(hash, key, value, bucketIndex);

        // Remove eldest entry if instructed 用来支持FIFO算法
        Entry<K,V> eldest = header.after;
        if (removeEldestEntry(eldest)) {//返回的false
            removeEntryForKey(eldest.key);//删除双向链表中的该节点
        }
    }

    /**
     * This override differs from addEntry in that it doesn't resize the
     * table or remove the eldest entry.
     * 重写了addEntry里调用的createEntry，将创建的新节点放在链表的尾部
     */
    void createEntry(int hash, K key, V value, int bucketIndex) {
        //新添加的节点放在table[i]上
        MyHashMap.Entry<K,V> old = table[bucketIndex];
        Entry<K,V> e = new Entry<>(hash, key, value, old);
        table[bucketIndex] = e;
        //将新节点放在双向链表的尾部
        e.addBefore(header);
        size++;
    }

    /**
     * Returns <tt>true</tt> if this map should remove its eldest entry.
     * This method is invoked by <tt>put</tt> and <tt>putAll</tt> after
     * inserting a new entry into the map.  It provides the implementor
     * with the opportunity to remove the eldest entry each time a new one
     * is added.  This is useful if the map represents a cache: it allows
     * the map to reduce memory consumption by deleting stale entries.
     *
     * <p>Sample use: this override will allow the map to grow up to 100
     * entries and then delete the eldest entry each time a new entry is
     * added, maintaining a steady state of 100 entries.
     * <pre>
     *     private static final int MAX_ENTRIES = 100;
     *
     *     protected boolean removeEldestEntry(Map.Entry eldest) {
     *        return size() > MAX_ENTRIES;
     *     }
     * </pre>
     *
     * <p>This method typically does not modify the map in any way,
     * instead allowing the map to modify itself as directed by its
     * return value.  It <i>is</i> permitted for this method to modify
     * the map directly, but if it does so, it <i>must</i> return
     * <tt>false</tt> (indicating that the map should not attempt any
     * further modification).  The effects of returning <tt>true</tt>
     * after modifying the map from within this method are unspecified.
     *
     * <p>This implementation merely returns <tt>false</tt> (so that this
     * map acts like a normal map - the eldest element is never removed).
     *
     * @param    eldest The least recently inserted entry in the map, or if
     *           this is an access-ordered map, the least recently accessed
     *           entry.  This is the entry that will be removed it this
     *           method returns <tt>true</tt>.  If the map was empty prior
     *           to the <tt>put</tt> or <tt>putAll</tt> invocation resulting
     *           in this invocation, this will be the entry that was just
     *           inserted; in other words, if the map contains a single
     *           entry, the eldest entry is also the newest.
     * @return   <tt>true</tt> if the eldest entry should be removed
     *           from the map; <tt>false</tt> if it should be retained.
     */
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return false;
    }
}
