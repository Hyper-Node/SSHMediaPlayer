package com.hypernode.sshmediaplayer;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johannes on 06.07.14.
 * Control a linux mpc audio player
 * via ssh
 */
public class MPC_controller extends Shell{
    public List<Song> getPlaylist() {
        return playlist;
    }


    public int current_volume = 80;

    final static String TAG="MPC_controller";
    private List<Song> playlist;

    public MPC_controller() {
        this.playlist=new ArrayList<Song>();
    }

    public void createSSHshell(OnCreateShellCompleted onCreateShellCompleted,LoginParameters loginParameters){

        CreateShellTask createShellTask = new CreateShellTask(onCreateShellCompleted,loginParameters);
        createShellTask.execute();

    }
    private class CreateShellTask extends AsyncTask<LoginParameters, Integer, Boolean> {
        private OnCreateShellCompleted listener;
        private LoginParameters parameters;
        public CreateShellTask(OnCreateShellCompleted onCreateShellCompleted,LoginParameters parameters){
            this.listener=onCreateShellCompleted;
            this.parameters=parameters;
        }
        @Override
        protected Boolean doInBackground(LoginParameters... params) {
            boolean success = createShellSession(parameters.username,parameters.hostname,
                    parameters.password,parameters.port);
            return success;
        }

        protected void onPostExecute(Boolean result) {
            this.listener.onCreateShellCompleted(result,createShellError);
        }

    }
    public interface OnCreateShellCompleted{
        void onCreateShellCompleted(boolean success, String error);
    }

    public void executeSSHCommand(String cmd,int ordinal, OnSSHCommandCompleted onSSHCommandCompleted){
        ExecuteSSHCommandTask executeSSHCommandTask = new ExecuteSSHCommandTask(onSSHCommandCompleted,ordinal);
        executeSSHCommandTask.execute(cmd);
    }

    private class ExecuteSSHCommandTask extends AsyncTask<String, Integer, Boolean> {
        private OnSSHCommandCompleted listener;
        private int ordinal;
        private String cmd;

        public ExecuteSSHCommandTask(OnSSHCommandCompleted onSSHCommandCompleted,int ordinal){
            this.listener=onSSHCommandCompleted;
            this.ordinal = ordinal;
        }
        @Override
        protected Boolean doInBackground(String... cmd) {

            this.cmd = cmd[0];
            return  execCommand(cmd[0]);
        }

        protected void onPostExecute(Boolean result) {
            parseResult(execCommandResult,cmd,ordinal);
            this.listener.onSSHCommandCompleted(result,execCommandResult,cmd,ordinal);

        }


    }
    public void parseResult(String result,String cmd,int ordinal){
        if(ordinal==MPC_controller.MpcCommand.PLAYLIST.ordinal()){

            String[] resultLines = result.split("\n");
            playlist = new ArrayList<Song>(resultLines.length);

            for(int i=0;i<resultLines.length;i++){
                String[] singleResult = resultLines[i].split("\t");

                    try {
                        Song song = new Song();
                        if(singleResult.length>=1) {
                            song.position = Integer.valueOf(singleResult[0]);
                        }
                        if(singleResult.length>=2) {
                            song.artist = singleResult[1];
                        }
                        if(singleResult.length>=3) {
                            song.title = singleResult[2];
                        }
                        if(singleResult.length>=4) {
                            song.file = singleResult[3];
                        }
                        if(singleResult.length>=5) {
                            song.time = singleResult[4];
                        }
                        playlist.add(song);

                    }catch (Exception ex){
                        Log.e(TAG,"parseResult Exception",ex);
                    }

            }

        }else if(ordinal==MPC_controller.MpcCommand.STATUS.ordinal()){

        }
    }
    public interface OnSSHCommandCompleted{
        void onSSHCommandCompleted(boolean success, String result,String cmd,int ordinal);
    }

    public void mpcAdd(OnSSHCommandCompleted onSSHCommandCompleted,String addlink){
        String cmd = MpcCommand.ADD.getCmd()+" "+addlink;
        executeSSHCommand(cmd,MpcCommand.ADD.ordinal(),onSSHCommandCompleted);
    }

