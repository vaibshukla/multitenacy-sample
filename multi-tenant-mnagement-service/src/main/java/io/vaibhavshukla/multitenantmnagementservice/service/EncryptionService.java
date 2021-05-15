package io.vaibhavshukla.multitenantmnagementservice.service;

public interface EncryptionService {

    String encrypt(String input , String secret , String salt);
}
