class Solution {
    // 主函数：负责宏观的地图巡视
    public int numIslands(char[][] grid) {
        // 1. 边界防御：如果网格为空，直接返回 0
        if (grid == null || grid.length == 0) {
            return 0;
        }

        int count = 0; // 记录岛屿数量
        int rows = grid.length;    // 地图的行数
        int cols = grid[0].length; // 地图的列数

        // 2. 开着直升机，双重循环遍历整个网格
        // TODO: 写一个外层 for 循环遍历行 (变量 i 从 0 到 rows-1)
        // TODO: 写一个内层 for 循环遍历列 (变量 j 从 0 到 cols-1)

        // TODO: 判断当前脚下的格子 grid[i][j] 是不是陆地 '1'
        // 如果是 '1'：
        //    a. 发现新岛屿！让 count 加 1
        //    b. 召唤沉岛魔法：调用 sinkIsland(grid, i, j); 把它和相连的陆地全炸沉

        return count; // 巡视完毕，返回总数
    }

    // 辅助函数：沉岛魔法 (这其实就是经典的 DFS 深度优先搜索)
    private void sinkIsland(char[][] grid, int i, int j) {
        // 3. 递归的终止条件（极其重要！不写这个会一直越界死循环）
        // TODO: 判断如果越界了，或者当前格子不是 '1'，就直接 return; 结束当前方向的蔓延。
        // 提示：什么叫越界？
        // i < 0 或者 i >= grid.length 或者 j < 0 或者 j >= grid[0].length

        // 4. 执行沉岛魔法
        // TODO: 把当前位置的陆地炸沉（将 grid[i][j] 赋值为海水 '0'）

        // 5. 派出四支小分队，继续向【上下左右】蔓延
        // TODO: 往上走，调用 sinkIsland(grid, i - 1, j);
        // TODO: 往下走，调用 ...
        // TODO: 往左走，调用 ...
        // TODO: 往右走，调用 ...
    }


    public static void main(String[] args) {
        Solution solution = new Solution();

        // 测试用例 1：应该返回 1 (所有 1 都连在一起)
        char[][] grid1 = {
                {'1', '1', '1', '1', '0'},
                {'1', '1', '0', '1', '0'},
                {'1', '1', '0', '0', '0'},
                {'0', '0', '0', '0', '0'}
        };
        System.out.println("测试用例 1:");
        System.out.println("预期结果: 1");
        System.out.println("实际结果: " + solution.numIslands(grid1));
        System.out.println("------------------------");

        // 测试用例 2：应该返回 3 (有三个独立的岛屿)
        char[][] grid2 = {
                {'1', '1', '0', '0', '0'},
                {'1', '1', '0', '0', '0'},
                {'0', '0', '1', '0', '0'},
                {'0', '0', '0', '1', '1'}
        };
        System.out.println("测试用例 2:");
        System.out.println("预期结果: 3");
        System.out.println("实际结果: " + solution.numIslands(grid2));
        System.out.println("------------------------");
    }
}