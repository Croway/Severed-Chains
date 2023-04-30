package legend.game.soundSfx;

import legend.game.unpacker.FileData;

final class SoundFont {
  private final Instrument[] instruments;

  SoundFont(final FileData data, final SoundBank soundBank) {
    final int instrumentsUpperBound = data.readUShort(0);


    this.instruments = new Instrument[instrumentsUpperBound + 1];
    int lastOffset = data.size();
    for(int instrument = instrumentsUpperBound; instrument >= 0; instrument--) {
      final int offset = data.readShort(2 + instrument * 2);

      if(offset != -1) {
        this.instruments[instrument] = new Instrument(data.slice(offset, lastOffset - offset), soundBank);

        lastOffset = offset;
      }
    }
  }

  Instrument getInstrument(final int index) {
    return this.instruments[index];
  }
}