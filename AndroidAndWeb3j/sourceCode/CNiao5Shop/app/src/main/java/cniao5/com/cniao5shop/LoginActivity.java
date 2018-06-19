package cniao5.com.cniao5shop;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.squareup.okhttp.Response;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cniao5.com.cniao5shop.bean.WalletBean;

import cniao5.com.cniao5shop.blockchain.Wallet;
import cniao5.com.cniao5shop.widget.CNiaoToolBar;
import cniao5.com.cniao5shop.widget.ClearEditText;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";


    @ViewInject(R.id.toolbar)
    private CNiaoToolBar mToolBar;
    @ViewInject(R.id.etxt_pk)
    private ClearEditText mEtxtPk;
    @ViewInject(R.id.etxt_name)
    private ClearEditText mEtxtName;
    @ViewInject(R.id.etxt_pwd)
    private ClearEditText mEtxtPwd;
    @ViewInject(R.id.etxt_pwd_2)
    private ClearEditText mEtxtPwd_2;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ProgressBar pgb=findViewById(R.id.progressbar);
            switch (msg.what) {
                case 0:
                    Toast.makeText(LoginActivity.this, "Wallet imported successfully.", Toast.LENGTH_SHORT).show();
                    pgb.setVisibility(ProgressBar.GONE);

                    Intent intent = new Intent(LoginActivity.this,Start.class);
                    startActivity(intent);
                    break;
                case 1:
                    Toast.makeText(LoginActivity.this, "两次输入的密码不一致，请确认！", Toast.LENGTH_SHORT).show();
                    pgb.setVisibility(ProgressBar.GONE);
                    break;
                case 2:
                    Toast.makeText(LoginActivity.this, "该钱包名已存在！", Toast.LENGTH_SHORT).show();
                    pgb.setVisibility(ProgressBar.GONE);
                    break;
                default:
                    pgb.setVisibility(ProgressBar.GONE);
                    break;
            }
        }

    };


    //private OkHttpHelper okHttpHelper = OkHttpHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ViewUtils.inject(this);

        initToolBar();
    }


    private void initToolBar(){


        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoginActivity.this.finish();
            }
        });

    }

    public void progressBar_Login(View view) {
        ProgressBar pgb=findViewById(R.id.progressbar);
        pgb.setVisibility(ProgressBar.VISIBLE);
        importWallet(view);
    }

    public void importWallet(View view) {

        try {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    String pk=mEtxtPk.getText().toString();
                    //String pk = "65e080f727d9ddca08bff41f57283fc7d5e032bb5af8de963dade6a6caaa1ec4";
                    String name=mEtxtName.getText().toString();
                    String password1=mEtxtPwd.getText().toString();
                    String password2=mEtxtPwd_2.getText().toString();
                    Message msg = mHandler.obtainMessage();
                    if(!password1.equals(password2)){
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                        return;
                    }
                    Wallet wallet=Wallet.getInstance();
                    //List namelist= null;
                    //namelist = wallet.getNameLists();
                    File f = new File(getFilesDir()+"/keystore");
                    if (!f.exists()){
                        f.mkdirs();
                    }
                    String filepath = getFilesDir()+"/keystore";
                    try {
                        List<WalletBean>lists=wallet.getLists(filepath);
                        for (WalletBean walletBean:lists){
                            String temp=walletBean.getName();
                            if(temp.equals(name)){
                                msg.what = 2;
                                mHandler.sendMessage(msg);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*if(namelist.contains(name)){
                        msg.what = 2;
                        mHandler.sendMessage(msg);
                        return;
                    }*/

                    wallet.importWallet(name,password1,pk,filepath);
                    msg.what = 0;
                    //msg.obj = "ok";//可以是基本类型，可以是对象，可以是List、map等；
                    mHandler.sendMessage(msg);
                }
            }).start();

            /*String filename = "myfile";
            String string = "Hello world!";
            FileOutputStream outputStream;
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();*/

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    @OnClick(R.id.btn_register_1)
    public void go_register(View view){
        Intent intent = new Intent(LoginActivity.this,RegActivity.class);
        startActivity(intent);
        //setResult(RESULT_OK);
        finish();
    }

    /*@OnClick(R.id.btn_login)
    public void login(View view){


        String pk = mEtxtPk.getText().toString().trim();
        if(TextUtils.isEmpty(pk)){
            ToastUtils.show(this, "请输入私钥");
            return;
        }

        String pwd = mEtxtPwd.getText().toString().trim();
        if(TextUtils.isEmpty(pwd)){
            ToastUtils.show(this,"请输入密码");
            return;
        }

        String pwd_2 = mEtxtPwd_2.getText().toString().trim();
        if(TextUtils.isEmpty(pwd_2)){
            ToastUtils.show(this,"请重复密码");
            return;
        }

        Map<String,Object> params = new HashMap<>(3);
        params.put("pk",pk);
        params.put("password", DESUtil.encode(Contants.DES_KEY,pwd));
        params.put("password_2", DESUtil.encode(Contants.DES_KEY,pwd_2));


        okHttpHelper.post(Contants.API.LOGIN, params, new SpotsCallBack<LoginRespMsg<User>>(this) {


            @Override
            public void onSuccess(Response response, LoginRespMsg<User> userLoginRespMsg) {


               CniaoApplication application =  CniaoApplication.getInstance();
                application.putUser(userLoginRespMsg.getData(), userLoginRespMsg.getToken());

                if(application.getIntent() == null){
                    setResult(RESULT_OK);
                    finish();
                }else{

                    application.jumpToTargetActivity(LoginActivity.this);
                    finish();

                }



            }

            @Override
            public void onError(Response response, int code, Exception e) {

            }
        });





    }*/



}
