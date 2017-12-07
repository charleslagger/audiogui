package record;

import org.jsfml.audio.ConstSoundBuffer;
import org.jsfml.audio.Sound;
import org.jsfml.audio.SoundBuffer;
import org.jsfml.audio.SoundBufferRecorder;

import java.io.IOException;
import java.nio.file.Paths;

public class RecordAudio {

    private SoundBufferRecorder soundBufferRecorder;
    private SoundBuffer soundBuffer;
    private Sound sound;

//------------------------------------------------------------------------------

    private RecordAudio() {
    }

    private static class PlayAudioHelper {
        private static final RecordAudio PLAY_AUDIO = new RecordAudio();
    }

    public static RecordAudio getIntance() {
        return PlayAudioHelper.PLAY_AUDIO;
    }

//------------------------------------------------------------------------------

    public void startRecord() {
        soundBufferRecorder = new SoundBufferRecorder();
        soundBufferRecorder.start();
    }

    public void playRecord(String pathFile) {
        soundBuffer = new SoundBuffer();
        try {
            soundBuffer.loadFromFile(Paths.get(pathFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        sound = new Sound(soundBuffer);
        sound.play();
    }

    public void stopRecord(String pathFile) {
        soundBufferRecorder.stop();
        ConstSoundBuffer constSoundBuffer = soundBufferRecorder.getBuffer();
        try {
            constSoundBuffer.saveToFile(Paths.get(pathFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopFile() {
        sound.stop();
    }

}
