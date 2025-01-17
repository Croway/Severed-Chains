package legend.game.combat.bent;

import legend.core.memory.Method;
import legend.game.characters.Element;
import legend.game.characters.ElementSet;
import legend.game.combat.types.AttackType;
import legend.game.modding.coremod.CoreMod;
import legend.game.scripting.ScriptFile;
import org.joml.Vector3f;

import static legend.game.combat.Battle.applyBuffOrDebuff;
import static legend.game.combat.Battle.applyMagicDamageMultiplier;
import static legend.game.combat.Battle.spellStats_800fa0b8;

public class MonsterBattleEntity extends BattleEntity27c {
  public Element displayElement_1c;

  /**
   * <ul>
   *   <li>0x1 - special enemy - takes one damage from magical attacks</li>
   *   <li>0x2 - special enemy - takes one damage from physical attacks</li>
   *   <li>0x4 - magical immunity</li>
   *   <li>0x8 - physical immunity</li>
   * </ul>
   */
  public int damageReductionFlags_6e;
  public int _70;
  public Element monsterElement_72;
  public final ElementSet monsterElementalImmunity_74 = new ElementSet();
  public int monsterStatusResistFlag_76;
  public final Vector3f targetArrowPos_78 = new Vector3f();

  public MonsterBattleEntity(final String name) {
    super(CoreMod.MONSTER_TYPE.get(), name);
  }

  @Override
  protected ScriptFile getScript() {
    return this.combatant_144.scriptPtr_10;
  }

  @Override
  public ElementSet getAttackElements() {
    return new ElementSet().add(spellStats_800fa0b8[this.spellId_4e].element_08);
  }

  @Override
  public Element getElement() {
    return this.monsterElement_72;
  }

  @Override
  public int applyPhysicalDamageMultipliers(final int damage) {
    return applyMagicDamageMultiplier(this, damage, 0);
  }

  @Override
  public void applyAttackEffects() {
    applyBuffOrDebuff(this, this);
  }

  @Override
  public int applyDamageResistanceAndImmunity(final int damage, final AttackType attackType) {
    if(attackType.isPhysical() && (this.damageReductionFlags_6e & 0x2) != 0) {
      return 1;
    }

    if(attackType.isMagical() && (this.damageReductionFlags_6e & 0x1) != 0) {
      return 1;
    }

    return super.applyDamageResistanceAndImmunity(damage, attackType);
  }

  @Override
  @Method(0x800f2d48L)
  public int calculatePhysicalDamage(final BattleEntity27c target) {
    final int atk = this.attack_34 + spellStats_800fa0b8[this.spellId_4e].multi_04;

    //LAB_800f2e28
    //LAB_800f2e88
    return atk * atk * 5 / target.getEffectiveDefence();
  }

  /**
   * @param magicType item (0), spell (1)
   */
  @Override
  @Method(0x800f8768L)
  public int calculateMagicDamage(final BattleEntity27c target, final int magicType) {
    int matk = this.magicAttack_36;
    if(magicType == 1) {
      matk += spellStats_800fa0b8[this.spellId_4e].multi_04;
    } else {
      //LAB_800f87c4
      matk += this.item_d4.damage_05;
    }

    //LAB_800f87d0
    //LAB_800f8844
    return matk * matk * 5 / target.getEffectiveMagicDefence();
  }

  @Override
  public int getStat(final BattleEntityStat statIndex) {
    return switch(statIndex) {
      case EQUIPMENT_ATTACK_ELEMENT_OR_MONSTER_DISPLAY_ELEMENT -> this.displayElement_1c.flag;

      case MONSTER_DAMAGE_REDUCTION -> this.damageReductionFlags_6e;
      case _54 -> this._70;
      case MONSTER_ELEMENT -> this.monsterElement_72.flag;
      case MONSTER_ELEMENTAL_IMMUNITY -> this.monsterElementalImmunity_74.pack();
      case MONSTER_STATUS_RESIST_FLAGS -> this.monsterStatusResistFlag_76;
      case MONSTER_TARGET_ARROW_POSITION_X -> Math.round(this.targetArrowPos_78.x);
      case MONSTER_TARGET_ARROW_POSITION_Y -> Math.round(this.targetArrowPos_78.y);
      case MONSTER_TARGET_ARROW_POSITION_Z -> Math.round(this.targetArrowPos_78.z);

      default -> super.getStat(statIndex);
    };
  }

  @Override
  public void setStat(final BattleEntityStat statIndex, final int value) {
    switch(statIndex) {
      case MONSTER_DAMAGE_REDUCTION -> this.damageReductionFlags_6e = value;
      case _54 -> this._70 = value;
      case MONSTER_ELEMENT -> this.monsterElement_72 = Element.fromFlag(value);
      case MONSTER_ELEMENTAL_IMMUNITY -> this.monsterElementalImmunity_74.unpack(value);
      case MONSTER_STATUS_RESIST_FLAGS -> this.monsterStatusResistFlag_76 = value;
      case MONSTER_TARGET_ARROW_POSITION_X -> this.targetArrowPos_78.x = value;
      case MONSTER_TARGET_ARROW_POSITION_Y -> this.targetArrowPos_78.y = value;
      case MONSTER_TARGET_ARROW_POSITION_Z -> this.targetArrowPos_78.z = value;

      default -> super.setStat(statIndex, value);
    }
  }
}
