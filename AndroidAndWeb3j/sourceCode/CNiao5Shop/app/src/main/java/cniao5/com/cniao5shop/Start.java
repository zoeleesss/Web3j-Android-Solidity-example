package cniao5.com.cniao5shop;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cniao5.com.cniao5shop.adapter.StartAdapter;
import cniao5.com.cniao5shop.blockchain.Wallet;
import cniao5.com.cniao5shop.utils.ToastUtils;


/**
 * Created by x on 2018/6/18.
 */

public class Start extends Activity {

    private List<String> names = new ArrayList<>();
    private List<String> passwords = new ArrayList<>();
    private List<String> filepaths = new ArrayList<>();
    private StartAdapter startAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet);


        initData();

        if (names.size() == 0){
            Intent intent = new Intent();
            intent.setClass(this,RegActivity.class);
            startActivity(intent);
        }
        else {

            ListView listView = findViewById(R.id.listview);
            startAdapter = new StartAdapter(this, R.layout.wallet_item, names);
            //new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,names)
            listView.setAdapter(startAdapter);

            View footerView  = getLayoutInflater().inflate(R.layout.start_footer_btn,null);
            Button btn= footerView.findViewById(R.id.btn_import);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(Start.this,RegActivity.class);
                    startActivity(intent);
                }
            });
            listView.addFooterView(footerView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    //通过view获取其内部的组件，进而进行操作
                    final String choice = names.get(i);
                    final String pass = passwords.get(i);
                    final String fi = filepaths.get(i);

                    //大多数情况下，position和id相同，并且都从0开始
                    //String showText = "点击第" + i + " wallet:  " + choice + "，ID为：" + l;
                    //Toast.makeText(Start.this, showText, Toast.LENGTH_LONG).show();

                    AlertDialog.Builder builder = new AlertDialog.Builder(Start.this);
                    final EditText et = new EditText(Start.this);
                    builder.setTitle("验证").setMessage("请输入 " + choice + " 的密码").
                            setView(et).
                            setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String inputPass = et.getText().toString();
                                    if (inputPass.equals(pass)) {
                                        ToastUtils.showMsg(Start.this, "验证成功");
                                        startUp(choice,pass,fi);
                                    } else {
                                        ToastUtils.showMsg(Start.this, "验证失败，请重试");
                                    }

                                }
                            }).
                            setNegativeButton("取消", null);
                    builder.create().show();

                }
            });
        }
    }

    public void initData(){

        Wallet wallet = Wallet.getInstance();
        String filepath = getFilesDir()+"/keystore";
        try {
            wallet.getLists(filepath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        names = wallet.getNames();
        passwords =wallet.getPasswords();
        filepaths = wallet.getFilepaths();

    }


    public void startUp(String name,String pass,String file)
    {

        Wallet wallet = Wallet.getInstance();
        wallet.useWallet(name,pass,file);

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
