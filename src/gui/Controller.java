package gui;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import record.JavaDSP;
import record.RecordAudio;
import record.WavePlot;

public class Controller {

    public HBox boxTop;
    public RadioButton radioRecord;
    public RadioButton radioFile;
    public TextField pathFileIn;
    public TextField pathFileOut;

    public GridPane processing;
    public TextField txF1;
    public TextField txF2;

    public Button btnRecord;
    public Button btnPlay;
    public Button btnStop;

    public RadioButton radioLowpass;
    public RadioButton radioHighpass;
    public RadioButton radioBandpass;
    public RadioButton radioBandstop;

//------------------------------------------------------------------------------

    private String filterTypes() {
        if (radioLowpass.isSelected()) {
            return "lowpass";
        } else if (radioHighpass.isSelected()) {
            return "highpass";
        } else if (radioBandpass.isSelected()) {
            return "bandpass";
        } else if (radioBandstop.isSelected()) {
            return "bandstop";
        }
        return "";
    }

//------------------------------------------------------------------------------

    private void defaultRatioRecord() {
        btnRecord.setDisable(false);
        btnPlay.setDisable(true);
        btnStop.setDisable(true);
        processing.setDisable(true);
        pathFileIn.setDisable(true);
        pathFileIn.clear();
        pathFileOut.clear();
    }

    private void defaultRatioFile() {
        btnRecord.setDisable(true);
        btnPlay.setDisable(false);
        btnStop.setDisable(true);
        processing.setDisable(false);
        pathFileIn.setDisable(false);
        pathFileIn.setText(pathFileOut.getText().trim());
        pathFileOut.clear();
    }

    private void defaultRecordAndPlay() {
        btnRecord.setDisable(true);
        btnPlay.setDisable(true);
        btnStop.setDisable(false);
        boxTop.setDisable(true);
    }

    private void defaultStopRecord() {
        btnRecord.setDisable(false);
        btnPlay.setDisable(false);
        btnStop.setDisable(true);
        boxTop.setDisable(false);
    }

    private void defaultStopFile() {
        btnRecord.setDisable(true);
        btnPlay.setDisable(false);
        btnStop.setDisable(true);
        boxTop.setDisable(false);
    }

//------------------------------------------------------------------------------

    public void radioRecordAction(ActionEvent event) {
        defaultRatioRecord();
    }

    public void radioFileAction(ActionEvent event) {
        defaultRatioFile();
    }

    public void LHAction(ActionEvent event) {
        txF2.setDisable(true);
    }

    public void PSAction(ActionEvent event) {
        txF2.setDisable(false);
    }

//------------------------------------------------------------------------------

    public void recordAction(ActionEvent event) {
        defaultRecordAndPlay();
        RecordAudio.getIntance().startRecord();
    }

    public void playAction(ActionEvent event) throws Exception {
        defaultRecordAndPlay();
        if (radioRecord.isSelected()) {
            RecordAudio.getIntance().playRecord(pathFileOut.getText().trim());
            WavePlot.main(pathFileOut.getText().trim());
        } else if (radioFile.isSelected()) {
            String inputFileName = pathFileIn.getText().trim();
            String filterTypes = filterTypes();
            String filterOrder = "5";
            String fcf1 = txF1.getText().trim();
            String fcf2 = txF2.getText().trim();
            String outputFileName = pathFileOut.getText().trim();

            //CONFUSE
            JavaDSP.getIntance().attribute(inputFileName, filterTypes, filterOrder, fcf1, fcf2, outputFileName);

            RecordAudio.getIntance().playRecord(pathFileOut.getText().trim());

            WavePlot.main(outputFileName);
        }
    }

    public void stopAction(ActionEvent event) {
        if (radioRecord.isSelected()) {
            defaultStopRecord();
            RecordAudio.getIntance().stopRecord(pathFileOut.getText().trim());

        } else if (radioFile.isSelected()) {
            defaultStopFile();
            RecordAudio.getIntance().stopFile();
        }

    }


}
