public class multiply {
    public String multiply(String num1, String num2) {

        String res = "";
        int sum = 0;
        int[] result = new int[num1.length() + num2.length()];
        for (int i = 0; i < num1.length(); i++){
            for (int j = 0; j < num2.length(); j++){
                sum = (num1.charAt(i) - '0') * (num2.charAt(j) - '0');
                result[i + j + 1] = sum % 10;
                result[i + j] = sum / 10;


            }
        }
        //result[result.length-1] = 余数?



        return res;
    }
}
