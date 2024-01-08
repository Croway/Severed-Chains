package legend.core.audio;

import legend.core.DebugHelper;
import legend.core.audio.opus.BufferedAudio;
import legend.core.audio.sequencer.Sequencer;
import legend.core.audio.sequencer.assets.BackgroundMusic;
import legend.game.unpacker.FileData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;

public final class AudioThread implements Runnable {
  private static final Logger LOGGER = LogManager.getFormatterLogger();
  private static final Marker SEQUENCER_MARKER = MarkerManager.getMarker("AUDIO_THREAD");

  private final long audioContext;
  private final long audioDevice;
  private final int nanosPerTick;
  private final int frequency;
  private final boolean stereo;
  private final Sequencer sequencer;
  private BufferedAudio xaSource;

  private long time;
  private boolean running;
  private boolean paused;
  private boolean disabled;

  public AudioThread(final int frequency, final boolean stereo, final int voiceCount, final int interpolationBitDepth) {
    if(1_000_000_000 % frequency != 0) {
      throw new IllegalArgumentException("Nanos (1_000_000_000) is not divisible by frequency " + frequency);
    }

    this.frequency = frequency;
    this.nanosPerTick = 1_000_000_000 / this.frequency;

    final String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
    this.audioDevice = alcOpenDevice(defaultDeviceName);

    final int[] attributes = {0};
    this.audioContext = alcCreateContext(this.audioDevice, attributes);
    alcMakeContextCurrent(this.audioContext);

    final ALCCapabilities alcCapabilities = ALC.createCapabilities(this.audioDevice);
    final ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

    if(!alCapabilities.OpenAL10) {
      LOGGER.warn(SEQUENCER_MARKER, "Device does not support OpenAL10. Disabling audio.");
      this.disabled = true;
    }

    this.stereo = stereo;

    this.sequencer = new Sequencer(this.frequency, this.stereo, voiceCount, interpolationBitDepth);
  }

  @Override
  public void run() {
    if(this.disabled) {
      return;
    }

    this.running = true;
    this.paused = false;

    this.time = System.nanoTime();

    while(this.running) {
      while(this.paused) {
        try {
          this.wait();
        } catch(InterruptedException e) {

        }
      }

      this.sequencer.tick();

      if(this.xaSource != null) {
        this.xaSource.tick();
      }

      final long interval = System.nanoTime() - this.time;
      final int toSleep = (int)(Math.max(0, this.nanosPerTick - interval) / 1_000_000);
      DebugHelper.sleep(toSleep);
      this.time += this.nanosPerTick;
    }

    this.sequencer.destroy();
    alcDestroyContext(this.audioContext);
    alcCloseDevice(this.audioDevice);
  }

  public synchronized void stop() {
    this.paused = false;
    this.running = false;
    this.notify();
  }

  public synchronized void loadBackgroundMusic(final BackgroundMusic backgroundMusic) {
    this.sequencer.loadBackgroundMusic(backgroundMusic);
  }

  // TODO not thread safe
  public synchronized BufferedAudio loadXa(final FileData data) {
    if(this.xaSource != null) {
      this.xaSource.destroy();
    }

    this.xaSource = new BufferedAudio(data, 48_000 / this.frequency);
    return this.xaSource;
  }
}
