package com.hypernode.sshmediaplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;

import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hypernode.sshmediaplayer.R;
import com.u1aryz.android.lib.newpopupmenu.PopupMenu;


public class MainActivity extends Activity implements ImageButton.OnClickListener,
        MPC_controller.OnCreateShellCompleted,MPC_controller.OnSSHCommandCompleted,
        ListView.OnItemClickListener,ListView.OnItemLongClickListener,PopupMenu.OnItemSelectedListener,
        SeekBar.OnSeekBarChangeListener{
    static  Activity instance;
    static final String TAG="MainActivity";
    TextView tv_output;



    private MPC_controller mpcController;
    private static final int MINI_MENU_PLAY=0;
    private static final int MINI_MENU_REMOVE=1;

    final static boolean YOUTUBE_ON=false;

    private  int runnable_time_ms =2000;
    private boolean runnable_active=false;
    boolean get_status=true;
    ImageButton button_next;
    ImageButton button_previous;
    ImageButton button_play;
    ImageButton button_pause;
    ImageButton button_add;
    TextView textView_status;
    TextView textView_seekpercent;
    SeekBar seekBar_position;

    EditText editText_link;
    ListView listView_titles;
    View view_online;
    String menuPos=null;
    PlaylistAdapter playlistAdapter;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance=this;
        tv_output = (TextView) findViewById(R.id.tv_output);
        button_next = (ImageButton) findViewById(R.id.button_next);
        button_previous =(ImageButton) findViewById(R.id.button_previous);
        button_play = (ImageButton) findViewById(R.id.button_play);
        button_pause = (ImageButton)findViewById(R.id.button_pause);
        button_add  = (ImageButton) findViewById(R.id.button_add);
        textView_status= (TextView)findViewById(R.id.textView_status);
        editText_link = (EditText)findViewById(R.id.editText_link);
        listView_titles = (ListView)findViewById(R.id.listView_titles);
        view_online = findViewById(R.id.view_onlineStatus);
        textView_seekpercent=(TextView)findViewById(R.id.textView_percent);
        seekBar_position=(SeekBar)findViewById(R.id.seekBar_position);
        mpcController = new MPC_controller();
        loginWithPref(); //try login with default preferences


        seekBar_position.setOnSeekBarChangeListener(this);
        button_add.setOnClickListener(this);
        button_next.setOnClickListener(this);
        button_previous.setOnClickListener(this);
        button_pause.setOnClickListener(this);
        button_play.setOnClickListener(this);

        playlistAdapter = new PlaylistAdapter(this,mpcController.getPlaylist());
        listView_titles.setAdapter(playlistAdapter);

        listView_titles.setOnItemClickListener(this);
        listView_titles.setOnItemLongClickListener(this);

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                    if(mpcController.current_volume<100) {
                        mpcController.current_volume += 5;
                    }
                    mpcController.mpcVolume(this,mpcController.current_volume);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    if(mpcController.current_volume>=5) {
                        mpcController.current_volume -= 5;
                    }
                    mpcController.mpcVolume(this,mpcController.current_volume);
                 }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startPreferenceActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startPreferenceActivity(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SetPreferenceActivity.class);
        startActivityForResult(intent, 0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        loginWithPref(); //try login with default preferences
    }

    private void loginWithPref(){
        SharedPreferences mySharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        //Network Preferences
        String host_ip_key = getString(R.string.remote_host_ip);
        String host_ip_pref = mySharedPreferences.getString(host_ip_key, null);

        String host_port_key = getString(R.string.remote_host_port);
        String host_port_pref = mySharedPreferences.getString(host_port_key, null);

        String ssl_username_key = getString(R.string.ssl_username);
        String ssl_username_pref = mySharedPreferences.getString(ssl_username_key, "");

        String ssl_password_key = getString(R.string.ssl_password);
        String ssl_password_pref = mySharedPreferences.getString(ssl_password_key, "");
        //Application Preferences

        if(host_ip_pref==null){ //if mandatory host ip is null show prompt
            startPreferenceActivity();
            return;
        }
        String get_status_key = getString(R.string.get_status);
        get_status = mySharedPreferences.getBoolean(get_status_key, true);


        String status_rate_key = getString(R.string.status_refresh_rate);
        String status_rate_pref = mySharedPreferences.getString(status_rate_key, "2000");


        try {
            runnable_time_ms = Integer.valueOf(status_rate_pref);

            LoginParameters loginParameters = new LoginParameters(
                    ssl_username_pref, host_ip_pref, ssl_password_pref, Integer.valueOf(host_port_pref));

            stopStatusRunnable();
            mpcController.createSSHshell(this, loginParameters);

        }catch(Exception ex){
            Log.e(TAG,"Exception: ",ex);
        }

    }
    @Override
    public void onClick(final View v) {
            if(v==button_play){
                mpcController.mpcPlay(this);
            }else if(v==button_pause){
                mpcController.mpcPause(this);
            }else if(v==button_next){
                mpcController.mpcNext(this);
            }else if(v==button_previous){
                mpcController.mpcPrev(this);
            }else if(v==button_add){
                if(editText_link!=null &&
                   editText_link.getText()!=null &&
                   !editText_link.getText().toString().equals("")){

                    String link = editText_link.getText().toString();
                    if(YOUTUBE_ON) {
                        String filteredLink = link.toLowerCase().replace(".", "");
                        if (!filteredLink.contains("youtube")) { //TODO add this in
                            link = link.replace("https", "http");
                            mpcController.mpcAdd(this, link);
                        } else {
                            mpcController.mpcAddYTLink(this, link);
                        }
                    }else{
                        mpcController.mpcAddYTLink(this, link);
                    }

                }
            }
            //&& only if previous succeded
            //status --> mpc status


    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            mpcController.mpcStatus((MPC_controller.OnSSHCommandCompleted) instance);

           if(runnable_active) {
               handler.postDelayed(this, runnable_time_ms);
           }

        }
    };

    public void startStatusRunnable(){
         Log.v(TAG,"startStatusRunnable");
        if(!get_status){
            Log.v(TAG,"startStatusRunnable: won't start cause other options");
            return;
        }
         handler = new Handler();
         handler.postDelayed(runnable, runnable_time_ms);
         runnable_active=true;
    }
    public void stopStatusRunnable(){
         Log.v(TAG,"stopStatusRunnable");
         runnable_active=false;
    }
    @Override
    public void onCreateShellCompleted(boolean success, String error) {
        Log.v(TAG,"onCreateShellCompleted");

        if(getApplicationContext()==null){
            Log.e(TAG,"ooCreateShellCompleted: No Application context");
            return;
        }

        if(success){
            Toast.makeText(getApplicationContext(), "Logged in SSH ", Toast.LENGTH_SHORT).show();
            view_online.setBackgroundResource(R.drawable.indicator_code_lock_point_area_green);
            mpcController.mpcVolume(this,mpcController.current_volume);
            mpcController.mpcPlaylist(this);
            startStatusRunnable();
         }else{
            Toast.makeText(getApplicationContext(), "Login Failure SSH :"+error, Toast.LENGTH_SHORT).show();
            view_online.setBackgroundResource(R.drawable.indicator_code_lock_point_area_red);
        }

    }

    @Override
    public void onSSHCommandCompleted(boolean success, String result, String cmd, int ordinal) {
        Log.v(TAG,"onSSHCommandCompleted "+result);
        if(success){
            if(ordinal==MPC_controller.MpcCommand.PLAYLIST.ordinal()){
                playlistAdapter.setPlaylistSongs(mpcController.getPlaylist());
                  playlistAdapter.notifyDataSetChanged();
            }else if(ordinal==MPC_controller.MpcCommand.ADD.ordinal()){
                  mpcController.mpcPlaylist(this); //refresh list
            }else if(ordinal==MPC_controller.MpcCommand.DEL.ordinal()){
                mpcController.mpcPlaylist(this); //refresh list
            }else if(ordinal==MPC_controller.MpcCommand.STATUS.ordinal()){
                textView_status.setText(result);
            }else if(ordinal==MPC_controller.MpcCommand.ADD.ordinal()){
                editText_link.setText(null);

            }else if(ordinal==MPC_controller.MpcCommand.ADDYT.ordinal()){
                Log.v(TAG,"onSSH yt");
                mpcController.mpcPlaylist(this);
                editText_link.setText(null);

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopStatusRunnable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startStatusRunnable();
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PlaylistAdapter.PlaylistViewHolder viewHolder
                = ((PlaylistAdapter.PlaylistViewHolder) view.getTag());
        if(viewHolder==null||viewHolder.textView_position==null
                ||viewHolder.textView_position.getText()==null)return;

        String posString = viewHolder.textView_position.getText().toString();
        if(posString==null)return;
        mpcController.mpcPlay(this, posString);

    }




    @Override
    public void onItemSelected(com.u1aryz.android.lib.newpopupmenu.MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case MINI_MENU_PLAY:
                mpcController.mpcPlay(this,menuPos);
                break;

            case MINI_MENU_REMOVE:
                mpcController.mpcDelete(this,menuPos);
                break;

        }
        menuPos=null;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
        //Get Pos Data
        PlaylistAdapter.PlaylistViewHolder viewHolder
                = ((PlaylistAdapter.PlaylistViewHolder) v.getTag());

        if(viewHolder==null || viewHolder.textView_position==null
                ||viewHolder.textView_position.getText()==null)return false;

        menuPos = viewHolder.textView_position.getText().toString();

        if(menuPos==null)return false;

        //Create Popup Menu
        PopupMenu menu = new PopupMenu(this);


        menu.setHeaderTitle("What to do?");
        // Set Listener
        menu.setOnItemSelectedListener(this);
        // Add Menu (Android menu like style)
         menu.add(MINI_MENU_PLAY, R.string.play).setIcon(
                 getResources().getDrawable(R.drawable.ic_media_embed_play));


        menu.add(MINI_MENU_REMOVE, R.string.remove).setIcon(
                getResources().getDrawable(R.drawable.ic_menu_delete));

        menu.show(v);

        return true; //Longclick was consumend

    }

     @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            Log.v(TAG,"onProgressChanged: user seeks to "+progress);
             textView_seekpercent.setText(progress + "%");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        Log.v(TAG,"onStopTrackingTouch: user seeks to "+progress);

        mpcController.mpcSeekPercent(this,progress);

    }
}
