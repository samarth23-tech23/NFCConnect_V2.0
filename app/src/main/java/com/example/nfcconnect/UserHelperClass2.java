package com.example.nfcconnect;

public class UserHelperClass2{
    String userName, nfcPassword,key;

    public UserHelperClass2() {
        this.userName = userName;
    }

    public UserHelperClass2(String userName, String encNFCpass,String KEY) {
        this.userName = userName;
        this.nfcPassword =encNFCpass;
        this.key=KEY;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNfcPassword() {
        return nfcPassword;
    }

    public void setNfcPassword(String encnfcPassword) {
        this.nfcPassword = encnfcPassword;
    }

    public String getKey(){return key;}
    public  void setKey(String key){this.key= key ;}
}

