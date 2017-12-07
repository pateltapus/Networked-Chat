import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


public class ChatServer extends JFrame {

  // GUI items
  JButton ssButton;
  JLabel machineInfo;
  JLabel portInfo;
  JTextArea history;
  private boolean running;

  // Network Items
  boolean serverContinue;
  ServerSocket serverSocket;
  Vector <PrintWriter> outStreamList;
  ArrayList<String> Username_pkey;
  ArrayList<String> userName;

   // set up GUI
   public ChatServer()
   {
      super( "Chat Server" );

      // set up the username list
      Username_pkey = new ArrayList<String>();
      userName = new ArrayList<String>();

      // set up the shared outStreamList
      outStreamList = new Vector<PrintWriter>();

      // get content pane and set its layout
      Container container = getContentPane();
      container.setLayout( new FlowLayout() );

      // create buttons
      running = false;
      ssButton = new JButton( "Start Listening" );
      ssButton.addActionListener( e -> doButton (e) );
      container.add( ssButton );

      String machineAddress = null;
      try
      {
        InetAddress addr = InetAddress.getLocalHost();
        machineAddress = addr.getHostAddress();
      }
      catch (UnknownHostException e)
      {
        machineAddress = "127.0.0.1";
      }
      machineInfo = new JLabel (machineAddress);
      container.add( machineInfo );
      portInfo = new JLabel (" Not Listening ");
      container.add( portInfo );

      history = new JTextArea ( 10, 40 );
      history.setEditable(false);
      container.add( new JScrollPane(history) );

      setSize( 500, 250 );
      setVisible( true );

   } // end CountDown constructor

   public static void main( String args[] )
   {
      ChatServer application = new ChatServer();
      application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
   }

    // handle button event
    public void doButton( ActionEvent event )
    {
       if (running == false)
       {
         new ConnectionThread (this);
       }
       else
       {
         serverContinue = false;
         ssButton.setText ("Start Listening");
         portInfo.setText (" Not Listening ");
       }
    }


 } // end class ChatServer


class ConnectionThread extends Thread
 {
   ChatServer gui;

   public ConnectionThread (ChatServer es3)
   {
     gui = es3;
     start();
   }

   public void run()
   {
     gui.serverContinue = true;

     try
     {
       gui.serverSocket = new ServerSocket(0);
       gui.portInfo.setText("Listening on Port: " + gui.serverSocket.getLocalPort());
       System.out.println ("Connection Socket Created");
       try {
         while (gui.serverContinue)
         {
           System.out.println ("Waiting for Connection");
           gui.ssButton.setText("Stop Listening");
           new CommunicationThread (gui.serverSocket.accept(), gui, gui.outStreamList, gui.Username_pkey, gui.userName);
         }
       }
       catch (IOException e)
       {
         System.err.println("Accept failed.");
         System.exit(1);
       }
     }
     catch (IOException e)
     {
       System.err.println("Could not listen on port: 10008.");
       System.exit(1);
     }
     finally
     {
       try {
         gui.serverSocket.close();
       }
       catch (IOException e)
       {
         System.err.println("Could not close port: 10008.");
         System.exit(1);
       }
     }
   }
 }


class CommunicationThread extends Thread
{
 //private boolean serverContinue = true;
 private Socket clientSocket;
 private ChatServer gui;
 private Vector<PrintWriter> outStreamList;
 private ArrayList<String> Username_pkey; //CODE ADDED BY MANNY
 private ArrayList<String> userName;


 public CommunicationThread (Socket clientSoc, ChatServer ec3,
                             Vector<PrintWriter> oSL, ArrayList<String> un_pk, ArrayList<String> uN)
   {
    clientSocket = clientSoc;
    gui = ec3;
    outStreamList = oSL;
    Username_pkey = un_pk;
    userName = uN;

    gui.history.insert ("Comminucating with Port" + clientSocket.getLocalPort()+"\n", 0);
    start();
   }

 public void run()
   {
    System.out.println ("New Communication Thread Started");

    try {
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
                                      true);
         outStreamList.add(out);

         BufferedReader in = new BufferedReader(
                 new InputStreamReader( clientSocket.getInputStream()));

         String inputLine;

         while ((inputLine = in.readLine()) != null)
             {


               // this is just in charge of sending the client list to the clients
              if(inputLine.charAt(0) == '*')
              {

                Username_pkey.add(inputLine); // adds both the username and the
                System.out.println("We have a new user  " + inputLine);
                String token = "[*]";
                String[] array = inputLine.split(token);
                System.out.println("Added user to list " + array[1]); // just debug output
                userName.add(array[1]);// adds the username to the arrayList


                // this handles sending all the client names to all the clients
                for ( PrintWriter out1: outStreamList )
                {
                  System.out.println ("Sending ClientList");
                  for(String userkey: Username_pkey)
                  {
                    out1.println (userkey);
                  }
                }
              // END OF SEND CLIENT LIST

              }


              // will send a message to all of the clients
              else
              {
                System.out.println ("Server: " + inputLine);
                gui.history.insert (inputLine+"\n", 0);
                String token = "[*]";
                String[] array = inputLine.split(token);

                // add a if else statment here
                // if statment will send only to matches of username

                // Loop through the outStreamList and send to all "active" streams
                //out.println(inputLine);
                if(array[0] == "ALL"){
                  for ( PrintWriter out1: outStreamList )
                  {
                    System.out.println ("Sending Message");
                    out1.println (inputLine);
                  }
                }
                // sending to individual person
                else
                {
                  System.out.println(" This is array[0] ");
                  int outputStreamIndex = userName.indexOf(array[1]);
                  PrintWriter temp = outStreamList.get(outputStreamIndex);
                  temp.println(inputLine);
                }

              }



              if (inputLine.equals("Bye."))
                  break;

              if (inputLine.equals("End Server."))
                  gui.serverContinue = false;
             }

         outStreamList.remove(out);
         out.close();
         in.close();
         clientSocket.close();
        }
    catch (IOException e)
        {
         System.err.println("Problem with Communication Server");
         //System.exit(1);
        }
    }
}
