package cniao5.com.cniao5shop;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.facebook.drawee.backends.pipeline.Fresco;

//import cniao5.com.cniao5shop.bean.User;
//import cniao5.com.cniao5shop.utils.UserLocalData;

/**
 * Created by <a href="http://www.cniao5.com">菜鸟窝</a>
 * 一个专业的Android开发在线教育平台
 */
public class CniaoApplication extends Application {

    //private User user;



    private static  CniaoApplication mInstance;


    public static  CniaoApplication getInstance(){

        return  mInstance;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        //initUser();
        Fresco.initialize(this);
    }




//    private void initUser(){
//
//        //this.user = UserLocalData.getUser(this);
//    }
//
//
//    public User getUser(){
//
//        return user;
//    }
//
//
//    public void putUser(User user,String token){
//        this.user = user;
//        UserLocalData.putUser(this,user);
//        UserLocalData.putToken(this,token);
//    }
//
//    public void clearUser(){
//        this.user =null;
//        UserLocalData.clearUser(this);
//        UserLocalData.clearToken(this);
//
//
//    }
//
//
//    public String getToken(){
//
//        return  UserLocalData.getToken(this);
//    }



    private  Intent intent;
    public void putIntent(Intent intent){
        this.intent = intent;
    }

    public Intent getIntent() {
        return this.intent;
    }

    public void jumpToTargetActivity(Context context){

        context.startActivity(intent);
        this.intent =null;
    }

}
