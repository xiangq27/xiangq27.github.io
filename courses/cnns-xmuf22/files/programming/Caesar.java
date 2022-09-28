//*******************************************************************
//
//   File: Caesar.java
//
//   Author: Y. Richard Yang      Email: yry@cs.yale.edu 
//
//   Class: Caesar
// 
//   Time spent on this problem: 
//   --------------------
//   
//      This program implements Caesar's cipher. It encodes the message
//   with a key.
//
//*******************************************************************
import java.util.Scanner;

public class Caesar {
	
    static final int NCHARS = 'z' - 'a' + 1;
    
    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);

        System.out.print("Your secret key: ");
        int key = console.nextInt();
        console.nextLine(); // To Skip the remaining newline

        System.out.print("Your secret message: ");
        String message = console.nextLine();
        message = message.toLowerCase();

        String encoded = encode(message, key);
        System.out.println("The encoded message: " + encoded);
        
        String decoded = decode(encoded, key);
        System.out.println("The decoded message: " + decoded);
        
        console.close();
    }

    // This method encodes the given text string using a Caesar
    // cipher, shifting each letter by the given number of places
    // specified by key.
    public static String encode(String text, int key) {
        String result = "";
        text = text.toLowerCase();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if ('a' <= ch && ch <= 'z') {
                int chInt = ch - 'a'; 
                chInt = (chInt + key) % NCHARS; // (ch + key) % 26
                ch = (char) (chInt + 'a');
            }
            result = result + ch;
        }
        return result;
    }
    
    public static String decode(String cipher, int key) {
    		return encode(cipher, 26-key);
    }
}
