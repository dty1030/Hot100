import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class isVaild {
    public boolean isValid(String s){
        int size = s.length();

        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < size; i++){
            if (s.charAt(i) == '(' || s.charAt(i) == '[' || s.charAt(i) == '{')stack.add(s.charAt(i));
            else if (!stack.empty() && stack.peek() == '(' && s.charAt(i)== ')')stack.pop();
            else if (!stack.empty() &&stack.peek() == '{' && s.charAt(i) =='}')stack.pop();
            else if (!stack.empty() &&stack.peek() == '[' && s.charAt(i) == ']')stack.pop();
            else return false;

        }
        if (stack.isEmpty())return true;
        return false;

    }
}
