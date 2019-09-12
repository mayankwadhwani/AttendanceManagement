package com.example.attendancesystem;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.math.BigDecimal;
import java.security.Provider;
import java.security.Security;

public class MainActivity extends AppCompatActivity {

    private static final Logger log = LoggerFactory.getLogger(MainActivity.class);
    private Web3j web3;
    private final String password = "medium";
    private String walletPath, filename;
    private File walletDir;
    private File path;
    private String INFURA_URL = "https://rinkeby.infura.io/v3/";
    private boolean INCLUDE_RAW_RESPONSES = false;
    private String TOKEN = "32ab89cf19494f4abf3ab9e9b1e6ef89";
    private EditText mPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPassword = (EditText) findViewById(R.id.password);
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        System.out.println("+++++++++++++++++++++++++++++++++++++"+path);
        if (!path.exists()) {
            path.mkdir();
        }
        setupBouncyCastle();
        setContentView(R.layout.activity_main);
        walletPath = String.valueOf(path);
        log.debug("Path for the wallet is : " + walletPath);
        walletDir = new File(walletPath);
    }

    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }


    public void connectToEthNetwork(View v) {
        toastAsync("Connecting to ethereum network...");
        web3 = Web3j.build(new HttpService(INFURA_URL+TOKEN));

        try {
//            log.info("Connected to Ethereum client version: "
//                    + web3.web3ClientVersion().send().getWeb3ClientVersion());
            Web3ClientVersion clientVersion = web3.web3ClientVersion().sendAsync().get();
            if (!clientVersion.hasError()) {
                toastAsync("Connected");
            } else {
                toastAsync("Not connected");
                toastAsync(clientVersion.getError().getMessage());
            }
        } catch (Exception e) {
            toastAsync("Error: " + e.getMessage());
        }
    }

    public void createWallet(View v) {
        try {
            log.info("Creating wallet.....");
            filename = WalletUtils.generateLightNewWalletFile(password, walletDir);
            toastAsync("wallet generated");
        } catch (Exception e) {
            toastAsync("Error: " + e.getMessage());
        }
    }

    public void getAddress(View v) {
        try {
            Credentials credentials = WalletUtils.loadCredentials(password, path+"/"+filename);
            toastAsync("Your address is :" + credentials.getAddress());
        } catch (Exception e) {
            toastAsync("Error: " + e.getMessage());
        }
    }

    public void sendTransaction(View v) {
        try {
            Credentials credentials = WalletUtils.loadCredentials(password, path+"/"+filename);
            TransactionReceipt transactionReceipt = Transfer.sendFunds(web3,
                    credentials,
                    "0x19e03255f667bdfd50a32722df860b1eeaf4d635",
                    BigDecimal.ONE, Convert.Unit.WEI).send();
            log.info("Transaction complete, view it at https://rinkeby.etherscan.io/tx/"
                    + transactionReceipt.getTransactionHash());
            log.info("Deploying smart contract");
            ContractGasProvider contractGasProvider = new DefaultGasProvider();
            Greeter contract = Greeter.deploy(web3, credentials, contractGasProvider, "test").send();

            String contractAddress = contract.getContractAddress();
            log.info("Smart contract deployed to address " + contractAddress);
            log.info("Value stored in remote smart contract:  " + contract.greet());

            TransactionReceipt transactionReceipt1 = contract.newGreeting("Well hello agai").send();
            log.info("New value stored in remote smart contract: " + contract.greet().send());

            for (Greeter.ModifiedEventResponse event : contract.getModifiedEvents(transactionReceipt1)) {
                log.info("Modify event fired, previous value: " + event.oldGreeting
                        + ", new value: " + event.newGreeting);
                log.info("Indexed event previous value: " + Numeric.toHexString(event.oldGreetingIdx)
                        + ", new value: " + Numeric.toHexString(event.newGreetingIdx));
            }
        } catch (Exception e) {
            toastAsync("Error: " + e.getMessage());
        }
    }

    public void toastAsync(final String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }
}


