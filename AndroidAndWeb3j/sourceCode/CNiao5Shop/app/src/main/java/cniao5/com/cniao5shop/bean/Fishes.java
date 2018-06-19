package cniao5.com.cniao5shop.bean;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URI;

import cniao5.com.cniao5shop.Contants;

/**
 * Created by <a href="http://www.cniao5.com">菜鸟窝</a>
 * 一个专业的Android开发在线教育平台
 */
public class Fishes implements Serializable {

    private BigInteger id;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    private Boolean isReady;
    private BigInteger gene;
    private BigInteger momId;
    private BigInteger dadId;
    private BigInteger cooldown;
    private BigInteger generation;
    private BigInteger birthTime;
    private String rare;
    private String state;//状态空闲\挂出出售\等待繁育\繁育冷却

    private String sell_seller;
    private BigInteger sell_price;
    private BigInteger sell_startedAt;

    private String breed_seller;
    private BigInteger breed_price;
    private BigInteger breed_startedAt;

    public void setSell_seller(String sell_seller) {
        this.sell_seller = sell_seller;
    }

    public void setSell_price(BigInteger sell_price) {
        this.sell_price = sell_price;
    }

    public void setSell_startedAt(BigInteger sell_startedAt) {
        this.sell_startedAt = sell_startedAt;
    }

    public void setBreed_seller(String breed_seller) {
        this.breed_seller = breed_seller;
    }

    public void setBreed_price(BigInteger breed_price) {
        this.breed_price = breed_price;
    }

    public void setBreed_startedAt(BigInteger breed_startedAt) {
        this.breed_startedAt = breed_startedAt;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }

    public void setGene(BigInteger gene) {
        this.gene = gene;
    }

    public void setMomId(BigInteger momId) {
        this.momId = momId;
    }

    public void setDadId(BigInteger dadId) {
        this.dadId = dadId;
    }

    public void setCooldown(BigInteger cooldown) {
        this.cooldown = cooldown;
    }

    public void setGeneration(BigInteger generation) {
        this.generation = generation;
    }

    public void setBirthTime(BigInteger birthTime) {
        this.birthTime = birthTime;
    }

    public void setRare(String rare) {
        this.rare = rare;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public String getRare() {
        return rare;
    }

    public String getSell_seller() {
        return sell_seller;
    }

    public BigInteger getSell_price() {
        return sell_price;
    }

    public BigInteger getSell_startedAt() {
        return sell_startedAt;
    }

    public String getBreed_seller() {
        return breed_seller;
    }

    public BigInteger getBreed_price() {
        return breed_price;
    }

    public BigInteger getBreed_startedAt() {
        return breed_startedAt;
    }

    public Boolean getReady() {
        return isReady;
    }

    public BigInteger getGene() {
        return gene;
    }

    public BigInteger getMomId() {
        return momId;
    }

    public BigInteger getDadId() {
        return dadId;
    }

    public BigInteger getCooldown() {
        return cooldown;
    }

    public BigInteger getGeneration() {
        return generation;
    }

    public BigInteger getBirthTime() {
        return birthTime;
    }

//    public String getRare() {
//
//        BufferedReader bufferedReader;
//        StringBuffer stringBuffer=null;
//        try {
//            URL url = new URL(Contants.API.FISH_RARE + getGene());
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("GET");
//            InputStream inputStream = connection.getInputStream();
//            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTf-8"));
//            String sread = null;
//            while ((sread = bufferedReader.readLine()) != null) {
//                stringBuffer.append(sread);
//                stringBuffer.append("\r\n");
//            }
//
////            Log.i("msg", "onClick: " + stringBuffer.toString());
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return stringBuffer.toString();
//
//    }

    //    private Long id;
//    private String name;
//    private String imgUrl;
//    private String description;
//    private Float price;
//
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getImgUrl() {
//        return imgUrl;
//    }
//
//    public void setImgUrl(String imgUrl) {
//        this.imgUrl = imgUrl;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public Float getPrice() {
//        return price;
//    }
//
//    public void setPrice(Float price) {
//        this.price = price;
//    }
}