    public void mpcPlay(OnSSHCommandCompleted onSSHCommandCompleted){
        String cmd = MpcCommand.PLAY.getCmd();
        executeSSHCommand(cmd,MpcCommand.PLAY.ordinal(),onSSHCommandCompleted);
    }
    public void mpcPlay(OnSSHCommandCompleted onSSHCommandCompleted,String playlistPosition){
        String cmd = MpcCommand.PLAY.getCmd()+" "+playlistPosition;
        executeSSHCommand(cmd,MpcCommand.PLAY.ordinal(),onSSHCommandCompleted);
    }
    public void mpcDelete(OnSSHCommandCompleted onSSHCommandCompleted,String playlistPosition){
        String cmd = MpcCommand.DEL.getCmd()+" "+playlistPosition;
        executeSSHCommand(cmd,MpcCommand.DEL.ordinal(),onSSHCommandCompleted);
    }
    public void mpcPause(OnSSHCommandCompleted onSSHCommandCompleted){
        String cmd = MpcCommand.PAUSE.getCmd();
        executeSSHCommand(cmd,MpcCommand.PAUSE.ordinal(),onSSHCommandCompleted);
    }
    public void mpcNext(OnSSHCommandCompleted onSSHCommandCompleted){
        String cmd = MpcCommand.NEXT.getCmd();
        executeSSHCommand(cmd,MpcCommand.NEXT.ordinal(),onSSHCommandCompleted);
    }
    public void mpcPrev(OnSSHCommandCompleted onSSHCommandCompleted){
        String cmd = MpcCommand.PREV.getCmd();
        executeSSHCommand(cmd,MpcCommand.PREV.ordinal(),onSSHCommandCompleted);
    }
    public void mpcStop(OnSSHCommandCompleted onSSHCommandCompleted){
        String cmd = MpcCommand.STOP.getCmd();
        executeSSHCommand(cmd,MpcCommand.STOP.ordinal(),onSSHCommandCompleted);
    }
    public void mpcVolume(OnSSHCommandCompleted onSSHCommandCompleted,int value){
        if(value>100){
            value=100;
        }
        if(value<0){
            value=0;
        }
        String cmd = MpcCommand.VOLUME.getCmd()+" "+value;
        executeSSHCommand(cmd,MpcCommand.VOLUME.ordinal(),onSSHCommandCompleted);
    }
    public void mpcPlaylist(OnSSHCommandCompleted onSSHCommandCompleted){
        String cmd = MpcCommand.PLAYLIST.getCmd();
        executeSSHCommand(cmd,MpcCommand.PLAYLIST.ordinal(),onSSHCommandCompleted);
    }
    public void mpcStatus(OnSSHCommandCompleted onSSHCommandCompleted){
        String cmd = MpcCommand.STATUS.getCmd();
        executeSSHCommand(cmd,MpcCommand.STATUS.ordinal(),onSSHCommandCompleted);
    }

    public void mpcRandom(OnSSHCommandCompleted onSSHCommandCompleted,boolean on){
        String param="";
        if(on){
            param+=" on";
        }else{
            param+=" off";
        }
        String cmd = MpcCommand.RANDOM.getCmd()+param;
        executeSSHCommand(cmd,MpcCommand.RANDOM.ordinal(),onSSHCommandCompleted);
    }
    public void mpcConsume(OnSSHCommandCompleted onSSHCommandCompleted,boolean on){
        String param="";
        if(on){
            param+=" on";
        }else{
            param+=" off";
        }
        String cmd = MpcCommand.CONSUME.getCmd()+param;
        executeSSHCommand(cmd,MpcCommand.CONSUME.ordinal(),onSSHCommandCompleted);
    }

    public void mpcSeekPercent(OnSSHCommandCompleted onSSHCommandCompleted,int percent){

        if(percent>100){
            percent=100;
        }else if(percent<0){
            percent=0;
        }

        String cmd = MpcCommand.SEEK.getCmd()+" "+percent+"%";
        executeSSHCommand(cmd,MpcCommand.SEEK.ordinal(),onSSHCommandCompleted);

    }

    //   "mpc playlist -f \"[%position%\t%artist%\t%title%\t%file%\t%time%]\""
    public void mpcAddYTLink(OnSSHCommandCompleted onSSHCommandCompleted,String ytLink){

        String ytId="";
        if(ytLink!=null &&ytLink!=""){
           String[] splitLink = ytLink.split("/");
            ytId= splitLink[splitLink.length-1];
            if(ytId.length()!=11){

                try{
                    ytId = ytLink.substring(ytLink.length()-11);
                }catch (Exception ex){
                    Log.e(TAG,"Exception getting ytId");
                    return;
                }
            }
        }else{
            Log.e(TAG,"mpcAddYTLink problem with Link ");
            return;
        }
        String cmd = MpcCommand.ADDYT.getCmd()+" "+ytId+")";
        executeSSHCommand(cmd,MpcCommand.ADDYT.ordinal(),onSSHCommandCompleted);

    }
    public class Song{
        public  int position=-1;
        public  String artist;
        public  String title;
        public  String file;
        public  String time;
    }


    public enum MpcCommand {

        PLAY("mpc play"),
        PAUSE("mpc pause"),
        STOP("mpc stop"),
        NEXT("mpc next"),
        PREV("mpc prev"),
        ADD("mpc add"),             //add <url>  (http only)
        ADDYT("mpc add $(youtube-dl --prefer-insecure -g -f140"), //Add youtube link to playlist param yt id sTPtBvcYkO8
        CLEAR("mpc clear"),         //removes all songs from playlist
        DEL("mpc del"),             //deletes song at <position>
        VOLUME("mpc volume"),       //sets volume to <value>
        PLAYLIST("mpc playlist -f \"[%position%\\t%artist%\\t%title%\\t%file%\\t%time%\t]\""),
        STATUS("mpc status"),
        CONSUME("mpc consume"),
        SEEK("mpc seek"),
        RANDOM("mpc random");


        private String cmd;

        private MpcCommand(String cmd){
            this.cmd = cmd;
        }

        public String getCmd(){
            return this.cmd;
        }
    }

}


