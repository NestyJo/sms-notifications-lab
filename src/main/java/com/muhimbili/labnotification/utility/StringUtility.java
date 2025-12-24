package com.muhimbili.labnotification.utility;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Long.parseLong;

@Service
public class StringUtility {

    public String generateRandomId() {
        return (randomStr() + randomStr()).replace("-","");
    }

    public String strUpperCase(String str){
        return str == null ? null : str.toUpperCase(Locale.ROOT);
    }

    public String strLowerCase(String str){
        return str == null ? null : str.toLowerCase(Locale.ROOT);
    }

    public String padding(String str, int len, char chars, String type){
        return strUpperCase(type).equalsIgnoreCase("left") ? StringUtils.leftPad(str, len,chars) : StringUtils.rightPad(str, len,chars);
    }

    public String randomChars() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(0, 3));
    }

    public String substr(Object str, int start, int end) {
        return StringUtils.substring(str.toString(), start, end);
    }

    public String randomStr(){
        return UUID.randomUUID().toString();
    }

    public String randomString(){
        return UUID.randomUUID().toString().replace("-","");
    }

    public String generateId(String prefix) {
        return  (prefix + Long.toHexString(System.currentTimeMillis() + parseLong("1" + ThreadLocalRandom.current().nextInt(0,16)))).toUpperCase();
    }

    public String generateId() {
        return  ("USER" + Long.toHexString(System.currentTimeMillis() + parseLong("1" + ThreadLocalRandom.current().nextInt(0,16)))).toUpperCase();
    }

    public String generateReference() {
        return  (Long.toHexString(System.currentTimeMillis() + parseLong("1" + ThreadLocalRandom.current().nextInt(0,16)))).toUpperCase();
    }

    public String hashString(String content){
        return hashString("MD5", content);
    }

    public String hashString(String hash, String content){
        String encrypted = "";
       switch (strUpperCase(hash)){
           case "MD5":
               encrypted = DigestUtils.md5Hex(content);
               break;
           case "SHA128":
               encrypted = DigestUtils.sha1Hex(content);
               break;
           case "SHA256":
               encrypted = DigestUtils.sha256Hex(content);
               break;
           case "SHA512":
               encrypted = DigestUtils.sha512Hex(content);
               break;
           default:
               encrypted = DigestUtils.md5Hex(content);
               break;
       }
       return encrypted;
    }

    public String generateRandomNumber(int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(0, 10)); // Append a random digit between 0 and 9
        }
        return sb.toString();
    }

    public UUID generateUUID() {
        return UUID.randomUUID();
    }
}
