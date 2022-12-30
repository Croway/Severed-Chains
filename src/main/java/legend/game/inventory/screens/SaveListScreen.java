package legend.game.inventory.screens;

import legend.core.MathHelper;
import legend.game.SaveManager;
import legend.game.types.MessageBoxResult;
import legend.game.types.Renderable58;
import org.lwjgl.glfw.GLFW;

import static legend.game.SItem.FUN_80104b60;
import static legend.game.SItem.allocateUiElement;
import static legend.game.SItem.getSlotY;
import static legend.game.SItem.glyphs_80114258;
import static legend.game.SItem.renderGlyphs;
import static legend.game.SItem.renderSaveListArrows;
import static legend.game.SItem.saves;
import static legend.game.Scus94491BpeSegment.scriptStartEffect;
import static legend.game.Scus94491BpeSegment_8002.deallocateRenderables;
import static legend.game.Scus94491BpeSegment_8002.playSound;
import static legend.game.Scus94491BpeSegment_8002.uploadRenderables;
import static legend.game.Scus94491BpeSegment_800b.saveListDownArrow_800bdb98;
import static legend.game.Scus94491BpeSegment_800b.saveListUpArrow_800bdb94;

public abstract class SaveListScreen extends MenuScreen {
  protected int loadingStage;

  private int selectedSlot;
  private int scroll;

  private double scrollAccumulator;

  private Renderable58 highlightLeftHalf;
  private Renderable58 highlightRightHalf;

  private final Runnable unload;

  protected SaveListScreen(final Runnable unload) {
    this.unload = unload;
  }

  @Override
  public void render() {
    switch(this.loadingStage) {
      case 0 -> {
        deallocateRenderables(0xff);
        scriptStartEffect(2, 10);

        saveListDownArrow_800bdb98 = null;
        saveListUpArrow_800bdb94 = null;
        this.scroll = 0;
        this.selectedSlot = 0;

        saves.clear();
        saves.addAll(SaveManager.loadAllDisplayData());

        this.highlightLeftHalf = allocateUiElement(129, 129, 16, getSlotY(this.selectedSlot));
        this.highlightRightHalf = allocateUiElement(130, 130, 192, getSlotY(this.selectedSlot));
        FUN_80104b60(this.highlightLeftHalf);
        FUN_80104b60(this.highlightRightHalf);
        renderSaveListArrows(this.scroll);

        deallocateRenderables(0);
        this.renderSavedGames(this.scroll, true, 0xff);

        this.loadingStage++;
      }

      case 1 -> {
        renderSaveListArrows(this.scroll);
        this.renderSavedGames(this.scroll, true, 0);

        if(this.scrollAccumulator >= 1.0d) {
          this.scrollAccumulator -= 1.0d;

          if(this.scroll > 0) {
            this.scroll(this.scroll - 1);
          }
        }

        if(this.scrollAccumulator <= -1.0d) {
          this.scrollAccumulator += 1.0d;

          if(this.scroll < this.menuCount() - 3) {
            this.scroll(this.scroll + 1);
          }
        }
      }

      case 2 -> {
        this.renderSavedGames(this.scroll, true, 0);
        this.unload.run();
      }
    }
  }

  @Override
  protected void mouseMove(final int x, final int y) {
    if(this.loadingStage != 1) {
      return;
    }

    for(int i = 0; i < 3; i++) {
      final int slotWidth = 344;
      final int slotHeight = 64;
      final int slotX = 20;
      final int slotY = getSlotY(i);

      if(MathHelper.inBox(x, y, slotX, slotY, slotWidth, slotHeight)) {
        if(i != this.selectedSlot) {
          playSound(1);
          this.selectedSlot = i;
          this.highlightLeftHalf.y_44 = getSlotY(this.selectedSlot);
          this.highlightRightHalf.y_44 = getSlotY(this.selectedSlot);
        }
      }
    }
  }

  @Override
  protected void mouseClick(final int x, final int y, final int button, final int mods) {
    if(this.loadingStage != 1) {
      return;
    }

    for(int i = 0; i < 3; i++) {
      final int slotWidth = 344;
      final int slotHeight = 64;
      final int slotX = 20;
      final int slotY = getSlotY(i);

      if(MathHelper.inBox(x, y, slotX, slotY, slotWidth, slotHeight)) {
        this.selectedSlot = i;
        this.onSelect(this.scroll + i);
      }
    }
  }

  @Override
  protected void keyPress(final int key, final int scancode, final int mods) {
    if(key == GLFW.GLFW_KEY_ESCAPE && mods == 0) {
      playSound(3);
      this.loadingStage = 2;
    }
  }

  @Override
  protected void mouseScroll(final double deltaX, final double deltaY) {
    if(this.loadingStage != 1) {
      return;
    }

    if(this.scrollAccumulator < 0 && deltaY > 0 || this.scrollAccumulator > 0 && deltaY < 0) {
      this.scrollAccumulator = 0;
    }

    this.scrollAccumulator += deltaY;
  }

  private void scroll(final int scroll) {
    playSound(1);
    this.scroll = scroll;
    this.highlightLeftHalf.y_44 = getSlotY(this.selectedSlot);
    this.highlightRightHalf.y_44 = getSlotY(this.selectedSlot);
    deallocateRenderables(0);
    this.renderSavedGames(this.scroll, true, 0xff);
  }

  private void renderSavedGames(final int fileScroll, final boolean renderSaves, final int a2) {
    if(a2 == 0xff) {
      renderGlyphs(glyphs_80114258, 0, 0);
    }

    if(renderSaves) {
      for(int i = 0; i < 3; i++) {
        this.renderSaveSlot(i, fileScroll + i, a2);
      }
    }

    uploadRenderables();
  }

  protected abstract int menuCount();
  protected abstract void renderSaveSlot(final int slot, final int fileIndex, final int a2);
  protected abstract void onSelect(final int slot);
  protected abstract void onMessageboxResult(final MessageBoxResult result);
}