package transformer;

import java.util.Arrays;
import java.util.LinkedList;

public class Transformer {

    static final double FLT_MAX = 3.40282e+038; /* max value */
    static final double TO_ZERO = Math.pow(2, -126);

    static String[] bcd = {
            "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001"
    };

    public static String fromDecIntegerToComplement(String s) {
        String sign;
        int[] array = new int[32];
        sign = s.charAt(0) == '-' ? "1" : "0";

        if (s.charAt(0) == '-' || s.charAt(0) == '+')
            s = s.substring(1);

        int acc = 0, fact = 1;
        int len = s.length();
        while (len > 0) {
            acc += (s.charAt(len - 1) - '0') * fact;
            fact *= 10;
            len--;
        }

        int curr = array.length - 1;
        while (acc > 0) {
            if (curr < 1) throw new Error("Too Large");
            array[curr] = acc % 2;
            acc /= 2;
            curr--;
        }

        if (sign.equals("1")) {
            for (int i = 0; i < array.length; i++) {
                array[i] = array[i] == 0 ? 1 : 0;
            }
            int carry = 1, tmp;
            for (int i = array.length - 1; i >= 0; i--) {
                tmp = array[i] + carry;
                array[i] = tmp % 2;
                carry = tmp / 2;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i : array) {
            sb.append((char) ('0' + i));
        }

        return sb.toString();
    }

    public static String fromDecIntegerToBCD(String s) {
        System.out.println(s);
        String sign;
        sign = s.charAt(0) == '-' ? "1101" : "1100";

        if (s.charAt(0) == '-' || s.charAt(0) == '+')
            s = s.substring(1);

        StringBuilder sb = new StringBuilder(sign);
        for (char c : s.toCharArray()) {
            sb.append(bcd[c - '0']);
            if (sb.length() >= 32) break;
        }
        while (sb.length() < 32) sb.insert(4, "0");
        return sb.toString();
    }

    public static String fromDecFractionToFloat(String s, int eLength, int sLength) {

        eLength = 8; sLength = 23;
        double src = Double.parseDouble(s);
        int sign = 0;
        if (src > FLT_MAX) {
            return "+Inf";
        } else if (src < -FLT_MAX) {
            return "-Inf";
        }

        if (src < 0) sign = 1;
        src = Math.abs(src);

        long intNum = (long) src;
        double fracNum = src - intNum;
        System.out.println(intNum + " " + fracNum);

        final int MAX_LEN = 32;
        StringBuilder mainBuilder = new StringBuilder();
        StringBuilder fracBuilder = new StringBuilder();

        // 0
        if (s.equals("0.0") || s.equals("+0.0") || s.equals("-0.0")) {
            for (int i = 0; i < eLength + sLength; i++) {
                mainBuilder.append("0");
            }
            return sign + mainBuilder.toString();
        }

        if (src < TO_ZERO && src > 0) {
            // Fraction part to Binary String
            System.out.println("OK");
            int cnt = 0;
            while (cnt <= 148) {
                fracNum *= 2;
                if (cnt >= 126) fracBuilder.append(fracNum >= 1 ? '1' : '0');
                if (fracNum >= 1) fracNum -= 1;
                cnt++;
            }

            fracBuilder.insert(0, sign);
            fracBuilder.insert(1, "00000000");
            return fracBuilder.toString();
        }

        // Integer part to Binary String
        while (intNum > 0) {
            mainBuilder.insert(0, (char) ((intNum % 2) + '0'));
            intNum /= 2;
        }

        // Fraction part to Binary String
        int cnt = 0;
        while (cnt < MAX_LEN) {
            fracNum *= 2;
            fracBuilder.append(fracNum >= 1 ? '1' : '0');
            if (fracNum >= 1) fracNum -= 1;
            cnt++;
        }

        // Current position of Pointer
        int pointerPosOffset = mainBuilder.length();
        System.out.println("Offset"+pointerPosOffset);
        mainBuilder.append(fracBuilder);
        int beginPosition = 0;
        for (int i = 0; i < mainBuilder.length(); i++) {
            if (mainBuilder.charAt(i) == '1') {
                pointerPosOffset = -(i - pointerPosOffset + 1);
                beginPosition = i + 1;
                break;
            }
        }

        System.out.println(mainBuilder);
        System.out.println(beginPosition + " " + pointerPosOffset);
        char[] sigd = new char[sLength];
        for (int i = 0; i < sLength; i++) sigd[i] = '0';
        for (int i = 0; i < sLength && i + beginPosition < mainBuilder.length(); i++) {
            sigd[i] = mainBuilder.charAt(i + beginPosition);
        }

        char[] expd = new char[eLength];
        int offset = (int) Math.pow(2.0, eLength - 1) - 1;
        if (pointerPosOffset < -offset + 1) {

        } else if (pointerPosOffset > offset) {

        }
        for (int i = 0; i < eLength; i++) expd[i] = '0';
        if (pointerPosOffset > 0) {
            pointerPosOffset--;
            expd[0] = '1';
            int tmp = eLength - 1;
            while (pointerPosOffset > 0) {
                System.out.println(tmp);
                expd[tmp] = pointerPosOffset % 2 == 0 ? '0' : '1';
                pointerPosOffset /= 2;
                tmp--;
            }
        } else {
            pointerPosOffset = -pointerPosOffset;
            int tmp = eLength - 1;
            while (pointerPosOffset > 0) {
                System.out.println(tmp);
                if (tmp < 0) return "madebi";
                expd[tmp] = pointerPosOffset % 2 == 0 ? '0' : '1';
                pointerPosOffset /= 2;
                tmp--;
            }
            for (int i = 1; i < eLength; i++) {
                expd[i] = expd[i] == '0' ? '1' : '0';
            }
        }

        StringBuilder res = new StringBuilder();
        res.append(sign);
        for (char e : expd) {
            res.append(e);
        }
        for (char g : sigd) res.append(g);
        return res.toString();
    }

    public static String fromBinFloatToDec(String s, int eLength, int sLength) {
        if (s.equals("11111111100000000000000000000000")) return "-Inf";
        if (s.equals("01111111100000000000000000000000")) return "+Inf";

        int sign = s.charAt(0) - '0';
        String expd = s.substring(1, 1+eLength);
        String sigd = s.substring(1+eLength, 1+eLength+sLength);
        LinkedList<Integer> intList = new LinkedList<>();
        LinkedList<Integer> fraList = new LinkedList<>();

        int expo = 0;
        for (int i = 0; i < eLength; i++) {
            expo *= 2;
            expo += expd.charAt(i) - '0';
            System.out.println(expo);
        }

        if (expo > 0) {
            expo -= Math.pow(2, eLength - 1) - 1;
            System.out.println(expo);
            for (char i : sigd.toCharArray()) {
                fraList.add(i - '0');
            }
            intList.add(1);

            while (expo > 0) {
                intList.push(fraList.pop());
                expo--;
            }

            if (expo < 0) {
                intList.pop();
                fraList.push(1);
                expo++;
                while (expo < 0) {
                    fraList.push(0);
                    expo++;
                }
            }

            Long resInt = 0l;
            while (!intList.isEmpty()) {
                resInt *= 2;
                resInt += intList.removeLast();
            }

            double resFrac = 0.0;
            double fact = 1.0;
            System.out.println(fraList);
            while (!fraList.isEmpty()) {
                fact /= 2.0;
                resFrac += (double) fraList.pop() * fact;
            }

            resFrac += resInt;
            String resFracStr = Double.toString(resFrac);


            return (sign == 1 ? "-" : "") + resFracStr;
        } else {
            return "0.0";
        }
    }

    public static String fromComplementToInteger(String s) {
        System.out.println("s" + s);
        int sign = s.charAt(0) - '0';
        char[] digArray = s.substring(1).toCharArray();

        Long res = 0l;
        if (sign == 1) {
            for (int i = 0; i < digArray.length; i++) {
                res *= 2;
                digArray[i] = digArray[i] == '1' ? '0' : '1';
                res += digArray[i] - '0';
            }
            res++;
            res = -res;
        } else {
            for (int i = 0; i < digArray.length; i++) {
                res *= 2;
                res += digArray[i] - '0';
            }
        }

        return Long.toString(res);
    }

    public static String fromBCDToInteger(String s) {
        int idx = 4;
        StringBuilder sb = new StringBuilder();
        String sign = s.substring(0, idx);
        if (sign.equals("1101")) sb.append("-");

        boolean flag = true;
        int tmp;
        while (idx < s.length()) {
            tmp = Arrays.binarySearch(bcd, s.substring(idx, idx+4));
            idx += 4;
            if (flag && tmp == 0) continue;
            else if (flag) flag = false;
            sb.append(tmp);
        }

        if (flag) sb.append("0");

        return sb.toString();
    }

    public static String expToDec(String s) {
        char sign = '+';
        StringBuilder sb = new StringBuilder();
        if (s.charAt(0) == '-' || s.charAt(0) == '+') {
            sign = s.charAt(0);
            s = s.substring(1);
        }

        int expPos;
        int exp = 0;
        for (expPos = 0; expPos < s.length(); expPos++) {
            if (s.charAt(expPos) == 'E') {
                exp = Integer.parseInt(s.substring(expPos + 1));
                break;
            }
        }

        if (exp < 0) {
            for (int i = 0; i < -exp; i++) {
                sb.append('0');
            }

            for (char c : s.toCharArray()) {
                if (c == 'E') break;
                if (c <= '9' && c >= '0') sb.append(c);
            }

            sb.insert(1, '.');
        } else {
            for (char c : s.toCharArray()) {
                if (c == 'E') break;
                if (c <= '9' && c >= '0') sb.append(c);
            }
            int tmp = exp - sb.length() + 1;
            for (int i = 0; i < tmp; i++) {
                sb.append('0');
            }
            sb.insert(exp + 1, '.');
        }

        return sign + sb.toString();
    }

    /**
     * Integer to binaryString
     *
     * @param numStr to be converted
     * @return result
     */
    public String intToBinary(String numStr) {
        //TODO:
//        return numStr;
        return fromDecIntegerToComplement(numStr);
    }

    /**
     * BinaryString to Integer
     *
     * @param binStr : Binary string in 2's complement
     * @return :result
     */
    public String binaryToInt(String binStr) {
        //TODO:
//        return binStr;
        return fromComplementToInteger(binStr);
    }

    /**
     * Float true value to binaryString
     * @param floatStr : The string of the float true value
     * */
    public String floatToBinary(String floatStr) {
        //TODO:
//        return floatStr;
        return fromDecFractionToFloat(floatStr, 8, 23);
    }

    /**
     * Binary code to its float true value
     * */
    public String binaryToFloat(String binStr) {
        //TODO:
//        return binStr;
        return fromBinFloatToDec(binStr, 8, 23);
    }

    /**
     * The decimal number to its NBCD code
     * */
    public String decimalToNBCD(String decimal) {
        //TODO:
//        return decimal;
        return fromDecIntegerToBCD(decimal);
    }

    /**
     * NBCD code to its decimal number
     * */
    public String NBCDToDecimal(String NBCDStr) {
        //TODO:
//        return NBCDStr;
        return fromBCDToInteger(NBCDStr);
    }

    public static void main(String[] args) {
//        System.out.println(fr);
        System.out.println(fromBinFloatToDec("10110101110100010000000000000000", 8,23));
    }

}
