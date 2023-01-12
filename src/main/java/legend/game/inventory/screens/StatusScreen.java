package legend.game.inventory.screens;

import static legend.game.SItem.FUN_801034cc;
import static legend.game.SItem._80114290;
import static legend.game.SItem.allocateUiElement;
import static legend.game.SItem.characterCount_8011d7c4;
import static legend.game.SItem.characterStatusGlyphs_801141a4;
import static legend.game.SItem.hasDragoon;
import static legend.game.SItem.renderCharacter;
import static legend.game.SItem.renderCharacterEquipment;
import static legend.game.SItem.renderCharacterSlot;
import static legend.game.SItem.renderCharacterStats;
import static legend.game.SItem.renderGlyphs;
import static legend.game.SItem.renderText;
import static legend.game.SItem.renderThreeDigitNumber;
import static legend.game.Scus94491BpeSegment.scriptStartEffect;
import static legend.game.Scus94491BpeSegment_8002.deallocateRenderables;
import static legend.game.Scus94491BpeSegment_8002.getUnlockedDragoonSpells;
import static legend.game.Scus94491BpeSegment_8002.getUnlockedSpellCount;
import static legend.game.Scus94491BpeSegment_8002.playSound;
import static legend.game.Scus94491BpeSegment_8002.uploadRenderables;
import static legend.game.Scus94491BpeSegment_8005.spells_80052734;
import static legend.game.Scus94491BpeSegment_800b.characterIndices_800bdbb8;
import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;

public class StatusScreen extends MenuScreen<StatusScreen.State> {
  protected State state;

  private int charSlot;

  private double scrollAccumulator;

  private final Runnable unload;

  public StatusScreen(final Runnable unload) {
    this.unload = unload;
  }

  @Override
  public MenuId menuId() {
    return MenuId.STATUS;
  }

  @Override
  public State getState() {
    return this.state;
  }

  @Override
  protected void render() {
    switch(this.state) {
      case INIT_1 -> {
        scriptStartEffect(2, 10);
        this.state = State.INIT_2;
      }

      case INIT_2 -> {
        deallocateRenderables(0xff);
        deallocateRenderables(0);
        renderGlyphs(characterStatusGlyphs_801141a4, 0, 0);
        this.renderStatusMenu(this.charSlot, 0xff);
        this.state = State.STATUS;
      }

      case STATUS -> {
        FUN_801034cc(this.charSlot, characterCount_8011d7c4.get());
        this.renderStatusMenu(this.charSlot, 0);

        if(this.scrollAccumulator >= 1.0d) {
          this.scrollAccumulator -= 1.0d;

          if(this.charSlot > 0) {
            this.scroll(this.charSlot - 1);
          }
        }

        if(this.scrollAccumulator <= -1.0d) {
          this.scrollAccumulator += 1.0d;

          if(this.charSlot < characterCount_8011d7c4.get() - 1) {
            this.scroll(this.charSlot + 1);
          }
        }
      }

      case UNLOAD -> {
        FUN_801034cc(this.charSlot, characterCount_8011d7c4.get());
        this.renderStatusMenu(this.charSlot, 0);
        this.unload.run();
      }
    }
  }

  private void scroll(final int slot) {
    playSound(1);
    this.charSlot = slot;
    this.state = State.INIT_2;
  }

  private void renderStatusMenu(final int charSlot, final long a1) {
    renderCharacterStats(characterIndices_800bdbb8.get(charSlot).get(), 0xff, a1 == 0xff);
    renderCharacterSlot(16, 21, characterIndices_800bdbb8.get(charSlot).get(), a1 == 0xff, false);
    renderCharacterEquipment(characterIndices_800bdbb8.get(charSlot).get(), a1 == 0xff);
    this.renderCharacterSpells(characterIndices_800bdbb8.get(charSlot).get(), a1 == 0xff);

    uploadRenderables();
  }

  private void renderCharacterSpells(final int charIndex, final boolean allocate) {
    if(charIndex == -1) {
      return;
    }

    if(allocate) {
      allocateUiElement(0x58, 0x58, 194, 101);
    }

    if(hasDragoon(gameState_800babc8.dragoonSpirits_19c.get(0).get(), charIndex)) {
      final byte[] spellIndices = new byte[8];
      getUnlockedDragoonSpells(spellIndices, charIndex);
      final int unlockedSpellCount = getUnlockedSpellCount(charIndex);

      for(int i = 0; i < 4; i++) {
        if(allocate && i < unlockedSpellCount) {
          renderCharacter(200, 127 + i * 14, i + 1);
        }

        //LAB_80109370
        final byte spellIndex = spellIndices[i];
        if(spellIndex != -1) {
          renderText(spells_80052734.get(spellIndex).deref(), 210, 125 + i * 14, 4);

          if(allocate) {
            renderThreeDigitNumber(342, 128 + i * 14, (int)_80114290.offset(spellIndex).get());
          }
        }
      }
    }
  }

  @Override
  protected void keyPress(final int key, final int scancode, final int mods) {
    if(this.state != State.STATUS || mods != 0) {
      return;
    }

    switch(key) {
      case GLFW_KEY_LEFT -> {
        if(this.charSlot > 0) {
          this.scroll(this.charSlot - 1);
        }
      }

      case GLFW_KEY_RIGHT -> {
        if(this.charSlot < characterCount_8011d7c4.get() - 1) {
          this.scroll(this.charSlot + 1);
        }
      }

      case GLFW_KEY_ESCAPE -> {
        playSound(3);
        this.state = State.UNLOAD;
      }
    }
  }

  @Override
  protected void mouseScroll(final double deltaX, final double deltaY) {
    if(this.state != State.STATUS) {
      return;
    }

    if(this.scrollAccumulator < 0 && deltaY > 0 || this.scrollAccumulator > 0 && deltaY < 0) {
      this.scrollAccumulator = 0;
    }

    this.scrollAccumulator += deltaY;
  }

  public enum State {
    INIT_1,
    INIT_2,
    STATUS,
    UNLOAD,
  }
}
