package com.ecoinsight.bdsp.asd.crypto;

public interface FileCryptoKeys {
    public static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
    public static final String DATA_ALGORITHM = "AES";
    public static final String SALT_FILENAME = "security_salt.key";
    public static final String ENCRYPTED_FILE_EXTENSION = "enc";
    public static final String MP4_FILE_EXTENSION = "mp4";
    public static final String ENCRYPTED_FILE_SUFFIX = "." + ENCRYPTED_FILE_EXTENSION;
    public static final String ENCRYPTED_FILE_PREFIX = "." + MP4_FILE_EXTENSION;
    public static final String SECURITY_PATH="SECURITY_PATH";
    
}
