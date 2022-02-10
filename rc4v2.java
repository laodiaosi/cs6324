import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.Math.abs;



public class RC4v2 {
    public static byte[] iv;

    static {
        SecureRandom srandom=new SecureRandom();
        srandom.nextBytes(iv);
    }
    public static void main(String[] args) {
        RC4v2 rc4 = new RC4v2();

        byte[] forDrop = new byte[3072];
        SecureRandom srandom=new SecureRandom();
        srandom.nextBytes(forDrop);

        String testWord = "a";
        String key1 = "keykeykeykeykeykey";

        byte[] pt = testWord.getBytes();
//        int length = (forDrop.length+pt.length);
//        byte[] combinePt = new byte[(forDrop.length+pt.length)];
//        System.arraycopy(forDrop, 0, combinePt, 0, forDrop.length);
//        System.arraycopy(pt, 0, combinePt, forDrop.length+1, combinePt.length);


        byte[] key = key1.getBytes();

        byte[] ct = rc4.encrypt(pt, key);
        byte[] dct = rc4.decrypt(ct, key);
//        byte[] realPtAfterDec = new byte[(combinePt.length-3072)];
//        System.arraycopy(dct, 3072, realPtAfterDec, 0, combinePt.length);
//        realPtAfterDec = Arrays.copyOfRange(dct, 1, 7);
        System.out.println(
                "pt"+ Arrays.toString(pt)+"\n"+ "ct"+Arrays.toString(ct)+"\n"+ "dct"+Arrays.toString(dct)
        );
        System.out.println(
                "plaintext:" + testWord + "\n" + "key:" + Arrays.toString(key) + "dct:" + Arrays.toString(dct)
        );
        if (Arrays.equals(pt,dct)){
            System.out.println(
                    "same"
            );
        }else{
            System.out.println(
                    "not same"
            );
        }
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
            i = (i + 1) % 256;
            j = (j + s[i]) % 256;
            swap(s, i, j);
            t = (s[i] + s[j]) % 256;
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
//        byte[] iv = new byte[32];
        byte[] t = new byte[256];
        int keylength = key.length;
//        int count = 0;
//        while(count<256){
//            SecureRandom srandom=new SecureRandom();
//            int srandom1 = srandom.nextInt(255);
//            if(Arrays.asList(iv).contains(srandom1)){
//
//            }
//        }
//        SecureRandom srandom=new SecureRandom();
//        int srandom1 = srandom.nextInt(255);
//        srandom.nextBytes(iv);
        //randomiv
//        byte[] iv = new byte[256];

//
        for (int i = 0; i < 256; i++) {
            s[i] = (byte) (Math.abs(iv[i%32])%256);
            t[i] = key[i % keylength];
        }
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + s[i] + t[i]) % 256;
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
