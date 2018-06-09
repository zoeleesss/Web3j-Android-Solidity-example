package com.cyrptokindom.fish.fishdapp.util;

import android.provider.UserDictionary;

import com.cyrptokindom.fish.fishdapp.util.config.Configuration;
import com.cyrptokindom.fish.fishdapp.util.config.Web3jUtil;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * Created by x on 2018/6/8.
 */

public class Wallet {

    private static final Wallet INSTANCE = new Wallet();
    
    private static Credentials credentials;


    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * 
     * @param password  password user once set
     * @param filename  including filepath+filename!!!!
     */
    private static void useWallet(String password,String filename){
        try {


            // temporary  use only
            if (filename.equals("")){
                tempImportWallet("123456","");
            }
            else {
                credentials = WalletUtils.loadCredentials(password, filename);
            }



        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
    }

    // temporary  use only
    private static void tempImportWallet(String password,String privateKey){

        String pk = "";
        if (privateKey.equals("")){
            //pk = "65e080f727d9ddca08bff41f57283fc7d5e032bb5af8de963dade6a6caaa1ec4";

            pk = "d5655f14e600fde1ea0078b61c8a5cacd7e058d9921a5e5bc297652dff2d9ade";
        }else {
            pk = privateKey;
        }
        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(pk,16));
        String pri = Numeric.toHexStringWithPrefix(ecKeyPair.getPrivateKey());
        credentials = Credentials.create(ecKeyPair);
        String address = credentials.getAddress();
        String pub = Numeric.toHexStringWithPrefix(ecKeyPair.getPublicKey());

        String filepath = "/Users/x/Desktop";


        try {
            String filename = WalletUtils.generateWalletFile(password,ecKeyPair,new File(filepath),true);

            System.out.println("filename: "+filename);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }


        System.out.println("original private key: "+pk);
        System.out.println("private key: "+pri+"\npublic key: "+pub+"\naddress: "+address);


    }


    private void importWallet(String password,String privateKey){

        String pk = "";
        if (privateKey.equals("")){
            pk = "65e080f727d9ddca08bff41f57283fc7d5e032bb5af8de963dade6a6caaa1ec4";
        }else {
            pk = privateKey;
        }
        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(pk,16));
        String pri = Numeric.toHexStringWithPrefix(ecKeyPair.getPrivateKey());
        credentials = Credentials.create(ecKeyPair);
        String address = credentials.getAddress();
        String pub = Numeric.toHexStringWithPrefix(ecKeyPair.getPublicKey());
        
        String filepath = "/Users/x/Desktop";


        try {
            String filename = WalletUtils.generateWalletFile(password,ecKeyPair,new File(filepath),true);

            System.out.println("filename: "+filename);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }


        System.out.println("original private key: "+pk);
        System.out.println("private key: "+pri+"\npublic key: "+pub+"\naddress: "+address);


    }


    public void createWallet(String password){


        try {
            
            String filepath = "/Users/x/Desktop";
            
            String filename = WalletUtils.generateLightNewWalletFile(password,new File(filepath));
            credentials = WalletUtils.loadCredentials(password,filepath+"/"+filename);
            String address = credentials.getAddress();
            ECKeyPair ecKeyPair = credentials.getEcKeyPair();
            String pri = Numeric.toHexStringWithPrefix(ecKeyPair.getPrivateKey());
            String pub = Numeric.toHexStringWithPrefix(ecKeyPair.getPublicKey());

            System.out.println("filename: "+filename);
            System.out.println("address: "+address);
            System.out.println("private key: "+pri+"\npublic key: "+pub);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }


    private Wallet() {}

    public static Wallet getInstance(String password,String filename) {
        
        if (credentials == null ){
            useWallet(password, filename);
        }

        return INSTANCE;

    }

    public static void main(String[] args) {
         Wallet wallet = getInstance("","");
         wallet.createWallet("123");
    }


}
