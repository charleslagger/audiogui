package record;


import lib.wave.AudioIo;
import lib.wave.SignalPlot;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class WavePlot extends JFrame {

    private static AudioIo.AudioSignal audioSignal;

//------------------------------------------------------------------------------

    private WavePlot() {
        setLocationByPlatform(true);
        setSize(new Dimension(1200, 300));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        SignalPlot signalPlot = new SignalPlot(audioSignal.data[0], -1, 1);
        signalPlot.setZoomModeHorizontal(true);
        setContentPane(signalPlot);
    }

//------------------------------------------------------------------------------

    public static void main(String args) throws Exception {
        String fileName = args;
        audioSignal = AudioIo.loadWavFile(fileName);
        startGuiThread();
    }

    private static void startGuiThread() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                guiThreadMain();
            }
        });
    }

    private static void guiThreadMain() {
        try {
            guiThreadInit();
        } catch (Throwable e) {
            System.err.print("Error: ");
            e.printStackTrace(System.err);
            JOptionPane.showMessageDialog(null, "Error: " + e, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(9);
        }
    }

    private static void guiThreadInit() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new WavePlot().setVisible(true);
    }

}