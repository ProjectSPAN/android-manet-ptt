package ro.ui.pttdroid.channels;

import java.util.List;

import ro.ui.pttdroid.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChannelAdapter extends BaseAdapter {
    private Context context;

    private List<Channel> channels;

    public ChannelAdapter(Context context, List<Channel> channels) {
        this.context = context;
        this.channels = channels;
    }

    public int getCount() {
        return channels.size();
    }

    public Object getItem(int position) {
        return channels.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        Channel channel = channels.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.channelrow, null);
            
            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            tvName.setText(channel.name);
            
            ImageButton btnInfo = (ImageButton) convertView.findViewById(R.id.btnInfo);            
            if (channel.valid) {
            	btnInfo.setImageResource(R.drawable.green_orb_icon);
            } else {
            	btnInfo.setImageResource(R.drawable.red_orb_icon);
            }
        }
        return convertView;
    }
}