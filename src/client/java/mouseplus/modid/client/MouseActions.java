package mouseplus.modid.client;

import mouseplus.modid.client.mixin.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public final class MouseActions {
    private MouseActions() {}

    // Состояние Shift+перетаскивания
    public static boolean quickDrag = false;
    public static final Set<Integer> visited = new HashSet<>();

    public static void resetDrag() {
        quickDrag = false;
        visited.clear();
    }

    private static void click(Minecraft mc, AbstractContainerMenu menu, Slot slot, int button, ClickType type) {
        mc.gameMode.handleInventoryMouseClick(menu.containerId, slot.index, button, type, mc.player);
    }

    // Вызывается каждый кадр, пока открыт инвентарь
    public static void onRender(Minecraft mc, AbstractContainerScreen<?> screen) {
        if (!quickDrag) return;
        if (mc.player == null || mc.gameMode == null) { resetDrag(); return; }
        if (!mc.mouseHandler.isLeftPressed()) { resetDrag(); return; }

        AbstractContainerMenu menu = screen.getMenu();
        if (!menu.getCarried().isEmpty()) return;

        Slot hovered = ((AbstractContainerScreenAccessor) screen).mouseplus$getHoveredSlot();
        if (hovered == null || !hovered.hasItem() || visited.contains(hovered.index)) return;

        visited.add(hovered.index);
        click(mc, menu, hovered, 0, ClickType.QUICK_MOVE);
    }

    // Колёсико: вниз = перенести 1 предмет в другой инвентарь, вверх = притянуть 1 такой же.
    // Возвращает true, если действие выполнено (тогда обычная прокрутка отменяется).
    public static boolean onScroll(Minecraft mc, AbstractContainerScreen<?> screen, double vertical) {
        if (mc.player == null || mc.gameMode == null || vertical == 0) return false;
        AbstractContainerMenu menu = screen.getMenu();
        if (!menu.getCarried().isEmpty()) return false;

        Slot hovered = ((AbstractContainerScreenAccessor) screen).mouseplus$getHoveredSlot();
        if (hovered == null || !hovered.hasItem()) return false;
        ItemStack stack = hovered.getItem();
        if (!hovered.mayPickup(mc.player) || !hovered.mayPlace(stack)) return false;

        if (vertical < 0) {
            Slot target = findTarget(menu, hovered, stack);
            if (target == null) return false;
            click(mc, menu, hovered, 0, ClickType.PICKUP); // берём стак
            click(mc, menu, target, 1, ClickType.PICKUP);  // кладём 1 шт.
            click(mc, menu, hovered, 0, ClickType.PICKUP); // возвращаем остаток
            return true;
        } else {
            if (stack.getCount() >= hovered.getMaxStackSize(stack)) return false;
            Slot source = findSource(mc, menu, hovered, stack);
            if (source == null) return false;
            click(mc, menu, source, 0, ClickType.PICKUP);
            click(mc, menu, hovered, 1, ClickType.PICKUP);
            click(mc, menu, source, 0, ClickType.PICKUP);
            return true;
        }
    }

    private static Slot findTarget(AbstractContainerMenu menu, Slot from, ItemStack stack) {
        Slot empty = null;
        for (Slot s : menu.slots) {
            if (s.container == from.container || !s.mayPlace(stack)) continue;
            ItemStack cur = s.getItem();
            if (cur.isEmpty()) {
                if (empty == null) empty = s;
            } else if (ItemStack.isSameItemSameComponents(cur, stack)
                    && cur.getCount() < s.getMaxStackSize(cur)) {
                return s; // сначала доливаем в существующие стаки
            }
        }
        return empty;
    }

    private static Slot findSource(Minecraft mc, AbstractContainerMenu menu, Slot to, ItemStack stack) {
        for (Slot s : menu.slots) {
            if (s.container == to.container || !s.hasItem() || !s.mayPickup(mc.player)) continue;
            if (ItemStack.isSameItemSameComponents(s.getItem(), stack)) return s;
        }
        return null;
    }
}
