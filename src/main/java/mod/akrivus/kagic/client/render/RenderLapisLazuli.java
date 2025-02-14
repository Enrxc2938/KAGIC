package mod.akrivus.kagic.client.render;

import mod.akrivus.kagic.client.model.ModelLapisLazuli;
import mod.akrivus.kagic.client.render.layers.LayerGemPlacement;
import mod.akrivus.kagic.client.render.layers.LayerHair;
import mod.akrivus.kagic.client.render.layers.LayerInsignia;
import mod.akrivus.kagic.client.render.layers.LayerLapisLazuliItem;
import mod.akrivus.kagic.client.render.layers.LayerSkin;
import mod.akrivus.kagic.client.render.layers.LayerUniform;
import mod.akrivus.kagic.client.render.layers.LayerVisor;
import mod.akrivus.kagic.entity.gem.EntityLapisLazuli;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.util.ResourceLocation;

public class RenderLapisLazuli extends RenderLivingBase<EntityLapisLazuli> {
	public RenderLapisLazuli() {
        super(Minecraft.getMinecraft().getRenderManager(), new ModelLapisLazuli(), 0.25F);
        this.addLayer(new LayerLapisLazuliItem(this));
        this.addLayer(new LayerSkin(this));
        this.addLayer(new LayerHair(this));
        this.addLayer(new LayerUniform(this));
        this.addLayer(new LayerInsignia(this));
        this.addLayer(new LayerVisor(this));
        this.addLayer(new LayerGemPlacement(this));
    }
	protected void preRenderCallback(EntityLapisLazuli entitylivingbaseIn, float partialTickTime) {
		if (entitylivingbaseIn.isBeingRidden() && entitylivingbaseIn.canBeSteered()) {
			GlStateManager.translate(0F, -1F, 1.25F);
			GlStateManager.rotate(90.0F, 1, 0, 0);
		}
	}
	protected ResourceLocation getEntityTexture(EntityLapisLazuli entity) {
		return new ResourceLocation("kagic:textures/entities/lapis_lazuli/lapis_lazuli.png");
	}
}
