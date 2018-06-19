package cniao5.com.cniao5shop;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import cniao5.com.cniao5shop.adapter.BaseAdapter;
import cniao5.com.cniao5shop.adapter.HWAdatper_center2;
import cniao5.com.cniao5shop.bean.Fishes;
import cniao5.com.cniao5shop.blockchain.FishAPI;
import cniao5.com.cniao5shop.utils.Pager;
import cniao5.com.cniao5shop.widget.CNiaoToolBar;
import dmax.dialog.SpotsDialog;

//import co.lujun.androidtagview.TagContainerLayout;

public class FishDetailActivity_center2 extends BaseActivity implements View.OnClickListener,Pager.OnPageListener<Fishes>  {

    @ViewInject(R.id.recyclerview)
    private RecyclerView mRecyclerView;

    @ViewInject(R.id.refresh_view)
    private MaterialRefreshLayout mRefreshLayout;

    @ViewInject(R.id.toolbar)
    private CNiaoToolBar mToolBar;

//    @ViewInject(R.id.btn_center2)
//    private Button mButton_center2;

    private SpotsDialog dialog;

    private HWAdatper_center2 mAdatper_center_2;

    private Fishes mWare;

    private List<Fishes> datas_test = new ArrayList<>();//将数据集放进缓存,不进行写操作
    private List<Fishes> datas_change = new ArrayList<>();//用来排序和筛选的数据集


    private final int INIT_MSG = 2;
    private final int REFRESH_MSG = 3;

    private int refresh_count = 0;
    private int last_refresh_count = 0;

    private boolean isFirst = true;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case INIT_MSG:
                    if (dialog != null) {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }

