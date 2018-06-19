package cniao5.com.cniao5shop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import cniao5.com.cniao5shop.bean.Fishes;
import cniao5.com.cniao5shop.http.OkHttpHelper;
import cniao5.com.cniao5shop.blockchain.FishAPI;
import cniao5.com.cniao5shop.utils.ToastUtils;
import cniao5.com.cniao5shop.widget.CNiaoToolBar;
import cniao5.com.cniao5shop.widget.TagsLayout;
import dmax.dialog.SpotsDialog;

public class FishDetailActivity_fish extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.webView)
    private WebView mWebView;

    @ViewInject(R.id.webView1)
    private WebView mWebView1;

    @ViewInject(R.id.webView2)
    private WebView mWebView2;

    private TextView momFish;

    private TextView dadFish;


    @ViewInject(R.id.toolbar)
    private CNiaoToolBar mToolBar;

    private Fishes mWare;

    private WebAppInterface mAppInterfce;
    private WebAppInterface1 mAppInterfce1;
    private WebAppInterface2 mAppInterfce2;

    private SpotsDialog mDialog;

    private List<String> tags = new ArrayList<>();

    private OkHttpHelper okHttpHelper = OkHttpHelper.getInstance();

    private final int INIT_MSG = 11123;
    private String momGene = "";
    private String dadGene = "";

    private String momGen = "";
    private String dadGen = "";


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0://挂出出售
                    if(mDialog!=null && mDialog.isShowing()){
                        mDialog.dismiss();
                    }

                    if((Boolean)msg.obj) {
                        mWare.setState("挂出出售");
                        TextView mstate0 = (TextView) findViewById(R.id.state);
                        mstate0.setText(mWare.getState());

                        Button button01 = (Button) findViewById(R.id.sell_fish);
                        Button button02 = (Button) findViewById(R.id.breed_fish);
                        button01.setText("取消出售");
                        button02.setClickable(false);
                        button01.setAlpha((float) 1.0);
                        button02.setAlpha((float) 0.5);

                    }else{
                        ToastUtils.showMsg(getApplicationContext(), "无效的输入");
                    }

                    break;

                case 1://取消出售
                    if(mDialog!=null && mDialog.isShowing()){
                        mDialog.dismiss();
                    }

                    mWare.setState("状态空闲");
                    TextView mstate1 = (TextView) findViewById(R.id.state);
                    mstate1.setText(mWare.getState());

                    Button button11 = (Button) findViewById(R.id.sell_fish);
                    Button button12 = (Button) findViewById(R.id.breed_fish);
                    button11.setText("挂出出售");
                    //button1.setClickable(true);
                    button12.setClickable(true);
                    button11.setAlpha((float)1.0);
                    button12.setAlpha((float)1.0);

                    break;

                case 2://挂出繁育
                    if(mDialog!=null && mDialog.isShowing()){
                        mDialog.dismiss();
                    }
                    if((Boolean)msg.obj) {
                        mWare.setState("等待繁育");
                        TextView mstate2 = (TextView) findViewById(R.id.state);
                        mstate2.setText(mWare.getState());

                        Button button21 = (Button) findViewById(R.id.sell_fish);
                        Button button22 = (Button) findViewById(R.id.breed_fish);
                        button22.setText("取消繁育");
                        button21.setClickable(false);
                        button21.setAlpha((float) 0.5);
                        button22.setAlpha((float) 1.0);

                    }else {
                        ToastUtils.showMsg(getApplicationContext(), "无效的输入");
                    }

                    break;

                case 3://取消繁育
                    if(mDialog!=null && mDialog.isShowing()){
                        mDialog.dismiss();
                    }

                    mWare.setState("状态空闲");
                    TextView mstate3 = (TextView) findViewById(R.id.state);
                    mstate3.setText(mWare.getState());

                    Button button31 = (Button) findViewById(R.id.sell_fish);
                    Button button32 = (Button) findViewById(R.id.breed_fish);
                    button32.setText("挂出繁育");
                    button31.setClickable(true);
                    button32.setClickable(true);
                    button31.setAlpha((float)1.0);
                    button32.setAlpha((float)1.0);

                    break;
                case INIT_MSG:

                    TagsLayout imageViewGroup = (TagsLayout) findViewById(R.id.image_layout);
                    ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    String[] tags={
                            "#"+mWare.getId().toString(),
                            mWare.getRare(),
                            "第"+mWare.getGeneration().toString()+"代",
                            mWare.getCooldown().toString()+"冷却",
                            FishAPI.timeStamp2Date(mWare.getBirthTime().toString())+"出生"
                    };
                    for (int i = 0; i < tags.length; i++) {
                        TextView textView = new TextView(FishDetailActivity_fish.this);
                        textView.setText(tags[i]);
                        textView.setTextColor(getResources().getColor(R.color.white));
                        textView.setBackgroundResource(R.drawable.selector_textview);
                        imageViewGroup.addView(textView, lp);
                    }




                    initToolBar();
                    initWebView();


                    momFish.setText("母亲: #"+mWare.getMomId()+" 第"+momGen+"代");
                    dadFish.setText("父亲: #"+mWare.getDadId()+" 第"+dadGen+"代");

                    break;
                default:
                    break;
            }
        }
    };



    public void ru0(final EditText et){

        mDialog = new SpotsDialog(this,"loading....");
        mDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run(){
                FishAPI fishAPI = FishAPI.getInstance();
                Boolean valid = true;

                try {
                    Long price = Long.valueOf(et.getText().toString());
                    fishAPI.putAFishOnSale(mWare.getId(),BigInteger.valueOf(price));

                }catch (NumberFormatException e){
                    valid = false;
            }

                Message msg = handler.obtainMessage();
                msg.what = 0;
                msg.obj = valid;
                handler.sendMessage(msg);
                }
        }).start();
    }


    public void ru1(){

        mDialog = new SpotsDialog(this,"loading....");
        mDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run(){
                FishAPI fishAPI = FishAPI.getInstance();
                fishAPI.cancelSale(mWare.getId());

                Message msg = handler.obtainMessage();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();
    }


    public void ru2(final EditText et){

        mDialog = new SpotsDialog(this,"loading....");
        mDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run(){
                Boolean valid = true;
                FishAPI fishAPI = FishAPI.getInstance();
                try {
                    Long price = Long.valueOf(et.getText().toString());
                    fishAPI.putAFishOnBreedingSale(mWare.getId(),BigInteger.valueOf(price));

                }catch (NumberFormatException e){
                    valid = false;
                }

                Message msg = handler.obtainMessage();
                msg.what = 2;
                msg.obj = valid;
                handler.sendMessage(msg);
            }
        }).start();
    }


    public void ru3(){

        mDialog = new SpotsDialog(this,"loading....");
        mDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run(){
                FishAPI fishAPI = FishAPI.getInstance();
                fishAPI.cancelBreedingSale(mWare.getId());

                Message msg = handler.obtainMessage();
                msg.what = 3;
                handler.sendMessage(msg);
            }
        }).start();
    }


    @OnClick(R.id.sell_fish)
    public void sell(View view){

        Button button1 = (Button) findViewById(R.id.sell_fish);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(button1.getText().toString().equals("挂出出售")) {
            final EditText et = new EditText(this);
            builder.setTitle("出售").setMessage("请输入金额").
                    setView(et).
                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //按下确定键后的事件,更新状态\按钮文字
                            ru0(et);

                        }
                    }).
                    setNegativeButton("取消", null);
            builder.create().show();

        }else if(button1.getText().toString().equals("取消出售") ){
            builder.setTitle("取消").setMessage("确定取消出售吗").

                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ru1();

                        }

                    }).
                    setNegativeButton("返回", null);
            builder.create().show();
        }

        //TextView mstate = (TextView) findViewById(R.id.state);
        //mstate.setText(mWare.getState());

        //Intent intent = new Intent(LoginActivity.this,RegActivity.class);
        //startActivity(intent);
        //setResult(RESULT_OK);
    }

    @OnClick(R.id.breed_fish)
    public void breed(View view){

        Button button2 = (Button) findViewById(R.id.breed_fish);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if( button2.getText().toString().equals("挂出繁育")) {
            final EditText et = new EditText(this);
            builder.setTitle("挂出").setMessage("请输入金额").
                    setView(et).
                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ru2(et);

                        }
                    }).
                    setNegativeButton("取消", null);
            builder.create().show();

        }else if(button2.getText().toString().equals("取消繁育") ){
            builder.setTitle("取消").setMessage("确定取消挂出吗").

                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ru3();
                        }
                    }).
                    setNegativeButton("返回", null);
            builder.create().show();
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_detail_fish);
        ViewUtils.inject(this);



        Serializable serializable = getIntent().getSerializableExtra(Contants.WARE);
        if(serializable ==null)
            this.finish();


        mDialog = new SpotsDialog(this,"loading....");
        mDialog.show();

        momFish = findViewById(R.id.momFish);
        dadFish = findViewById(R.id.dadFish);

        mWare = (Fishes) serializable;
        Log.v("mware",mWare.getGeneration().toString());

        TextView mstate = (TextView) findViewById(R.id.state);
        mstate.setText(mWare.getState());

        if(!mWare.getState().equals("状态空闲")){
            if(mWare.getState().equals("挂出出售")){
                Button button1 = (Button) findViewById(R.id.sell_fish);
                Button button2 = (Button) findViewById(R.id.breed_fish);

                button1.setText("取消出售");
                button2.setText("挂出繁育");
                button2.setClickable(false);
                button2.setAlpha((float) 0.5);

            }else if(mWare.getState().equals("等待繁育")){
                Button button1 = (Button) findViewById(R.id.sell_fish);
                Button button2 = (Button) findViewById(R.id.breed_fish);

                button1.setText("挂出出售");
                button2.setText("取消繁育");
                button1.setClickable(false);
                button1.setAlpha((float) 0.5);

            }else if(mWare.getState().equals("繁育冷却")){
                Button button1 = (Button) findViewById(R.id.sell_fish);
                Button button2 = (Button) findViewById(R.id.breed_fish);

                button1.setText("挂出出售");
                button2.setText("挂出繁育");
                button1.setClickable(false);
                button1.setAlpha((float) 0.5);
                button2.setClickable(false);
                button2.setAlpha((float) 0.5);
            }
        //状态空闲
        } else{
            Button button1 = (Button) findViewById(R.id.sell_fish);
            Button button2 = (Button) findViewById(R.id.breed_fish);

            button1.setText("挂出出售");
            button2.setText("挂出繁育");
            button1.setClickable(true);
            button2.setClickable(true);


        }

       /* TagsLayout imageViewGroup = (TagsLayout) findViewById(R.id.image_layout);
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        String[] tags={
                mWare.getRare(),
                "第"+mWare.getGeneration().toString()+"代",
                mWare.getCooldown().toString()+"冷却",
                FishAPI.timeStamp2Date(mWare.getBirthTime().toString())+"出生"
            };
        for (int i = 0; i < tags.length; i++) {
            TextView textView = new TextView(this);
            textView.setText(tags[i]);
            textView.setTextColor(getResources().getColor(R.color.white));
            textView.setBackgroundResource(R.drawable.selector_textview);
            imageViewGroup.addView(textView, lp);
        }

        initToolBar();
        initWebView();*/

        getGenes();
    }

    private void getGenes(){

        BigInteger momId = mWare.getMomId();
        BigInteger dadId = mWare.getDadId();

        FishAPI fishAPI = FishAPI.getInstance();
        List<Object> temp_value = fishAPI.getDetailsOfFish(momId);
        List<Object> temp_value_2 =  fishAPI.getDetailsOfFish(dadId);
        momGene = temp_value.get(1).toString();
        dadGene = temp_value_2.get(1).toString();
        momGen = temp_value.get(5).toString();
        dadGen = temp_value_2.get(5).toString();

        Message msg = handler.obtainMessage();
        msg.what = INIT_MSG;
        handler.sendMessage(msg);

    }


    private void initWebView(){

        WebSettings settings = mWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        settings.setBlockNetworkImage(false);
        settings.setAppCacheEnabled(true);

        WebSettings settings1 = mWebView1.getSettings();

        settings1.setJavaScriptEnabled(true);
        settings1.setUseWideViewPort(true);
        settings1.setLoadWithOverviewMode(true);

        settings1.setBlockNetworkImage(false);
        settings1.setAppCacheEnabled(true);

        WebSettings settings2 = mWebView2.getSettings();

        settings2.setJavaScriptEnabled(true);
        settings2.setUseWideViewPort(true);
        settings2.setLoadWithOverviewMode(true);

        settings2.setBlockNetworkImage(false);
        settings2.setAppCacheEnabled(true);


        // mWebView.loadUrl(Contants.API.WARES_DETAIL);
        mWebView.loadUrl(Contants.API.FISH_IMG+mWare.getGene().toString());
        mWebView1.loadUrl(Contants.API.FISH_IMG+momGene);
        mWebView2.loadUrl(Contants.API.FISH_IMG+dadGene);

        mAppInterfce = new WebAppInterface(this);
        mAppInterfce1 = new WebAppInterface1(this);
        mAppInterfce2 = new WebAppInterface2(this);
        mWebView.addJavascriptInterface(mAppInterfce,"appInterface");
        mWebView.setWebViewClient(new WC());
        mWebView1.addJavascriptInterface(mAppInterfce1,"appInterface");
        mWebView1.setWebViewClient(new WC());
        mWebView2.addJavascriptInterface(mAppInterfce2,"appInterface");
        mWebView2.setWebViewClient(new WC());



    }

    private void initToolBar(){
        mToolBar.setNavigationOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDialog != null){
            mDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        this.finish();
    }


    class  WC extends WebViewClient{
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if(mDialog !=null && mDialog.isShowing())
                mDialog.dismiss();

            mAppInterfce.showDetail();


        }
    }

    class WebAppInterface{

        private Context mContext;
        public WebAppInterface(Context context){
            mContext = context;
        }

        @JavascriptInterface
        public  void showDetail(){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mWebView.loadUrl("javascript:showDetail("+mWare.getGene()+")");

                }
            });
        }


    }

    class WebAppInterface1{

        private Context mContext;
        public WebAppInterface1(Context context){
            mContext = context;
        }

        @JavascriptInterface
        public  void showDetail(){


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mWebView1.loadUrl("javascript:showDetail("+mWare.getGene()+")");

                }
            });
        }


    }

    class WebAppInterface2{

        private Context mContext;
        public WebAppInterface2(Context context){
            mContext = context;
        }

        @JavascriptInterface
        public  void showDetail(){


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mWebView2.loadUrl("javascript:showDetail("+mWare.getGene()+")");

                }
            });
        }


    }
}
