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

    private int resource = -1;
    
    private List<Channel> channels;

    public ChannelAdapter(Context context, int resource, List<Channel> channels) {
        this.context = context;
        this.resource = resource;
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
            convertView = inflater.inflate(resource, null);
        }
        
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        tvName.setText(channel.name);
        
 		// set channel status icon
        ImageButton btnInfo = (ImageButton) convertView.findViewById(R.id.btnInfo);            	
    	int resource = ChannelHelper.getChannelStatusResource(channel);
    	btnInfo.setImageResource(resource);

        return convertView;
    }
}