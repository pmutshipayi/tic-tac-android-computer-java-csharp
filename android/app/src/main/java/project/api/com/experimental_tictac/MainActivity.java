package project.api.com.experimental_tictac;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.net.InterfaceAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private ClientAsyncTask clientAsyncTask;
    private int[] btnID = new int[]{R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9};
    private EditText editText, ipAddress;
    private Button connectBtn, retryBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        for(int i = 0; i < btnID.length; i++){
            final Button btn = (Button)findViewById(btnID[i]);
            final int t = i;
            btn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    btn.setTextColor(Color.rgb(100, 50, 200));
                    String idName = v.getResources().getResourceName(v.getId());
                    try{
                        clientAsyncTask.sendMsg("move$"+idName.split("/")[1].replaceAll("button", ""));
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(), "error : "+e, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        editText = (EditText)findViewById(R.id.editText);
        connectBtn = (Button)findViewById(R.id.button10);
        retryBtn = (Button)findViewById(R.id.retryBtn);
        retryBtn.setEnabled(false);
        ipAddress = (EditText)findViewById(R.id.ipAddress);

        connectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String userName = editText.getText().toString();
                if(userName != null && !userName.isEmpty())
                    userName = "userName$"+userName;
                else
                    userName = "userName$Android";

                // On receive data from the server.

                clientAsyncTask = new ClientAsyncTask(getApplicationContext(), new Handler(), userName, ipAddress.getText().toString(), new ClientAsyncTask.onReceivedMsg() {
                    @Override
                    public void receivedMsg(String msg) {
                        String[] sp = msg.split("\\$");
                        if(sp[0].trim().equals("userName")){

                        }else if(sp[0].trim().equals("move")){
                            /*      When validating a move
                             *   sp[0] = move
                             *   ss[0] = pos
                             *   ss[1] = sign
                             */
                            String[] ss = sp[1].split("_");
                            Button btn = null;
                             switch (Integer.parseInt(ss[0].trim())){
                                 case 1:
                                     btn = (Button)findViewById(R.id.button1);
                                     break;
                                 case 2:
                                     btn = (Button)findViewById(R.id.button2);
                                     break;
                                 case 3:
                                     btn = (Button)findViewById(R.id.button3);
                                     break;
                                 case 4:
                                     btn = (Button)findViewById(R.id.button4);
                                     break;
                                 case 5:
                                     btn = (Button)findViewById(R.id.button5);
                                     break;
                                 case 6:
                                     btn = (Button)findViewById(R.id.button6);
                                     break;
                                 case 7:
                                     btn = (Button)findViewById(R.id.button7);
                                     break;
                                 case 8:
                                     btn = (Button)findViewById(R.id.button8);
                                     break;
                                 case 9:
                                     btn = (Button)findViewById(R.id.button9);
                                     break;
                             }
                             if(btn != null){
                                 try{
                                     String x = ss[1];
                                     btn.setText("X");
                                     btn.setTextColor(Color.parseColor("#ff0000"));
                                 }catch (Exception e){
                                     btn.setText("O");
                                     btn.setTextColor(Color.parseColor("#0094ff"));
                                 }
                             }
                        }else if(sp[0].trim().equals("error")){
                            Toast.makeText(getApplicationContext(), sp[1], Toast.LENGTH_SHORT).show();
                        }else if(sp[0].trim().equals("win")){
                            Toast.makeText(getApplicationContext(), "Congratulation !\nyou won the match.", Toast.LENGTH_SHORT).show();
                            coloredSquare(0, sp[1]);
                        }
                        else if(sp[0].trim().equals("lose")){
                            Toast.makeText(getApplicationContext(), "You lost.", Toast.LENGTH_LONG).show();
                            coloredSquare(1, sp[1]);
                        }


                    }
                });
                clientAsyncTask.execute();
            }
        });
    }
    void newGame(){
        // Clear everything.
        for(int i = 0; i < btnID.length; i++){
            Button btn = (Button)findViewById(btnID[i]);
            btn.setText("");
            btn.setTextColor(Color.BLACK);
            btn.setBackground(getResources().getDrawable(R.drawable.btn_border));
        }
        retryBtn.setEnabled(false);
    }
    void coloredSquare(int type, String args){
        /* @param
        *   type == 0, the local user won, so paint the square to black
        *   type == 1, it means the remote player won, so paint the square red
        */
        String sp[] = args.split(",");
        for(int i = 0; i < sp.length; i++){
            if(sp[i] != null && !sp[i].isEmpty()){
                Button btn = null;
                try{
                    btn = (Button)findViewById(btnID[Integer.parseInt(sp[i])-1]);
                    if(type == 0){
                        btn.setTextColor(Color.WHITE);
                        btn.setBackgroundColor(Color.GREEN);
                    }else{
                        btn.setTextColor(Color.WHITE);
                        btn.setBackgroundColor(Color.RED);
                    }
                }catch (Exception e){}
            }
        }
        retryBtn.setEnabled(true);
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
