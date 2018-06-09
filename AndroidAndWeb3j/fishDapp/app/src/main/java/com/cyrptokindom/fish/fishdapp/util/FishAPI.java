package com.cyrptokindom.fish.fishdapp.util;

import com.cyrptokindom.fish.fishdapp.util.config.Configuration;
import com.cyrptokindom.fish.fishdapp.util.config.Web3jUtil;

import jnr.ffi.annotations.In;
import org.web3j.abi.datatypes.Bool;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import okhttp3.*;
import org.web3j.tuples.generated.Tuple10;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.*;
import org.web3j.tx.response.QueuingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * 快速开始
 */
public class FishAPI {

    private static Web3j web3j;

    private static FishContracts_sol_FishCore fishCore;

    private static Credentials credentials;

    private static final FishAPI INSTANCE = new FishAPI();

    public static FishAPI getInstance(){

        if (web3j == null){
            initialize();
        }

        return INSTANCE;
    }

    private FishAPI(){}

    public static void main(String[] args) {


        FishAPI fishAPI = getInstance();

        String s ="";
        //s+="total supply: "+getTotalSupply();
        //s+="\nfish ids of owner: "+getFishesOfOwner(contractOwner);
        //s+="\nfish details of fish id 0: "+getDetailsOfFish(new BigInteger("0"));
        //s+="\non sale fish ids: "+getTokensOfOnSaleFishes().toString();
        //s+="\non breedingsale fish ids: "+getTokensOfBreedingSaleFishes().toString();
        // s+="the owner of fish id 1: "+getOwnerOfAFish(new BigInteger("1"));
        //s+="\nversion: "+version();


//        boolean success = fishAPI.putAFishOnSale(new BigInteger("2"),Web3jUtil.etherToWei(BigDecimal.valueOf(0.05)));
//        s+="trying to put fish id 2 on sale at 0.05 ether\n";
//        s+="on sale status: "+success;
//        List<BigInteger> ids=fishAPI.getTokensOfOnSaleFishes();
//        s+="\non sale fish id: "+ids;
//        s+="\nsale info of fish 2: "+fishAPI.getOnSaleInfoOfAFish(new BigInteger("2"));
//




      /**
       * trial 3  **/
          List<Object> list = fishAPI.getOnSaleInfoOfAFish(new BigInteger("2"));
        BigInteger price = (BigInteger) list.get(1);
        boolean success = fishAPI.buyAFish(new BigInteger("2"),price);
        s+="\nsale info of fish id 2: "+fishAPI.getOnSaleInfoOfAFish(new BigInteger("2"));
        s+="\n0x199912b014df964230d0328384078af454c58e65 trying to buy fish id 2 on sale at 0.05 ether";
        s+="\nbuying status: "+success;
        s+="\nowned fish: "+fishAPI.getFishesOfOwner("0x199912b014df964230d0328384078af454c58e65");
        s+="\ncurrent on sale: "+fishAPI.getTokensOfOnSaleFishes();
        s+="\ntrying to sell fish id 2 on sale at 0.6 ether";
        boolean su = fishAPI.putAFishOnSale(new BigInteger("2"),Web3jUtil.etherToWei(BigDecimal.valueOf(0.6)));
        s+="\non sale status:" +su;
        s+="\ncurrent on sale: "+fishAPI.getTokensOfOnSaleFishes();
        s+="\nsale info of fish id 2: "+fishAPI.getOnSaleInfoOfAFish(new BigInteger("2"));

        System.out.println(s);

    }

