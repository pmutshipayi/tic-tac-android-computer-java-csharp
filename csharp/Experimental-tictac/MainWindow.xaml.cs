using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Linq;
using System.Net;
namespace Experimental_tictac
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        private Server server;
        private Button[] btns;
        private delegate void updateUIcallBack(string txt);
        private string lastMsgFromClient = null;
        private int playerTurn = 1;   // Player default 0 will this computer.
        private string[] playerSign = {"X", "0" };
        private string otherPlayerName = null;
        private bool isGameRunning = false;
        private ArrayList remotePlayerMoves = new ArrayList();
        private ArrayList localPlayerMoves = new ArrayList();
        private string localPlayerName = "";
        private string remotePlayerName = "";
        private int[,] combinaison = new int[,]{{1, 2, 3},{4, 5, 6}, {7, 8, 9}, {1, 5, 9}, {7, 5, 3}, {1, 4, 7}, {2, 5, 8}, {3, 6, 9}};
        private int countMoves = 0;
        /*                Tic tac toe.
         *  Computer vs android
         *  
         *   Bugs
         *   ****
         *   Can't send 
         * 
         * 
         */
        public MainWindow()
        {
            InitializeComponent();
           // AsyncSocketListenner.StartListening();
            server = new Server();
            //server.CallBack(onReceivedMsg);
           
            Thread thread = new Thread(new ThreadStart(server.start));
            //server.CallBack(onReceivedMsg);
            // Thread receiveThread = new Thread(new ThreadStart(onReceive));
           /// receiveThread.Start();

            thread.Start();
            
            // Initialize btns
            btns = new Button[] { b1, b2, b3, b4, b5, b6, b7, b8, b9};
            for (int i = 0; i < btns.Length; i++)
            {
                Button btn = btns[i];
                btn.Click += this.boardBtnClicked;
             
            }

           // Create a second thread for receiving data
            Thread tThread = new Thread(new ThreadStart(this.mThread));
            tThread.Start();
            //onReceivedMsg("userName$parfait");
            retryBtn.IsEnabled = false;

            IPHostEntry ipHostEntry = Dns.GetHostEntry(Dns.GetHostName());


            ipAddress.Content = "The program is running on : "+Convert.ToString(ipHostEntry.AddressList.FirstOrDefault(address => address.AddressFamily == AddressFamily.InterNetwork));
        }
        public void mThread()
        {
            while (true)
            {
                Thread.Sleep(100);
                string msg = server.getReceivedData();
                if (msg != null)
                {
                    // save this msg in, "private string msg", so that when we will want to send the data to the client, we will use it.
                    //Console.WriteLine(msg);
                    lastMsgFromClient = msg;
                    onReceivedMsg(msg);
                }
            }
        }
        public void onReceivedMsg(string msg)
        {
            string[] sp = msg.Split(new string[] { "$" }, StringSplitOptions.None);
            if (sp[0] == "userName")
            {
                
                this.Dispatcher.Invoke(() =>
                {
                    StringBuilder sb = new StringBuilder();
                    sb.Append(textBox.Text);
                    sb.Append("  VS ");
                    sb.Append(sp[1]);
                    boardInfo.Text = sb.ToString();
                    remotePlayerName = sp[1];
                    localPlayerName = textBox.Text;
                    server.sendMsg("{userName$" + textBox.Text + "}", lastMsgFromClient);
                });
            }
            else if (sp[0] == "move")
            {
                if (playerTurn == 1)
                {
                    // Can play
                    
                    StringBuilder sb = new StringBuilder();
                    sb.Append("b");
                    sb.Append(sp[1].Trim());
                    this.Dispatcher.Invoke(() =>
                    {
                        Button btn = (Button)FindName(sb.ToString());
                        btn.Foreground = (Brush)new BrushConverter().ConvertFrom("#ff0000");
                        //btn.Background = (Brush)new BrushConverter().ConvertFrom("#ff0000");
                        if (btn.Content != null && btn.Content != "")
                        {
                            // The current buttons is used
                            server.sendMsg("{error$"+determineWinner(remotePlayerMoves)+"}", lastMsgFromClient);
                        }
                        else
                        {
                            btn.Content = playerSign[playerTurn];
                            remotePlayerMoves.Add(sp[1]);
                            changePlayer();
                            // Send back to the remote user about the successfull operation

                            server.sendMsg("{move$"+sp[1]+"_"+playerSign[playerTurn] + "}", lastMsgFromClient);

                            if (determineWinner(remotePlayerMoves) != null)
                            {
                                server.sendMsg("{win$"+determineWinner(remotePlayerMoves)+"}", lastMsgFromClient);
                                retryBtn.IsEnabled = true;
                                MessageBox.Show("You lose ");
                                return;
                            }
                            
                        }
                    });
                }
                else
                {
                    // It's not the turn of the remote player,
                    server.sendMsg("{error$not_your_turn}", lastMsgFromClient);
                }
            }
        }
        public void boardBtnClicked(object sender, EventArgs e)
        {
            // When the localPlayer play
            Button btn = sender as Button;

            // Check if it's the turn of the local player
            if (playerTurn == 0)
            {
                // Ok, the localPlayer can play
                // Check if the button is used or not
               
                if (btn.Content != "")
                {
                    // The button is already used
                    MessageBox.Show("Wrong move!!!");
                }
                else
                {
                    // Ok let place the move
                    string move = new Regex("b").Replace(btn.Name.ToString(), "");   // get the digit position
                    btn.Content = playerSign[playerTurn];
                    btn.Foreground = (Brush)new BrushConverter().ConvertFrom("#0094ff");
                    //btn.Background = (Brush)new BrushConverter().ConvertFrom("#0094ff");
                    localPlayerMoves.Add(move);
                    changePlayer();
                    // send the move to the remote player

                    
                    server.sendMsg("{move$"+move+"_"+playerSign[playerTurn]+"}", lastMsgFromClient);
                    if (determineWinner(localPlayerMoves) != null)
                    {
                        server.sendMsg("{lose$"+determineWinner(localPlayerMoves)+"}", lastMsgFromClient);
                        retryBtn.IsEnabled = true;
                        MessageBox.Show("You won the match");
                    }
                }
            }
            else
            {
                MessageBox.Show("It's the turn of " + remotePlayerName);
            }
            
        }
        private void Button_Click(object sender, RoutedEventArgs e)
        {
            newGame();
        }
        /*
         *          Functions dealing with the game
         */
        void newGame()
        {
            retryBtn.IsEnabled = false;
            remotePlayerMoves.Clear();
            localPlayerMoves.Clear();
            for (int i = 0; i < btns.Length; i++)
            {
                Button btn = btns[i];
                btn.Content = "";
                btn.Background = (Brush)new BrushConverter().ConvertFrom("#ffffff");
            }

        }
        private string determineWinner(ArrayList list)
        {
            // Check only the size of "list" is more than 2
            StringBuilder strMatched = new StringBuilder();
            if (list.Count > 2)
            {
                int matched = 0;    // the var will count the matchers
                for (int i = 0; i < 7; i++)
                {
                    for (int j = 0; j < 3; j++)
                    {
                        for (int e = 0; e < list.Count; e++)
                            if (combinaison[i, j] == int.Parse(list[e] + ""))
                            {
                                matched++;
                                strMatched.Append(int.Parse(list[e] + ""));
                                strMatched.Append(",");
                            }
                                
                        if (matched == 3)
                        {
                            return strMatched.ToString();
                        }
                            
                    }
                    matched = 0;
                    strMatched.Clear();
                }
            }    
            return null;
        }
        public void changePlayer()
        {
            if (playerTurn == 0)
                playerTurn = 1;
            else
                playerTurn = 0;
        }
    }
}
