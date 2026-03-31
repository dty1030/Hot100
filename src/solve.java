public class solve {
    public void solve(char[][] board){
        for (int i = 0; i < board.length; i++){
            for (int j = 0; j < board[i].length; j++){
                if (i == 0 || j == 0 || i+1 >= board.length || j+1 >= board[i].length){
                    helper(board, i, j);
                }

            }
        }
        for (int i = 0; i < board.length; i++){
            for (int j = 0; j < board[i].length; j++){
                if (board[i][j] == 'O'){
                    board[i][j] = 'X';
                }
                if (board[i][j] == 'P'){
                    board[i][j] = 'O';
                }

            }
        }
    }
    //把与某个起点连通的整片 O 都标成安全区域
    void helper(char[][] board, int i, int j){
        if (i < 0 || i >= board.length || j < 0 || j >= board[i].length || board[i][j] != 'O')return;
        //判断是否为在边缘区域

        board[i][j] = 'P';
        helper(board, i+1, j);
        helper(board, i-1, j);
        helper(board, i, j+1);
        helper(board, i, j-1);

    }
}
