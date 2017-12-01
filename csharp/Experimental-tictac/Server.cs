using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Windows.Controls;
using System.Windows;
using System.Text.RegularExpressions;
using System.IO;
namespace Experimental_tictac
{
    class Server
    {
        private NetworkStream stream;
        private Byte[] bytes;
        public string data;
        private TcpClient client;
        public delegate void UpdateTextCallback(string msg);
        public Server()
        {
            
        }
        public void start()
        {
            TcpListener server = null;
            try
            {
                Int32 port = 2945;
                server = new TcpListener(port);
                server.Start();
                
                // buffer for reading data
                bytes = new Byte[256];
                data = null;
                
                // Enter listening loop
                while(true){
                    Console.WriteLine("Waiting for a connection...");
                    
                    // Perfom a blocking call to accept request
                    // Can also user server.AcceptSocket().
                    client = server.AcceptTcpClient();
                    Console.WriteLine("Connected!!!");

                    data = null;

                    // Get a stream object fro reading and wrinting
                    stream = client.GetStream();
                    
                    int i;

                    // Loop to receive all the data sent by the client
                   while ((i = stream.Read(bytes, 0, bytes.Length)) != 0)
                    {
                        // Translate data bytes to a ASCII string.
                        data = System.Text.Encoding.ASCII.GetString(bytes, 0, i);
                        //Console.WriteLine("Received : {0}", data);
                       // Process the data sent by the client.
                        /*txtBlock.Dispatcher.Invoke(
                               new UpdateTextCallback(this.UpdateText),
                               new object[] { data }
                            );*/
                    }
                    // Shutdown and end connection
                   // client.Close();
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("Error : " + e);
            }
            finally
            {
                server.Stop();
            }
            Console.WriteLine("Done!!!!");
        }
        public string getReceivedData(){
            string r = data;
            data = null;
            return r;
        }
        public bool sendMsg(string msg, string data)
        {
            // @param data is the last message sent by the client, so from main activity we gonna specify it.
            string tmp = new Regex("\\S").Replace(data, "");
            msg = new Regex("").Replace(tmp, msg);
            byte[] s = System.Text.Encoding.ASCII.GetBytes(msg);
            
            stream = client.GetStream();
            stream.Write(s, 0, s.Length);
            
            return false;
        }
        public void stopServer()
        {

        }
    }
}
