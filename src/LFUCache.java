import java.util.HashMap;

class LFUCache {

    int capacity;
    HashMap<Integer, Integer> hashMap;
    int cnt;
    int[][] cache;
    int cachePointer;

    public LFUCache(int capacity) {
        this.capacity = capacity;
        hashMap = new HashMap<>(capacity);
        cache = new int[capacity][2];
    }

    public int get(int key) {
        if (hashMap.get(key) == null) return -1;
        int index = -1;
        for (int i = 0; i < cache.length; i++){
            if (cache[i][0] == key){
                index = i;
                break;
            }
        }
        cache[index][1]++;
        //更新一下使用顺序
        //1. 如果上次使用的就是这个key,不变
        if (index == 0)return hashMap.get(key);
        //2. 不是, 更新顺序
        //备份一下

        int[] temp = cache[index];
        //向右一次移动
        for (int x = index; x > 0 ; x--){
            cache[x] = cache[x - 1];
        }
        cache[0] = temp;
        return hashMap.get(key);
    }

    public void put(int key, int value) {

        //----先看key是否已经存在---
        if (hashMap.containsKey(key)){
            //如果key存在, 直接修改hashmap,并且cache[][1]++
            hashMap.replace(key, value);
            //进行cache的使用顺序修改, 并且cache[][1]++;
            int index = 0;
            //得到cache中key的坐标
            for (int i = 0; i < cache.length; i++){
                if (cache[i][0] == key){
                      index = i;
                }
            }
            int[] temp = cache[index];
            //向右一次移动
            for (int x = index; x > 0 ; x--){
                cache[x] = cache[x - 1];
            }
            cache[0] = temp;
            cache[0][1]++;
            return;
        }
        //如果已经满了
        if (hashMap.size() >= capacity) {
            //遍历,寻找cnt最少的key并去掉
            int minCnt = Integer.MAX_VALUE;
            int minIndex = -1;
            int minKey = -1;
            for (int y = 0; y < cache.length; y++){
                if (cache[y][1] <= minCnt) {
                    //循环过后,自动找到最右侧(最久未使用的,如果cnt相同)
                    minCnt = cache[y][1];
                    minIndex = y;
                    minKey = cache[y][0];
                }

            }
            hashMap.remove(minKey);
            //删除完成-----
            //移动cache
            for (int n = minIndex; n > 0; n--){
                cache[n] = cache[n-1];
            }
            //插入新{key, value}
            cache[0] = new int[]{key, 1};
            //插进hashmap
            hashMap.put(key, value);
            return;
        }
        //如果没满-----
        hashMap.put(key, value);
        //向右挪一格
        for (int i = cachePointer; i >= 1; i--){
            cache[i] = cache[i-1];
        }
        cache[0] = new int[]{key, 1};
        cachePointer++;
    }

    public static void main(String[] args) {
        LFUCache cache = new LFUCache(2);

        cache.put(1, 10);
        cache.put(2, 20);

        System.out.println("get(1) = " + cache.get(1));
        System.out.println("get(2) = " + cache.get(2));
        System.out.println("get(3) = " + cache.get(3));

        System.out.println("cache[0][0] = " + cache.cache[0][0]);
        System.out.println("cache[0][1] = " + cache.cache[0][1]);
    }
}