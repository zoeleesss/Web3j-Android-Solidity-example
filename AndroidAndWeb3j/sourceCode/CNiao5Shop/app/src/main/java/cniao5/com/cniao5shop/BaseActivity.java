package cniao5.com.cniao5shop;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

//import cniao5.com.cniao5shop.bean.User;

/**
 * ProjectName:CNiao5Shop
 * Autor： <a href="http://www.cniao5.com">菜鸟窝</a>
 * Description：
 * <p>
 * 菜鸟窝是一个只专注做Android开发技能的在线学习平台，课程以实战项目为主，对课程与服务”吹毛求疵”般的要求，打造极致课程，是菜鸟窝不变的承诺
 */
public class BaseActivity extends AppCompatActivity {


    protected static final String TAG = BaseActivity.class.getSimpleName();

    public void startActivity(Intent intent,boolean isNeedLogin){


        if(isNeedLogin){

//            User user =CniaoApplication.getInstance().getUser();
//            if(user !=null){
//                super.startActivity(intent);
//            }
//            else{

                CniaoApplication.getInstance().putIntent(intent);
                Intent loginIntent = new Intent(this
                        , LoginActivity.class);
                super.startActivity(intent);

           // }

        }
        else{
            super.startActivity(intent);
        }

    }
}