                    mAdatper_center_2 = new HWAdatper_center2(FishDetailActivity_center2.this, datas_change);
                    mAdatper_center_2.w = mWare;
                    mRecyclerView.setAdapter(mAdatper_center_2);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(FishDetailActivity_center2.this));
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());

                    break;
                case REFRESH_MSG:
                    refresh(datas_test, 0, 0);

                    Log.v("refresh", "end. last: " + last_refresh_count + " curr: " + refresh_count);
                    last_refresh_count = refresh_count;
                    mRefreshLayout.finishRefresh();

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_detail_center2);
        ViewUtils.inject(this);



        Serializable serializable = getIntent().getSerializableExtra(Contants.WARE);
        if(serializable ==null)
            this.finish();


        mWare = (Fishes) serializable;

        getWares();
        initToolBar();
        initRefreshLayout();

        dialog = new SpotsDialog(this,"loading....");
        dialog.show();





        /*mAdatper_center_2 = new HWAdatper_center2(this,datas_change);



        mRecyclerView.setAdapter(mAdatper_center_2);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());*/

    }



    private void initToolBar(){
        mToolBar.setNavigationOnClickListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        this.finish();
    }

    @Override
    public void refresh(List<Fishes> datas, int totalPage, int totalCount) {

        mAdatper_center_2.refreshData(datas);
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void loadMore(List<Fishes> datas, int totalPage, int totalCount) {

        mAdatper_center_2.loadMoreData(datas);
        mRecyclerView.scrollToPosition(mAdatper_center_2.getDatas().size());
    }

    @Override
    public void load(List<Fishes> datas, int totalPage, int totalCount) {
        datas = datas_change;

        mAdatper_center_2 = new HWAdatper_center2(this,datas);

        mAdatper_center_2.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                builder.setTitle("选择").setMessage("确定支付...ether吗").
//                        setPositiveButton("确定", null).
//                        setNegativeButton("取消", null);
//                builder.create().show();

                Fishes fishes = mAdatper_center_2.getItem(position);

                //Intent intent = new Intent(FishDetailActivity_center2.this, FishDetailActivity.class);

                //intent.putExtra(Contants.WARE,fishes);

                //startActivity(intent);

            }
        });


        mRecyclerView.setAdapter(mAdatper_center_2);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));
    }



    public List<Fishes> myDataTest(){
        List<Fishes> datas_temp = new ArrayList<>();

        for(Fishes w : datas_test){
            Fishes w_temp = new Fishes();

            w_temp.setReady(w.getReady());
            w_temp.setGene(w.getGene());
            w_temp.setMomId(w.getMomId());
            w_temp.setDadId(w.getDadId());
            w_temp.setCooldown(w.getCooldown());
            w_temp.setGeneration(w.getGeneration());
            w_temp.setBirthTime(w.getBirthTime());
            w_temp.setRare(w.getRare());
            w_temp.setState(w.getState());
            w_temp.setId(w.getId());

            w_temp.setSell_seller(w.getSell_seller());w_temp.setSell_price(w.getSell_price());w_temp.setSell_startedAt(w.getSell_startedAt());

            w_temp.setBreed_seller(w.getBreed_seller());w_temp.setBreed_price(w.getBreed_price());w_temp.setBreed_startedAt(w.getBreed_startedAt());

            datas_temp.add(w_temp);
        }

        return datas_temp;

    }




    public void getWares(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                FishAPI fishAPI = FishAPI.getInstance();

                List<BigInteger> ids = new ArrayList<BigInteger>();


                //获得owner的鱼
                ids = ru2(ids);
                while (ids.size() == 0) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.v("ids2' size", new Integer(ids.size()).toString());

                List<Fishes> temp_wares = new ArrayList<Fishes>();

                int index = 0;
                for (BigInteger id : ids) {

                    Fishes w = new Fishes();
                    List<Object> temp_value = fishAPI.getDetailsOfFish(id);
                    List<Object> temp_value_2 = fishAPI.getOnSaleInfoOfAFish(id);
                    List<Object> temp_value_3 = fishAPI.getOnBreedingSaleInfoOfAFish(id);

                    w.setId(id);
                    w.setReady((Boolean) temp_value.get(0));
                    w.setGene((BigInteger) temp_value.get(1));
                    w.setMomId((BigInteger) temp_value.get(2));
                    w.setDadId((BigInteger) temp_value.get(3));
                    w.setCooldown((BigInteger) temp_value.get(4));
                    w.setGeneration((BigInteger) temp_value.get(5));
                    w.setBirthTime((BigInteger) temp_value.get(6));

                    if (temp_value_2.get(0).toString() != null &&
                            !temp_value_2.get(0).toString().equals("0x0000000000000000000000000000000000000000")) {
                        w.setSell_seller(temp_value_2.get(0).toString());
                        w.setSell_price((BigInteger) temp_value_2.get(1));
                        Log.v("temp_value2.0", temp_value_2.get(0).toString());
                        w.setSell_startedAt((BigInteger) temp_value_2.get(2));
                        w.setState("挂出出售");

                    } else if (temp_value_3.get(0).toString() != null &&
                            !temp_value_3.get(0).toString().equals("0x0000000000000000000000000000000000000000")) {
                        w.setBreed_seller(temp_value_3.get(0).toString());
                        w.setBreed_price((BigInteger) temp_value_3.get(1));
                        w.setBreed_startedAt((BigInteger) temp_value_3.get(2));
                        w.setState("等待繁育");

                    } else if (w.getReady()) {
                        w.setState("状态空闲");
                    } else {
                        w.setState("繁育冷却");
                    }

                    //设置品质
                    ru(w);
                    while (w.getRare() == null) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                    temp_wares.add(w);
                    index++;

                }
                datas_test = temp_wares;
                datas_change = myDataTest();

                if (isFirst) {
                    isFirst = false;
                    Message message = handler.obtainMessage();
                    message.what = INIT_MSG;
                    handler.sendMessage(message);
                } else {
                    refresh_count++;
                    Message message = handler.obtainMessage();
                    message.what = REFRESH_MSG;
                    handler.sendMessage(message);
                }



            }
        }).start();
    }
    static public void ru(final Fishes w) {
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

    static public List<BigInteger> ru2(final List<BigInteger> ids) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FishAPI fishAPI = FishAPI.getInstance();
                synchronized (this) {
                    ids.clear();
                    ids.addAll(fishAPI.getFishesOfOwner(FishAPI.getCredentials().getAddress()));
                    Log.v("ids' size",new Integer(ids.size()).toString());
                }
            }
        }).start();
        return ids;
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
                getWares();
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
                Toast.makeText(FishDetailActivity_center2.this, "没有更多啦O(∩_∩)O", Toast.LENGTH_SHORT).show();
                mRefreshLayout.finishRefreshLoadMore();
                //}
            }
        });
    }


}
