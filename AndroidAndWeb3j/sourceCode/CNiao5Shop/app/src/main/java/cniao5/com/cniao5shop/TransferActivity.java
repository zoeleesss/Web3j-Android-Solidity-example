package cniao5.com.cniao5shop;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.math.BigDecimal;

import cniao5.com.cniao5shop.blockchain.FishAPI;
import cniao5.com.cniao5shop.http.OkHttpHelper;
import cniao5.com.cniao5shop.utils.ToastUtils;
import cniao5.com.cniao5shop.widget.CNiaoToolBar;
import cniao5.com.cniao5shop.widget.ClearEditText;
import dmax.dialog.SpotsDialog;

/**
 * Created by x on 2018/6/18.
 */

public class TransferActivity extends AppCompatActivity {

    @ViewInject(R.id.toolbar)
    private CNiaoToolBar mToolBar;
    @ViewInject(R.id.etxt_address)
    private ClearEditText address;
    @ViewInject(R.id.etxt_amount)
    private ClearEditText amount;

    private SpotsDialog dialog;
    private final int TRANSFER_MSG = 13164;
    //private final int INIT_MSG = 14791;

    private Double user_amount = 0.0;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /*case INIT_MSG:

                    break;*/
                case TRANSFER_MSG:
                    if (dialog != null) {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }
                    Toast.makeText(TransferActivity.this,"转账成功",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        ViewUtils.inject(this);
        initToolBar();

        new Thread(new Runnable() {
            @Override
            public void run() {
                FishAPI fishAPI = FishAPI.getInstance();
                BigDecimal balance = fishAPI.getETHBalance();
                user_amount = Double.parseDouble(balance.toString());
            }
        }).start();

    }


    private void initToolBar(){


        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TransferActivity.this.finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }


    @OnClick(R.id.btn_transfer)
    public void transfer(View view){
        final String add = address.getText().toString().trim();
        final Double eth_amount = Double.parseDouble(amount.getText().toString().trim());

        if (eth_amount >= user_amount){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("转账").setMessage("余额不足！\n您的余额:\t"+user_amount+" ETH\n但是您想转:"+eth_amount+" ETH").setPositiveButton("确定",null).show();
        }
        else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("转账").setMessage("确定要给\n" + add + "\n转" + eth_amount + " ETH吗?").setNeutralButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog = new SpotsDialog(TransferActivity.this, "loading...");
                    dialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FishAPI fishAPI = FishAPI.getInstance();
                            fishAPI.sendETH(add,eth_amount);

                            Message msg = handler.obtainMessage();
                            msg.what = TRANSFER_MSG;
                            msg.obj = "ok";
                            handler.sendMessage(msg);
                        }
                    }).start();
                }
            }).show();
        }

    }
}
