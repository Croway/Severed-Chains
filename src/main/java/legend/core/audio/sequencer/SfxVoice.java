package legend.core.audio.sequencer;

import legend.core.MathHelper;
import legend.core.audio.sequencer.assets.Channel;
import legend.core.audio.sequencer.assets.Instrument;
import legend.core.audio.sequencer.assets.InstrumentLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

final class SfxVoice {
  private static final Logger LOGGER = LogManager.getFormatterLogger();
  private static final Marker VOICE_MARKER = MarkerManager.getMarker("SFX_VOICE");
  static final short[] EMPTY = {0, 0, 0};

  private final int index;
  private final LookupTables lookupTables;
  private final VoiceCounter counter;
  private final AdsrEnvelope adsrEnvelope = new AdsrEnvelope();
  private final SoundBankEntry soundBankEntry = new SoundBankEntry();

  private Channel channel;
  private Instrument instrument;
  private InstrumentLayer layer;

  /** playingNote.used_00 */
  private boolean used;
  /** voice.pitch */
  private int sampleRate;
  /** playingNote.noteNumber_02 */
  private int note;
  /** playingNote.velocityVolume_2c */
  private int velocityVolume;
  private int pitchBendMultiplier;
  private boolean isModulation;
  private int modulation;
  /** waveforms_800c4ab8.waveforms_02 */
  private byte[][] breathControls;
  /** playingNote.breath_3c */
  private int breath;
  /** playingNote.breathControlListIndex_10 */
  private int breathControlIndex;
  /** playingNote._12 */
  private int breathControlPosition;

  private boolean isPortamento;
  private int portamentoNote;
  private int newPortamento;
  private int portamentoTicksTotal;
  private int portamentoTicksRemaining;

  private int priorityOrder;
  private VoicePriority priority;

  private float volumeLeft = 1.0f;
  private float volumeRight = 1.0f;

  private boolean hasSamples;
  private final short[] samples = new short[28 + EMPTY.length];

  SfxVoice(final int index, final LookupTables lookupTables, final int interpolationBitDepth) {
    this.index = index;
    this.lookupTables = lookupTables;
    this.counter = new VoiceCounter(interpolationBitDepth);
  }

  void tick(final int[] output) {
    if(!this.used) {
      output[0] = 0;
      output[1] = 0;
      return;
    }

    this.handleEffectsOverTime();

    this.adsrEnvelope.tick();

    final int sample = (this.sampleVoice() * this.adsrEnvelope.getCurrentLevel()) >> 15;

    output[0] = (short)(sample * this.volumeLeft);
    output[1] = (short)(sample * this.volumeRight);
  }

  private int sampleVoice() {
    if(!this.hasSamples) {
      this.soundBankEntry.loadSamples(this.samples);

      this.hasSamples = true;
    }

    final int sampleIndex = this.counter.getCurrentSampleIndex();

    final float[] interpolationWeights = this.lookupTables.getInterpolationWeights(this.counter.getInterpolationIndex());

    final float sample = interpolationWeights[0] * this.samples[sampleIndex]
      + interpolationWeights[1] * this.samples[sampleIndex + 1]
      + interpolationWeights[2] * this.samples[sampleIndex + 2]
      + interpolationWeights[3] * this.samples[sampleIndex + 3];


    this.hasSamples = !this.counter.add(this.sampleRate);

    return (int)sample;
  }

  private void handleEffectsOverTime() {
    if(this.isModulation || this.isPortamento) {
      this.modulationAndPortamento();
    }
  }

