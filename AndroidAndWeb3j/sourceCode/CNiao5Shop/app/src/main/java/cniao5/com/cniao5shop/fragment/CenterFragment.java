package cniao5.com.cniao5shop.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cniao5.com.cniao5shop.Contants;
import cniao5.com.cniao5shop.R;
import cniao5.com.cniao5shop.FishDetailActivity_center;
import cniao5.com.cniao5shop.adapter.BaseAdapter;
import cniao5.com.cniao5shop.adapter.HWAdatper_center;
import cniao5.com.cniao5shop.bean.Fishes;
import cniao5.com.cniao5shop.blockchain.FishAPI;
import cniao5.com.cniao5shop.utils.Pager;
import cniao5.com.cniao5shop.utils.ToastUtils;
import dmax.dialog.SpotsDialog;


public class CenterFragment extends BaseFragment implements Pager.OnPageListener<Fishes> {

    //public static final int TAG_DEFAULT=0;
    //public static final int TAG_SALE=1;
    //public static final int TAG_PRICE=2;

    @ViewInject(R.id.tab_layout)
    private TabLayout mTablayout;

    @ViewInject(R.id.recyclerview)
    private RecyclerView mRecyclerView;

    @ViewInject(R.id.refresh_view)
    private MaterialRefreshLayout mRefreshLayout;

    @ViewInject(R.id.toolbar_searchview)
    private EditText mSearchView;

    private HWAdatper_center mAdatper_center;
    private SpotsDialog dialog;

    //private   Pager pager;
    private List<Fishes> datas_test = new ArrayList<>();//将数据集放进缓存,不进行写操作
    private List<Fishes> datas_change = new ArrayList<>();//用来排序和筛选的数据集
    //private int orderBy = 0;
    //private long campaignId = 0;

    private final int SORT_MSG = 1;
    private final int INIT_MSG = 2;
    private final int REFRESH_MSG = 3;

    private int refresh_count = 0;
    private int last_refresh_count = 0;

    private boolean isFirst = true;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case SORT_MSG:
                    List<Fishes> temp_fishes = (List<Fishes>) msg.obj;
                    datas_change = mAdatper_center.getDatas();
                    datas_change.clear();
                    datas_change.addAll(temp_fishes);
                    mAdatper_center.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(0);
                    break;
                case INIT_MSG:
                    if (dialog!=null) {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }

