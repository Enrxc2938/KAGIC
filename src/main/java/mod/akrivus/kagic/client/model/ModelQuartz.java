package mod.akrivus.kagic.client.model;

import mod.akrivus.kagic.entity.EntityGem;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

public class ModelQuartz extends ModelBiped {
    private final ModelRenderer bipedCape;

    public ModelQuartz() {
		super(0.0F, 0.0F, 64, 64);
		// Head.
		this.bipedHead = new ModelRenderer(this, 0, 0);
		this.bipedHead.addBox(-4F, -12F, -4F, 8, 8, 8);
		this.bipedHead.setRotationPoint(0F, 0F, 0F);
	    // Hair.
		this.bipedHeadwear = new ModelRenderer(this, 32, 0);
		this.bipedHeadwear.addBox(-4F, -12F, -4F, 8, 8, 8, 1.1F);
		this.bipedHeadwear.setRotationPoint(0F, 0F, 0F);
		// Body.
		this.bipedBody = new ModelRenderer(this, 16, 16);
		this.bipedBody.addBox(-5F, -4F, -3F, 10, 16, 6);
		this.bipedBody.setRotationPoint(0F, 0F, 0F);
	    // Right arm.
		this.bipedRightArm = new ModelRenderer(this, 48, 16);
		this.bipedRightArm.addBox(-4F, -4F, -2F, 4, 14, 4);
		this.bipedRightArm.setRotationPoint(0F, 0F, 0F);
	    // Left arm.
		this.bipedLeftArm = new ModelRenderer(this, 48, 34);
		this.bipedLeftArm.addBox(0F, -4F, -2F, 4, 14, 4);
		this.bipedLeftArm.setRotationPoint(0F, 0F, 0F);
		// Right leg.
		this.bipedRightLeg = new ModelRenderer(this, 0, 16);
		this.bipedRightLeg.addBox(1F, 0F, -2F, 4, 12, 4);
		this.bipedRightLeg.setRotationPoint(0F, 0F, 0F);
	  	// Left leg.
		this.bipedLeftLeg = new ModelRenderer(this, 0, 32);
		this.bipedLeftLeg.addBox(-5F, 0F, -2F, 4, 12, 4);
		this.bipedLeftLeg.setRotationPoint(0F, 0F, 0F);

        this.bipedCape = new ModelRenderer(this, 0, 0);
        this.bipedCape.setTextureSize(64, 32);
        this.bipedCape.addBox(-5.0F, -4.0F, -2.f, 10, 20, 1);
    }
    
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		this.bipedHead.render(scale);
		this.bipedHeadwear.render(scale);
		this.bipedBody.render(scale);
		this.bipedRightArm.render(scale);
		this.bipedLeftArm.render(scale);
		this.bipedRightLeg.render(scale);
		this.bipedLeftLeg.render(scale);
	}
	
    public void renderCape(float scale)
    {
        this.bipedCape.render(scale);
    }

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
/*
        if (entityIn.isSneaking()) {
            this.bipedCape.rotationPointY = 2.0F;
        } else {
            this.bipedCape.rotationPointY = 0.0F;
        }
        this.bipedCape.rotationPointY = -4.0F;
        this.bipedBody.rotationPointZ = -1f;*/
	}
	
	public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_, float partialTickTime) {
        this.rightArmPose = ModelBiped.ArmPose.EMPTY;
        this.leftArmPose = ModelBiped.ArmPose.EMPTY;
        
        ItemStack itemstack = entitylivingbaseIn.getHeldItem(EnumHand.MAIN_HAND);
        if (itemstack != null && itemstack.getItem() == Items.BOW && ((EntityGem) entitylivingbaseIn).isSwingingArms()) {
            if (entitylivingbaseIn.getPrimaryHand() == EnumHandSide.RIGHT) {
                this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
            else {
                this.leftArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
        }
        super.setLivingAnimations(entitylivingbaseIn, p_78086_2_, p_78086_3_, partialTickTime);
    }
}
