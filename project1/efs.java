import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author <Your Name>
 * @netid <NetID>
 * @email <Email Address>
 */
public class EFS extends Utility{

    public static final int BLOCK_MAC = 256;


    public EFS(Editor e)
    {
        super(e);
        set_username_password();
    }


    /**
     * Return HMAC of message with given key using SHA256
     * HMAC = H(k ^ opad || H((k ^ ipad) || m))
     */
    private static byte[] hmac(byte[] key, byte[] content) throws Exception {
        // XOR key
        byte[] opad = new byte[key.length];
        byte[] ipad = new byte[key.length];
        for (int i = 0; i < key.length; i++) {
            opad[i] = (byte) (key[i] ^ 0x5c);
            ipad[i] = (byte) (key[i] ^ 0x36);
        }

        // hash (key ^ ipad) || message
        ByteArrayOutputStream toHash = new ByteArrayOutputStream( );
        toHash.write(ipad);
        toHash.write(content);
        byte[] hashed = hash_SHA256(toHash.toByteArray());

        // hash (key ^ opad) concatenated to first hash
        toHash.reset();
        toHash.write(opad);
        toHash.write(hashed);
        return hash_SHA256(toHash.toByteArray());
    }

    /**
     * Convert bytes to hex
     * https://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal
     */
    private String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