                    mAdatper_center = new HWAdatper_center(getActivity(),datas_change);
                    mAdatper_center.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Fishes fishes = mAdatper_center.getItem(position);
                            Intent intent = new Intent(getActivity(), FishDetailActivity_center.class);
                            intent.putExtra(Contants.WARE, fishes);
                            startActivity(intent);
                        }
                    });
                    mRecyclerView.setAdapter(mAdatper_center);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    break;
                case REFRESH_MSG:
                    refresh(datas_test,0,0);

                    Log.v("refresh","end. last: "+last_refresh_count+" curr: "+refresh_count);
                    last_refresh_count = refresh_count;
                    mRefreshLayout.finishRefresh();

                default:
                    break;
            }
        }
    };






    public List<Fishes> myDataTest(){
        List<Fishes> datas_temp = new ArrayList<>();

        for(Fishes w : datas_test){
            Fishes w_temp = new Fishes();

            w_temp.setId(w.getId());
            w_temp.setReady(w.getReady());
            w_temp.setGene(w.getGene());
            w_temp.setMomId(w.getMomId());
            w_temp.setDadId(w.getDadId());
            w_temp.setCooldown(w.getCooldown());
            w_temp.setGeneration(w.getGeneration());
            w_temp.setBirthTime(w.getBirthTime());
            w_temp.setRare(w.getRare());
            w_temp.setState(w.getState());

            w_temp.setSell_seller(w.getSell_seller());w_temp.setSell_price(w.getSell_price());w_temp.setSell_startedAt(w.getSell_startedAt());

            w_temp.setBreed_seller(w.getBreed_seller());w_temp.setBreed_price(w.getBreed_price());w_temp.setBreed_startedAt(w.getBreed_startedAt());

            datas_temp.add(w_temp);
        }

        return datas_temp;

    }



    public void getFishes(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                FishAPI fishAPI = FishAPI.getInstance();
                //Log.v("on sale fish ids size:",FishAPI.getTokensOfOnSaleFishes().toString());
                //获得所有在繁育中心的鱼的id
                List<BigInteger> ids = new ArrayList<BigInteger>();
                ids = fishAPI.getTokensOfBreedingSaleFishes();
                List<Fishes> temp_fishes = new ArrayList<Fishes>();
                //List<Object> temp_simpleInfo = new ArrayList<Object>();


                int index = 0;
                for (BigInteger id : ids){
                    Fishes w = new Fishes();
                    List<Object> temp_value =  fishAPI.getDetailsOfFish(id);
                    List<Object> temp_value_2 =  fishAPI.getOnBreedingSaleInfoOfAFish(id);
                    w.setReady((Boolean) temp_value.get(0));
                    w.setGene((BigInteger) temp_value.get(1));
                    w.setMomId((BigInteger) temp_value.get(2));
                    w.setDadId((BigInteger) temp_value.get(3));
                    w.setCooldown((BigInteger) temp_value.get(4));
                    w.setGeneration((BigInteger) temp_value.get(5));
                    w.setBirthTime((BigInteger) temp_value.get(6));

                    w.setBreed_seller(temp_value_2.get(0).toString());
                    w.setBreed_price((BigInteger)temp_value_2.get(1));
                    w.setBreed_startedAt((BigInteger)temp_value_2.get(2));
                    w.setId(id);

                    //设置状态
                    w.setState("等待繁育");

                    //设置品质
                    ru(w);
                    while (w.getRare() == null) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    temp_fishes.add(w);

                    index++;
                }



                datas_test = temp_fishes;
                datas_change = myDataTest();

                if (isFirst)
                {
                    isFirst = false;
                    Message message = handler.obtainMessage();
                    message.what = INIT_MSG;
                    handler.sendMessage(message);
                }
                else {
                    refresh_count++ ;
                    Message message = handler.obtainMessage();
                    message.what = REFRESH_MSG;
                    handler.sendMessage(message);
                }
            }
        }).start();

    }

    public void ru(final Fishes w) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    String rare = FishAPI.getRareOfAGene(w.getGene());
                    w.setRare(rare);
                }
            }
        }).start();
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_center,container,false);
    }


    public void initRefreshLayout() {
        //设置支持下拉加载更多
        mRefreshLayout.setLoadMore(true);
        //刷新以及加载回调
        mRefreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
                //下拉刷新回调，更改当前状态为下拉刷新状态，把当前页置为第一页，
                //向服务器请求数据
                //curState = STATE_REFRESH;
                //curPage = 1;
                getFishes();
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                //上拉加载更多回调，更改当前状态为上拉加载更多状态，页数加1
                //并且在判断还有更多的情况下向服务器请求数据
                //否则提示用户没有更多数据，关闭上拉加载更多
                //curState = STATE_LOAD_MORE;
                //curPage = curPage + 1;
                //if (curPage <= totalPage) {
                  //  getData();
                //} else {
                    ToastUtils.showMsg(getActivity(), "没有更多啦O(∩_∩)O");
                    mRefreshLayout.finishRefreshLoadMore();
                //}
            }
        });
    }



    @Override
    public void init() {


        // set datas_test = getFishes();
        getFishes();
        initTab();
        initSearch();
        dialog = new SpotsDialog(getContext(),"loading...");
        dialog.show();


    }

    private void initSearch(){
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //sort(myDataTest());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //no-op
            }

            @Override
            public void afterTextChanged(Editable editable) {

                //datas_change.clear();
                //datas_change.addAll(myDataTest());
                //mAdatper.notifyDataSetChanged();

                String s = editable.toString();

                if(!s.equals("")) {

                    List<Fishes> temp_fishes = new ArrayList<Fishes>();
                    for (Fishes temp_fish : myDataTest()) {
                        //精确查找
                        if (temp_fish.getRare().equals(s.trim())) {
                            temp_fishes.add(temp_fish);
                        }

                    }

                    if (temp_fishes.size() == 0) {
                        ToastUtils.showMsg(getActivity(), "找不到对应品质");

                    } else {
                        ToastUtils.mToast.cancel();
                        sort(temp_fishes);
//                        datas_change.clear();
//                        datas_change.addAll(temp_fishes);
//                        mAdatper.notifyDataSetChanged();
//                        mRecyclerView.scrollToPosition(0);
                    }

                }
                else {
                    ToastUtils.mToast.cancel();
                    sort(myDataTest());
                }

            }
        });
    }

    private void sort(final List<Fishes> sort_data){
        //Toast.makeText(getActivity(), "选中的"+tab.getText(), Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {

                TabLayout.Tab tab = mTablayout.getTabAt(mTablayout.getSelectedTabPosition());
                if(tab.getText()== "默认"){
                    List<Fishes> temp_fishes = new ArrayList<Fishes>();

                    for (Fishes w : myDataTest()){
                        for (Fishes w_2 : sort_data){
                            if (w_2.getGene() == w.getGene()){
                                temp_fishes.add(w);
                                break;
                            }
                        }
                    }


                    Message msg = handler.obtainMessage();
                    msg.what = SORT_MSG;
                    msg.obj = temp_fishes;
                    handler.sendMessage(msg);

                }
                else if(tab.getText()== "价格"){
                    List<Fishes> temp_fishes = new ArrayList<Fishes>(sort_data);

                    Collections.sort(temp_fishes, new Comparator<Fishes>() {
                        @Override
                        public int compare(Fishes w1, Fishes w2) {
                            int i = w1.getBreed_price().compareTo(w2.getBreed_price());
                            if(i == 0){
                                return w1.getGene().compareTo(w2.getGene());
                            }
                            return i;
                        }
                    });
                    Message msg = handler.obtainMessage();
                    msg.what = SORT_MSG;
                    msg.obj = temp_fishes;
                    handler.sendMessage(msg);

                }else if(tab.getText()== "代数"){
                    List<Fishes> temp_fishes = new ArrayList<Fishes>(sort_data);

                    Collections.sort(temp_fishes, new Comparator<Fishes>() {
                        @Override
                        public int compare(Fishes w1, Fishes w2) {
                            int i = w1.getGeneration().compareTo(w2.getGeneration());
                            if(i == 0){
                                return w1.getGene().compareTo(w2.getGene());
                            }
                            return i;
                        }
                    });
                    Message msg = handler.obtainMessage();
                    msg.what = SORT_MSG;
                    msg.obj = temp_fishes;
                    handler.sendMessage(msg);

                }



            }
        }).start();


    }

    private void initTab(){
        TabLayout.Tab tab= mTablayout.newTab();
        tab.setText("默认");
        //tab.setTag(TAG_DEFAULT);
        mTablayout.addTab(tab);

        tab= mTablayout.newTab();
        tab.setText("价格");
        //tab.setTag(TAG_PRICE);
        mTablayout.addTab(tab);

        tab= mTablayout.newTab();
        tab.setText("代数");
        //tab.setTag(TAG_SALE);
        mTablayout.addTab(tab);

        mTablayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                                @Override
                                                public void onTabSelected(TabLayout.Tab tab) {
                                                    sort(datas_change);
                                                }

                                                @Override
                                                public void onTabUnselected(TabLayout.Tab tab) {
                                                //    Toast.makeText(getActivity(), "未选中的"+tab.getText(), Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onTabReselected(TabLayout.Tab tab) {
                                                //    Toast.makeText(getActivity(), "复选的"+tab.getText(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
    }

    @Override
    public void load(List<Fishes> datas, int totalPage, int totalCount) {
        datas = datas_change;

        mAdatper_center = new HWAdatper_center(getActivity(),datas);

        mAdatper_center.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Fishes fishes = mAdatper_center.getItem(position);
                Log.v("mfish_1", fishes.getGeneration().toString());
                Intent intent = new Intent(getActivity(), FishDetailActivity_center.class);

                intent.putExtra(Contants.WARE, fishes);

                startActivity(intent);


            }
        });


        mRecyclerView.setAdapter(mAdatper_center);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));
    }

    @Override
    public void refresh(List<Fishes> datas, int totalPage, int totalCount) {

        mAdatper_center.refreshData(datas);
        mRecyclerView.scrollToPosition(0);
    }




    @Override
    public void loadMore(List<Fishes> datas, int totalPage, int totalCount) {

        mAdatper_center.loadMoreData(datas);
        mRecyclerView.scrollToPosition(mAdatper_center.getDatas().size());
    }




}
