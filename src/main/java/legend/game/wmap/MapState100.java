package legend.game.wmap;

import legend.core.gte.VECTOR;

import java.util.Arrays;

public class MapState100 {
  /** 800c6798 */
  public int _00;
  /** 800c679c */
  public int _04;
  /** 800c67a0 */
  public int _08;
  /** 800c67a4 */
  public int _0c;
  /** 800c67a8 */
  public int _10;
  /** 800c67aa */
  public int areaIndex_12;
  /** The section of the path that the player is on (800c67ac) */
  public int pathIndex_14;
  /** The path dot the player is on (800c67ae) */
  public int dotIndex_16;
  /** The distance the player is from the dot (range: 0-3) (800c67b0) */
  public int dotOffset_18;

  /** +1 - left, -1 - right (800c67b4) */
  public int facing_1c;
  /** Not the canonical player pos, just a copy (for animation purposes?) (800c67b8) */
  public final VECTOR playerPos_20 = new VECTOR();
  /** 800c67c8 */
  public final VECTOR nextDotPos_30 = new VECTOR();
  /** 800c67d8 */
  public final VECTOR[] _40 = new VECTOR[7];
  /** 800c6848 */
  public final VECTOR _b0 = new VECTOR();
  /** 800c6858 */
  public int _c0;
  /** 800c685a */
  public short previousPlayerRotation_c2;
  /** 800c685c */
  public int submapCut_c4;
  /** 800c685e */
  public int _c6;
  /** 800c6860 */
  public short submapCut_c8;
  /** 800c6862 */
  public short submapScene_ca;

  /** 800c6868 */
  public boolean disableInput_d0;
  /** 800c686c */
  public int _d4;
  /** 800c6870 */
  public int _d8;
  /** 800c6874 */
  public final int[] _dc = new int[7];
  /** 800c6890 */
  public int _f8;
  /** 800c6894 */
  public int _fc;

  public MapState100() {
    Arrays.setAll(this._40, i -> new VECTOR());
  }
}