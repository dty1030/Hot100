public class numIslands {
    public int numIslands(char[][] grid) {
        int res = 0;
        //先计算右边界

        for (int i = 0; i < grid.length; i++){
            for (int j = 0; j < grid[i].length; j++){
                if (grid[i][j] == '1'){
                    res++;
                    helper(grid, i, j);
                }

            }

        }







        return res;
    }
    void helper(char[][] grid, int i, int j){
        if (i < 0 || j < 0 || i >= grid.length || j >= grid[i].length  )return;
        if (grid[i][j] != '1')return;
        grid[i][j] = '0';
        //上
        helper(grid, i-1, j);
        //下
        helper(grid, i+1, j);
        //左
        helper(grid, i, j-1);
        //右
        helper(grid, i, j+1);
        }


    public static void main(String[] args) {
        char[][] grid = {
                {'1', '1', '0', '0', '0'},
                {'1', '1', '0', '0', '0'},
                {'0', '0', '1', '0', '0'},
                {'0', '0', '0', '1', '1'}
        };

        numIslands solution = new numIslands();
        int ans = solution.numIslands(grid);

        System.out.println(ans); // 预期输出: 3
    }

    }





