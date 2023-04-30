package legend.game.soundSfx;

import legend.core.audio.MidiChannel;
import legend.game.unpacker.FileData;

import java.util.List;

final class Channel implements MidiChannel {
  final int channelIndex_01;
  /** -1 means none */
  int instrumentIndex_02;
  int volume_03;
  /** 0x40 is middle, 0 is left, and 0x7f is right. */
  int pan_04;
  // 05-08 is possibly ADSR lo and hi
  final int modulation_09;
  double pitchBend_0a;
  final int _0b;
  final int breath_0c;
  final int volume_0e;

  private final SoundFont soundFont;

  Channel(final FileData channelData, final SoundFont soundFont) {
    this.channelIndex_01 = channelData.readByte(0x01);
    this.instrumentIndex_02 = channelData.readByte(0x02);
    //this.volume_03 = channelData.readByte(0x03);
    this.volume_03 = 64;
    this.pan_04 = channelData.readByte(0x04);
    this.modulation_09 = channelData.readByte(0x09);
    this.pitchBend_0a = 0;
    //this.pitchBend_0a = (channelData.readByte(0x0a) - 64) / 64d;
    this._0b = channelData.readByte(0x0b);
    this.breath_0c = channelData.readByte(0x0c);
    this.volume_0e = channelData.readByte(0x0e);

    this.soundFont = soundFont;
  }

  @Override
  public double getVolume() {
    //TODO verify volumes
    return this.volume_03 / 127d;
  }

  void setVolume(final int value) {
    //TODO verify volumes
    this.volume_03 = value;
  }

  @Override
  public int getPan() {
    return this.pan_04;
  }

  void setPan(final int value) {
    this.pan_04 = value;
  }

  @Override
  public double getPitchBend() {
    return this.pitchBend_0a;
  }

  void setPitchBend(final int value) {
    this.pitchBend_0a = (value - 64) / 64d;
  }

  void setProgram(final int value) {
    this.instrumentIndex_02 = value;
  }

  List<InstrumentLayer> getLayers(final int note) {
    return this.soundFont.getInstrument(this.instrumentIndex_02).getLayers(note);
  }
}