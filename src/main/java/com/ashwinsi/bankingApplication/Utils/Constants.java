package com.ashwinsi.bankingApplication.Utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class Constants {
    static String DEFAULT_USER_EMAIL = "admin@gmail.com";
    static String DEFAULT_USER_PASSWORD = "root";
    static String DEFAULT_USER_NAME = "admin";
    static String DEFAULT_USER_PHONENUMBER = "9444133344";

    static String DEFAULT_ADMIN_EMAIL = "admin@gmail.com";
    static String DEFAULT_ADMIN_PASSWORD = "root";


    //actions performed
    public static final String ACTION_LOGIN = "USER_LOGIN";
    public static final String ACTION_USER_SIGNUP = "USER_SIGNUP";
    public static final String ACTION_INCORRECT_LOGIN = "USER_INCORRECT_PASSWORD";
    public static final String ACTION_START_TRANSACTION = "USER_START_TRANSACTION";
    public static final String ACTION_WITHDRAW = "USER_WITHDRAW";
    public static final String ACTION_DEPOSIT = "USER_DEPOSIT";
    public static final String ACTION_TRANSFER = "USER_TRANSFER";
    public static final String ACTION_CREATE_ACCOUNT = "USER_CREATE_ACCOUNT";
    public static final String ACTION_UPDATE_ACCOUNT_BALANCE = "USER_UPDATE_ACCOUNT_BALANCE";
    public static final String ACTION_GET_TRANSACTION = "USER_GET_ACCOUNT_TRANSACTION";
    public static final String ACTION_GET_ALL_TRANSACTION = "USER_GET_ACCOUNT_TRANSACTION";

    // Threshold and cache expiration constants
    public static final int THRESHOLD_LOGIN = 5;
    public static final int THRESHOLD_START_TRANSACTION = 5;
    public static final int THRESHOLD_WITHDRAW = 5;
    public static final int THRESHOLD_DEPOSIT = 5;
    public static final int THRESHOLD_TRANSFER = 5;
    public static final int THRESHOLD_CREATE_ACCOUNT = 5;
    public static final int THRESHOLD_UPDATE_ACCOUNT_BALANCE = 5;
    public static final int THRESHOLD_GET_TRANSACTION = 5;
    public static final int THRESHOLD_GET_ALL_TRANSACTION = 5;
    public static final int THRESHOLD_INCORRECT_LOGIN = 5;
    public static final int THRESHOLD_USER_SIGNUP = 5;

    public static final int CACHE_EXPIRATION_LOGIN = 5;
    public static final int CACHE_EXPIRATION_START_TRANSACTION = 5;
    public static final int CACHE_EXPIRATION_WITHDRAW = 5;
    public static final int CACHE_EXPIRATION_DEPOSIT = 5;
    public static final int CACHE_EXPIRATION_TRANSFER = 5;
    public static final int CACHE_EXPIRATION_CREATE_ACCOUNT = 5;
    public static final int CACHE_EXPIRATION_UPDATE_ACCOUNT_BALANCE = 5;
    public static final int CACHE_EXPIRATION_GET_TRANSACTION = 5;
    public static final int CACHE_EXPIRATION_GET_ALL_TRANSACTION = 5;
    public static final int CACHE_EXPIRATION_INCORRECT_LOGIN = 5;
    public static final int CACHE_EXPIRATION_USER_SIGNUP = 5;

    public static final int THRESHOLD_NO_USER_ACCOUNT = 5;

    private static final int OTP_ATTEMPT_LOGIN = 3;
    private static final int OTP_ATTEMPT_WITHDRAW = 3;
    private static final int OTP_ATTEMPT_TRANSFER = 3;
    private static final int OTP_ATTEMPT_CREATE_ACCOUNT = 3;
    private static final int OTP_ATTEMPT_USER_SIGNUP = 3;


    // KAFKA CONSTANTS
    public static final String KAFKA_EMAIL_TOPIC = "email-events";


    // Thresholds and cache expiration times for actions
    public static final HashMap<String, Integer[]> ACTION_MAPPING = new HashMap<>();

    public static final HashMap<String, Integer> OTP_ATTEMPTS_MAPPING = new HashMap<>();

    static {
        // ACTION -> [THRESHOLD, TIME FOR EXPIRATION TIME (in mins)]
        initializeActionMap();

        // ACTION ->  OTP_ATTEMPTS
        initializeOtpAttemptMap();
    }


    static void initializeActionMap(){
        ACTION_MAPPING.put(ACTION_LOGIN, new Integer[]{THRESHOLD_LOGIN, CACHE_EXPIRATION_LOGIN});
        ACTION_MAPPING.put(ACTION_START_TRANSACTION, new Integer[]{THRESHOLD_START_TRANSACTION, CACHE_EXPIRATION_START_TRANSACTION});
        ACTION_MAPPING.put(ACTION_WITHDRAW, new Integer[]{THRESHOLD_WITHDRAW, CACHE_EXPIRATION_WITHDRAW});
        ACTION_MAPPING.put(ACTION_DEPOSIT, new Integer[]{THRESHOLD_DEPOSIT, CACHE_EXPIRATION_DEPOSIT});
        ACTION_MAPPING.put(ACTION_TRANSFER, new Integer[]{THRESHOLD_TRANSFER, CACHE_EXPIRATION_TRANSFER});
        ACTION_MAPPING.put(ACTION_CREATE_ACCOUNT, new Integer[]{THRESHOLD_CREATE_ACCOUNT, CACHE_EXPIRATION_CREATE_ACCOUNT});
        ACTION_MAPPING.put(ACTION_UPDATE_ACCOUNT_BALANCE, new Integer[]{THRESHOLD_UPDATE_ACCOUNT_BALANCE, CACHE_EXPIRATION_UPDATE_ACCOUNT_BALANCE});
        ACTION_MAPPING.put(ACTION_GET_TRANSACTION, new Integer[]{THRESHOLD_GET_TRANSACTION, CACHE_EXPIRATION_GET_TRANSACTION});
        ACTION_MAPPING.put(ACTION_GET_ALL_TRANSACTION, new Integer[]{THRESHOLD_GET_ALL_TRANSACTION, CACHE_EXPIRATION_GET_ALL_TRANSACTION});
        ACTION_MAPPING.put(ACTION_INCORRECT_LOGIN, new Integer[]{THRESHOLD_INCORRECT_LOGIN, CACHE_EXPIRATION_INCORRECT_LOGIN});
        ACTION_MAPPING.put(ACTION_USER_SIGNUP, new Integer[]{THRESHOLD_USER_SIGNUP, CACHE_EXPIRATION_USER_SIGNUP});
    }

    static void initializeOtpAttemptMap(){
        OTP_ATTEMPTS_MAPPING.put(ACTION_LOGIN, OTP_ATTEMPT_LOGIN);
        OTP_ATTEMPTS_MAPPING.put(ACTION_WITHDRAW, OTP_ATTEMPT_WITHDRAW);
        OTP_ATTEMPTS_MAPPING.put(ACTION_TRANSFER, OTP_ATTEMPT_TRANSFER);
        OTP_ATTEMPTS_MAPPING.put(ACTION_CREATE_ACCOUNT, OTP_ATTEMPT_CREATE_ACCOUNT);
        OTP_ATTEMPTS_MAPPING.put(ACTION_USER_SIGNUP, OTP_ATTEMPT_USER_SIGNUP);
    }

    public static Integer getOtpAttempts(String action){
        if(!OTP_ATTEMPTS_MAPPING.containsKey(action)){
            System.out.println("MAPPING MISSING");
            return 5;
        }
        return OTP_ATTEMPTS_MAPPING.get(action);
    }

    public static Integer[] getActionMapping(String action){
        if(!ACTION_MAPPING.containsKey(action)){
            System.out.println("MAPPING MISSING");
            return new Integer[]{10, 10};
        }
        return ACTION_MAPPING.get(action);
    }
}
