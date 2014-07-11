package com.hypernode.sshmediaplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hypernode.sshmediaplayer.R;

import java.util.List;

/**
 * Created by johannes on 07.07.14.
 */
public class PlaylistAdapter extends BaseAdapter {

    private Context mContext;
    private List<MPC_controller.Song> playlistSongs;
    private LayoutInflater mLayoutInflater = null;

    public PlaylistAdapter(Context context, List<MPC_controller.Song> playlistSongs) {
        mContext = context;
        this.playlistSongs=playlistSongs;
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return playlistSongs.size();
    }

    public void setPlaylistSongs(List<MPC_controller.Song> playlistSongs) {
        this.playlistSongs = playlistSongs;
    }

    @Override
    public Object getItem(int position) {
        return playlistSongs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        PlaylistViewHolder viewHolder;
        if (convertView == null) {
             v = mLayoutInflater.inflate(R.layout.listview_title_element, null);
             viewHolder = new PlaylistViewHolder(v);
             v.setTag(viewHolder);
        } else {
            viewHolder = (PlaylistViewHolder) v.getTag();
        }
        MPC_controller.Song currentSong = playlistSongs.get(position);
        if(currentSong==null)return v;

        viewHolder.textView_position.setText( String.valueOf(currentSong.position));
        String artist = currentSong.artist;
        String title = currentSong.title;
        if(!artist.equals("") || !title.equals("")) {
            viewHolder.textView_title.setText(artist + "-" + title);
        }else{
            viewHolder.textView_title.setText(currentSong.file);
        }
        viewHolder.textView_time.setText(currentSong.time);
        return v;
    }

    class PlaylistViewHolder {
        public TextView textView_position;
        public TextView textView_title;
        public TextView textView_time;

        public PlaylistViewHolder(View base) {
            textView_position = (TextView) base.findViewById(R.id.textView_position);
            textView_title= (TextView) base.findViewById(R.id.textView_title);
            textView_time = (TextView) base.findViewById(R.id.textView_time);

        }
    }
}
