import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.Position.Bias;

public class ChatClient extends JFrame implements ActionListener
{
  // GUI items
  JButton sendButton;
  JButton connectButton;
  JTextField machineInfo;
  JTextField portInfo;
  JTextField message;
  JTextArea history;

  JList<String> listUsers;
  DefaultListModel<String> listModelUsers = new DefaultListModel<>();

  //Username array
  String[] userNames = { "All" };
  String userName;

  //SHA ints
  int pValue;
  int qValue;

  // Network Items
  boolean connected;
  Socket echoSocket;
  PrintWriter out;
  BufferedReader in;

  // send username flag
  boolean uflag = false;

   // set up GUI
   public ChatClient()
   {
      super( "Echo Client" );

      // get content pane and set its layout
      Container container = getContentPane();
      container.setLayout (new BorderLayout ());

      // set up the North panel
      JPanel upperPanel = new JPanel ();
      upperPanel.setLayout (new GridLayout (1,3));
      container.add (upperPanel, BorderLayout.NORTH);

      // create buttons
      connected = false;

      //upperPanel.add ( new JLabel ("Message: ", JLabel.RIGHT) );
      //message = new JTextField ("");
      //message.addActionListener( this );
      //upperPanel.add( message );

      //sendButton = new JButton( "Send Message" );
      //sendButton.addActionListener( this );
      //sendButton.setEnabled (false);
      //upperPanel.add( sendButton );

      upperPanel.add ( new JLabel ("Server Address: ", JLabel.RIGHT) );
      machineInfo = new JTextField ("127.0.0.1");
      upperPanel.add( machineInfo );

      upperPanel.add ( new JLabel ("Server Port: ", JLabel.RIGHT) );
      portInfo = new JTextField ("");
      upperPanel.add( portInfo );

      connectButton = new JButton( "Connect to Server" );
      connectButton.addActionListener( this );
      upperPanel.add( connectButton );

      // set up the center panel
      JPanel centerPanel = new JPanel ();
      centerPanel.setLayout (new GridLayout (1,2));
      container.add (centerPanel, BorderLayout.CENTER);

      //list with all the users
      listModelUsers.addElement("All Users");
      listUsers = new JList<>(listModelUsers);
      listUsers.setVisibleRowCount(6);
      listUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      JScrollPane scrollPane = new JScrollPane(listUsers);
      Dimension d = listUsers.getPreferredSize();
      d.width = 100;
      d.height = 150;
      scrollPane.setPreferredSize(d);
      centerPanel.add(scrollPane);

      //chat history
      history = new JTextArea ( 10, 30 );
      history.setEditable(false);
      centerPanel.add( new JScrollPane(history) );

      // set up the lower panel
      JPanel lowerPanel = new JPanel ();
      lowerPanel.setLayout (new GridLayout (1,3));
      container.add (lowerPanel, BorderLayout.SOUTH);

      lowerPanel.add ( new JLabel ("Message: ") );
      message = new JTextField ("");
      message.addActionListener( this );
      lowerPanel.add( message );

      sendButton = new JButton( "Send Message" );
      sendButton.addActionListener( this );
      sendButton.setEnabled (false);
      lowerPanel.add( sendButton );

      pValue = 0;
      qValue = 0;

      setSize( 500, 300 );
      setVisible( true );

   } // end CountDown constructor

   public static void main( String args[] )
   {
      ChatClient application = new ChatClient();
      application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
   }

