package com.seetong.app.seetong.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.model.LanDeviceInfo;
import com.seetong.app.seetong.sdk.impl.LibImpl;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/10/24.
 */
public class LanSearchListAdapter extends BaseAdapter {

    private LanSearchActivity context;
    private LayoutInflater inflater;
    private List<LanDeviceInfo> data;

    private class ViewHolder {
        public TextView devId;
        public TextView devIp;
        public Button checkBtn;
        public boolean bChecked;
    }

    public LanSearchListAdapter(LanSearchActivity context, List<LanDeviceInfo> data) {
        this.context = context;
        this.data = data;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.lan_search_list_item, parent, false);
            viewHolder.devId = (TextView) convertView.findViewById(R.id.dev_id);
            viewHolder.devIp = (TextView) convertView.findViewById(R.id.dev_ip);
            viewHolder.checkBtn = (Button) convertView.findViewById(R.id.dev_check);
            viewHolder.bChecked = false;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (data.get(position).getEntry().getCloudId().equals("")) {
            viewHolder.devId.setText("null");
        } else {
            viewHolder.devId.setText(data.get(position).getEntry().getCloudId());
        }
        viewHolder.devIp.setText(data.get(position).getEntry().getLanCfg().getIPAddress());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.get(position).getChecked()) {
                    viewHolder.checkBtn.setBackgroundResource(R.drawable.lan_dev_checkbox);
                    data.get(position).setChecked(false);
                } else {
                    StringBuffer ipAddress = new StringBuffer();
                    LibImpl.getInstance().getFuncLib().GetOneIPAddress(ipAddress);
                    int mask = getIpV4Value("255.255.255.0");
                    if (data.get(position).getEntry().getCloudId().equals("")) {
                        context.showGetIdTipDialog(data.get(position).getEntry(), data.get(position).getIndex());
                        return;
                    } else if (!checkSameSegment(data.get(position).getEntry().getLanCfg().getIPAddress(), ipAddress.toString(), mask)) {
                        data.get(position).getEntry().getLanCfg().setIPAddress(ipAddress.toString());
                        context.showGetIdTipDialog(data.get(position).getEntry(), data.get(position).getIndex());
                        return;
                    }
                    viewHolder.checkBtn.setBackgroundResource(R.drawable.lan_dev_checkbox_select);
                    data.get(position).setChecked(true);
                }
            }
        });

        return convertView;
    }

    public static boolean ipV4Validate(String ipv4)
    {
        final String IPV4_REGEX = "((\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})";
        return ipv4Validate(ipv4, IPV4_REGEX);
    }

    private static boolean ipv4Validate(String addr,String regex) {
        return addr != null && Pattern.matches(regex, addr.trim());
    }

    public static int getIpV4Value(String ipOrMask)
    {
        byte[] addr = getIpV4Bytes(ipOrMask);
        int address1  = addr[3] & 0xFF;
        address1 |= ((addr[2] << 8) & 0xFF00);
        address1 |= ((addr[1] << 16) & 0xFF0000);
        address1 |= ((addr[0] << 24) & 0xFF000000);
        return address1;
    }

    public static byte[] getIpV4Bytes(String ipOrMask) {
        try {
            String[] addrs = ipOrMask.split("\\.");
            int length = addrs.length;
            byte[] addr = new byte[length];
            for (int index = 0; index < length; index++) {
                addr[index] = (byte) (Integer.parseInt(addrs[index]) & 0xff);
            }
            return addr;
        } catch (Exception ignored) {
        }
        return new byte[4];
    }

    public static boolean checkSameSegment(String ip1,String ip2, int mask) {
        // 判断IPV4是否合法
        if(!ipV4Validate(ip1)) {
            return false;
        }
        if(!ipV4Validate(ip2)) {
            return false;
        }
        int ipValue1 = getIpV4Value(ip1);
        int ipValue2 = getIpV4Value(ip2);
        return (mask & ipValue1) == (mask & ipValue2);
    }
}
