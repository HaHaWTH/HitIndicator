package com.rosymaple.hitindication.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.capability.latesthits.ClientLatestHits;
import com.rosymaple.hitindication.capability.latesthits.Hit;
import com.rosymaple.hitindication.capability.latesthits.Indicator;
import com.rosymaple.hitindication.config.HitIndicatorClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = HitIndication.MODID, value = Dist.CLIENT)
public class RenderEvents {
    private static final ResourceLocation INDICATOR_RED = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_red.png");
    private static final ResourceLocation INDICATOR_BLUE = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_blue.png");

    private static final int textureWidth = 42;
    private static final int textureHeight = 13;

    @SubscribeEvent
    public static void onRender(RenderGameOverlayEvent.Post event) {
        if(event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE)
            return;

        Minecraft mc = Minecraft.getInstance();
        TextureManager textureManager = mc.getTextureManager();

        int screenMiddleX = event.getWindow().getScaledWidth() / 2;
        int screenMiddleY = event.getWindow().getScaledHeight() / 2;

        Vector2f lookVec = getLookVec(mc.player);
        Vector2f playerPos = new Vector2f((float)mc.player.getPosX(), (float)mc.player.getPosZ());
        for(Hit hit : ClientLatestHits.latestHits) {
            drawIndicator(event.getMatrixStack(), hit, textureManager, screenMiddleX, screenMiddleY, playerPos, lookVec);
        }
    }

    private static void drawIndicator(MatrixStack stack, Hit hit, TextureManager textureManager, int screenMiddleX, int screenMiddleY, Vector2f playerPos, Vector2f lookVec) {
        Vector3d sourceVec3d = hit.getLocation();
        Vector2f diff = new Vector2f((float)(sourceVec3d.x - playerPos.x), (float)(sourceVec3d.z - playerPos.y));
        double angleBetween = angleBetween(lookVec, diff);
        float opacity = hit.getLifeTime() >= 25
                ? HitIndicatorClientConfigs.IndicatorOpacity.get()
                : HitIndicatorClientConfigs.IndicatorOpacity.get() * hit.getLifeTime() / 25.0f;
        opacity /= 100.0f;

        float defaultScale = 1 + HitIndicatorClientConfigs.IndicatorDefaultScale.get() / 100.0f;
        int scaledTextureWidth = (int)Math.floor(textureWidth * defaultScale);
        int scaledTextureHeight = (int)Math.floor(textureHeight * defaultScale);
        if(HitIndicatorClientConfigs.SizeDependsOnDamage.get()) {
            float scale = MathHelper.clamp(hit.getDamagePercent() > 30 ? 1 + hit.getDamagePercent() / 125.0f : 1, 0, 3);
            scaledTextureWidth = (int)Math.floor(scaledTextureWidth * scale);
            scaledTextureHeight = (int)Math.floor(scaledTextureHeight* scale);
        }

        bindIndicatorTexture(textureManager, hit.getIndicator());

        GL11.glPushMatrix();
        GL11.glColor4f(1, 1, 1, opacity);
        GL11.glTranslatef(screenMiddleX, screenMiddleY, 0);
        GL11.glRotatef((float)angleBetween, 0, 0, 1);
        GL11.glTranslatef(-screenMiddleX, -screenMiddleY, 0);
        AbstractGui.blit(stack, screenMiddleX - scaledTextureWidth / 2, screenMiddleY - scaledTextureHeight / 2 - 30 , 0, 0, scaledTextureWidth, scaledTextureHeight, scaledTextureWidth, scaledTextureHeight);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    private static void bindIndicatorTexture(TextureManager textureManager, Indicator type) {
        switch(type) {
            case BLUE: textureManager.bindTexture(INDICATOR_BLUE);  break;
            default: textureManager.bindTexture(INDICATOR_RED);  break;
        }
    }

    private static double angleBetween(Vector2f first, Vector2f second) {
        double dot = first.x * second.x + first.y * second.y;
        double cross = first.x * second.y - second.x * first.y;
        double res = Math.atan2(cross, dot) * 180 / Math.PI;

        return res;
    }

    private static Vector2f getLookVec(ClientPlayerEntity player) {
        return new Vector2f((float)(-Math.sin(-player.rotationYaw * Math.PI / 180.0 - Math.PI)), (float)(-Math.cos(-player.rotationYaw * Math.PI / 180.0 - Math.PI)));
    }
}
