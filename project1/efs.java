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
        byte[] key = secureRandomNumber(16);//hmac key for metadata file 0
        secretData += key.toString() + "\n";
        byte[] iv = secureRandomNumber(4);//iv
        secretData += iv.toString() + "\n";
        byte[] key_for_real_file = secureRandomNumber(4);//hmac key for other data file without metadata file 0
        secretData += key_for_real_file.toString() + "\n";
        pad_metadata(secretData,128);//pad aes key for meta data encrypt
        //encrypt meta data
        byte[] encrypted = encript_AES(secretData.getBytes(), passSalt.substring(0, passSalt.length()/2).getBytes());
        toWrite += encrypted.toString() + "\n";

        //hmac for metadata
        toWrite += hmac(key, toWrite.getBytes()).toString() + "\n";
        //hmac for file without file 0
        String initial = "0";
        byte[] initial_hmac_wholefile = initial.getBytes(StandardCharsets.UTF_8);
        toWrite += hmac(key_for_real_file,initial_hmac_wholefile).toString() + "\n";
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
    public String findUser(String file_name) throws Exception {
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
        //get aes-ctr iv key counter
        byte[] iv = decryptMeta[3].getBytes(StandardCharsets.UTF_8);
        byte[] key = passSalt.substring(0, passSalt.length()/2).getBytes();
        //decryption file ???? need iv+count mac aeskey
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
            //decrypt block
            File encrypt_block = new File(root, Integer.toString(i));
            byte[] decrypt_block = aesCtr_decrypt(file_name,password,read_from_file(encrypt_block),iv,i);
            String temp = byteArray2String(decrypt_block);
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
        String str_content = byteArray2String(content);
        String passSalt = password + strs[1].substring(0, 128 - password.length());
        String[] decryptMeta = new String(decript_AES(strs[2].getBytes(StandardCharsets.UTF_8), passSalt.substring(0, passSalt.length()/2).getBytes())).split("\n");

        if (hash_SHA256(passSalt.getBytes()).toString().compareTo(decryptMeta[1]) != 0) {
            throw new PasswordIncorrectException();
        }
        //check start position

        //get original file length
        int length = length(file_name, password);
        int con_length = str_content.length();
        //check start and end position valid
        if(starting_position>length || starting_position+con_length>length||starting_position<0){
            throw new Exception();
        }
        //get aes-ctr iv key counter
         byte[] iv = decryptMeta[3].getBytes(StandardCharsets.UTF_8);
         byte[] key = passSalt.substring(0, passSalt.length()/2).getBytes();
//         aesCtr_decrypt(file_name,password,key,iv,);

    //change file;need one step in loop to decrypt block
        int len = str_content.length();
        int start_block = starting_position / Config.BLOCK_SIZE;
        int end_block = (starting_position + len) / Config.BLOCK_SIZE;
        String whole_file = "";
        for (int i = start_block + 1; i <= end_block + 1; i++) {
            int sp = (i - 1) * Config.BLOCK_SIZE - starting_position;
            int ep = (i) * Config.BLOCK_SIZE - starting_position;
            String prefix = "";
            String postfix = "";
            //prefix
            if (i == start_block + 1 && starting_position != start_block * Config.BLOCK_SIZE) {
                File encrypt_block = new File(file, Integer.toString(i));
                byte[] decrypt_block = aesCtr_decrypt(file_name,password,read_from_file(encrypt_block),iv,i);
                prefix = byteArray2String(decrypt_block);
                prefix = prefix.substring(0, starting_position - start_block * Config.BLOCK_SIZE);
                sp = Math.max(sp, 0);
            }
            //postfix
            if (i == end_block + 1) {
                File end = new File(file, Integer.toString(i));
                if (end.exists()) {
                    File encrypt_block = new File(file, Integer.toString(i));
                    byte[] decrypt_block = aesCtr_decrypt(file_name,password,read_from_file(encrypt_block),iv,i);
                    postfix = byteArray2String(decrypt_block);
                    if (postfix.length() > starting_position + len - end_block * Config.BLOCK_SIZE) {
                        postfix = postfix.substring(starting_position + len - end_block * Config.BLOCK_SIZE);
                    } else {
                        postfix = "";
                    }
                }
                ep = Math.min(ep, len);
            }

            String temp = prefix + str_content.substring(sp, ep) + postfix;
            whole_file+=temp;
            byte[] byte_temp = temp.getBytes(StandardCharsets.UTF_8);
            String toWrite = aesCtr_encrypt(file_name,password,byte_temp,iv,i).toString();
            //padding
            while (toWrite.length() < Config.BLOCK_SIZE) {
                toWrite += '\0';
            }
            save_to_file(toWrite.getBytes(), new File(file, Integer.toString(i)));
        }

    //compute whole file hmac
        //update whole file hmac
        byte[] content_after_modify = whole_file.getBytes(StandardCharsets.UTF_8);
        byte[] whole_file_hmac_key = decryptMeta[4].getBytes(StandardCharsets.UTF_8);
        byte[] new_whole_file_hamc = hmac(whole_file_hmac_key,content_after_modify);
        String new_hmac_value = new_whole_file_hamc.toString()+"\n";
        strs[5] = new_hmac_value;
//            String secret_data_in_meta =
        String toWrite_hmac = "";
        for (String t : strs) {
            toWrite_hmac += t + "\n";
        }
        while (toWrite_hmac.length() < Config.BLOCK_SIZE) {
            toWrite_hmac += '\0';
        }
        save_to_file(toWrite_hmac.getBytes(), meta);
    //compute new hmac
    //update metadata
        if (content.length + starting_position > length(file_name, password)) {
            File initial_meta = new File(file, "0");
            String initial_meta_string = byteArray2String(read_from_file(initial_meta));
            byte[] cipher_text_meta = initial_meta_string.getBytes(StandardCharsets.UTF_8);
            initial_meta_string=decript_AES(cipher_text_meta,key).toString();
            String[] initial_meta_strs = initial_meta_string.split("\n");
            initial_meta_strs[0] = Integer.toString(content.length + starting_position);//change length in meta data
            String toWrite_1 = "";
            for (String t_1 : initial_meta_strs) {
                toWrite_1 += t_1 + "\n";
            }
            while (toWrite_1.length() < Config.BLOCK_SIZE) {
                toWrite_1 += '\0';
            }
            save_to_file(toWrite_1.getBytes(), new File(file, "0"));
            //get key for meta data file 0 hmac
            String passSalt_hmac = password + initial_meta_strs[1].substring(0, 128 - password.length());
            //decrypt secure data in meta data
            String[] decryptMeta_hmac = new String(decript_AES(initial_meta_strs[2].getBytes(StandardCharsets.UTF_8), passSalt_hmac.substring(0, passSalt_hmac.length()/2).getBytes())).split("\n");
            byte[] hmac_key_file0 = decryptMeta_hmac[2].getBytes(StandardCharsets.UTF_8);
//            byte[] hmac_key_except_file0 = decryptMeta_hmac[4].getBytes(StandardCharsets.UTF_8);

//            byte[] new_except_file0_hmac = hmac(hmac_key_except_file0, content_after_modify);
            byte[] new_file0_hmac = hmac(hmac_key_file0,toWrite_1.getBytes(StandardCharsets.UTF_8));
            initial_meta_strs[3] = new_file0_hmac.toString();
            String toWrite_2 = "";
            for (String t : initial_meta_strs) {
                toWrite_2 += t + "\n";
            }
            while (toWrite_2.length() < Config.BLOCK_SIZE) {
                toWrite_2 += '\0';
            }
            //

            save_to_file(toWrite_2.getBytes(), new File(file, "0"));

        }
        //update hmac for whole file except file 0
    }

    /**
     * Steps to consider...:<p>
  	 *  - verify password <p>
     *  - check the equality of the computed and stored HMAC values for metadata and physical file blocks<p>
     */
    @Override
    public boolean check_integrity(String file_name, String password) throws Exception {
    	boolean check_sign=true;
        File file = new File(file_name);
        File meta = new File(file, "0");
        String s = byteArray2String(read_from_file(meta));
        String[] strs = s.split("\n");
//        String str_content = byteArray2String(content);
        String passSalt = password + strs[1].substring(0, 128 - password.length());
        String[] decryptMeta = new String(decript_AES(strs[2].getBytes(StandardCharsets.UTF_8), passSalt.substring(0, passSalt.length()/2).getBytes())).split("\n");

        if (hash_SHA256(passSalt.getBytes()).toString().compareTo(decryptMeta[1]) != 0) {
            throw new PasswordIncorrectException();
        }
//        int file_length = length(file_name, password);
        //check file 0 integrity
        //get hmac key for meta data
//        String[] decryptMeta = decryptMeta(file_name,password);
        byte[] hmac_file0_key = decryptMeta[2].getBytes(StandardCharsets.UTF_8);
        //get  file 0(meta data) content
        String get_metadata_encrpty = "";
        for (String t : strs) {
            get_metadata_encrpty += t + "\n";
        }
        while (get_metadata_encrpty.length() < Config.BLOCK_SIZE) {
            get_metadata_encrpty += '\0';
        }
        byte[] file0_content =  get_metadata_encrpty.getBytes(StandardCharsets.UTF_8);
        //calculate content mac
        byte[] file0_hmac_value =  hmac(hmac_file0_key,file0_content);
        //check if there are equal
        if(strs[4]!=file0_hmac_value.toString()){
            check_sign=false;
        }
        //check whole file without file 0 integrity
        //get hmac key
//         String[] decryptMeta = decryptMeta(file_name,password);
         byte[] hmac_wholefile_key = decryptMeta[4].getBytes(StandardCharsets.UTF_8);
         //get iv
        byte[] iv = decryptMeta[3].getBytes(StandardCharsets.UTF_8);
        //get decrypt content
        int content_length = length(file_name,password);
        byte[] wholefile_content=read(file_name,0,content_length,password);
        String content = wholefile_content.toString();
        int i=0;
        int j=1024;
        int temp=content_length/Config.BLOCK_SIZE+1;
        String block;
        String content_plaintext="";
        //not sure if content "/0" the hmac will change or not?so i re-padding "/0" like create function do
        for(int k=1;k<=(content_length/Config.BLOCK_SIZE+1);k++){
            File encrypt_block = new File(file, Integer.toString(k));
            byte[] decrypt_block = aesCtr_decrypt(file_name,password,read_from_file(encrypt_block),iv,k);
            block = byteArray2String(decrypt_block);
            content_plaintext +=block;
        }
        //calculate content mac
        byte[] content_get_from_plaintext = content_plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] whole_file_hmac_key = decryptMeta[4].getBytes(StandardCharsets.UTF_8);
        byte[] whole_file_hamc_value = hmac(whole_file_hmac_key,content_get_from_plaintext);
        String calculate_hmac_value = whole_file_hamc_value.toString()+"\n";
        //check if there are equal
        if(calculate_hmac_value!=strs[5]){
            check_sign=false;
        }


        return check_sign;
  }

    /**
     * Steps to consider... <p>
     *  - verify password <p>
     *  - truncate the content after the specified length <p>
     *  - re-pad, update metadata and HMAC <p>
     */
    @Override
    public void cut(String file_name, int len, String password) throws Exception {
       //get file
        File file = new File(file_name);
        File meta = new File(file, "0");
        String s = byteArray2String(read_from_file(meta));
        String[] strs = s.split("\n");
//        String str_content = byteArray2String(content);
        String passSalt = password + strs[1].substring(0, 128 - password.length());
        String[] decryptMeta = new String(decript_AES(strs[2].getBytes(StandardCharsets.UTF_8), passSalt.substring(0, passSalt.length()/2).getBytes())).split("\n");

        if (hash_SHA256(passSalt.getBytes()).toString().compareTo(decryptMeta[1]) != 0) {
            throw new PasswordIncorrectException();
        }
        //iv and key
        byte[] iv = decryptMeta[3].getBytes(StandardCharsets.UTF_8);
        byte[] key = passSalt.substring(0, passSalt.length()/2).getBytes();
//        File root = new File(file_name);
        int file_length = length(file_name, password);

        if (len > file_length) {
            throw new Exception();
        }
        // copy last block
        int end_block = (len) / Config.BLOCK_SIZE;
        File block_ciphertext = new File(file, Integer.toString(end_block + 1));
        byte[] decrypt_block = aesCtr_decrypt(file_name,password,read_from_file(block_ciphertext),iv,end_block + 1);
        String str = byteArray2String(decrypt_block);
        str = str.substring(0, len - end_block * Config.BLOCK_SIZE);
        while (str.length() < Config.BLOCK_SIZE) {
            str += '\0';
        }
        byte[] encrypt_block = aesCtr_encrypt(file_name,password,str.getBytes(StandardCharsets.UTF_8),iv,end_block + 1);
        save_to_file(encrypt_block,block_ciphertext);
        //delete after len
        int cur = end_block + 2;
        File file_delete = new File(file, Integer.toString(cur));
        while (file_delete.exists()) {
            file_delete.delete();
            cur++;
        }
        //update meta data
        //update length
        File initial_meta = new File(file, "0");
        String initial_meta_string = byteArray2String(read_from_file(initial_meta));
        byte[] cipher_text_meta = initial_meta_string.getBytes(StandardCharsets.UTF_8);
        initial_meta_string=decript_AES(cipher_text_meta,key).toString();
        String[] initial_meta_strs = initial_meta_string.split("\n");
        initial_meta_strs[0] = Integer.toString(len);//change length in meta data
        //updata whole file hmac
        byte[] hmac_wholefile_key = decryptMeta[4].getBytes(StandardCharsets.UTF_8);
//        byte[] iv = decryptMeta[3].getBytes(StandardCharsets.UTF_8);
        byte[] wholefile_content=read(file_name,0,len,password);
        String content = wholefile_content.toString();
        String block;
        String content_plaintext="";
        //not sure if content "/0" the hmac will change or not?so i re-padding "/0" like create function do
        for(int k=1;k<=(len/Config.BLOCK_SIZE+1);k++){
            File encrypt_block_1 = new File(file, Integer.toString(k));
            byte[] decrypt_block_1 = aesCtr_decrypt(file_name,password,read_from_file(encrypt_block_1),iv,k);
            block = byteArray2String(decrypt_block);
            content_plaintext +=block;
        }
        byte[] content_get_from_plaintext = content_plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] whole_file_hmac_key = decryptMeta[4].getBytes(StandardCharsets.UTF_8);
        byte[] whole_file_hamc_value = hmac(whole_file_hmac_key,content_get_from_plaintext);
        String calculate_hmac_value = whole_file_hamc_value.toString()+"\n";
        strs[5]=calculate_hmac_value;
        //updata meta hmac
        byte[] hmac_file0_key = decryptMeta[2].getBytes(StandardCharsets.UTF_8);
        String get_metadata_encrpty = "";
        for (String t : strs) {
            get_metadata_encrpty += t + "\n";
        }
        while (get_metadata_encrpty.length() < Config.BLOCK_SIZE) {
            get_metadata_encrpty += '\0';
        }
        byte[] file0_content =  get_metadata_encrpty.getBytes(StandardCharsets.UTF_8);
        byte[] file0_hmac_value =  hmac(hmac_file0_key,file0_content);
        strs[4]=file0_hmac_value.toString();
        String toWrite = "";
        for (String t : strs) {
            toWrite += t + "\n";
        }
        while (toWrite.length() < Config.BLOCK_SIZE) {
            toWrite += '\0';
        }
        save_to_file(toWrite.getBytes(), new File(file, "0"));

    }

