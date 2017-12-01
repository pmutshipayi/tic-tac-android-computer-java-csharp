package project.api.com.experimental_tictac;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by parfait mutshipayi on 9/21/2017.
 */

public class ClientAsyncTask extends AsyncTask<String, String, TCPClient>{
    private static String COMMAND = "shutdown -s";
    private TCPClient tcpClient;
    private Handler mHandler;
    private static final String TAG = "ClientAsyncTask";
    private Context activityCtx;
    private onReceivedMsg rMsg;
    private String ipAddress;
    /*
         The ClientAsyncTask constructor width handler passed as argument.
     */
    public ClientAsyncTask(Context ctx, Handler mHandler, String msg, String ipAddress, onReceivedMsg onMsg){
        this.mHandler = mHandler;
        this.COMMAND = msg;
        this.activityCtx = ctx;
        this.rMsg = onMsg;
        this.ipAddress = ipAddress;
    }
    public void sendMsg(String msg){
        tcpClient.sendMessage(msg);
    }
    @Override
    protected TCPClient doInBackground(String... params){
        Log.d(TAG, "In do in background");
        try{
            tcpClient =  new TCPClient(mHandler, COMMAND, ipAddress, new TCPClient.MessageCallBack(){
                @Override
                public void callbackMessageReceiver(String message){
                    publishProgress(message);
                }
            });
        }catch (Exception e){
            Log.d(TAG, "Caught null pointer exception");
        }
        tcpClient.run();
        return null;
    }
    @Override
    protected void onProgressUpdate(String... values){
        super.onProgressUpdate(values);
        StringBuilder sb = new StringBuilder();
        for(String s : values){
            sb.append(s);
        }
        Log.d(TAG, "on progress update, values : "+ sb.toString());
        String[] sp = sb.toString().split("\\}");
        String msgFromServer = "";
        try{
            msgFromServer = sp[1].replaceAll("[{}]", "");
        }catch (Exception e){
            msgFromServer = sp[0].replaceAll("[{}]", "");
        }
        //Toast.makeText(activityCtx, "Received : "+msgFromServer, Toast.LENGTH_SHORT).show();
       // Toast.makeText(activityCtx, "Received : "+sb.toString(), Toast.LENGTH_SHORT).show();
        rMsg.receivedMsg(msgFromServer);
    }
    @Override
    protected void onPostExecute(TCPClient result){
        super.onPostExecute(result);
        Log.d(TAG, "IN on post execute");
        // fix later
        if(result != null){
            //result.stopClient();
        }
    }
    public interface onReceivedMsg{
        void receivedMsg(String msg);
    }
}
