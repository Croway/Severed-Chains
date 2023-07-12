package legend.core.audio;

import legend.core.audio.assets.Channel;
import legend.core.audio.assets.Instrument;
import legend.core.audio.assets.InstrumentLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;

final class Voice {
  private static final Logger LOGGER = LogManager.getFormatterLogger();
  private static final Marker VOICE_MARKER = MarkerManager.getMarker("VOICE");

  static final short[] EMPTY = {0, 0, 0};
  private final int index;
  private final LookupTables lookupTables;

  private final VoiceCounter counter;
  private final AdsrEnvelope adsrEnvelope = new AdsrEnvelope();
  private final SoundBankEntry soundBankEntry = new SoundBankEntry();

  private Channel channel;
  private Instrument instrument;
  private InstrumentLayer layer;

  //Playing note
  private boolean used;
  private boolean lowPriority;
  private int priorityOrder;
  private int sampleRate;
  private int note;
  private int velocity;
  private int pitchBendMultiplier;
  private int breathControlIndex;
  private boolean isModulation;
  private int modulation;
  private boolean isPortamento;
  private int portamentoNote;
  private byte[][] breathControls;
  private int breath;
  private int breathControlPosition;

  private boolean _18;
  private boolean highPriority;

  private double volumeLeft;
  private double volumeRight;


  private boolean hasSamples;
  private final short[] samples = new short[28 + EMPTY.length];

  private float outLeft;
  private float outRight;

  Voice(final int index, final LookupTables lookupTables, final int interpolationBitDepth) {
    this.index = index;
    this.lookupTables = lookupTables;
    this.counter = new VoiceCounter(interpolationBitDepth);
  }

  void tick() {
    if(!this.used) {
      this.outLeft = 0;
      this.outRight = 0;
      return;
    }

    this.variableParameters();

    this.adsrEnvelope.tick();

    final short sample = this.sampleVoice();

    final short adsrApplied = (short)((sample * this.adsrEnvelope.getCurrentLevel()) >> 15);

    this.outLeft = (float)(adsrApplied * this.volumeLeft / 0x8000);
    this.outRight = (float)(adsrApplied * this.volumeRight / 0x8000);
  }

  public float getOutLeft() {
    return this.outLeft;
  }

  public float getOutRight() {
    return this.outRight;
  }

  @Nullable
  public InstrumentLayer getLayer() {
    return this.layer;
  }

  private short sampleVoice() {
    if(!this.hasSamples) {
      this.soundBankEntry.loadSamples(this.samples);

      this.hasSamples = true;
    }

    final int interpolationIndex = this.counter.getInterpolationIndex();
    final int sampleIndex = this.counter.getCurrentSampleIndex();

    final double[] interpolationWeights = this.lookupTables.getInterpolationWeights(interpolationIndex);
    final double value = interpolationWeights[0] * this.samples[sampleIndex]
      + interpolationWeights[1] * this.samples[sampleIndex + 1]
      + interpolationWeights[2] * this.samples[sampleIndex + 2]
      + interpolationWeights[3] * this.samples[sampleIndex + 3];

    if(this.counter.add(this.sampleRate)) {
      this.hasSamples = false;
    }

    return (short)value;
  }

  //see FUN_800470fc
  private void variableParameters() {
    if(!this.used) {
      return;
    }

    if(this.isModulation || this.isPortamento) { //TODO sequenceData_104
      int sixtyFourths = this.layer.getSixtyFourths();
      int note = this.note;
      int keyRoot = this.layer.getKeyRoot();
      int pitchBend = this.channel.getPitchBend();
      int pitchBendMultiplier = this.pitchBendMultiplier;

      if(this.isModulation || this.isPortamento) {
        //Swaps the offset from note to root key, since note is set to 120 down the line
        keyRoot = 120 + keyRoot - note;

        if(this.isModulation) {
          if((this.breath & 0xfff) != 120) { //TODO  || ticksPerSecond != 60
            this.breathControlPosition += this.breath & 0xfff;
          } else {
            final int var = this.breath & 0xf000;
            if(var != 0) {
              this.breath = this.breath & 0xfff | var - 0x1000;
              this.breathControlPosition += this.breath & 0xfff;
            } else {
              this.breath |= 0x6000;
            }
          }

          pitchBend = 0x80;

          if(this.breathControls != null && this.breathControls.length > 0) {

            if(this.breathControlPosition >= 0xf0) {
              this.breathControlPosition = (this.breath & 0xfff) >>> 1;
            }

            pitchBend = this.breathControls[this.breathControlIndex][this.breathControlPosition >>> 2] & 0xff;
          }

          //Set the note to 120, unless portamento
          note = this.portamentoNote;

          //TODO this should only be called when non-poly aka playingNote_1c == 0
          //Pitch bend will be overwritten, so it has to be converted into note offset and cents
          final int _64ths = (this.channel.getPitchBend() - 64) * this.pitchBendMultiplier;
          note = note + _64ths / 64;
          sixtyFourths +=_64ths - (_64ths / 64) * 64;
          pitchBendMultiplier = 1;


          // Here, pitch bend is either 0x80 or the value from the breath control wave
          pitchBend = pitchBend * this.modulation / 255 - ((this.modulation + 1) / 2 - 64);
        }

        //TODO portamento

        //TODO playingNote_42 == 1 || SequenceData_104

        final int pitch = 0x1000;

        this.sampleRate = (pitch * this.calculateSampleRate(keyRoot, note, sixtyFourths, pitchBend, pitchBendMultiplier)) >> 12;
      }
    }


  }

