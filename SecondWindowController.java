package pl.nanaki.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import pl.nanaki.main.*;

import java.net.URL;
import java.util.ResourceBundle;

public class SecondWindowController implements Initializable {

    public String path;
    public boolean isTCP;
    public FirstWindowController firstWindowController;

    @FXML
    private Button serverButton;

    @FXML
    private Button sendButton;


    public void setFirstWindowController(FirstWindowController firstWindowController) {
        this.firstWindowController = firstWindowController;
    }


    public void wyjscie() {
        Polecenia.removePortFromFile(String.valueOf(firstWindowController.getPort()));
        Platform.exit();
        System.exit(0);
    }

    private p2pServer server;
    @FXML
    public void serverStart() {
            path = firstWindowController.getDirectory();
            isTCP = firstWindowController.isTCPSelected();
        Runnable serverThread = new Runnable() {
            public void run() {
                server = new p2pServer(SecondWindowController.this, firstWindowController.getPort());
            }
        };
        Thread t2 = new Thread(serverThread);
        t2.setDaemon(true);
        t2.start();

        serverButton.setDisable(true);
        sendButton.setDisable(false);

    }
    @FXML
    public TextArea cmd;
    @FXML
    public TextArea cmdClient;
    @FXML
    public TextArea cmdServer;


    public void initialize(URL location, ResourceBundle resources) {
        cmd.setOnKeyPressed(keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.ENTER)
                        startClientAndSendMessage();
                }
        );
    }

    //private p2pClient client;

    @FXML
    public void startClientAndSendMessage() {

        Runnable clientThread = new Runnable() {
            public void run() {
                new p2pClient(SecondWindowController.this, firstWindowController.getPort()); //
            }
        };
        Thread t1 = new Thread(clientThread);
        t1.setDaemon(true);
        t1.start();

    //new p2pClient(this, 11111);
     //   cmdClient.setText(String.valueOf(this) + firstWindowController.getPort() + firstWindowController.getDirectory());
    }

}