    // handle button event
    public void actionPerformed( ActionEvent event )
    {

       if ( connected &&
           (event.getSource() == sendButton ||
            event.getSource() == message ) )
       {
         doSendMessage();
       }
       else if (event.getSource() == connectButton)
       {
         JTextField nameField = new JTextField(5);
         JTextField pValueField = new JTextField(5);
         JTextField qValueField = new JTextField();

         //Add this part to a loop until a unique user is entered
         JPanel myPanel = new JPanel(new GridLayout(2,3,5,5));
         myPanel.add(new JLabel("Enter username"));
         myPanel.add(nameField);
         //myPanel.add(Box.createHorizontalStrut(15)); // a spacer
         myPanel.add(new JLabel("Enter p value:"));
         myPanel.add(pValueField);
         //myPanel.add(Box.createHorizontalStrut(15)); // a spacer
         myPanel.add(new JLabel("Enter q value:"));
         myPanel.add(qValueField);
         myPanel.add(Box.createHorizontalStrut(15)); // a spacer


         while(true){
             int result = JOptionPane.showConfirmDialog(null, myPanel,
               "Please Enter your info", JOptionPane.OK_CANCEL_OPTION);

             if (result == JOptionPane.OK_OPTION)//IF the ok option is selected
             {
               //Parse info from text fields
               userName = nameField.getText();
               if(!pValueField.getText().equals(""))
                 pValue =  Integer.parseInt(pValueField.getText());
               else
                 pValue = 0;

               if(!qValueField.getText().equals(""))
                qValue =  Integer.parseInt(qValueField.getText());
               else
                 qValue = 0;


               DefaultListModel model = (DefaultListModel)listUsers.getModel();
               // If list contains username
               if(model.contains(userName)) {
                 JOptionPane.showMessageDialog(null, "Username entered is already in used select a new one");
                 continue;
               }
               else{//if its unique

                 doManageConnection();
                 break;
               }
             } //END OF IF OK CLICKED

             else if (result == JOptionPane.CANCEL_OPTION)//If cancel was selected
             {
                JOptionPane.showConfirmDialog(null, "Cancel button was selected",
                   "Sign in canceled", JOptionPane.OK_CANCEL_OPTION);
                break;
             }

             else{//Exit loop if anything else is pressed
               break;
             }
         }


         // send the username to the server
         message.setText("*" + userName + "*" + "8");
         doSendMessage();
         message.setText(" ");

       }
    }

    public void doSendMessage()
    {
      try
      {
        out.println(message.getText());
        //history.insert ("From Server: " + in.readLine() + "\n" , 0);
      }
      catch (Exception e)
      {
        history.insert ("Error in processing message ", 0);
      }
    }

    public void doManageConnection()
    {
      if (connected == false)
      {
        String machineName = null;
        int portNum = -1;
        try {
            machineName = machineInfo.getText();
            portNum = Integer.parseInt(portInfo.getText());
            echoSocket = new Socket(machineName, portNum );
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                                        echoSocket.getInputStream()));

            // start a new thread to read from the socket
            new CommunicationReadThread (in, this, listModelUsers);

            sendButton.setEnabled(true);
            connected = true;
            connectButton.setText("Disconnect from Server");
        } catch (NumberFormatException e) {
            history.insert ( "Server Port must be an integer\n", 0);
        } catch (UnknownHostException e) {
            history.insert("Don't know about host: " + machineName , 0);
        } catch (IOException e) {
            history.insert ("Couldn't get I/O for "
                               + "the connection to: " + machineName , 0);
        }

      }
      else
      {
        try
        {
          out.close();
          in.close();
          echoSocket.close();
          sendButton.setEnabled(false);
          connected = false;
          connectButton.setText("Connect to Server");
        }
        catch (IOException e)
        {
            history.insert ("Error in closing down Socket ", 0);
        }
      }


    }

 } // end class EchoServer3

// Class to handle socket reads
//   THis class is NOT written as a nested class, but perhaps it should
class CommunicationReadThread extends Thread
{
 //private Socket clientSocket;
 private ChatClient gui;
 private BufferedReader in;
 private  DefaultListModel<String> listModelUsers;


 public CommunicationReadThread (BufferedReader inparam, ChatClient ec3, DefaultListModel<String> lmu)
   {
    in = inparam;
    gui = ec3;
    listModelUsers = lmu;
    start();
    gui.history.insert ("Communicating with Port\n", 0);

   }

 public void run()
   {
    System.out.println ("New Communication Thread Started");

    try {
         String inputLine; // what it recieves from the server

         while ((inputLine = in.readLine()) != null)
             {
               if(inputLine.charAt(0) == '*')
               {
                 gui.history.insert("new Client List item " + inputLine, 0);
                 listModelUsers.addElement(inputLine);
               }


              //history.insert ("From Server: " + in.readLine() + "\n" , 0);
              System.out.println ("Client: " + inputLine);
              gui.history.insert ("From Server: " + inputLine + "\n", 0);

              if (inputLine.equals("Bye."))
                  break;

             }

         in.close();
         //clientSocket.close();
        }
    catch (IOException e)
        {
         System.err.println("Problem with Client Read");
         //System.exit(1);
        }
    }
}
