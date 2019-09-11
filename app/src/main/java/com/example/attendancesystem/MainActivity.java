package com.example.attendancesystem;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private Web3j web3;
    private final String password = "medium";
    private String walletPath;
    private File walletDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        walletPath = getFilesDir().getAbsolutePath();
        walletDir = new File(walletPath);
    }

    public void connectToEthNetwork(View v){
        toastAsync("Connecting to ethereum network...");
        web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/32ab89cf19494" +
                "f4abf3ab9e9b1e6ef89"));
        try{
            Web3ClientVersion clientVersion = web3.web3ClientVersion().sendAsync().get();
            if(!clientVersion.hasError()) {
                toastAsync("Connected");
            } else {
                toastAsync(clientVersion.getError().getMessage());
            }
        } catch (Exception e) {
            toastAsync(e.getMessage());
        }
    }

    public void createWallet(View v){
        try {
            WalletUtils.generateLightNewWalletFile(password, walletDir);
            toastAsync("wallet generated");
        } catch (Exception e) {
            toastAsync(e.getMessage());
        }
    }

    public void getAddress(View v){
        try {
            Credentials credentials = WalletUtils.loadCredentials(password, walletDir);
            toastAsync("Your address is :" + credentials.getAddress());
        } catch (Exception e) {
            toastAsync(e.getMessage());
        }
    }

//    public void sendTrasaction(View v){
//        try{
//            Credentials credentials = WalletUtils.loadCredentials(password, walletDir);
//            TransactionReceipt transactionReceipt = Transfer.sendFunds(web3, credentials, )
//        }
//    }

    public void toastAsync(final String message){
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

}
