package cniao5.com.cniao5shop;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cniao5.com.cniao5shop.bean.Tab;
import cniao5.com.cniao5shop.fragment.CenterFragment;
import cniao5.com.cniao5shop.fragment.FishFragment;
import cniao5.com.cniao5shop.fragment.HotFragment;
import cniao5.com.cniao5shop.fragment.MineFragment;
import cniao5.com.cniao5shop.widget.FragmentTabHost;

public class MainActivity extends BaseActivity {



    private LayoutInflater mInflater;

    private FragmentTabHost mTabhost;


    private List<Tab> mTabs = new ArrayList<>(5);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTab();

    }


    private void initTab() {


        //Tab tab_home = new Tab(HomeFragment.class,R.string.home,R.drawable.selector_icon_home);
        Tab tab_hot = new Tab(HotFragment.class,R.string.hot,R.drawable.selector_icon_hot);
        Tab tab_center = new Tab(CenterFragment.class,R.string.center,R.drawable.selector_icon_category);
        Tab tab_fish = new Tab(FishFragment.class,R.string.fish,R.drawable.selector_icon_home);
        //Tab tab_category = new Tab(CategoryFragment.class,R.string.catagory,R.drawable.selector_icon_category);
        Tab tab_mine = new Tab(MineFragment.class,R.string.mine,R.drawable.selector_icon_mine);

        //mTabs.add(tab_home);
        mTabs.add(tab_hot);
        mTabs.add(tab_center);
        mTabs.add(tab_fish);
        mTabs.add(tab_mine);



        mInflater = LayoutInflater.from(this);
        mTabhost = (FragmentTabHost) this.findViewById(android.R.id.tabhost);
        mTabhost.setup(this,getSupportFragmentManager(),R.id.realtabcontent);

        for (Tab tab : mTabs){

            TabHost.TabSpec tabSpec = mTabhost.newTabSpec(getString(tab.getTitle()));

            tabSpec.setIndicator(buildIndicator(tab));

            mTabhost.addTab(tabSpec,tab.getFragment(),null);

        }

        mTabhost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        mTabhost.setCurrentTab(0);


    }




    private  View buildIndicator(Tab tab){


        View view =mInflater.inflate(R.layout.tab_indicator,null);
        ImageView img = (ImageView) view.findViewById(R.id.icon_tab);
        TextView text = (TextView) view.findViewById(R.id.txt_indicator);

        img.setBackgroundResource(tab.getIcon());
        text.setText(tab.getTitle());

        return  view;
    }


}
