package com.hypernode.sshmediaplayer;

/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/**
 * This program enables you to connect to sshd server and get the shell prompt.
 * $ CLASSPATH=.:../build javac Shell.java
 * $ CLASSPATH=.:../build java Shell
 * You will be asked username, hostname and passwd.
 * If everything works fine, you will get the shell prompt. Output may
 * be ugly because of lacks of terminal-emulation, but you can issue commands.
 *
 */
import android.util.Log;
import android.widget.TextView;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;


public class Shell{
    private static final String TAG = "Shell";
    public String createShellError;
    public String execCommandResult;
    private JSch jsch;
    private Session session;



   private void createUserInfo(final String password){
        UserInfo ui = new MyUserInfo(){
            public void showMessage(String message){
                Log.v(TAG, "MyUserInfo- showMessage: "+message);
            }
            public boolean promptYesNo(String message){
                Log.v(TAG,"MyUserInfo- promptYesNo: "+message);
                //make choice ?
                return true;
            }
            public String getPassphrase(){
                return password;
            }

            public boolean promptPassword(String message){
                return true;
            }
            // If password is not given before the invocation of Session#connect(),
            // implement also following methods,
            // * UserInfo#getPassword(),
            // * UserInfo#promptPassword(String message) and
            // * UIKeyboardInteractive#promptKeyboardInteractive()
        };

        session.setUserInfo(ui);
    }

   public boolean createShellSession(String username,String hostname, final String password,int port){
       createShellError=null;
       try{
           JSch.setLogger(new MyLogger());
           this.jsch= new JSch();
           this.session=jsch.getSession(username,hostname);
           session.setPassword(password);
           createUserInfo(password);
           session.connect(30000); // making a connection with timeout.


           return true;
       }catch (Exception ex){
           Log.e(TAG,"createShell Exception: "+ex);
           createShellError=ex.toString();
           return false;
       }
   }
    public boolean execCommand(String command){
        execCommandResult=null;
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();
            channel.connect();

            //waitForChannelClosed(channel);
            while (true) { //todo add timeout or smt
                if (channel.isClosed()) {
                    break;
                }
            }
            channel.disconnect();
            String result;
            if (channel.getExitStatus() != 0) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stderr));
                result = readAll(bufferedReader);
                Log.e(TAG, "Exited with error: " + result);
                execCommandResult=result;
                return false;
            } else {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stdout));
                result = readAll(bufferedReader);
                execCommandResult=result;

                Log.v(TAG, "Exited normally: " + result);
                return true;
            }

        }catch(Exception ex){
            Log.e(TAG,"ExecCommand Exception: ",ex);
            execCommandResult=ex.toString();
            return false;
        }

    }
    public String readAll(BufferedReader bufferedReader){
        try {
            StringBuilder sb = new StringBuilder();
            final int BUFFSIZE =1024;

            char[] chars = new char[BUFFSIZE];
            while(true) { //Todo test for bigger answers
                int count = bufferedReader.read(chars);
                if(count==-1){
                    return "-1";
                }
                char[] charShort = Arrays.copyOfRange(chars, 0, count);
                sb.append(String.valueOf(charShort));
                if(count<BUFFSIZE){
                    break;
                }else{
                    chars = new char[BUFFSIZE];
                }
            }
            String text= sb.toString();

            return  text;
        } catch (Exception e) {
            Log.e(TAG,"readAll Exception: ",e);
            return "-1";
        }
    }
    @Deprecated
    public boolean createShell(String username,String hostname, final String password,int port,
                        final TextView tv_output){
        try{

            JSch.setLogger(new MyLogger());
            JSch.setConfig("StrictHostKeyChecking", "no");

            this.jsch  = new JSch();
            //HostKeyRepository hkr = jsch.getHostKeyRepository();
            //ConfigRepository cr = jsch.getConfigRepository();
             this.session=jsch.getSession(username,hostname);
            //Session session=jsch.getSession(username, hostname, port);
             session.setPassword(password);

            createUserInfo(password);

            // It must not be recommended, but if you want to skip host-key check,
            // invoke following,
             session.setConfig("StrictHostKeyChecking", "no");


            //Cipher server to client/client to server
            session.setConfig("cipher.s2c", "aes256-ctr,hmac-sha1-96");
            session.setConfig("cipher.c2s", "aes256-ctr,hmac-sha1-96");

             session.setConfig("CheckCiphers", "aes128-cbc");

            //session.connect();
            session.connect(30000); // making a connection with timeout.

            //Channel channel=session.openChannel("shell");
            Channel channel = (Channel)session.openChannel("exec");

            channel.setInputStream(null);
            // Enable agent-forwarding.
            //((ChannelShell)channel).setAgentForwarding(true);
           // this.inputStream = channel.getInputStream();
            //BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            //((ChannelShell)channel).start();
            OutputStream outputStream = System.out;

            channel.setOutputStream(outputStream, true);
           // ((ChannelExec)channel).setCommand("mpc play");

            channel.connect(3*1000);
            channel.disconnect();
            /*
             while(true){
                String line;
                 int bytes;
                 int offset=0;
                 String string="";
                 bytes=reader.read();
                 if(bytes!=0){
                     char[] chars = new char[bytes];
                     int done_read= reader.read(chars,offset,bytes);
                     offset=offset+done_read;
                     string  = chars.toString();
                 }

                if(string=="exit"){
                    break;
                }
            }
            */
            return true;
        }
        catch(Exception e){
            Log.e(TAG,"createShell Exception",e);
            return false;
        }
    }
    @Deprecated
    public void writeToShell(String command){
        Log.v(TAG,"writeToShell");

        try {
            //Channel channel=session.openChannel("shell");
            Channel channel = (Channel) session.openChannel("exec");

             ((ChannelExec) channel).setCommand(command);
            channel.connect(3 * 1000);
            channel.disconnect();
        }catch(Exception ex){
            Log.v(TAG,"writeToShell Exception: "+ex);
        }

    }
    public static abstract class MyUserInfo
            implements UserInfo, UIKeyboardInteractive{
        public String getPassword(){ return null; }
        public boolean promptYesNo(String str){ return false; }
        public String getPassphrase(){ return null; }
        public boolean promptPassphrase(String message){ return false; }
        public boolean promptPassword(String message){ return false; }
        public void showMessage(String message){ }
        public String[] promptKeyboardInteractive(String destination,
                                                  String name,
                                                  String instruction,
                                                  String[] prompt,
                                                  boolean[] echo){
            return null;
        }
    }
    public static class MyLogger implements com.jcraft.jsch.Logger {
        static java.util.Hashtable name=new java.util.Hashtable();
        static{
            name.put(new Integer(DEBUG), "DEBUG: ");
            name.put(new Integer(INFO), "INFO: ");
            name.put(new Integer(WARN), "WARN: ");
            name.put(new Integer(ERROR), "ERROR: ");
            name.put(new Integer(FATAL), "FATAL: ");
        }
        public boolean isEnabled(int level){
            return true;
        }
        public void log(int level, String message){
            Log.v(TAG,"Logger: "+name.get(new Integer(level))
                    +message);
        }
    }
}