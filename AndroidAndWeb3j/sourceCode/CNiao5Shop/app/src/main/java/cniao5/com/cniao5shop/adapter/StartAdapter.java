package cniao5.com.cniao5shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cniao5.com.cniao5shop.R;

/**
 * Created by x on 2018/6/18.
 */

public class StartAdapter extends ArrayAdapter {
    private final int resourceId;

    public StartAdapter(Context context, int textViewResourceId, List<String> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String name = (String) getItem(position); // 获取当前项的实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);//实例化一个对象

        TextView walletName = (TextView) view.findViewById(R.id.name);//获取该布局内的文本视图
        TextView index = (TextView) view.findViewById(R.id.index);
        walletName.setText(name);//为文本视图设置文本内容
        index.setText("# "+(position+1));
        return view;
    }
}