    /**
     * Convert hex string to byte array
     * https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
     */
    private byte[] unhex(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    public String pad_metadata(String s,int number) throws Exception {
        while(s.length()<=number){
            s += '\0';
        }
        return s;
    }

    /**
     * Steps to consider... <p>
     *  - add padded username and password salt to header <p>
     *  - add password hash and file length to secret data <p>
     *  - AES encrypt padded secret data <p>
     *  - add header and encrypted secret data to metadata <p>
     *  - compute HMAC for integrity check of metadata <p>
     *  - add metadata and HMAC to metadata file block <p>
     */
    @Override
    public void create(String file_name, String user_name, String password) throws Exception {
        dir = new File(file_name);

        dir.mkdirs();
        File meta = new File(dir, "0"); // metadata in file 0
        String toWrite = "";

        // HEADER
        // (username + password_salt)
        toWrite += username + "\n";//
        pad_metadata(toWrite,128);
        byte[] salt = secureRandomNumber(128); // password salt
        String temp = salt.toString();
        toWrite += temp + "\n";
        pad_metadata(toWrite,128);

        // SECRET DATA
        // AES(length + password_hash + encryption_key)
        String secretData = "";
        secretData = "0\n";  // length of the file
        String passSalt = password + temp.substring(0, 128 - password.length());
        secretData += hash_SHA256(passSalt.getBytes()).toString() + "\n"; // password hash
        byte[] key = secureRandomNumber(16);//hmac key
        secretData += key.toString() + "\n";
        byte[] iv = secureRandomNumber(4);//hmac key
        secretData += iv.toString() + "\n";
        pad_metadata(secretData,128);//pad aes key for meta data encrypt
        //encrypt meta data
        byte[] encrypted = encript_AES(secretData.getBytes(), passSalt.substring(0, passSalt.length()/2).getBytes());
        toWrite += encrypted.toString() + "\n";

        //hmac
        toWrite += hmac(key, toWrite.getBytes()).toString() + "\n";
        // padding block 0
        while (toWrite.length() < Config.BLOCK_SIZE) {
            toWrite += '\0';
        }
        save_to_file(toWrite.getBytes(), meta);
    }
    private String[] decryptMeta(String file_name, String password) throws Exception{
        File file = new File(file_name);
        File meta = new File(file, "0");
        String s = byteArray2String(read_from_file(meta));
        String[] strs = s.split("\n");

        String passSalt = password + strs[1].substring(0, 128 - password.length());
        String[] decryptMeta = new String(decript_AES(strs[2].getBytes(StandardCharsets.UTF_8), passSalt.substring(0, passSalt.length()/2).getBytes())).split("\n");

        if (hash_SHA256(passSalt.getBytes()).toString().compareTo(decryptMeta[1]) != 0) {
            throw new PasswordIncorrectException();
        }

        return decryptMeta;
    }
    /**
     * Steps to consider... <p>
     *  - check if metadata file size is valid <p>
     *  - get username from metadata <p>
     */
//    @Override
    public String findUser(String file_name,String password) throws Exception {
        File file = new File(file_name);
        File meta = new File(file, "0");
        String[] strs;
        if (meta.length() == Config.BLOCK_SIZE) {
            String s = byteArray2String(read_from_file(meta));
            strs = s.split("\n");
        } else {
            throw new Exception("meta file length not right ");
        }

        return strs[1];
//        byte[] Block_0_Metadata = read_from_file(meta);
//        if(Block_0_Metadata.length <= Config.BLOCK_SIZE){
//            String s = byteArray2String(Block_0_Metadata);
////            s = String.valueOf(decryptMeta(file_name,password));//function of decryptMeta is String[]
////            String[] strs = s.split("\n");
//            String[] strs = decryptMeta(file_name,password);
//            return strs[1];
//        }else{
//            throw new Exception("metadata:file 0  size not right,wrong in findUser");
//        }
        //need decrypt from meta data??
    }

    /**
     * Steps to consider...:<p>
     *  - get password, salt then AES key <p>     
     *  - decrypt password hash out of encrypted secret data <p>
     *  - check the equality of the two password hash values <p>
     *  - decrypt file length out of encrypted secret data
     */
    @Override
    public int length(String file_name, String password) throws Exception {
        File file = new File(file_name);
        File meta = new File(file, "0");
//        String s = byteArray2String(read_from_file(meta));
//        String[] strs = s.split("\n");
//        return Integer.parseInt(strs[0]);
        String[] s = decryptMeta(file_name,password);//steps finish in decryptMeta function
        String str = s[2];
        String[] strs = str.split("\n");
        return Integer.parseInt(strs[0]);
        //get from decryptMeta
    }

    /**
     * Steps to consider...:<p>
     *  - verify password <p>
     *  - check check if requested starting position and length are valid <p>
     *  - decrypt content data of requested length 
     */
    @Override
    // do we need function to get password ,passwordsalt ,iv, aeskey,count from metadata??
    //maybe can achieve combine decrypt file 0 and use swtich loop
    public byte[] read(String file_name, int starting_position, int len, String password) throws Exception {
        File root = new File(file_name);
        int file_length = length(file_name, password);
        //verify password copy from function decryptMeta()
        File meta = new File(root, "0");
        String s = byteArray2String(read_from_file(meta));
        String[] strs = s.split("\n");

        String passSalt = password + strs[1].substring(0, 128 - password.length());
        String[] decryptMeta = new String(decript_AES(strs[2].getBytes(StandardCharsets.UTF_8), passSalt.substring(0, passSalt.length()/2).getBytes())).split("\n");

        if (hash_SHA256(passSalt.getBytes()).toString().compareTo(decryptMeta[1]) != 0) {
            throw new PasswordIncorrectException();
        }
        //get iv aes key

        //decryption file ???? need iv+count mac aeskey

        need a funcion aesCtrDecrypt()
        do we need count to decrypt or just need key plaintext and iv

        //read decryption string
        // i am not sure how to do
        if (starting_position + len > file_length) {
            throw new Exception("read end position out of bound ,please check");
        }
       // need change root to the decrpt version of file
        int start_block = starting_position / Config.BLOCK_SIZE;

        int end_block = (starting_position + len) / Config.BLOCK_SIZE;

        String toReturn = "";

        for (int i = start_block + 1; i <= end_block + 1; i++) {
            String temp = byteArray2String(read_from_file(new File(root, Integer.toString(i))));
            if (i == end_block + 1) {
                temp = temp.substring(0, starting_position + len - end_block * Config.BLOCK_SIZE);
            }
            if (i == start_block + 1) {
                temp = temp.substring(starting_position - start_block * Config.BLOCK_SIZE);
            }
            toReturn += temp;
        }

        return toReturn.getBytes("UTF-8");
    }
//change meta data
//    public void ChangeMetadata(String file_name,String user_name,String password){
//        decryptMeta(file_name,password);
//        //need to change different part and re-encrypt file 0
//    }
    
    /**
     * Steps to consider...:<p>
	 *	- verify password <p>
     *  - check check if requested starting position and length are valid <p>
     *  - ### main procedure for update the encrypted content ### <p>
     *  - compute new HMAC and update metadata 
     */
    @Override
    public void write(String file_name, int starting_position, byte[] content, String password) throws Exception {
        //verify password
        File file = new File(file_name);
        File meta = new File(file, "0");
        String s = byteArray2String(read_from_file(meta));
        String[] strs = s.split("\n");

        String passSalt = password + strs[1].substring(0, 128 - password.length());
        String[] decryptMeta = new String(decript_AES(strs[2].getBytes(StandardCharsets.UTF_8), passSalt.substring(0, passSalt.length()/2).getBytes())).split("\n");

        if (hash_SHA256(passSalt.getBytes()).toString().compareTo(decryptMeta[1]) != 0) {
            throw new PasswordIncorrectException();
        }
        //check start position

        //get original file length
        int length = length(file_name, password);
        int con_length = content.length;
        //check start and end position valid
        if(starting_position>length || starting_position+con_length>length){
            throw new Exception();
        }
        //get aes-ctr iv key counter
         byte[] iv = decryptMeta[3].getBytes(StandardCharsets.UTF_8);
         byte[] key = passSalt.substring(0, passSalt.length()/2).getBytes();
//         aesCtr_decrypt(file_name,password,key,iv,);

    //change file;need one step in loop to decrypt block

    //compute new hmac
    //update metadata
    }

    /**
     * Steps to consider...:<p>
  	 *  - verify password <p>
     *  - check the equality of the computed and stored HMAC values for metadata and physical file blocks<p>
     */
    @Override
    public boolean check_integrity(String file_name, String password) throws Exception {
    	return true;
  }

    /**
     * Steps to consider... <p>
     *  - verify password <p>
     *  - truncate the content after the specified length <p>
     *  - re-pad, update metadata and HMAC <p>
     */
    @Override
    public void cut(String file_name, int length, String password) throws Exception {
    }

    //aes-ctr encrypt
    private String[] aesCtrEncrypt (byte[] plainText ,byte[] key, byte[] iv) throws IOException {
        byte counter = 0;
//        byte[] plainText;

        ByteArrayOutputStream input = new ByteArrayOutputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for(counter = 0;counter<cipherText.length/256;counter++){
                  input.write(iv);
                  input.write(ByteBuffer.allocate(1).put(counter));
                  //here have problem how to xor byte[]
                  cipherText = output.write(input^plainText);
                  //aes from Utility file
        }
        return cipherText;

        //problem is need to XOR each byte in two byte array
        // 1 counter+iv
        // 2 plaintext
        //but what is length of aes ??
        // 128 byte? do we need 128 byte ivs or arbitrary length iv

    }
    public byte[] getaeskey(String file_name, String password) throws Exception {
//        File file = new File(file_name);
//        File meta = new File(file, "0");
//        String s = byteArray2String(read_from_file(meta));
//        String[] strs = s.split("\n");
//        return Integer.parseInt(strs[0]);
        String[] meta  = decryptMeta(file_name,password);
        String s = meta.toString();
        String[] strs = s.split("\n");
        hash_key =

    }
    //aes-ctr encrypt and decrypt two functions are same
    private byte[] aesCtr_encrypt (String file_name,String password,byte[] plainText , byte[] iv, int block_number) throws Exception {
//        String[] meta  = decryptMeta(filename,password);
//        byte[] key =
        //get key
        File file = new File(file_name);
        File meta = new File(file, "0");
        String s = byteArray2String(read_from_file(meta));
        String[] strs = s.split("\n");

        String passSalt = password + strs[1].substring(0, 128 - password.length());
        byte[] key = passSalt.substring(0, passSalt.length()/2).getBytes();
        //decrypt aes-ctr
        int counter = block_number;//user block number as counter for ctr
        ByteArrayOutputStream combine_iv_counter = new ByteArrayOutputStream();
        ByteArrayOutputStream cipher_text = new ByteArrayOutputStream();//ciphertext after XOR
        byte[] arr = new byte[4];
        ByteBuffer buffer = ByteBuffer.wrap(arr);
        buffer.putInt(block_number);
        combine_iv_counter.write(iv);//iv byte[4]
        combine_iv_counter.write(buffer.array());//counter byte[4],file length upper bound 2^32 for aes ctr to encrypt
        //iv+counter = 8 byte
        //first encrypt iv+counter
        byte[] encrypt_iv_counter = encript_AES(combine_iv_counter.toByteArray(),key);
        for (int i=0;i<encrypt_iv_counter.length;i++){
            cipher_text.write(encrypt_iv_counter[i]^plainText[i]);
        }
        return cipher_text.toByteArray();
    }
    private byte[] aesCtr_decrypt (String file_name,String password,byte[] cipher_text , byte[] iv, int block_number) throws Exception {
//        String[] meta  = decryptMeta(filename,password);
//        byte[] key =
        //get key
        File file = new File(file_name);
        File meta = new File(file, "0");
        String s = byteArray2String(read_from_file(meta));
        String[] strs = s.split("\n");

        String passSalt = password + strs[1].substring(0, 128 - password.length());
        byte[] key = passSalt.substring(0, passSalt.length()/2).getBytes();
        //decrypt aes-ctr
        int counter = block_number;//user block number as counter for ctr
        ByteArrayOutputStream combine_iv_counter = new ByteArrayOutputStream();
        ByteArrayOutputStream plain_text = new ByteArrayOutputStream();//ciphertext after XOR
        byte[] arr = new byte[4];
        ByteBuffer buffer = ByteBuffer.wrap(arr);
        buffer.putInt(block_number);
        combine_iv_counter.write(iv);//iv byte[4]
        combine_iv_counter.write(buffer.array());//counter byte[4],file length upper bound 2^32 for aes ctr to encrypt
        //iv+counter = 8 byte
        //first encrypt iv+counter
        byte[] encrypt_iv_counter = encript_AES(combine_iv_counter.toByteArray(),key);
        for (int i=0;i<encrypt_iv_counter.length;i++){
            plain_text.write(encrypt_iv_counter[i]^cipher_text[i]);
        }
        return plain_text.toByteArray();
    }
  
}
