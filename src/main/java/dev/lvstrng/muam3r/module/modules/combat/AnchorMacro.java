package dev.lvstrng.muam3r.module.modules.combat;

import dev.lvstrng.muam3r.event.events.ItemUseListener;
import dev.lvstrng.muam3r.event.events.TickListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.module.setting.KeybindSetting;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.*;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

//not mine
public final class AnchorMacro extends Module implements TickListener, ItemUseListener {
	private final BooleanSetting whileUse = new BooleanSetting(EncryptedString.of("While Use"), true).setDescription(EncryptedString.of("If it should trigger while eating/using shield"));
	private final BooleanSetting stopOnKill = new BooleanSetting(EncryptedString.of("Stop on Kill"), false).setDescription(EncryptedString.of("Doesn't anchor if body nearby"));
	private final BooleanSetting clickSimulation = new BooleanSetting(EncryptedString.of("Click Simulation"), false).setDescription(EncryptedString.of("Makes the CPS hud think you're legit"));
	private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0, 20, 0, 1);
	private final NumberSetting switchChance = new NumberSetting(EncryptedString.of("Switch Chance"), 0, 100, 100, 1);
	private final NumberSetting placeChance = new NumberSetting(EncryptedString.of("Place Chance"), 0, 100, 100, 1).setDescription(EncryptedString.of("Randomization"));
	private final NumberSetting glowstoneDelay = new NumberSetting(EncryptedString.of("Glowstone Delay"), 0, 20, 0, 1);
	private final NumberSetting glowstoneChance = new NumberSetting(EncryptedString.of("Glowstone Chance"), 0, 100, 100, 1);
	private final NumberSetting explodeDelay = new NumberSetting(EncryptedString.of("Explode Delay"), 0, 20, 0, 1);
	private final NumberSetting explodeChance = new NumberSetting(EncryptedString.of("Explode Chance"), 0, 100, 100, 1);
	private final NumberSetting explodeSlot = new NumberSetting(EncryptedString.of("Explode Slot"), 1, 9, 1, 1);
	private final BooleanSetting onlyOwn = new BooleanSetting(EncryptedString.of("Only Own"), false);
	private final BooleanSetting onlyCharge = new BooleanSetting(EncryptedString.of("Only Charge"), false);

	private int switchClock = 0;
	private int glowstoneClock = 0;
	private int explodeClock = 0;

	//hashset cuz in a hashset stuff cant repeat iirc
	private final Set<BlockPos> ownedAnchors = new HashSet<>();

	public AnchorMacro() {
		super(EncryptedString.of("Anchor Macro"),
				EncryptedString.of("Automatically blows up respawn anchors for you"),
				-1,
				Category.COMBAT);
		addSettings(whileUse, stopOnKill, clickSimulation, placeChance, switchDelay, switchChance, glowstoneDelay, glowstoneChance, explodeDelay, explodeChance, explodeSlot, onlyOwn, onlyCharge);
	}

	@Override
	public void onEnable() {
		eventManager.add(TickListener.class, this);
		eventManager.add(ItemUseListener.class, this);
		switchClock = 0;
		glowstoneClock = 0;
		explodeClock = 0;
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(TickListener.class, this);
		eventManager.remove(ItemUseListener.class, this);
		super.onDisable();
	}

	@Override
	public void onTick() {
		if (mc.currentScreen != null)
			return;

		if (((mc.player.getMainHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD) || mc.player.getMainHandStack().getItem() instanceof ShieldItem || mc.player.getOffHandStack().getItem() instanceof ShieldItem || mc.player.getOffHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD)) && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS) && !whileUse.getValue())
			return;

		if(stopOnKill.getValue() && WorldUtils.isDeadBodyNearby())
			return;

		int randomInt = MathUtils.randomInt(1, 100);

		if (KeyUtils.isKeyPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
			if (mc.crosshairTarget instanceof BlockHitResult hit) {
				if (BlockUtils.isBlock(hit.getBlockPos(), Blocks.RESPAWN_ANCHOR)) {
					if (onlyOwn.getValue() && !ownedAnchors.contains(hit.getBlockPos()))
						return;

					mc.options.useKey.setPressed(false);

					if (BlockUtils.isAnchorNotCharged(hit.getBlockPos())) {
						randomInt = MathUtils.randomInt(1, 100);

						if (randomInt <= placeChance.getValueInt()) {
							if (!mc.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
								if (switchClock != switchDelay.getValueInt()) {
									switchClock++;
									return;
								}

								randomInt = MathUtils.randomInt(1, 100);

								if(randomInt <= switchChance.getValueInt()) {
									switchClock = 0;
									InventoryUtils.selectItemFromHotbar(Items.GLOWSTONE);
								}
							}

							if (mc.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
								if (glowstoneClock != glowstoneDelay.getValueInt()) {
									glowstoneClock++;
									return;
								}

								randomInt = MathUtils.randomInt(1, 100);

								if(randomInt <= glowstoneChance.getValueInt()) {
									glowstoneClock = 0;

									if (clickSimulation.getValue())
										MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);

									WorldUtils.placeBlock(hit, true);
								}
							}
						}
					}

					if (BlockUtils.isAnchorCharged(hit.getBlockPos())) {
						int slot = explodeSlot.getValueInt() - 1;

						if (mc.player.getInventory().selectedSlot != slot) {
							if (switchClock != switchDelay.getValueInt()) {
								switchClock++;
								return;
							}

							if(randomInt <= switchChance.getValueInt()) {
								switchClock = 0;
								mc.player.getInventory().selectedSlot = slot;
							}
						}

						if (mc.player.getInventory().selectedSlot == slot) {
							if (explodeClock != explodeDelay.getValueInt()) {
								explodeClock++;
								return;
							}

							randomInt = MathUtils.randomInt(1, 100);

							if(randomInt <= explodeChance.getValueInt()) {
								explodeClock = 0;

								if (!onlyCharge.getValue()) {
									if (clickSimulation.getValue())
										MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);

									WorldUtils.placeBlock(hit, true);

									ownedAnchors.remove(hit.getBlockPos());
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onItemUse(ItemUseEvent event) {
		if (mc.crosshairTarget instanceof BlockHitResult hitResult && hitResult.getType() == HitResult.Type.BLOCK) {
			if (mc.player.getMainHandStack().getItem() == Items.RESPAWN_ANCHOR) {
				Direction dir = hitResult.getSide();
				BlockPos pos = hitResult.getBlockPos();

				if (!mc.world.getBlockState(pos).isReplaceable()) {
					switch (dir) {
						case UP: {
							ownedAnchors.add(pos.add(0, 1, 0));
							break;
						}
						case DOWN: {
							ownedAnchors.add(pos.add(0, -1, 0));
							break;
						}
						case EAST: {
							ownedAnchors.add(pos.add(1, 0, 0));
							break;
						}
						case WEST: {
							ownedAnchors.add(pos.add(-1, 0, 0));
							break;
						}
						case NORTH: {
							ownedAnchors.add(pos.add(0, 0, -1));
							break;
						}
						case SOUTH: {
							ownedAnchors.add(pos.add(0, 0, 1));
							break;
						}
					}
				} else ownedAnchors.add(pos);
			}

			BlockPos bp = hitResult.getBlockPos();

			if(BlockUtils.isAnchorCharged(bp))
				ownedAnchors.remove(bp);
		}
	}
}
