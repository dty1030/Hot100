import java.util.ArrayList;
import java.util.List;

public class canFinish {
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) {
            graph.add(new ArrayList<>());

        }
        //   ↑外层List：下标 = 节点编号
        //          ↑内层List：这个节点指向哪些节点
        int[] state = new int[numCourses];
        // === 2. 把 prerequisites 填进邻接表 ===
        for (int[] p : prerequisites) {
            // 在 p[1] 的邻居列表里加入 p[0]
            // ↑ 这一行你来填
            graph.get(p[1]).add(p[0]);
        }

        for (int i = 0; i < numCourses; i++){
            if (!dfs(i, graph, state))return false;
        }
        return true;


    }
    boolean dfs(int x, List<List<Integer>> graph, int[] state){
        if (state[x] == 1)return false;
        else if (state[x] == 2) {
            return true;
        }
        else {
            state[x] = 1;
            for (int neighbor: graph.get(x)){
                if (!dfs(neighbor, graph, state))return false;
            }
            state[x] = 2;
            return true;
        }
    }

    public static void main(String[] args) {
        canFinish solution = new canFinish();

        // 测试 1：能完成
        System.out.println(solution.canFinish(2, new int[][]{{1, 0}}));
        // 期望：true

        // 测试 2：互相依赖（环）
        System.out.println(solution.canFinish(2, new int[][]{{1, 0}, {0, 1}}));
        // 期望：false

        // 测试 3：多门课，链式依赖
        System.out.println(solution.canFinish(4, new int[][]{{1, 0}, {2, 1}, {3, 2}}));
        // 期望：true

        // 测试 4：复杂依赖（没环）
        System.out.println(solution.canFinish(4, new int[][]{{1, 0}, {2, 0}, {3, 1}, {3, 2}}));
        // 期望：true

        // 测试 5：自己依赖自己
        System.out.println(solution.canFinish(1, new int[][]{{0, 0}}));
        // 期望：false

        // 测试 6：没有任何依赖
        System.out.println(solution.canFinish(3, new int[][]{}));
        // 期望：true
    }

}
