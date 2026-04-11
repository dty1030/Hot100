import java.util.ArrayList;
import java.util.List;

public class restoreIpAddress {
    public List<String> restoreIpAddresses(String s) {

        List<String> res = new ArrayList<>();
        helper(res, 0, "", 0, s);
        return res;
    }
    void helper(List<String> res, int count, String cur, int index, String s){
        if (count == 4 && index == s.length()) {
            res.add(cur);
            return;
        }
        String segement = s.substring(index, index + 3);
        if (index + 3 <= s.length()) {
            if (Integer.parseInt(segement) <= 255 && Integer.parseInt(segement) >= 0){
                if (segement.length() == 1 || segement.charAt(0) != '0'){
                    helper(res, count + 1, cur + s.substring(index, index + 3) + '.', index + 3, s);
                }
            }
        }
        if (index + 2 <= s.length()) {
            segement = s.substring(index, index + 2);
            if (Integer.parseInt(segement) <= 255 && Integer.parseInt(segement) >= 0){
                if (segement.length() == 1 || segement.charAt(0) != '0'){
                    helper(res, count + 1, cur + s.substring(index, index + 2) + '.', index + 2, s);
                }
            }
        }
        segement = s.substring(index, index + 1);
        if (Integer.parseInt(segement) <= 255 && Integer.parseInt(segement) >= 0){
                helper(res, count + 1, cur + s.substring(index, index + 1), index + 1, s);
        }
    }
}
