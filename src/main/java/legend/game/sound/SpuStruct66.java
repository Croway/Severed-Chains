package legend.game.sound;

public class SpuStruct66 {
  public boolean used_00;
  public int noteNumber_02;
  public int commandChannel_04;
  public int channelIndex_06;
  public int _08;
  public int voiceIndex_0a;
  public int _0c;
  public int instrumentIndex_0e;
  public int _10;
  public int _12;
  public boolean modulationEnabled_14;
  public int modulation_16;
  public int _18;
  public int _1a;
  public int _1c;
  public int maxKeyRange_1e;
  public int minKeyRange_20;
  public int playableSoundIndex_22;
  public int patchIndex_24;
  public int sequenceIndex_26;
  public int volume_28;
  public int _2a;
  public int volume_2c;
  public int volume_2e;
  public int[] _30 = new int[2];
  public int volume_34;
  public int cents_36;
  public int pitchBend_38;
  public int _3a;
  public int breath_3c;
  public int _3e;
  public int rootKey_40;
  public int _42;
  public boolean portamentoChanging_44;
  public boolean volumeChanging_46;
  public boolean panChanging_48;
  public int _4a;
  public int pan_4c;
  /** Maybe tempo? */
  public int _4e;
  public int newVolume_50;
  public int previousVolume_52;
  public int remainingVolumeChangeTime_54;
  public int totalVolumeChangeTime_56;
  public int newPan_58;
  /** Pan value before last pan command */
  public int previousPan_5a;
  public int remainingPanTime_5c;
  public int totalPanTime_5e;
  public int newPortamento_60;
  public int portamentoTimeRemaining_62;
  public int portamentoTimeTotal_64;

  public void clear() {
    this.used_00 = false;
    this.noteNumber_02 = 0;
    this.commandChannel_04 = 0;
    this.channelIndex_06 = 0;
    this._08 = 0;
    this.voiceIndex_0a = 0;
    this._0c = 0;
    this.instrumentIndex_0e = 0;
    this._10 = 0;
    this._12 = 0;
    this.modulationEnabled_14 = false;
    this.modulation_16 = 0;
    this._18 = 0;
    this._1a = 0;
    this._1c = 0;
    this.maxKeyRange_1e = 0;
    this.minKeyRange_20 = 0;
    this.playableSoundIndex_22 = 0;
    this.patchIndex_24 = 0;
    this.sequenceIndex_26 = 0;
    this.volume_28 = 0;
    this._2a = 0;
    this.volume_2c = 0;
    this.volume_2e = 0;
    this._30[0] = 0;
    this._30[1] = 0;
    this.volume_34 = 0;
    this.cents_36 = 0;
    this.pitchBend_38 = 0;
    this._3a = 0;
    this.breath_3c = 0;
    this._3e = 0;
    this.rootKey_40 = 0;
    this._42 = 0;
    this.portamentoChanging_44 = false;
    this.volumeChanging_46 = false;
    this.panChanging_48 = false;
    this._4a = 0;
    this.pan_4c = 0;
    this._4e = 0;
    this.newVolume_50 = 0;
    this.previousVolume_52 = 0;
    this.remainingVolumeChangeTime_54 = 0;
    this.totalVolumeChangeTime_56 = 0;
    this.newPan_58 = 0;
    this.previousPan_5a = 0;
    this.remainingPanTime_5c = 0;
    this.totalPanTime_5e = 0;
    this.newPortamento_60 = 0;
    this.portamentoTimeRemaining_62 = 0;
    this.portamentoTimeTotal_64 = 0;
  }
}
