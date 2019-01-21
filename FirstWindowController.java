package pl.nanaki.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import pl.nanaki.main.Main;

import java.io.File;
import java.io.IOException;

public class FirstWindowController {

    private Main main;

    public void setMain(Main main) {
        this.main = main;
    }


    private String directory;
    private int port;
    private boolean isTCP;

    public String getDirectory() {
        return directory;
    }

    public int getPort() {
        return port;
    }

    public boolean isTCPSelected() {
        return isTCP;
    }

    @FXML
    private Button startButton;

    @FXML
    private void setPath() {
        DirectoryChooser diretoryChooser = new DirectoryChooser();
        diretoryChooser.setTitle("Open Resource File");
        File init = new File("C:\\Users\\GrzegorzPawlaszek\\IntelliJ-workspace");
        diretoryChooser.setInitialDirectory(init);
        File dir = diretoryChooser.showDialog(main.window);
        String path = init.getAbsolutePath();
        if (dir != null) {
            path = dir.getAbsolutePath();
        }
        directory = path;
        startButton.setDisable(false);

    }

    @FXML
    private TextField portField;

    @FXML
    private RadioButton TCPButton;

    public void startApplication() throws IOException {
        try {
            port = Integer.parseInt(portField.getText());
            main.loadScene("/fxml/SecondWindow.fxml");
            isTCP = TCPButton.isSelected();
        } catch (NumberFormatException e){
            portField.clear();
            portField.setPromptText("Wrong number format");
        }
    }

}