package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;
import com.example.AsymmetricGridView.library.Utils;
import com.example.AsymmetricGridView.library.widget.AsymmetricGridView;
import com.example.AsymmetricGridView.library.widget.AsymmetricGridViewAdapter;
import com.example.AsymmetricGridView.model.DemoItem;
import com.example.AsymmetricGridView.widget.DefaultListAdapter;
import com.seetong.app.seetong.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/11.
 */
public class TestActivity extends BaseActivity {
    private static final String TAG = "TestActivity";
    private AsymmetricGridView listView;
    private ListAdapter adapter;
    private int currentOffset = 0;
    private AsymmetricGridViewAdapter<DemoItem> asymmetricAdapter;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        listView = (AsymmetricGridView)findViewById(R.id.listView);

        // you can also use ListAdapterWrapper class if you want to test with an WrapperListAdapter:
        // adapter = new ListAdapterWrapper(this, listView, new ArrayList<DemoItem>());

        adapter = new DefaultListAdapter(this, listView, new ArrayList<DemoItem>());

        if (adapter instanceof WrapperListAdapter)
            asymmetricAdapter = (AsymmetricGridViewAdapter) ((WrapperListAdapter) adapter).getWrappedAdapter();
        else
            asymmetricAdapter = (AsymmetricGridViewAdapter<DemoItem>) adapter;
        asymmetricAdapter.appendItems(getMoreItems(50));

        listView.setRequestedColumnCount(3);
        listView.setRequestedHorizontalSpacing(Utils.dpToPx(this, 3));
        listView.setAdapter(adapter);
        listView.setDebugging(true);
    }

    private List<DemoItem> getMoreItems(int qty) {
        final List<DemoItem> items = new ArrayList<DemoItem>();

        for (int i = 0; i < qty; i++) {
//            int colSpan = Math.random() < 0.2f ? 2 : 1;
            int colSpan;
            // Swap the next 2 lines to have items with variable
            // column/row span.
            // int rowSpan = Math.random() < 0.2f ? 2 : 1;

            if (i%6==0||i%6==5){
                colSpan=2;
            }else {
                colSpan=1;
            }
            int rowSpan = colSpan;
            System.out.println("<<<<<<<<<"+rowSpan);
            final DemoItem item = new DemoItem(colSpan, rowSpan, currentOffset + i);
            items.add(item);
        }

        currentOffset += qty;

        return items;
    }
}
