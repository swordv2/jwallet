package net.yufeng.jwallet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Wallet;

/**
 * 
 * @author <sword@yufeng.net>
 *
 */
public class JWallet {
    final static NetworkParameters NETWORK = NetworkParameters.prodNet();
    final static String VERSION = "1.0";

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        JWallet jw = new JWallet();
        Wallet wallet = null;
        File walletFile = null;

        /*
         * loading params
         */
        HashMap<String, String> params = new HashMap<String, String>();
        for(String arg : args) {
            if(!arg.contains("=")){
                params.put(arg, null);
            }
            else {
                String[] param = arg.split("=");
                params.put(param[0].toLowerCase(), param[1]);
            }
        }

        if(params.containsKey("--version") || params.containsKey("-V")) {
            jw.version();
        }
        /*
         * for help
         */
        if(params.containsKey("--help") || params.containsKey("-h")) {
            jw.usage();
        }

        /*
         * check the wallet file
         */
        if(params.containsKey("--wallet")) {
            String s = params.get("--wallet");
            if(s == null || s.equals("")) {
                jw.usage();
            }
            else {
                try {
                    walletFile = new File(s);
                    wallet = Wallet.loadFromFile(walletFile);
                }
                catch (IOException e) {
                    System.err.println("Wallet File: " + s + " cannot open!");
                }
            }
        }
        if(wallet == null && (params.containsKey("--privkeys") || params.containsKey("--balance") || params.containsKey("--import"))) {
            jw.usage();
        }

        if(params.containsKey("--privkeys")) {
            jw.dumpPrivKey(wallet);
        }
        else if(params.containsKey("--importprivkey")) {
            String key = params.get("--importprivkey");
            if(key == null || key.length() != 64) {
                jw.usage();
            }
            else {
                jw.importPrivKey(walletFile, key);
            }
        }
        else if(params.containsKey("--balance")) {
            jw.balance(wallet);
        }
        else {
            jw.usage();
        }
    }
    private void usage() {
        System.out.println("Usage: java -jar jwallet.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -V, --version         show program's version number and exit");
        System.out.println("  -h, --help            show this help message and exit");
        System.out.println("  --wallet=WALLETFILE   wallet filename");
        System.out.println("  --privkeys            dump private keys in hexadecimal format");
        System.out.println("  --importprivkey=KEY   import private key from vanitygen in hexadecimal format");
        System.out.println("  --balance             print balance of the wallet");
        System.exit(1);
    }

    private void version() {
        System.out.println(VERSION);
        System.exit(1);
    }

    private void balance(Wallet wallet) {
        String balance = wallet.getBalance().toString();
        System.out.print("Balance: " + (balance.length() > 8 ? balance.substring(0, balance.length() - 8) : 0) + ".");
        if(balance.length() < 8) {
            for(int i = balance.length(); i < 8; i++) {
                System.out.print("0");
            }
        }
        else {
            balance = balance.substring(balance.length() - 8);
        }
        System.out.println(balance);
    }

    private void dumpPrivKey(Wallet wallet) {
        for(ECKey key : wallet.keychain) {
            StringBuffer sb = new StringBuffer(101);
            sb.append(key.toAddress(NETWORK)).append(": ");
            for(byte b : key.getPrivKeyBytes()) {
                //System.out.print(b + " ");
                String s = Integer.toHexString(b & 0xFF);
                if(s.length() < 2) {
                    for(int i = s.length(); i < 2; i++) {
                        sb.append("0");
                    }
                }
                sb.append(s);
            }
            //System.out.println();

            String privKey = sb.toString()/*.substring(0, sb.length() - 1)*/;
            System.out.println(/*"[" + */privKey/*.substring(36) + "]" + privKey.substring(36).length()*/);

            /*for(byte b : hexStringToBytes(privKey.substring(36))) {
                System.out.print(b + " ");
            }
            System.out.println();*/
        }
    }

    private void importPrivKey(File walletFile, String key) throws IOException {
        Wallet wallet = Wallet.loadFromFile(walletFile);
        wallet.keychain.add(ECKey.fromASN1(hexStringToBytes(key)));
        wallet.saveToFile(walletFile);
    }

    private static byte[] hexStringToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
