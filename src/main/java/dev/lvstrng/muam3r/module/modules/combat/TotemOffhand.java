package dev.lvstrng.muam3r.module.modules.combat;

import dev.lvstrng.muam3r.event.events.TickListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.EncryptedString;
import dev.lvstrng.muam3r.utils.InventoryUtils;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class TotemOffhand extends Module implements TickListener {
    private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0, 5, 0, 1);
    private final NumberSetting equipDelay = new NumberSetting(EncryptedString.of("Equip Delay"), 1, 5, 1, 1);
    private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), false);

    private int switchClock, equipClock, switchBackClock;
    private int previousSlot = -1;
    boolean sent, active = false;

    public TotemOffhand() {
        super(EncryptedString.of("Totem Offhand"), EncryptedString.of("Switches to your totem slot and offhands a totem if you dont have one already"), -1, Category.COMBAT);
        addSettings(switchDelay, equipDelay, switchBack);
    }

    @Override
    public void onEnable() {
        eventManager.add(TickListener.class, this);
        reset();

        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(TickListener.class, this);
        super.onDisable();
    }

    @Override
    public void onTick() {
        if(mc.currentScreen != null)
            return;

        if(mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING)
            active = true;

        if(active) {
            if (switchClock < switchDelay.getValueInt()) {
                switchClock++;
                return;
            }

            if(previousSlot == -1)
                previousSlot = mc.player.getInventory().selectedSlot;

            if (InventoryUtils.selectItemFromHotbar(Items.TOTEM_OF_UNDYING)) {
                if (equipClock < equipDelay.getValueInt()) {
                    equipClock++;
                    return;
                }

                if (!sent) {
                    mc.getNetworkHandler().getConnection().send(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                    sent = true;
                    return;
                }
            }

            if(switchBackClock < switchDelay.getValue()) {
                switchBackClock++;
            } else {
                if(switchBack.getValue())
                    InventoryUtils.setInvSlot(previousSlot);

                reset();
            }
        }
    }

    public void reset() {
        switchClock = 0;
        equipClock = 0;
        switchBackClock = 0;
        previousSlot = -1;

        sent = false;
        active = false;
    }
}