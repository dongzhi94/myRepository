package dong.zhi.collections;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.*;

/**
 * HashMap 是一个散列表，它存储的内容是键值对(key-value)映射。
 * 特点：HashMap 的实现不是同步的，这意味着它不是线程安全的。它的key、value都可以为null。此外，HashMap中的映射不是有序的
 * 性能：HashMap 的实例有两个参数影响其性能：“初始容量” 和 “加载因子”。
 *      容量：是哈希表中桶的数量，初始容量 只是哈希表在创建时的容量。
 *      加载因子：是哈希表在其容量自动增加之前可以达到多满的一种尺度。默认为0.75，在时间和空间上追求折中。
 *      当哈希表中的条目数超出了加载因子与当前容量的乘积时，则要对该哈希表进行 rehash 操作（即重建内部数据结构），从而哈希表将具有大约两倍的桶数。
 *      加载因子过大，减少了空间开销，但增加了查询的成本
 *
 */
public class MyHashMap<K,V> extends AbstractMap<K,V> implements Map<K,V> {
    /**
     * 默认初始容量（hash桶的数量），必须是2的幂次
     * 1、为何使用位运算：位运算高效
     * 大家都知道，所有程序在计算机中都是以二进制形式存储，那位运算，其实就是直接对内存中的二进制位进行操作，自然比其他运算高效。
     * 2、为何必须是2的幂次：hash算法使用取模运算决定位置，下面为了提高效率，想将取模运算转换为位运算（&运算），而当容量为2的n次方时，
     * h&(length-1) 与 h%length等价，但他们是等价但不等效（效率），其效果都是取h与length的模，
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * 最大容量
     * int为四个字节，32位，除去符号位还剩31位，再考虑到这里其实是定义HashMap Bukect数组的长度，考虑到Java堆存储空间的限制，定位30位，其大小为1073741824。
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 默认加载因子.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 空的table作为默认.
     */
    static final Entry<?,?>[] EMPTY_TABLE = {};

    /**
     * 定义了真正hashmap桶使用的数组，长度必须为2的幂次.
     * transient含义：表示该数据不参与序列化。假设hashmap中存储数据的数组还有很多空间没有被使用，序列化也没有意义。所以下文会有手动使用 writeObject() 方法，只序列化实际存储元素的数组。
     *
     * 序列化：把内存中的对象变为字节序列的过程。
     */
    transient Entry<K,V>[] table = (Entry<K,V>[]) EMPTY_TABLE;

    /**
     * size是真正的当前hashmap包含的K/V数据的大小.
     */
    transient int size;

    /**
     * 阈值，当达到阈值时会触发一些事情。？
     */
    // If table == EMPTY_TABLE then this is the initial capacity at which the
    // table will be created when inflated.
    int threshold;

    /**
     * 加载因子
     */
    final float loadFactor;

    /**
     * 记录hashmap结构被修改的次数，结构修改指改变了K/V映射，如新增、删除一个元素，又或是改变了内部结构，如rehash。这个字段被用来当迭代器的fail-fast。什么意思？
     * fail-fast机制：我们造，hashmap是线程不安全的，因此如果在使用迭代器的过程中有其他线程修改了hashmap，那么将抛出ConcurrentModificationException，这就是所谓fail-fast策略。
     *下次看到HashMap使用中抛出ConcurrentModificationException, 就知道有多线程并发使用了。怎么处理？ConcurrentHashMap。
     */
    transient int modCount;

    /**
     * 该参数与Holder为JDK1.7新添加的，针对于字符串的key，提供一个新的hash算法会提供更好的hashcode分布从而减少冲突。
     * 如果想启用尝鲜这个特性，你需要设置jdk.map.althashing.threshold这个系统属性的值为一个非负数（默认是-1）这个值代表了一个集合大小的threshold，超过这个值，就会使用新的hash算法。
     * 需要注意的一点，只有当re-hash的时候，新的hash算法才会起作用。
     */
    static final int ALTERNATIVE_HASHING_THRESHOLD_DEFAULT = Integer.MAX_VALUE;//2147483647=2^31-1,32位系统中符号型整型常量

    /**
     * Holder本身只是加载获取这个配置参数而已
     */
    private static class Holder {

