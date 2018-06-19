package cniao5.com.cniao5shop.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.widget.Button;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import cniao5.com.cniao5shop.Contants;
import cniao5.com.cniao5shop.R;
import cniao5.com.cniao5shop.bean.Fishes;
import cniao5.com.cniao5shop.blockchain.config.Configuration;
import cniao5.com.cniao5shop.utils.ToastUtils;

/**
 * Created by <a href="http://www.cniao5.com">菜鸟窝</a>
 * 一个专业的Android开发在线教育平台
 */
public class HWAdatper_center extends SimpleAdapter<Fishes> {


    //CartProvider provider ;

    public HWAdatper_center(Context context, List<Fishes> datas) {
        super(context, R.layout.template_hot_wares, datas);

        //provider = new CartProvider(context);
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, final Fishes fishes) {
        SimpleDraweeView draweeView = (SimpleDraweeView) viewHolder.getView(R.id.drawee_view);

        draweeView.setImageURI(Uri.parse(Contants.API.FISH_IMG+ fishes.getGene().toString()));

        //鱼的品质和挂出繁育日期
        //viewHolder.getTextView(R.id.text_title).setText(fishes.getRare()+" | "+"挂出:"+FishAPI.timeStamp2Date(fishes.getBreed_startedAt().toString()));
        //鱼的品质和出售日期
        String str1="<font color=\""+ Configuration.getColorOfRare(fishes.getRare())+"\">"+ fishes.getRare()+"</font>"+"<font color=\"black\"> | "+ fishes.getGeneration()+"代 | </font>"+"<font color=\"black\">"+ fishes.getCooldown()+"冷却</font>";
        viewHolder.getTextView(R.id.text_title).setText(Html.fromHtml(str1));
        //价格
        viewHolder.getTextView(R.id.text_price).setText(fishes.getBreed_price()+" wei");




        Button button =viewHolder.getButton(R.id.btn_add);
        if(button !=null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   // provider.put(fishes);

                    ToastUtils.show(context, "已添加到购物车");
                }
            });
        }

    }




    public void  resetLayout(int layoutId){


        this.layoutResId  = layoutId;

        notifyItemRangeChanged(0,getDatas().size());


    }

    @Override
    public Boolean refreshData(List<Fishes> list) {
        return super.refreshData(list);
    }
}
