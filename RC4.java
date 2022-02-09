

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
public class RC4 {
    public static void main(String[] args) {
        RC4 rc4 = new RC4();

        String testWord = "aaaaaaa";
        String key1 = "key";

        byte[] pt = testWord.getBytes();

        byte[] key = key1.getBytes();

        byte[] ct = rc4.encrypt(pt, key);
        byte[] dct = rc4.decrypt(ct, key);

        System.out.print(
                "plaintext:" + testWord + "\n" + "key:" + Arrays.toString(key) + "dct:" + Arrays.toString(dct)
        );
    }

    //encrypt
    public byte[] encrypt(byte[] pt, byte[] key) {
        int l = pt.length;
        byte[] s = new byte[256];
//        byte[] cs = new byte[l];//cipher stream
        byte[] ct = new byte[l];


        ksa(s, key);
//        rpga(s, cs, l);

        int i = 0, j = 0, k, t;
//        while (l--) {
//            i = (i + 1) % 255;
//            j = (j + s[i]) % 255;
//            swap(s, i, j);
//            t = (s[i] + s[j]) % 255;
//            k = s[t];
//            ct[c] = (byte) (ct[c] ^ k);
//            c++;
//        }
        for (int c = 0; c < l; c++) {
            i = (i + 1) % 255;
            j = (j + s[i]) % 255;
            swap(s, i, j);
            t = (s[i] + s[j]) % 255;
            k = s[t];
            ct[c] = (byte) (ct[c] ^ k);
        }
        return ct;
    }

    //decrypt same as encyprt
    public byte[] decrypt(byte[] ct, byte[] key) {
        return encrypt(ct, key);
    }

    //ksa
    //
    public void ksa(byte[] s, byte[] key) {
        byte[] iv = new byte[256];
        byte[] t = new byte[256];
        int keylength = key.length;
        SecureRandom srandom=new SecureRandom();
        srandom.nextBytes(iv);
        for (int i = 0; i < 256; i++) {
            s[i] = iv[i];
            t[i] = key[i % keylength];
        }
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + s[i] + t[i]) % 255;
            swap(s, i, j);
        }
    }

    //rpga
//    public void rpga(byte[] s, byte[] cipherStream, int plaintextLength) {
//        int i = 0, j = 0;
//        for (int c = 0; c < plaintextLength; c++) {
//            i = (i + 1) % 255;
//            j = (j + s[i]) % 255;
//            swap(s, i, j);
//            cipherStream[c] = (byte) (s[(s[i] + s[j]) % 256]);
//        }
//    }

    //swap
    public void swap(byte[] s, int i, int j) {
        byte temp = s[i];
        s[i] = s[j];
        s[j] = temp;
    }
}
