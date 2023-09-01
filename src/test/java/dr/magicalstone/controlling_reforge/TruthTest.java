package dr.magicalstone.controlling_reforge;

import java.util.Arrays;

public class TruthTest {
    public static void main(String[] args) {
        associative:
        for (int i = 0; i < 16; i++) {
            for (int v = 0; v < 8; v++) {
                if (((i >>> ((((i >>> ((v & 0b110) >>> 1)) & 0b001) << 1) + (v & 0b001))) & 0b001) != ((i >>> (((v & 0b100) >>> 1) + ((i >>> (v & 0b011)) & 0b001)) & 0b001))) {
                    continue associative;
                }
            }
            System.out.println(Integer.toBinaryString(i + 0b10000).substring(1));
        }
    }
}
