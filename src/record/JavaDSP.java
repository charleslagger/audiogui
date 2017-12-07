package record;

import lib.filter.FilterPassType;
import lib.filter.IirFilterAudioInputStreamExstrom;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class JavaDSP {

    private JavaDSP() {
    }

    private static class JavaDSPHelper {
        private static final JavaDSP JAVA_DSP = new JavaDSP();
    }

    public static JavaDSP getIntance() {
        return JavaDSPHelper.JAVA_DSP;
    }

//------------------------------------------------------------------------------

    private void filterWavFile(String inputFileName, FilterPassType filterPassType, int filterOrder, double fcf1, double fcf2, String outputFileName) throws Exception {
        AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(inputFileName));
        AudioInputStream filterStream = IirFilterAudioInputStreamExstrom.getAudioInputStream(inputStream, filterPassType, filterOrder, fcf1, fcf2);
        AudioSystem.write(filterStream, AudioFileFormat.Type.WAVE, new File(outputFileName));
    }

    public void attribute(String inFileName, String filterType, String filterOrder, String f1, String f2, String outFileName) throws Exception {
        String inputFileName = inFileName;
        FilterPassType filterPassType = FilterPassType.valueOf(filterType);
        int fOrder = Integer.valueOf(filterOrder);
        double fcf1 = Double.valueOf(f1);
        double fcf2 = Double.valueOf(f2);
        String outputFileName = outFileName;
        filterWavFile(inputFileName, filterPassType, fOrder, fcf1, fcf2, outputFileName);
    }
}