  private void modulationAndPortamento() {
    int sixtyFourths = this.layer.getSixtyFourths();

    // Change key root and note difference to offset of 120 (center for portamento), since note will be used for further pitch bending
    int keyRoot = 120 + this.layer.getKeyRoot() - this.note;
    int note = this.portamentoNote;

    int pitchBend = this.channel.getPitchBend();
    int pitchBendMultiplier = this.pitchBendMultiplier;

    if(this.isModulation) {

    }

    if(this.isPortamento) {
      if(this.portamentoTicksRemaining != 0) {
        this.portamentoTicksRemaining--;

        // Calculates how many notes should the pitch be shifted by. Portamento is in 10ths of a note centered on 120 (octave up and down 0-240)
        final float fraction = (this.newPortamento * (this.portamentoTicksTotal - this.portamentoTicksRemaining)) / (this.portamentoTicksTotal * 10.0f);
        final int noteOffset = (int)fraction;
        note = this.portamentoNote + noteOffset;
        sixtyFourths += Math.round(fraction * 64 - noteOffset * 64);

        note = MathHelper.clamp(note, 0x0c, 0xf3);

        // TODO store note somewhere (this looks sketchy AF)

        // Storing portamentoNote in case we want to shift some more
        if(this.portamentoTicksRemaining <= 0) {
          this.portamentoNote = note;

          // TODO wait what? The PSX is ditching the sixtyFourths here??

          this.isPortamento = false;
        }
      }
    }

    this.sampleRate = this.calculateSampleRate(keyRoot, note, sixtyFourths, pitchBend, pitchBendMultiplier);
  }

  void polyphonicKeyPressure(final Channel channel, final Instrument instrument, final InstrumentLayer layer, final int note, final int velocityVolume, final byte[][] breathControls, final int playingVoices) {
    this.channel = channel;
    this.instrument = instrument;
    this.layer = layer;
    this.note = note;
    this.velocityVolume = velocityVolume;
    this.breathControls = breathControls;

    this.counter.reset();
    this.adsrEnvelope.load(this.layer.getAdsr());
    this.soundBankEntry.load(this.layer.getSoundBankEntry());

    this.sampleRate = this.calculateSampleRate(this.layer.getKeyRoot(), note, this.layer.getSixtyFourths(), this.channel.getPitchBend(), this.pitchBendMultiplier);


    this.used = true;
    this.hasSamples = false;
    System.arraycopy(EMPTY, 0, this.samples, 28, EMPTY.length);
  }

  void portamento(final int newPortamento, final int portamentoTime) {
    this.isPortamento = true;
    this.newPortamento = newPortamento;
    this.portamentoTicksTotal = portamentoTime * 4 * 60;
    this.portamentoTicksRemaining = this.portamentoTicksTotal;
  }

  void clear() {
    LOGGER.info(VOICE_MARKER, "Clearing Voice %d", this.index);

    this.used = false;
    this.note = 0;
    this.channel = null;
    this.layer = null;
    this.instrument = null;
    this.isModulation = false;
    this.modulation = 0;
    this.breath = 0;
    this.breathControlIndex = 0;
    this.breathControlPosition = 0;
    this.priority = VoicePriority.Low;
    System.arraycopy(EMPTY, 0, this.samples, 28, EMPTY.length);
  }

  boolean isUsed() {
    return this.used;
  }

  boolean isFinished() {
    return this.adsrEnvelope.isFinished() || (this.soundBankEntry.isEnd() && this.counter.getCurrentSampleIndex() >= EMPTY.length);
  }

  Channel getChannel() {
    return this.channel;
  }

  int getPriorityOrder() {
    return this.priorityOrder;
  }

  void setPriorityOrder(final int priorityOrder) {
    this.priorityOrder = priorityOrder;
  }



  private int calculateSampleRate(final int rootKey, final int note, final int sixtyFourths, final int pitchBend, final int pitchBendMultiplier) {
    final int offsetIn64ths = (note - rootKey) * 64 + sixtyFourths + (pitchBend - 64) * pitchBendMultiplier;

    if(offsetIn64ths >= 0) {
      final int octaveOffset = offsetIn64ths / 768;
      final int sampleRateOffset = offsetIn64ths - octaveOffset * 768;
      return this.lookupTables.getSampleRate(sampleRateOffset) << octaveOffset;
    }

    final int octaveOffset = (offsetIn64ths + 1) / -768 + 1;
    final int sampleRateOffset = offsetIn64ths + octaveOffset * 768;
    return this.lookupTables.getSampleRate(sampleRateOffset) >> octaveOffset;
  }
}
