package mod.akrivus.kagic.entity.ai;

import java.util.List;

import mod.akrivus.kagic.entity.gem.EntityRuby;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIRubyFuse extends EntityAIBase {
    private final EntityRuby ruby;
    private final double movementSpeed;
    private EntityRuby otherRuby;	
    public EntityAIRubyFuse(EntityRuby ruby, double speed) {
        this.ruby = ruby;
        this.movementSpeed = speed;
        this.setMutexBits(1);
    }
    public boolean shouldExecute() {
    	if (this.ruby.canFuse()) {
	    	List<EntityRuby> list = this.ruby.world.<EntityRuby>getEntitiesWithinAABB(EntityRuby.class, this.ruby.getEntityBoundingBox().grow(16.0D, 8.0D, 16.0D));
	        double distance = Double.MAX_VALUE;
	        for (EntityRuby ruby : list) {
	            if (ruby.canFuseWith(this.ruby) && ruby.compatIndex != this.ruby.compatIndex) {
	                double newDistance = this.ruby.getDistanceSqToEntity(ruby);
	                if (newDistance <= distance) {
	                    distance = newDistance;
	                    this.otherRuby = ruby;
	                }
	            }
	        }
	    	return this.otherRuby != null;
    	}
    	return false;
    }
    public boolean continueExecuting() {
        return this.otherRuby != null && !this.otherRuby.isDead && this.ruby.canEntityBeSeen(this.otherRuby);
    }
    public void startExecuting() {
		this.ruby.getLookHelper().setLookPositionWithEntity(this.otherRuby, 30.0F, 30.0F);
    }
    public void resetTask() {
    	this.ruby.getNavigator().clearPathEntity();
        this.otherRuby = null;
    }
    public void updateTask() {
    	if (this.ruby.getDistanceSqToEntity(this.otherRuby) > this.otherRuby.width * 2) {
			this.ruby.getNavigator().tryMoveToEntityLiving(this.otherRuby, this.movementSpeed);
		}
    	else if (this.ruby.compatIndex > this.otherRuby.compatIndex) {
    		this.ruby.world.spawnEntity(this.ruby.fuse(this.otherRuby));
	    	this.otherRuby.setDead();
	    	this.ruby.setDead();
	        this.resetTask();
    	}
    }
}