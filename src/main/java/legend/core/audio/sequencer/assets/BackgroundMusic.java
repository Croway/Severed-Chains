package legend.core.audio.sequencer.assets;

import legend.core.audio.sequencer.assets.sequence.Command;
import legend.core.audio.sequencer.assets.sequence.bgm.SequenceBuilder;
import legend.game.unpacker.FileData;
import legend.game.unpacker.Unpacker;

import java.util.List;

public final class BackgroundMusic {
  private final int songId;

  private final int volume;
  private final int tickPerQuarterNote;
  private double samplesPerTick;

  private final byte[][] breathControls;
  private final byte[] velocityRamp = new byte[0x80];

  private final Command[] sequence;
  private int sequencePosition;

  public BackgroundMusic(final List<FileData> files, final int fileId) {
    this.songId = files.get(0).readUShort(0);

    final int fileOffset = files.size() == 5 ? 1 : 0;

    final FileData sshd = files.get(2 + fileOffset);

    final SoundBank soundBank;
    if(fileOffset == 0) {
      soundBank = new SoundBank(files.get(3));
    } else {
      final byte[] soundBankData = new byte[sshd.readInt(0x4)];
      int offset = files.get(4).size();
      files.get(4).copyFrom(0, soundBankData, 0, offset);
      final int bankCount = files.get(1).readUShort(0x0);

      for(int i = 1; i < bankCount; i++) {
        final FileData extraBank = Unpacker.loadFile("SECT/DRGN0.BIN/" + (fileId + i));
        extraBank.copyFrom(0, soundBankData, offset, extraBank.size());
        offset += extraBank.size();
      }

      soundBank = new SoundBank(new FileData(soundBankData));
    }

    final int[] subfileOffsets = new int[3];

    for(int i = 0; i < subfileOffsets.length; i++) {
      subfileOffsets[i] = sshd.readInt(16 + i * 4);
    }

    final FileData sssq = files.get(1 + fileOffset);

    this.volume = sssq.readUByte(0x0);
    this.tickPerQuarterNote = sssq.readUShort(0x2);
    this.setTempo(sssq.readUShort(0x4));

    final SoundFont soundFont = new SoundFont(sshd.slice(subfileOffsets[0], subfileOffsets[1] - subfileOffsets[0]), soundBank);

    final Channel[] channels = new Channel[0x10];
    for(int channel = 0; channel < channels.length; channel++) {
      channels[channel] = new Channel(sssq.slice(16 + channel * 16, 16), this.volume, soundFont);
    }

    this.sequence = SequenceBuilder.create(sssq.slice(0x110), channels);

    sshd.copyFrom(subfileOffsets[1] + 2, this.velocityRamp, 0, 0x80);

    if(subfileOffsets[2] == -1) {
      this.breathControls = new byte[0][];
    } else {
      this.breathControls = new byte[sshd.readUShort(subfileOffsets[2]) + 1][];
    }

    for(int i = 0; i < this.breathControls.length; i++) {
      this.breathControls[i] = sshd.slice(subfileOffsets[2] + sshd.readUShort(2 + i * 2 + subfileOffsets[2]), 0x40).getBytes();
    }
  }

  public Command getNextCommand() {
    return this.sequence[this.sequencePosition++];
  }

  public int getVolume() {
    return this.volume;
  }

  public void setTempo(final int tempo) {
    this.samplesPerTick = 2_646_000d / (tempo * this.tickPerQuarterNote);
  }

  public double getSamplesPerTick() {
    return this.samplesPerTick;
  }

  public byte[][] getBreathControls() {
    return this.breathControls;
  }

  public int getVelocityVolume(final int velocity) {
    return this.velocityRamp[velocity];
  }

  public int getSongId() {
    return this.songId;
  }
}
