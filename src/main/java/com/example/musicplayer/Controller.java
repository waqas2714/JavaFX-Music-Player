package com.example.musicplayer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Initializable {

    @FXML
    private Label songLabel;
    @FXML
    private Button playButton, pauseButton, resetButton, previousButton, nextButton;
    @FXML
    private ComboBox<String> speedComboBox;
    @FXML
    private ProgressBar songProgressBar;
    @FXML
    private Slider volumeSlider;

    private Media media;
    private MediaPlayer mediaPlayer;
    private File directory;
    private File[] files;
    private ArrayList<File> songs;
    private int songNumber;
    private int[] speeds = {25, 50, 75, 100, 125, 150, 175, 200};
    private Timer timer;
    private TimerTask task;
    private boolean running;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        songs = new ArrayList<File>();
        directory = new File(getClass().getResource("/music").getFile());
        files = directory.listFiles();//gets all the files from the directory
        for (File file: files) {
            songs.add(file);
            System.out.println(file);
        }
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        songLabel.setText(songs.get(songNumber).getName());

        for (int i = 0; i < speeds.length ; ++i){
            speedComboBox.getItems().add(Integer.toString(speeds[i]) + "%");
        }

        speedComboBox.setOnAction(this::changeSpeed);

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                mediaPlayer.setVolume(volumeSlider.getValue()*0.01);
            }
        });

        songProgressBar.setStyle("-fx-accent: #00FF00;");
        songProgressBar.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double progressBarWidth = songProgressBar.getWidth();
            double newSongTime = (mouseX / progressBarWidth) * mediaPlayer.getTotalDuration().toSeconds();
            mediaPlayer.seek(Duration.seconds(newSongTime));
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            nextMedia(); // Call your method to play the next song
        });
    }

    public void playMedia(){
        mediaPlayer.setVolume(volumeSlider.getValue()*0.01);
        changeSpeed(null);
        beginTimer();
        mediaPlayer.play();
    }
    public void pauseMedia(){
        mediaPlayer.pause();
        cancelTimer();
    }
    public void resetMedia(){
        mediaPlayer.seek(Duration.seconds(0));
        songProgressBar.setProgress(0);
    }
    public void nextMedia(){
        if(songNumber < songs.size() - 1){
            ++songNumber;
            mediaPlayer.stop();

            if (running == true){cancelTimer();}

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            songLabel.setText(songs.get(songNumber).getName());

            playMedia();
        }else{
            songNumber = 0;
            mediaPlayer.stop();
            if (running == true){cancelTimer();}

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            songLabel.setText(songs.get(songNumber).getName());

            playMedia();
        }
    }
    public void previousMedia(){
        if(songNumber > 0){
            --songNumber;
            mediaPlayer.stop();
            if (running == true){cancelTimer();}
            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            songLabel.setText(songs.get(songNumber).getName());

            playMedia();
        }else{
            songNumber = songs.size() - 1;
            mediaPlayer.stop();
            if (running == true){cancelTimer();}

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            songLabel.setText(songs.get(songNumber).getName());

            playMedia();
        }
    }
    public void changeSpeed(ActionEvent event) {
        if (speedComboBox.getValue() == null) {
            mediaPlayer.setRate(1);
        } else {
            //This was done to remove the % sign from the selected
            // string in the speedbox so that string can be parse
            //to the integer successfully
            mediaPlayer.setRate(Integer.parseInt(speedComboBox.getValue().substring(0, speedComboBox.getValue().length() - 1)) * 0.01);
        }
    }
    public void beginTimer(){
        timer = new Timer();
        task = new TimerTask() {

            public void run() {
                running = true;
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = media.getDuration().toSeconds();
                songProgressBar.setProgress(current/end);

                if (current/end == 1){
                    cancelTimer();
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, 500);
    }
    public void cancelTimer(){
        running = false;
        timer.cancel();
    }
}
