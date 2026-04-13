package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model;//package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.commons.codec.digest.DigestUtils;
//import org.bouncycastle.jcajce.provider.digest.SHA3;
//
//import java.security.MessageDigest;
//
//public class ETagGenerator {
//
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    public static String generateETag(Object tenant) throws Exception {
//        // Serialize the object to JSON
//        String jsonString = objectMapper.writeValueAsString(tenant);
//
//        // Create SHA3-256 hash using Apache Commons Codec
//        byte[] hashBytes = DigestUtils.sha256(jsonString.getBytes());       // TODO - update it to sha3-256, Java 17 upgrade is required for this
//        //return Hex.encodeHexString(hashBytes);
//
//
//                //SHA3-256 for Java 8
//        // Create a MessageDigest instance for SHA3-256 from BouncyCastle
//        MessageDigest digest = new SHA3.Digest256();
//
//        // Compute the hash
//        byte[] hash = digest.digest(jsonString.getBytes());
//
//        // Print the hash in hexadecimal format
//        StringBuilder hexString = new StringBuilder();
//        for (byte b : hash) {
//            hexString.append(String.format("%02x", b));
//        }
//
//        System.out.println("SHA3-256 Hash: " + hexString.toString());
//        return hexString.toString();
//
//    }
//
//    public static void main(String[] args) {
//        try {
//            // Sample JSON string
//            String jsonString = "{\"name\": \"Example\", \"value\": 124}";
//
//            // Generate SHA3-256 hash
//            String sha3Hex = generateETag(jsonString);
//
//            // Print the hash
//            System.out.println("SHA3-256 Hash: " + sha3Hex);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
//
