package cniao5.com.cniao5shop.blockchain.config;

/**
 * Created by x on 2018/6/8.
 */

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.concurrent.ExecutionException;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthCoinbase;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

public class Web3jUtil {

    public static Web3j buildHttpClient() {
        return Web3jFactory.build(new HttpService(Configuration.RPC_URL));
    }

    public static String getClientVersion(Web3j web3j) throws InterruptedException, ExecutionException {
        Web3ClientVersion client = web3j
                .web3ClientVersion()
                .sendAsync()
                .get();

        return client.getWeb3ClientVersion();
    }

    public static EthCoinbase getCoinbase(Web3j web3j) throws InterruptedException, ExecutionException {
        return web3j
                .ethCoinbase()
                .sendAsync()
                .get();
    }

    /**
     * Returns the list of addresses owned by this client.
     */
    public static EthAccounts getAccounts(Web3j web3j) throws InterruptedException, ExecutionException {
        return web3j
                .ethAccounts()
                .sendAsync()
                .get();
    }

    /**
     * Returns the balance (in Ether) of the specified account address.
     */
    public static BigDecimal getBalanceEther(Web3j web3j, String address) throws InterruptedException, ExecutionException {
        return weiToEther(getBalanceWei(web3j, address));
    }

    /**
     * Returns the balance (in Wei) of the specified account address.
     */
    public static BigInteger getBalanceWei(Web3j web3j, String address) throws InterruptedException, ExecutionException {
        EthGetBalance balance = web3j
                .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .sendAsync()
                .get();

        return balance.getBalance();
    }

    /**
     * Return the nonce (tx count) for the specified address.
     */
    public static BigInteger getNonce(Web3j web3j, String address) throws InterruptedException, ExecutionException {
        EthGetTransactionCount ethGetTransactionCount =
                web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).sendAsync().get();

        return ethGetTransactionCount.getTransactionCount();
    }

    /**
     * Converts the provided Wei amount (smallest value Unit) to Ethers.
     */
    public static BigDecimal weiToEther(BigInteger wei) {
        return Convert.fromWei(wei.toString(), Convert.Unit.ETHER);
    }

    public static BigInteger etherToWei(BigDecimal ether) {
        return Convert.toWei(ether, Convert.Unit.ETHER).toBigInteger();
    }



    /**
     * Returns the TransactionRecipt for the specified tx hash as an optional.
     */
    public static TransactionReceipt getReceipt(Web3j web3j, String transactionHash)
            throws Exception
    {
        EthGetTransactionReceipt receipt = web3j
                .ethGetTransactionReceipt(transactionHash)
                .sendAsync()
                .get();

        return receipt.getTransactionReceipt();
    }


}