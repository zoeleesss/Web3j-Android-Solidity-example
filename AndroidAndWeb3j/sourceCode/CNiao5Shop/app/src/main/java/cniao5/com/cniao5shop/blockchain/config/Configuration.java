package cniao5.com.cniao5shop.blockchain.config;

import java.math.BigInteger;

public class Configuration {

    // see https://www.reddit.com/r/ethereum/comments/5g8ia6/attention_miners_we_recommend_raising_gas_limit/
    public static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);

    // http://ethereum.stackexchange.com/questions/1832/cant-send-transaction-exceeds-block-gas-limit-or-intrinsic-gas-too-low
    public static final BigInteger GAS_LIMIT_ETHER_TX = BigInteger.valueOf(21_000);
    public static final BigInteger GAS_LIMIT_GREETER_TX = BigInteger.valueOf(500_000L);
    public static String RPC_URL = "https://ropsten.infura.io/yCBWSUS7mbDeBBazCvvK";


    public static String contractAddress="0x7c64d7f1d9e74e7f0a4d9a33a6d147cc235ca749";
    public static String contractOwner = "0x80753a86047d27b51d542ab453c24f5ad1401f79";

    public static String[] rares = new String[]{"普通","稀有","卓越","史诗","神话","传说","超神","宇宙之主"};

    public static int getIndexOfRares(String str){
        int index = 0;
        for (String rare:rares){
            if (rare.equals(str)){
                return index;
            }
            index ++;
        }
        return index;
    }

    public static String getColorOfRare(String str){
        String color = "";
        if (str.equals("普通")) {color = "#C4C4C4";}  // 灰白
        else if(str.equals("稀有")) {color = "#32CD32";}  // 绿色
        else if(str.equals("卓越")) {color = "#00B2EE";}  // 浅蓝
        else if(str.equals("史诗")) {color = "#0000FF";} //深蓝
        else if(str.equals("神话")) {color = "#9B30FF";}  // 紫色
        else if(str.equals("传说")) {color = "#EE6AA7";}  // 粉色
        else if(str.equals("超神")) {color = "#EEB422";}  // 金色
        else if(str.equals("宇宙之主")) {color = "#EE0000";} // 红色

        return color;
    }
}
