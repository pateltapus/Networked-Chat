import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.*;
import java.util.*;
import javax.swing.text.Position.Bias;
import java.lang.StringBuilder;

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
  ArrayList<String> userNames;
  String userName;
  StringBuilder destination;

  //SHA ints
  int pValue;
  int qValue;
  MessageType rsa;

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
      userNames = new ArrayList<String>();
      destination = new StringBuilder();
      //setup RSA encrption class
      rsa = new MessageType();
      // get content pane and set its layout
      Container container = getContentPane();
      container.setLayout (new BorderLayout ());

      // set up the North panel
      JPanel upperPanel = new JPanel ();
      upperPanel.setLayout (new GridLayout (1,3));
      container.add (upperPanel, BorderLayout.NORTH);

      // create buttons
      connected = false;


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

      listUsers.addListSelectionListener( new SharedListSelectionHandler(destination));

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

               if(!(rsa.setKeys(pValue, qValue)))
               {
                  JOptionPane.showMessageDialog(null, "Invalid p or q value please enter again!");
                  continue;
               }
               rsa.addPublicKey(rsa.getPublicKey(), userName);

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
         int publicKey[] = rsa.getPublicKey();
         message.setText("*" + userName + "*" + publicKey[0] + "*" + publicKey[1]);
         doSendMessage();
         message.setText("");

       }
    }



    public void doSendMessage()
    {
      try
      {
        if(message.getText().charAt(0) == '*')
        {
          out.println(message.getText());
        }
        else if (!(destination.toString().equals("All Users")))
        {
          int temp[] = rsa.hmap.get(destination.toString());
          
          //encrypt message based on destination
          String sendMessage = rsa.encryptMessage(message.getText(),destination.toString());
          sendMessage = (userName + "*" + destination.toString() + "*" + sendMessage);
          out.println(sendMessage);
          history.insert(userName + ": " + message.getText() + "\n", 0);
        }
        // this will send it to all the users on the list
        else
        {
          for(String name: userNames)
          {
            //encrypt the message for the destination
            if(!(name.equals(userName))){
              String sendMessage = rsa.encryptMessage(message.getText(), name);
              sendMessage = (userName + "*" + name + "*" + sendMessage);
              out.println(sendMessage);
            }
            else
            {
              history.insert(userName + ": " + message.getText() + "\n", 0);
            }
          }
        }
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
            new CommunicationReadThread (in, this, listModelUsers, destination, userNames, rsa);

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

} 

//action listener for list

 class SharedListSelectionHandler implements ListSelectionListener {
   StringBuilder destination;
      public SharedListSelectionHandler(StringBuilder dst)
      {
        destination = dst;
      }

        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()){
              JList source = (JList)e.getSource();
              String selected = source.getSelectedValue().toString();
              System.out.println(selected);
              destination.delete(0, destination.length());
              destination.append(selected);
              System.out.println(" sending to " + destination.toString());
            }
        }
    }

// Class to handle socket reads
class CommunicationReadThread extends Thread
{
 private ChatClient gui;
 private BufferedReader in;
 private  DefaultListModel<String> listModelUsers;
 private StringBuilder destination;
 private ArrayList<String> userNames;
 private MessageType rsa;

 public CommunicationReadThread (BufferedReader inparam, ChatClient ec3, DefaultListModel<String> lmu, StringBuilder dst, ArrayList<String> uNames, MessageType mtype)
   {
    in = inparam;
    gui = ec3;
    listModelUsers = lmu;
    destination = dst;
    userNames = uNames;
    rsa = mtype;
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
               String token = "[*]";
               String[] array = inputLine.split(token);

               if(inputLine.charAt(0) == '*')
               {
                 if(listModelUsers.contains(array[1]) == false)
                 {
                   listModelUsers.addElement(array[1]);
                   userNames.add(array[1]);
                   //add public key here for rsa encryption
                   int pKey[] = new int[2];
                   pKey[0] = Integer.parseInt(array[2]);
                   pKey[1] = Integer.parseInt(array[3]);
                   rsa.addPublicKey(pKey,array[1]);
                 }
               }
               else
               {
                 System.out.println ("Source: " + array[0]);
                 String displayMessage = rsa.decryptMessage(array[2]);
                 gui.history.insert (array[0] + ": "  + displayMessage + "\n", 0);
               }

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
