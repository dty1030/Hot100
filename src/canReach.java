import java.util.ArrayList;
import java.util.List;

public class canReach {
    public boolean canReach(int[] arr, int start) {
        int reach = arr[start];
        boolean[] visited = new boolean[arr.length];
        List<Integer> zero = new ArrayList<>();
        for (int i = 0; i < arr[start]; i++){
            if (i + arr[i] > arr.length){
                reach = i - arr[i];
            }
            else if (i - arr[i] < 0){
                reach = i + arr[i];
            }
            else return false;
        }

    }
    void helper(int[] arr, boolean[] visited, int start){
        if (arr[start] == 0)return;
        visited[start] = true;
        if (start + arr[start] < arr.length && !visited[start] ){
            helper(arr, visited, start - arr[start]);
        }
        if (start - arr[start] < 0 && !visited[start]){
            helper(arr, visited, start + arr[start]);
        }

    }
}
