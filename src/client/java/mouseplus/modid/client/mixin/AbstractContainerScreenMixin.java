package mouseplus.modid.client.mixin;

import mouseplus.modid.client.MouseActions;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    // Shift + ЛКМ по слоту = начало "быстрого перетаскивания"
    @Inject(method = "slotClicked", at = @At("HEAD"))
    private void mouseplus$onSlotClicked(Slot slot, int slotId, int button, ClickType type, CallbackInfo ci) {
        if (type == ClickType.QUICK_MOVE && button == 0 && slot != null) {
            MouseActions.quickDrag = true;
            MouseActions.visited.clear();
            MouseActions.visited.add(slot.index); // этот слот ваниль уже обработала
        } else if (type != ClickType.QUICK_MOVE) {
            MouseActions.resetDrag();
        }
    }
}
