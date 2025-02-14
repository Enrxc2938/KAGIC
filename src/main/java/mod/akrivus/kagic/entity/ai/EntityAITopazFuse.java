package mod.akrivus.kagic.entity.ai;

import java.util.List;

import mod.akrivus.kagic.entity.gem.EntityTopaz;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAITopazFuse extends EntityAIBase {
    private final EntityTopaz topaz;
    private final double movementSpeed;
    private EntityTopaz otherTopaz;	
    public EntityAITopazFuse(EntityTopaz topaz, double speed) {
        this.topaz = topaz;
        this.movementSpeed = speed;
        this.setMutexBits(1);
    }
    public boolean shouldExecute() {
    	if (this.topaz.canFuse()) {
	    	List<EntityTopaz> list = this.topaz.world.<EntityTopaz>getEntitiesWithinAABB(EntityTopaz.class, this.topaz.getEntityBoundingBox().grow(16.0D, 8.0D, 16.0D));
	        double distance = Double.MAX_VALUE;
	        for (EntityTopaz topaz : list) {
	            if (topaz.canFuseWith(this.topaz) && topaz.compatIndex != this.topaz.compatIndex) {
	                double newDistance = this.topaz.getDistanceSqToEntity(topaz);
	                if (newDistance <= distance) {
	                    distance = newDistance;
	                    this.otherTopaz = topaz;
	                }
	            }
	        }
	    	return this.otherTopaz != null;
    	}
    	return false;
    }
    public boolean continueExecuting() {
        return this.otherTopaz != null && !this.otherTopaz.isDead && this.topaz.canEntityBeSeen(this.otherTopaz);
    }
    public void startExecuting() {
		this.topaz.getLookHelper().setLookPositionWithEntity(this.otherTopaz, 30.0F, 30.0F);
    }
    public void resetTask() {
    	this.topaz.getNavigator().clearPathEntity();
        this.otherTopaz = null;
    }
    public void updateTask() {
    	if (this.topaz.getDistanceSqToEntity(this.otherTopaz) > this.otherTopaz.width * 2) {
			this.topaz.getNavigator().tryMoveToEntityLiving(this.otherTopaz, this.movementSpeed);
		}
    	else if (this.topaz.compatIndex > this.otherTopaz.compatIndex) {
    		this.topaz.world.spawnEntity(this.topaz.fuse(this.otherTopaz));
	    	this.otherTopaz.setDead();
	    	this.topaz.setDead();
	        this.resetTask();
    	}
    }
}