  void keyOn(final Channel channel, final Instrument instrument, final InstrumentLayer layer, final int note, final int velocity, final byte[][] breathControls, final int playingVoices) {
    LOGGER.info(VOICE_MARKER, "Voice %d Key On", this.index);

    this.channel = channel;
    this.instrument = instrument;
    this.layer = layer;
    this.note = note;
    this.velocity = velocity;
    this.pitchBendMultiplier = this.layer.isPitchBendMultiplierFromInstrument() ? this.instrument.getPitchBendMultiplier() : this.layer.getPitchBendMultiplier();
    this.breathControls = breathControls;
    this.breath = this.channel.getBreath();
    this.breathControlPosition = 0;
    this.priorityOrder = playingVoices;

    this.highPriority = this.layer.isHighPriority();
    this.lowPriority = !this.highPriority;

    if(this.layer.isModulation() && this.channel.getModulation() != 0) {
      this.breathControlIndex = this.layer.isBreathControlIndexFromInstrument() ? this.instrument.getBreathControlIndex() : this.layer.getBreathControlIndex();

      this.isModulation = true;
      this.modulation = this.channel.getModulation();
    } else {
      this.isModulation = false;
      this.modulation = 0;
    }

    this.portamentoNote = 120;

    if(this.channel.get_0b() == 0x7F) {
      this._18 = true;
    }

    this.counter.reset();
    this.adsrEnvelope.load(layer.getAdsr());
    this.soundBankEntry.load(layer.getSoundBankEntry());

    this.sampleRate = this.calculateSampleRate(this.layer.getKeyRoot(), note, this.layer.getSixtyFourths(), this.channel.getPitchBend(), this.pitchBendMultiplier);
    this.volumeLeft = this.calculateVolume(true);
    this.volumeRight = this.calculateVolume(false);

    this.used = true;
    this.hasSamples = false;
    System.arraycopy(EMPTY, 0, this.samples, 28, EMPTY.length);
  }

  void keyOff() {
    LOGGER.info(VOICE_MARKER, "Voice %d Key Off", this.index);

    this.adsrEnvelope.keyOff();

    if(!this.highPriority) {
      this.lowPriority = true;
      this._18 = false;
    } else if (!this._18) {
      this.lowPriority = true;
    } else {
      this._18 = false;
    }
  }

  boolean isUsed() {
    return this.used;
  }

  boolean isFinished() {
    return this.adsrEnvelope.isFinished() || (this.soundBankEntry.isEnd() && this.counter.getCurrentSampleIndex() >= EMPTY.length);
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
    this.isPortamento = false;
    this.portamentoNote = 0;

    this.highPriority = false;
    this._18 = false;
  }

  boolean isLowPriority() {
    return this.lowPriority;
  }

  int getPriorityOrder() {
    return this.priorityOrder;
  }

  void setPriorityOrder(final int value) {
    this.priorityOrder = value;
  }

  Channel getChannel() {
    return this.channel;
  }

  int getNote() {
    return this.note;
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

  //TODO needs to reflect possibility of Instrument pitch bend multiplier
  void updateSampleRate() {
    if(this.layer != null) {
      this.sampleRate = this.calculateSampleRate(this.layer.getKeyRoot(), this.note, this.layer.getSixtyFourths(), this.channel.getPitchBend(), this.layer.getPitchBendMultiplier());
    }
  }

  private double calculateVolume(final boolean left) {
    double volume = this.channel.getAdjustedVolume() * this.instrument.getVolume() * this.layer.getVolume() * this.velocity;
    volume /= 0x4000;
    volume *= this.calculatePan(left);

    if(this.layer.getLockedVolume() == 0) {
      return volume / 0x8000;
    }

    return (double)((this.layer.getLockedVolume() << 8) | ((int)volume >> 7)) / 0x8000 ;
  }

  void updateVolume() {
    if(this.layer != null) {
      this.volumeLeft = this.calculateVolume(true);
      this.volumeRight = this.calculateVolume(false);
    }
  }

  private double calculatePan(final boolean left) {
    int pan = this.channel.getPan();

    //TODO only if not poly
    pan = this.lookupTables.mergePan(pan, this.lookupTables.mergePan(this.instrument.getPan(), this.layer.getPan()));

    return this.lookupTables.getPan(pan, left);
  }

  void setModulation(final int value) {
    this.isModulation = true;
    this.modulation = value;
  }

  void setBreath(final int value) {
    this.breath = value;
  }
}
