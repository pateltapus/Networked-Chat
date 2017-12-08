//https://www.mkyong.com/java/how-to-determine-a-prime-number-in-java/
import java.lang.Math;
import java.lang.StringBuilder;
import java.util.HashMap;
import java.util.*;
import java.math.BigInteger;


public class MessageType
{
  int clientPublicKey[];
  int clientPrivateKey[];
  HashMap<String, int[]> hmap;
  
  public MessageType()
  {
    hmap = new HashMap<String, int[]>();
    clientPublicKey = new int[2];
    clientPrivateKey = new int[2];
  }
  
  public void addPublicKey(int pKey[], String username)
  {
    hmap.put(username, pKey);
  }
  
  public boolean setKeys(int p, int q)
  {
    int n,phi,e,d;
    n = p * q;
    if(isPrime(p) == false)
    {
      return false;
    }
    if(isPrime(q) == false)
    {
      return false;
    }
    if(n < (int)Math.pow(128,2))
    {
      return false;
    }
    else if(p < 131 || q < 131)
    {
      return false;
    }
    phi = (p-1)*(q-1);
    e = 2;
    while(relativelyPrime(e, phi) == false)
    {
      e++;
      if(e > n)
      {
        System.out.println("Error e is greater than n");
        return false;
      }
    }
    
    d = 0;
    while((e*d) % phi != 1)
    {
      d++;
    }
    clientPublicKey[0] = e;
    clientPublicKey[1] = n;
    clientPrivateKey[0] = d;
    clientPrivateKey[1] = n;
    return true;
  }
  
  public int[] getPublicKey()
  {
    return clientPublicKey;
  }
 public String encryptMessage(String message, String username)
  {
    int pKey[] = hmap.get(username);
    int e = pKey[0];
    int n = pKey[1];
    char block[] = new char[2];
    int length = message.length();
    StringBuilder sb = new StringBuilder();
    int ascii;
    int encrypted;
    for(int i = 0; i < length; i+=2)
    {
      if((i+1) == length)
      {
        block[0] = message.charAt(i);
        block[1] = '\0';
      }
      else
      {
        block[0] = message.charAt(i);
        block[1] = message.charAt(i+1);
      }
      BigInteger bi1, modN, bi3, exponent;
      ascii = (int)block[0] + (int)block[1] * 128;
      bi1 = new BigInteger(Integer.toString(ascii));
      modN = new BigInteger(Integer.toString(n));
      exponent = new BigInteger(Integer.toString(e));
      bi3 = bi1.modPow(exponent,modN);
     
      encrypted = ((int)Math.pow(ascii,e)) % n;
      sb.append(bi3);
      sb.append('/');      
    }
    return sb.toString();
  }
  
  public String decryptMessage(String message)
  {
    String dMessage;
    StringBuilder sb = new StringBuilder();
    ArrayList<String> eList = new ArrayList<String>();
    int index = -1;
    int prev = -1;
    while(true)
    {
      
      prev = index;
      index = message.indexOf('/', index+1);
      eList.add(message.substring(prev+1, index));
      
      if(index  + 1 == message.length())
      {
        break;
      }
    }
    BigInteger decrypt, dValue, nValue,c1, c2;
    
    for(int i = 0; i < eList.size(); i++)
    {
      decrypt = new BigInteger(eList.get(i));
      dValue = new BigInteger(Integer.toString(clientPrivateKey[0]));
      nValue = new BigInteger(Integer.toString(clientPrivateKey[1]));
      decrypt = decrypt.modPow(dValue,nValue);
      c2 = decrypt.divide(BigInteger.valueOf(128));
      c1 = decrypt.subtract(c2.multiply(BigInteger.valueOf(128)));
      sb.append((char)Integer.parseInt(c1.toString()));
      sb.append((char)Integer.parseInt(c2.toString()));
    }
    return sb.toString(); 
  }
  
  //checks whether an int is prime or not.
  public boolean isPrime(int n) {
    //check if n is a multiple of 2
    if (n%2==0) return false;
    //if not, then just check the odds
    for(int i=3;i*i<=n;i+=2) {
      if(n%i==0)
        return false;
    }
    return true;
  }
  private boolean relativelyPrime(int a, int b) 
  {
    return gcd(a,b) == 1;
  }
  private int gcd(int a, int b) 
  {
    int t;
    while(b != 0){
        t = a;
        a = b;
        b = t%b;
    }
    return a;
  }
}