package mouseplus.modid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

public class MousePlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;
            if (screen instanceof CreativeModeInventoryScreen) return;

            MouseActions.resetDrag();

            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) ->
                    MouseActions.onRender(client, containerScreen));

            ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, horizontal, vertical) ->
                    !MouseActions.onScroll(client, containerScreen, vertical));

            ScreenEvents.remove(screen).register(s -> MouseActions.resetDrag());
        });
    }
}