//    //aes-ctr encrypt
//    private String[] aesCtrEncrypt (byte[] plainText ,byte[] key, byte[] iv) throws IOException {
//        byte counter = 0;
////        byte[] plainText;
//
//        ByteArrayOutputStream input = new ByteArrayOutputStream();
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        for(counter = 0;counter<cipherText.length/256;counter++){
//                  input.write(iv);
//                  input.write(ByteBuffer.allocate(1).put(counter));
//                  //here have problem how to xor byte[]
//                  ciphertext = output.write(input^plainText);
//                  //aes from Utility file
//        }
//        return cipherText;
//
//        //problem is need to XOR each byte in two byte array
//        // 1 counter+iv
//        // 2 plaintext
//        //but what is length of aes ??
//        // 128 byte? do we need 128 byte ivs or arbitrary length iv
//
//    }
//    public byte[] getaeskey(String file_name, String password) throws Exception {
////        File file = new File(file_name);
////        File meta = new File(file, "0");
////        String s = byteArray2String(read_from_file(meta));
////        String[] strs = s.split("\n");
////        return Integer.parseInt(strs[0]);
//        String[] meta  = decryptMeta(file_name,password);
//        String s = meta.toString();
//        String[] strs = s.split("\n");
//        hash_key =
//
//    }
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

