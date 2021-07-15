package com.inspw.mcmod.anvildupefix.mixin;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.BlockTags;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(
            method = "onTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
            cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerContext;run(Ljava/util/function/BiConsumer;)V")
    )
    public void onTakeOutputContextRunHook(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        this.context.run((world, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            if (!player.getAbilities().creativeMode && blockState.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
                BlockState newBlockState = AnvilBlock.getLandingState(blockState);
                if (newBlockState == null) {
                    world.removeBlock(blockPos, false);
                    world.syncWorldEvent(1029, blockPos, 0);
                    // This is the bug.
                    // player.getInventory().offerOrDrop(stack);
                } else {
                    world.setBlockState(blockPos, newBlockState, 2);
                    world.syncWorldEvent(1030, blockPos, 0);
                }
            } else {
                world.syncWorldEvent(1030, blockPos, 0);
            }
        });
        ci.cancel();
    }

    @Override
    @Shadow
    protected boolean canTakeOutput(PlayerEntity player, boolean present) {
        return false;
    }

    @Override
    @Shadow
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
    }

    @Override
    @Shadow
    protected boolean canUse(BlockState state) {
        return false;
    }

    @Override
    @Shadow
    public void updateResult() {
    }
}
