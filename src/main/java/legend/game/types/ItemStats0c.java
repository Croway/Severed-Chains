package legend.game.types;

import legend.core.memory.Value;
import legend.core.memory.types.ByteRef;
import legend.core.memory.types.MemoryRef;
import legend.core.memory.types.UnsignedByteRef;

public class ItemStats0c implements MemoryRef {
  private final Value ref;

  /**
   * <ul>
   *   <li>0x2 - target all</li>
   * </ul>
   */
  public final UnsignedByteRef target_00;
  public final UnsignedByteRef element_01;
  public final UnsignedByteRef damage_02;
  /**
   * Read left to right
   *
   * <ul>
   *   <li>0x80 - power defence</li>
   *   <li>0x40 - power magic defence</li>
   *   <li>0x20 - power attack</li>
   *   <li>0x10 - power magic attack</li>
   *   <li>0x8 - attack hit</li>
   *   <li>0x4 - magic attack hit</li>
   *   <li>0x2 - attack avoid</li>
   *   <li>0x1 - magic attack avoid</li>
   * </ul>
   */
  public final UnsignedByteRef special1_03;
  /**
   * Read left to right
   *
   * <ul>
   *   <li>0x80 - physical immunity</li>
   *   <li>0x40 - magical immunity</li>
   *   <li>0x20 - speed up</li>
   *   <li>0x10 - speed down</li>
   *   <li>0x8 - SP per physical hit</li>
   *   <li>0x4 - MP per physical hit</li>
   *   <li>0x2 - SP per magical hit</li>
   *   <li>0x1 - MP per magical hit</li>
   * </ul>
   */
  public final UnsignedByteRef special2_04;
  public final UnsignedByteRef damage_05;
  public final ByteRef specialAmount_06;
  public final ByteRef icon_07;
  public final UnsignedByteRef status_08;
  public final UnsignedByteRef percentage_09;
  public final UnsignedByteRef uu2_0a;
  /**
   * <ul>
   *   <li>0x04 - cause status</li>
   *   <li>0x08 - cure status</li>
   *   <li>0x10 - revive</li>
   *   <li>0x20 - SP</li>
   *   <li>0x40 - MP</li>
   *   <li>0x80 - HP</li>
   * </ul>
   */
  public final UnsignedByteRef type_0b;

  public ItemStats0c(final Value ref) {
    this.ref = ref;

    this.target_00 = ref.offset(1, 0x00L).cast(UnsignedByteRef::new);
    this.element_01 = ref.offset(1, 0x01L).cast(UnsignedByteRef::new);
    this.damage_02 = ref.offset(1, 0x02L).cast(UnsignedByteRef::new);
    this.special1_03 = ref.offset(1, 0x03L).cast(UnsignedByteRef::new);
    this.special2_04 = ref.offset(1, 0x04L).cast(UnsignedByteRef::new);
    this.damage_05 = ref.offset(1, 0x05L).cast(UnsignedByteRef::new);
    this.specialAmount_06 = ref.offset(1, 0x06L).cast(ByteRef::new);
    this.icon_07 = ref.offset(1, 0x07L).cast(ByteRef::new);
    this.status_08 = ref.offset(1, 0x08L).cast(UnsignedByteRef::new);
    this.percentage_09 = ref.offset(1, 0x09L).cast(UnsignedByteRef::new);
    this.uu2_0a = ref.offset(1, 0x0aL).cast(UnsignedByteRef::new);
    this.type_0b = ref.offset(1, 0x0bL).cast(UnsignedByteRef::new);
  }

  @Override
  public long getAddress() {
    return this.ref.getAddress();
  }
}
