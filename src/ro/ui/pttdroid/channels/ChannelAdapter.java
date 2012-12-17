package ro.ui.pttdroid.channels;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ChannelAdapter extends BaseAdapter implements OnClickListener {
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
            convertView = inflater.inflate(R.layout.channel_row, null); // TODO
        }
        return convertView;
    }

	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
}