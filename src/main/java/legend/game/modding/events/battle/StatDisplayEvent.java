package legend.game.modding.events.battle;

import legend.game.combat.bent.BattleEvent;
import legend.game.combat.bent.PlayerBattleEntity;


/**
 * DEPRECATED: subject to removal, use not recommended. Better ways to do this will be introduced in the future.
 */
@Deprecated
public class StatDisplayEvent extends BattleEvent {
  public final int charSlot;
  public final PlayerBattleEntity player;

  public StatDisplayEvent(final int charSlot, final PlayerBattleEntity player) {
    this.charSlot = charSlot;
    this.player = player;
  }
}