    public String version(){
        try {
            Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().sendAsync().get();

            String clientVersion = web3ClientVersion.getWeb3ClientVersion();
            return clientVersion;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private static void initialize(){
        web3j = Web3jUtil.buildHttpClient();
        Wallet wallet = Wallet.getInstance("","");
        credentials = wallet.getCredentials();
        fishCore = FishContracts_sol_FishCore.load(Configuration.contractAddress,web3j,credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
    }


    /**
     *
     * @apiNote return total supply of the fishes
     * @return BigInteger
     */

    public BigInteger getTotalSupply(){
        try {
            BigInteger totalSupply = fishCore.totalSupply().sendAsync().get();
            return totalSupply;
        }
        catch (Exception e){
            System.err.println(" error in getTotalSupply ");
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     *
     * @apiNote return all indexes of the fishes belonged to the owner
     * @param owner String:    address of the owner
     * @return List<BigInteger>
     *
     *
     */


    public List<BigInteger> getFishesOfOwner(String owner){
        try {
            List tokens = fishCore.tokensOfOwner(owner).send();
            return tokens;
        }
        catch (Exception e){
            System.err.println(" error in getFishesOfOwner ");
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @apiNote get detailed info of a fish
     * @param index BigInteger:     array index of fishes list
     * @return List<Object>
     *
     *     index    value       type
     *     0        isReady     Boolean
     *     1        gene        BigInteger
     *     2        momId       BigInteger
     *     3        dadId       BigInteger
     *     4        cooldown    BigInteger
     *     5        generation  BigInteger
     *     6        birthTime   BigInteger
     *
     */

    public List<Object> getDetailsOfFish(BigInteger index){

        try {
            List<Object> details = new ArrayList<>();
            Tuple10 tuple10 = fishCore.getFish(index).sendAsync().get();
            details.add(tuple10.getValue2());
            details.add(tuple10.getValue10());
            details.add(tuple10.getValue7());
            details.add(tuple10.getValue8());
            details.add(tuple10.getValue3());
            details.add(tuple10.getValue9());
            details.add(tuple10.getValue6());
            return details;
        }
        catch (Exception e){
            System.err.println(" error in getDetailsOfFish ");
            e.printStackTrace();
            return null;
        }

    }

    /**
     *
     * @apiNote return the indexes of fishes that are on sale
     * @return List<BigInteger>
     *
     */


    public List<BigInteger> getTokensOfOnSaleFishes(){

        try {
            List<BigInteger> details;
            details = fishCore.onSaleTokens().sendAsync().get();
            return details;
        }
        catch (Exception e){
            System.err.println(" error in getTokensOfOnSaleFishes ");
            e.printStackTrace();
            return null;
        }

    }

    /**
     *
     * @apiNote return the indexes of fishes that are on breeding sale. (waiting for mating)
     * @return List<BigInteger>
     */

    public List<BigInteger> getTokensOfBreedingSaleFishes(){

        try {
            List<BigInteger> details;
            details = fishCore.onBreedingsSaleTokens().sendAsync().get();
            return details;
        }
        catch (Exception e){
            System.err.println(" error in getTokensOfOnBreedingSaleFishes ");
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @apiNote return the detail info of a fish on sale
     * @param index BigInteger:         array index of fish list
     * @return List<Object>
     *
     *     index    value               type            comment                     Unit
     *     0        seller              String          the address of seller
     *     1        price               BigInteger      on sale price               wei (1 ether = 10^18 wei)
     *     2        startedAt           BigInteger      time of putting it on sale  timestamp
     *
     *     example:
     *     [0x80753a86047d27b51d542ab453c24f5ad1401f79, 100000, 1528446231]
     */

    public List<Object> getOnSaleInfoOfAFish(BigInteger index){
        try {
            Tuple3<String, BigInteger, BigInteger> fish = fishCore.tokenIdToSale(index).sendAsync().get();
            List<Object> details = new ArrayList<>();
            details.add(fish.getValue1());
            details.add(fish.getValue2());
            details.add(fish.getValue3());
            return details;
        }
        catch (Exception e){
            System.err.println(" error in getOnSaleInfoOfAFish ");
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @apiNote get owner of a fish
     * @param index BigInteger:     array index of fishes list
     * @return  String
     */

    public String getOwnerOfAFish(BigInteger index){

        try {

            String owner = fishCore.ownerOf(index).sendAsync().get();
            return owner;
        }
        catch (Exception e){
            System.err.println(" error in getOwnerOfAFish ");
            e.printStackTrace();
            return null;
        }
    }







    /***
     *
     *
     *
     *
     *              S       E       T
     *
     *
     *
     *
     *
     */


    public boolean putAFishOnSale(BigInteger index,BigInteger price){


        TransactionReceipt txReceipt = null;
        try {
            txReceipt = fishCore.createSale(index,price).sendAsync().get();
            // get tx hash and tx fees
            String txHash = txReceipt.getTransactionHash();
            BigInteger txFees = txReceipt
                    .getCumulativeGasUsed()
                    .multiply(Configuration.GAS_PRICE);

            System.out.println("hash: " + txHash);
            System.out.println("fees: " + Web3jUtil.weiToEther(txFees));
            return true;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean buyAFish(BigInteger index, BigInteger money){
        TransactionReceipt txReceipt = null;
        try {
            txReceipt = fishCore.buyOwnership(index,money).sendAsync().get();
            // get tx hash and tx fees
            String txHash = txReceipt.getTransactionHash();
            BigInteger txFees = txReceipt
                    .getCumulativeGasUsed()
                    .multiply(Configuration.GAS_PRICE);

            System.out.println("hash: " + txHash);
            System.out.println("fees: " + Web3jUtil.weiToEther(txFees));
            return true;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }


    /***
     *
     *
     *
     *
     *
     *              Wallet    Functions  except import & create & export
     *
     *
     *
     *
     *
     */





    /**
     *
     *
     *
     * @param credentials
     * @param to
     */

    public void sendETH(Credentials credentials,String to) {

        try {
            TransactionReceipt transactionReceipt = Transfer.sendFunds(
                    web3j, credentials, to,
                    BigDecimal.valueOf(1.0), Convert.Unit.ETHER)
                    .send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}
