package cniao5.com.cniao5shop.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.drawee.view.SimpleDraweeView;

import java.math.BigInteger;
import java.util.List;

import cniao5.com.cniao5shop.Contants;
import cniao5.com.cniao5shop.R;
import cniao5.com.cniao5shop.bean.Fishes;
import cniao5.com.cniao5shop.blockchain.FishAPI;
import cniao5.com.cniao5shop.blockchain.config.Configuration;
import cniao5.com.cniao5shop.blockchain.config.Web3jUtil;
import cniao5.com.cniao5shop.utils.ToastUtils;
import dmax.dialog.SpotsDialog;

/**
 * Created by <a href="http://www.cniao5.com">菜鸟窝</a>
 * 一个专业的Android开发在线教育平台
 */
public class HWAdatper_center2 extends SimpleAdapter<Fishes> {

    public Fishes w;

    private SpotsDialog mDialog;

    private BaseViewHolder vh;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    if(mDialog!=null && mDialog.isShowing()){
                        mDialog.dismiss();
                    }
                    if((Boolean)msg.obj) {
                        final Button button = vh.getButton(R.id.btn_center2);
                        vh.getTextView(R.id.text_price).setText("繁育冷却中");
                        button.setAlpha((float) 0.5);
                        button.setClickable(false);
                    }
                    else{
                        ToastUtils.showMsg(HWAdatper_center2.super.context, "余额不足");
                    }
            }
        }
    };


    public void ru(final Fishes fishes){

        mDialog = new SpotsDialog(HWAdatper_center2.super.context,"loading....");
        mDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run(){
                Boolean valid = true;
                FishAPI fishAPI = FishAPI.getInstance();

                BigInteger balance =  Web3jUtil.etherToWei(fishAPI.getETHBalance());
                if (w.getBreed_price().compareTo(balance) > 0) {
                    valid = false;

                } else {
                    Log.v("breed_center_id:", w.getId().toString());
                    Log.v("breed_owner_id:", fishes.getId().toString());
                    fishAPI.buyMatingWithAFish(w.getId(), fishes.getId(),w.getBreed_price());

                }

                Message msg = handler.obtainMessage();
                msg.what = 0;
                msg.obj = valid;
                handler.sendMessage(msg);
            }
        }).start();
    }


    public HWAdatper_center2(Context context, List<Fishes> datas) {
        super(context, R.layout.template_center2_wares, datas);
    }

    @Override
    protected void convert(final BaseViewHolder viewHolder, final Fishes fishes) {
        SimpleDraweeView draweeView = (SimpleDraweeView) viewHolder.getView(R.id.drawee_view);

        draweeView.setImageURI(Uri.parse(Contants.API.FISH_IMG+ fishes.getGene().toString()));

        vh = viewHolder;

        //鱼的品质和出生时间
        //鱼的品质和出售日期
        String str1="<font color=\""+ Configuration.getColorOfRare(fishes.getRare())+"\">"+ fishes.getRare()+"</font>"+"<font color=\"black\"> | "+ fishes.getGeneration()+"代 | </font>"+"<font color=\"black\">"+ fishes.getCooldown()+"冷却</font>";
        viewHolder.getTextView(R.id.text_title).setText(Html.fromHtml(str1));
        final Button button =viewHolder.getButton(R.id.btn_center2);
        if(button !=null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HWAdatper_center2.super.context);
                    builder.setTitle("选择").setMessage("确定支付 "+w.getBreed_price()+" wei吗").
                            setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ru(fishes);
                                }
                            }).
                            setNegativeButton("取消", null);
                    builder.create().show();
                }
            });
        }

        //状态:状态空闲\挂出出售\等待繁育\繁育冷却
        if(fishes.getReady()) {
            if(fishes.getState().equals("状态空闲")){
            viewHolder.getTextView(R.id.text_price).setText("状态空闲");
                button.setAlpha((float)1.0);
            }else if(fishes.getState().equals("挂出出售")){
                viewHolder.getTextView(R.id.text_price).setText("挂出出售");
                button.setAlpha((float)0.5);
                button.setClickable(false);
            }else if(fishes.getState().equals("等待繁育")){
                viewHolder.getTextView(R.id.text_price).setText("等待繁育");
                button.setAlpha((float)0.5);
                button.setClickable(false);
            }
        }else{
            viewHolder.getTextView(R.id.text_price).setText("繁育冷却中");
            button.setAlpha((float)0.5);
            button.setClickable(false);
        }


    }



    public void  resetLayout(int layoutId){


        this.layoutResId  = layoutId;

        notifyItemRangeChanged(0,getDatas().size());


    }



}
