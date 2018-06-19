package cniao5.com.cniao5shop.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.AlertDialog;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

//import cniao5.com.cniao5shop.AddressListActivity;
import cniao5.com.cniao5shop.Contants;
import cniao5.com.cniao5shop.History;
import cniao5.com.cniao5shop.LoginActivity;
import cniao5.com.cniao5shop.R;
//import cniao5.com.cniao5shop.bean.User;
import cniao5.com.cniao5shop.Start;
import cniao5.com.cniao5shop.TransferActivity;
import cniao5.com.cniao5shop.blockchain.FishAPI;
import cniao5.com.cniao5shop.blockchain.Wallet;
import cniao5.com.cniao5shop.utils.ToastUtils;
import cniao5.com.cniao5shop.widget.CNiaoToolBar;
import de.hdodenhof.circleimageview.CircleImageView;


public class MineFragment extends BaseFragment{

    //用户头像\姓名
    @ViewInject(R.id.img_head)
    private CircleImageView mImageHead;
    @ViewInject(R.id.txt_switch)
    private TextView mTxtUserName;

    @ViewInject(R.id.current)
    private CNiaoToolBar current;

    //退出钱包
//    @ViewInject(R.id.btn_logout)
//    private Button mbtnLogout;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mine,container,false);
    }

    @Override
    public void init() {

        showUser();
    }


    private  void showUser(){


        mTxtUserName.setText("切换钱包");
        Wallet wallet = Wallet.getInstance();
        String name = wallet.getCurrentName();

        current.setTitle(name);


    }


    @OnClick(value = {R.id.img_head,R.id.txt_switch})
    public void toStartActivity(View view){


        Intent intent = new Intent(getActivity(), Start.class);

        startActivityForResult(intent, Contants.REQUEST_CODE);

    }
/*
    @OnClick(R.id.txt_my_orders)
    public void toMyOrderActivity(View view){

        startActivity(new Intent(getActivity(), MyOrderActivity.class),true);
    }


    @OnClick(R.id.txt_my_address)
    public void toAddressActivity(View view){

        startActivity(new Intent(getActivity(), AddressListActivity.class),true);
    }

 @OnClick(R.id.txt_my_favorite)
    public void toFavoriteActivity(View view){

        startActivity(new Intent(getActivity(), MyFavoriteActivity.class),true);
    }
*/

    //钱包余额
    @OnClick(R.id.txt_my_currency)
    public void myCurrency(View view){
        //原来用 startActivity(new Intent(getActivity(), MyFavoriteActivity.class),true); 类似的话未登录可以直接跳转到登录activity
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        FishAPI fishAPI = FishAPI.getInstance();
        builder.setTitle("余额").setMessage(fishAPI.getETHBalance().toString()+" ETH\n注: 1ETH = 10^18 WEI").setPositiveButton("确定", null).show();

    }

    //导出私钥
    @OnClick(R.id.txt_my_private_key)
    public void toMyPrivateKey(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final FishAPI fishAPI = FishAPI.getInstance();
        builder.setTitle("私钥").setMessage(fishAPI.exportPrivateKey()).setNeutralButton("复制", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ToastUtils.show(getActivity(),"复制成功");
                //添加到剪切板
                ClipboardManager clipboardManager =
                        (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                /**之前的应用过期的方法，clipboardManager.setText(copy);*/
                assert clipboardManager != null;
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null,fishAPI.exportPrivateKey()));
                if (clipboardManager.hasPrimaryClip()){
                    clipboardManager.getPrimaryClip().getItemAt(0).getText();
                }
            }
        }).setPositiveButton("确定", null).show();
    }

    //钱包地址
    @OnClick(R.id.txt_my_address)
    public void toMyAddress(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final FishAPI fishAPI = FishAPI.getInstance();
        builder.setTitle("地址").setMessage(fishAPI.getAddress()).setNeutralButton("复制", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ToastUtils.show(getActivity(),"复制成功");
                //添加到剪切板
                ClipboardManager clipboardManager =
                        (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                /**之前的应用过期的方法，clipboardManager.setText(copy);*/
                assert clipboardManager != null;
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null,fishAPI.getAddress()));
                if (clipboardManager.hasPrimaryClip()){
                    clipboardManager.getPrimaryClip().getItemAt(0).getText();
                }
            }
        }).setPositiveButton("确定", null).show();
    }

    //查看历史
    @OnClick(R.id.txt_my_transfer)
    public void myTransfer(View view){
        Intent intent = new Intent(getActivity(),TransferActivity.class);
        startActivity(intent);
    }


    //查看历史
    @OnClick(R.id.txt_my_history)
    public void myHistory(View view){
        Intent intent = new Intent(getActivity(),History.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        showUser();
    }


}