        /**
         * Table capacity above which to switch to use alternative hashing.
         */
        static final int ALTERNATIVE_HASHING_THRESHOLD;

        static {
            String altThreshold = java.security.AccessController.doPrivileged(
                    new sun.security.action.GetPropertyAction(
                            "jdk.map.althashing.threshold"));//null，何时会变？

            int threshold;
            try {
                threshold = (null != altThreshold)
                        ? Integer.parseInt(altThreshold)
                        : ALTERNATIVE_HASHING_THRESHOLD_DEFAULT;//Integer.MAX_VALUE

                // disable alternative hashing if -1
                if (threshold == -1) {
                    threshold = Integer.MAX_VALUE;
                }

                if (threshold < 0) {
                    throw new IllegalArgumentException("value must be positive integer.");
                }
            } catch(IllegalArgumentException failed) {
                throw new Error("Illegal value for 'jdk.map.althashing.threshold'", failed);
            }

            ALTERNATIVE_HASHING_THRESHOLD = threshold;//Integer.MAX_VALUE
        }
    }

    /**
     * A randomizing value associated with this instance that is applied to
     * hash code of keys to make hash collisions harder to find. If 0 then
     * alternative hashing is disabled.
     */
    transient int hashSeed = 0;

    /**
     * 初始化容量和加载因子为参数的构造器
     * 这里的初始化容量与上面相同，指桶或数组的长度
     */
    public MyHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                    loadFactor);

        this.loadFactor = loadFactor;
        //将阈值设在初始化容量 TODO why?
        threshold = initialCapacity;
        init();
    }

    /**
     * Constructs an empty <tt>MyHashMap</tt> with the specified initial
     * capacity and the default load factor (0.75).
     */
    public MyHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>MyHashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public MyHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 从一个已有的map构造出一个新的hashmap
     */
    public MyHashMap(Map<? extends K, ? extends V> m) {
        //已有map的长度/加载因子+1，与默认初始化容量，取大的一方作为初始化容量
        //+1是为了防止为0的情况
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
                DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        inflateTable(threshold);

        putAllForCreate(m);
    }

    //比入参初始容量大的最小的2的幂数,如果大于最大容量，则=最大容量
    private static int roundUpToPowerOf2(int number) {
        // assert number >= 0 : "number must be non-negative";
        return number >= MAXIMUM_CAPACITY
                ? MAXIMUM_CAPACITY
                : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
    }

    /**
     * Inflates the table.
     */
    private void inflateTable(int toSize) {
        // Find a power of 2 >= toSize 比入参初始容量大的最小的2的幂数，作为数组的容量
        int capacity = roundUpToPowerOf2(toSize);//最大值为最大容量1<<30
        //之后根据计算好的数组长度，创建Entry数组，并针对Bucket数组容量巨大的采用新的Hash算法。  TODO why?
        //容量*加载因子=阈值，保证阈值不能超过最大容量
        threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
        table = new Entry[capacity];
        initHashSeedAsNeeded(capacity);//返回了false
    }

    // internal utilities

    /**
     * init函数提及一下，注释讲其作可作为一个钩子来被子类使用，它已经作为模版模式被所有的构造器，clone等调用。
     */
    void init() {
    }

    /**
     * Initialize the hashing mask value. We defer initialization until we
     * really need it.
     * @param capacity 最大值为1<<30
     */
    final boolean initHashSeedAsNeeded(int capacity) {
        //当hashmap初始化时，hashseed为0,0！=0值为false
        boolean currentAltHashing = hashSeed != 0;
        boolean useAltHashing = sun.misc.VM.isBooted() &&
                (capacity >= Holder.ALTERNATIVE_HASHING_THRESHOLD);//Holder.ALTERNATIVE_HASHING_THRESHOLD值为int的最大值，true && false = false
        boolean switching = currentAltHashing ^ useAltHashing; //false ^ false = false,异或运算，二进制位不同为1
        if (switching) {
            hashSeed = useAltHashing
                    ? sun.misc.Hashing.randomHashSeed(this)
                    : 0;
        }
        //返回false
        return switching;
    }

    /**
     * Retrieve object hash code and applies a supplemental hash function to the
     * result hash, which defends against poor quality hash functions.  This is
     * critical because MyHashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ
     * in lower bits. Note: Null keys always map to hash 0, thus index 0.
     */
    final int hash(Object k) {
        int h = hashSeed;//hashseed并没有重新赋值，初始化时为0
        if (0 != h && k instanceof String) {
            return sun.misc.Hashing.stringHash32((String) k);
        }

        //与0相异或，保留原值
        h ^= k.hashCode();//第一步，将key的hash值与hashseed做异或运算

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        //第二次hash：将上一步所得hash值进行无符号右移20位，和hash值无符号右移12位进行异或运算。相当于，hash值取高12位和高20位进行异或运算
        //最终结果：高12位置为0，中间8位保持原hash值高8位，后12位为：原hash值高12位（第1到12位）和去除高8位的中间12位（第9到20位）进行异或
        h ^= (h >>> 20) ^ (h >>> 12);
        //第三次hash：右移7位（高19位为0），右移4位（高16位为0），进行异或，结果为高16位为0，第17-19位三位保持上一步结果的高三位，其余进行异或，
        //所得结果在于第二部结果进行异或，结果：高12位仍然为0，第13-16位保持第二步的13-16位（即，原hash值的高4位），其余进行异或
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Returns index for hash code h.
     */
    static int indexFor(int h, int length) {
        // assert Integer.bitCount(length) == 1 : "length must be a non-zero power of 2";
        return h & (length-1);
    }

    /**
     * map中K/V的数量
     */
    public int size() {
        return size;
    }

    /**
     * 返回map重K/V数量是否为0
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     */
    public V get(Object key) {
        //如果key为null，则直接找table[0]
        if (key == null)
            return getForNullKey();
        Entry<K,V> entry = getEntry(key);

        return null == entry ? null : entry.getValue();
    }

    /**
     * Offloaded version of get() to look up null keys.  Null keys map
     * to index 0.  This null case is split out into separate methods
     * for the sake of performance in the two most commonly used
     * operations (get and put), but incorporated with conditionals in
     * others.
     */
    private V getForNullKey() {
        if (size == 0) {
            return null;
        }
        for (Entry<K,V> e = table[0]; e != null; e = e.next) {
            if (e.key == null)
                return e.value;
        }
        return null;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     * 判断是否包含key
     */
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Returns the entry associated with the specified key in the
     * MyHashMap.  Returns null if the MyHashMap contains no mapping
     * for the key.
     */
    final Entry<K,V> getEntry(Object key) {
        if (size == 0) {
            return null;
        }

        int hash = (key == null) ? 0 : hash(key);
        for (Entry<K,V> e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            Object k;
            //判断hash值，key是否相同
            if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                return e;
        }
        return null;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     */
    public V put(K key, V value) {
        //如果table为空集合
        if (table == EMPTY_TABLE) {
            //构造一个初始数组，threshold为阈值，构造方法中初始化
            inflateTable(threshold);
        }
        if (key == null)
            return putForNullKey(value);//如果原来就有key为null的节点，则返回更新前的值
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        for (Entry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
        addEntry(hash, key, value, i);
        return null;
    }

    /**
     * Offloaded version of put for null keys
     * key为null的放在table[0]的位置
     */
    private V putForNullKey(V value) {
        //便利table[0]的链表，将key为null的值改为value
        for (Entry<K,V> e = table[0]; e != null; e = e.next) {
            if (e.key == null) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }
        //table[0]没有key为null的，则新增一个entry节点
        modCount++;//hashmap的结构改动（新增，删除，rehash等）时，modCount值才++
        addEntry(0, null, value, 0);//hash值为0，key为null
        return null;
    }

    /**
     * This method is used instead of put by constructors and
     * pseudoconstructors (clone, readObject).  It does not resize the table,
     * check for comodification, etc.  It calls createEntry rather than
     * addEntry.
     */
    private void putForCreate(K key, V value) {
        int hash = null == key ? 0 : hash(key);
        int i = indexFor(hash, table.length);

        /**
         * Look for preexisting entry for key.  This will never happen for
         * clone or deserialize.  It will only happen for construction if the
         * input Map is a sorted map whose ordering is inconsistent w/ equals.
         * 可能传入的旧map是有序的key可重复的？但是hashmap不允许key重复，所以去重吗？ TODO
         */
        for (Entry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k)))) {
                e.value = value;
                return;
            }
        }

        createEntry(hash, key, value, i);
    }

    private void putAllForCreate(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            putForCreate(e.getKey(), e.getValue());
    }

    /**
     * Rehashes the contents of this map into a new array with a
     * larger capacity.  This method is called automatically when the
     * number of keys in this map reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not
     * resize the map, but sets threshold to Integer.MAX_VALUE.
     * This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two;
     *        must be greater than current capacity unless current
     *        capacity is MAXIMUM_CAPACITY (in which case value
     *        is irrelevant).
     */
    void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {//如果目前hashmap已经达到了最大容量，则不进行扩容，但将阈值增加至int的最大值
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];//新容量为旧容量的2倍，创建信的entry数组（table）
        transfer(newTable, initHashSeedAsNeeded(newCapacity));
        table = newTable;//将新table赋值给成员变量
        threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);//重新计算阈值
    }

    /**
     * Transfers all entries from current table to newTable.
     */
    void transfer(Entry[] newTable, boolean rehash) {
        int newCapacity = newTable.length;
        //直接遍历老的hashmap
        for (Entry<K,V> e : table) {
            while(null != e) {
                Entry<K,V> next = e.next;
                //如果需要rehash则将节点进行rehash
                if (rehash) {
                    e.hash = null == e.key ? 0 : hash(e.key);
                }
                //hash值与newCapacity-1做位运算&，相当于取模运算，计算出节点的位置
                int i = indexFor(e.hash, newCapacity);
                e.next = newTable[i];//将E的next指针指向table[i]当前节点，例如是a,即，将e放置在a前面
                newTable[i] = e;//再将e放在table[i]的位置上，即，新来的节点都会放置在当前table[i]的链表最前端
                e = next;//遍历旧链表下一个节点
            }
        }
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0)
            return;

        if (table == EMPTY_TABLE) {
            inflateTable((int) Math.max(numKeysToBeAdded * loadFactor, threshold));
        }

        /*
         * Expand the map if the map if the number of mappings to be added
         * is greater than or equal to threshold.  This is conservative; the
         * obvious condition is (m.size() + size) >= threshold, but this
         * condition could result in a map with twice the appropriate capacity,
         * if the keys to be added overlap with the keys already in this map.
         * By using the conservative calculation, we subject ourself
         * to at most one extra resize.
         */
        if (numKeysToBeAdded > threshold) {
            int targetCapacity = (int)(numKeysToBeAdded / loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY)
                targetCapacity = MAXIMUM_CAPACITY;
            int newCapacity = table.length;
            while (newCapacity < targetCapacity)
                newCapacity <<= 1;
            if (newCapacity > table.length)
                resize(newCapacity);
        }

        for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param  key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public V remove(Object key) {
        Entry<K,V> e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }

    /**
     * Removes and returns the entry associated with the specified key
     * in the MyHashMap.  Returns null if the MyHashMap contains no mapping
     * for this key.
     */
    final Entry<K,V> removeEntryForKey(Object key) {
        if (size == 0) {
            return null;
        }
        int hash = (key == null) ? 0 : hash(key);
        int i = indexFor(hash, table.length);
        Entry<K,V> prev = table[i];
        Entry<K,V> e = prev;

        while (e != null) {
            Entry<K,V> next = e.next;
            Object k;
            if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k)))) {
                modCount++;
                size--;
                if (prev == e)
                    table[i] = next;
                else
                    prev.next = next;
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Special version of remove for EntrySet using {@code Map.Entry.equals()}
     * for matching.
     */
    final Entry<K,V> removeMapping(Object o) {
        if (size == 0 || !(o instanceof Map.Entry))
            return null;

        Map.Entry<K,V> entry = (Map.Entry<K,V>) o;
        Object key = entry.getKey();
        int hash = (key == null) ? 0 : hash(key);
        int i = indexFor(hash, table.length);
        Entry<K,V> prev = table[i];
        Entry<K,V> e = prev;

        while (e != null) {
            Entry<K,V> next = e.next;
            if (e.hash == hash && e.equals(entry)) {
                modCount++;
                size--;
                if (prev == e)
                    table[i] = next;
                else
                    prev.next = next;
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        modCount++;
        Arrays.fill(table, null);
        size = 0;
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
        if (value == null)
            return containsNullValue();

        Entry[] tab = table;
        for (int i = 0; i < tab.length ; i++)
            for (Entry e = tab[i] ; e != null ; e = e.next)
                if (value.equals(e.value))
                    return true;
        return false;
    }

    /**
     * Special-case code for containsValue with null argument
     */
    private boolean containsNullValue() {
        Entry[] tab = table;
        for (int i = 0; i < tab.length ; i++)
            for (Entry e = tab[i] ; e != null ; e = e.next)
                if (e.value == null)
                    return true;
        return false;
    }

    /**
     * Returns a shallow copy of this <tt>MyHashMap</tt> instance: the keys and
     * values themselves are not cloned.
     *
     * @return a shallow copy of this map
     */
    public Object clone() {
        MyHashMap<K,V> result = null;
        try {
            result = (MyHashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // assert false;
        }
        if (result.table != EMPTY_TABLE) {
            result.inflateTable(Math.min(
                    (int) Math.min(
                            size * Math.min(1 / loadFactor, 4.0f),
                            // we have limits...
                            MyHashMap.MAXIMUM_CAPACITY),
                    table.length));
        }
        result.entrySet = null;
        result.modCount = 0;
        result.size = 0;
        result.init();
        result.putAllForCreate(this);

        return result;
    }

    /**
     * 节点的数据结构
     */
    static class Entry<K,V> implements Map.Entry<K,V> {
        final K key;
        V value;
        Entry<K,V> next;
        int hash;

        /**
         * Creates new entry.
         */
        Entry(int h, K k, V v, Entry<K,V> n) {
            value = v;
            next = n;
            key = k;
            hash = h;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry)o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            //两个key的equals相同
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                //两个key对应的value的equals相同
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        public final String toString() {
            return getKey() + "=" + getValue();
        }

        /**
         * This method is invoked whenever the value in an entry is
         * overwritten by an invocation of put(k,v) for a key k that's already
         * in the MyHashMap.
         */
        void recordAccess(MyHashMap<K,V> m) {
        }

        /**
         * This method is invoked whenever the entry is
         * removed from the table.
         */
        void recordRemoval(MyHashMap<K,V> m) {
        }
    }

    /**
     * Adds a new entry with the specified key, value and hash code to
     * the specified bucket.  It is the responsibility of this
     * method to resize the table if appropriate.
     *
     * Subclass overrides this to alter the behavior of put method.
     */
    void addEntry(int hash, K key, V value, int bucketIndex) {
        //map的size超过阈值，并且指定的桶已经被初始化，需要对map进行扩容
        if ((size >= threshold) && (null != table[bucketIndex])) {
            //hashmap扩容为原来的2倍
            resize(2 * table.length);
            //重新计算新增节点的hash值
            hash = (null != key) ? hash(key) : 0;
            //重新计算该hash值所在桶的索引，取模运算
            bucketIndex = indexFor(hash, table.length);
        }
        //创建新的节点
        createEntry(hash, key, value, bucketIndex);
    }

    /**
     * Like addEntry except that this version is used when creating entries
     * as part of Map construction or "pseudo-construction" (cloning,
     * deserialization).  This version needn't worry about resizing the table.
     *
     * Subclass overrides this to alter the behavior of MyHashMap(Map),
     * clone, and readObject.
     */
    void createEntry(int hash, K key, V value, int bucketIndex) {
        //将table[i]位置上的节点保存
        Entry<K,V> e = table[bucketIndex];
        //创建新的节点，指向原table[i]位置上的节点，即每新增一个节点时，都会放到链表的最前边。免去遍历
        table[bucketIndex] = new Entry<>(hash, key, value, e);
        //map的size++
        size++;
    }

    private abstract class HashIterator<E> implements Iterator<E> {
        Entry<K,V> next;        // next entry to return
        int expectedModCount;   // For fast-fail
        int index;              // current slot
        Entry<K,V> current;     // current entry

        HashIterator() {
            expectedModCount = modCount;
            if (size > 0) { // advance to first entry
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Entry<K,V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Entry<K,V> e = next;
            if (e == null)
                throw new NoSuchElementException();

            if ((next = e.next) == null) {
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
            current = e;
            return e;
        }

        public void remove() {
            if (current == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Object k = current.key;
            current = null;
            MyHashMap.this.removeEntryForKey(k);
            expectedModCount = modCount;
        }
    }

    private final class ValueIterator extends HashIterator<V> {
        public V next() {
            return nextEntry().value;
        }
    }

    private final class KeyIterator extends HashIterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

    private final class EntryIterator extends HashIterator<Map.Entry<K,V>> {
        public Map.Entry<K,V> next() {
            return nextEntry();
        }
    }

    // Subclass overrides these to alter behavior of views' iterator() method
    Iterator<K> newKeyIterator()   {
        return new KeyIterator();
    }
    Iterator<V> newValueIterator()   {
        return new ValueIterator();
    }
    Iterator<Map.Entry<K,V>> newEntryIterator()   {
        return new EntryIterator();
    }


    // Views

    private transient Set<Map.Entry<K,V>> entrySet = null;

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     */
    /*public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null ? ks : (keySet = new KeySet()));
    }*/

    private final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return newKeyIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object o) {
            return containsKey(o);
        }
        public boolean remove(Object o) {
            return MyHashMap.this.removeEntryForKey(o) != null;
        }
        public void clear() {
            MyHashMap.this.clear();
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     */
    /*public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null ? vs : (values = new Values()));
    }*/

    private final class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return newValueIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public void clear() {
            MyHashMap.this.clear();
        }
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    public Set<Map.Entry<K,V>> entrySet() {
        return entrySet0();
    }

    private Set<Map.Entry<K,V>> entrySet0() {
        Set<Map.Entry<K,V>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    private final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return newEntryIterator();
        }
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<K,V> e = (Map.Entry<K,V>) o;
            Entry<K,V> candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }
        public boolean remove(Object o) {
            return removeMapping(o) != null;
        }
        public int size() {
            return size;
        }
        public void clear() {
            MyHashMap.this.clear();
        }
    }

    /**
     * Save the state of the <tt>MyHashMap</tt> instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <i>capacity</i> of the MyHashMap (the length of the
     *             bucket array) is emitted (int), followed by the
     *             <i>size</i> (an int, the number of key-value
     *             mappings), followed by the key (Object) and value (Object)
     *             for each key-value mapping.  The key-value mappings are
     *             emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException
    {
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();

        // Write out number of buckets
        if (table==EMPTY_TABLE) {
            s.writeInt(roundUpToPowerOf2(threshold));
        } else {
            s.writeInt(table.length);
        }

        // Write out size (number of Mappings)
        s.writeInt(size);

        // Write out keys and values (alternating)
        if (size > 0) {
            for(Map.Entry<K,V> e : entrySet0()) {
                s.writeObject(e.getKey());
                s.writeObject(e.getValue());
            }
        }
    }

    private static final long serialVersionUID = 362498820763181265L;

    /**
     * Reconstitute the {@code MyHashMap} instance from a stream (i.e.,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException
    {
        // Read in the threshold (ignored), loadfactor, and any hidden stuff
        s.defaultReadObject();
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new InvalidObjectException("Illegal load factor: " +
                    loadFactor);
        }

        // set other fields that need values
        table = (Entry<K,V>[]) EMPTY_TABLE;

        // Read in number of buckets
        s.readInt(); // ignored.

        // Read number of mappings
        int mappings = s.readInt();
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " +
                    mappings);

        // capacity chosen by number of mappings and desired load (if >= 0.25)
        int capacity = (int) Math.min(
                mappings * Math.min(1 / loadFactor, 4.0f),
                // we have limits...
                MyHashMap.MAXIMUM_CAPACITY);

        // allocate the bucket array;
        if (mappings > 0) {
            inflateTable(capacity);
        } else {
            threshold = capacity;
        }

        init();  // Give subclass a chance to do its thing.

        // Read the keys and values, and put the mappings in the MyHashMap
        for (int i = 0; i < mappings; i++) {
            K key = (K) s.readObject();
            V value = (V) s.readObject();
            putForCreate(key, value);
        }
    }

    // These methods are used when serializing HashSets
    int   capacity()     { return table.length; }
    float loadFactor()   { return loadFactor;   }
}
