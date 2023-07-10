package legend.core.audio;

import legend.core.DebugHelper;
import legend.core.audio.assets.BackgroundMusic;
import legend.core.audio.assets.SoundFactory;

import static legend.game.Scus94491BpeSegment_8005.reverbConfigs_80059f7c;

public final class SoundTest {
  public static void main(final String[] args) {
    final AudioThread audioThread = new AudioThread(100, true, 24, 9);
    final BackgroundMusic bgm = SoundFactory.backgroundMusic(5820);
    audioThread.loadBackgroundMusic(bgm);

    final Thread spuThread = new Thread(audioThread);
    spuThread.setName("SPU");

    spuThread.start();

    DebugHelper.sleep(100000);

    audioThread.stop();
  }
}
