package project.api.com.experimental_tictac;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by parfait mutshipayi mbm on 9/22/2017.
 */

class TCPClient {
    private static final String TAG = "TCPClient";
    private  Handler mHandler;
    private String ipNumber, incomingMessage, command;
    private BufferedReader in;
    private PrintWriter out;
    private MessageCallBack listener = null;
    private boolean mRun = false;

    public TCPClient(Handler mHandler, String command, String ipNumber, MessageCallBack listener){
        this.listener = listener;
        this.ipNumber = ipNumber;
        this.command = command;
        this.mHandler = mHandler;
    }
    public void sendMessage(String message){
        if(out != null && !out.checkError()){
            out.println(message);
            out.flush();
            //mHandler.sendEmptyMessageDelayed(MainActivity.SEND);
            Log.d(TAG, "Message : "+ message+" sent to "+ipNumber);
        }
    }
    public void stopClient(){
        Log.d(TAG, "Client stopped");
        mRun = false;
    }
    public void run(){
        mRun = true;
        try{
            // Creating the InetAddress object from ipNumber passed via constructor from IpGetter class
            InetAddress serverAddress = InetAddress.getByName(ipNumber);
            Log.d(TAG, "Connecting...");
           // mHandler.sendEmptyMessageDelayed(MainActivity.CONNECTING, 100);
            // Here socket is created with hardcoded port
            Socket socket = new Socket(serverAddress, 2945);

            try{
                // Create PrintWriter object for sending the messages to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                // Create the Buffered reader for receiving the message from the server
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Log.d(TAG, "In/out created");

                // sending the message with the command specified by AsyncTask
                this.sendMessage(command);
                //mHandler.sendEmptyMessageDelayed(MainActivity.SENDING, 2000);

                // listen for the incoming message while mRun is true
                while(mRun){
                    incomingMessage = in.readLine();
                    if(incomingMessage != null && listener != null){
                        /*
                             Incoming message is passed to MessageCallBack object.
                             next it is retrieved by AsynTask and passed to onPublishProgress method.
                         */
                        listener.callbackMessageReceiver(incomingMessage);
                    }
                    incomingMessage = null;
                }
                Log.d(TAG, "Received message "+incomingMessage);
            }catch (Exception e){
                Log.d(TAG, "ERROR : "+e);
            }finally {
                out.flush();
                out.close();
                in.close();
                socket.close();
               // mHandler.sendEmptyMessageDelayed(MainActivity.SENT, 3000);
                Log.d(TAG, "Socket closed");
            }
        }catch (Exception e){
            Log.d(TAG, "ERROR => "+ e);
            //mHandler.sendEmptyMessageDelayed(MainActivity.ERROR, 2000);
        }
    }
    public interface MessageCallBack {
        void callbackMessageReceiver(String message);
    }
}
