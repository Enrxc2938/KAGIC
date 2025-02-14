package mod.akrivus.kagic.entity.ai;

import java.util.List;

import mod.akrivus.kagic.entity.EntityGem;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;

public class EntityAIPickUpItems extends EntityAIBase {
    private final EntityGem gem;
    private final double movementSpeed;
    private EntityItem item;

    public EntityAIPickUpItems(EntityGem entityIn, double movementSpeedIn) {
        this.gem = entityIn;
        this.movementSpeed = movementSpeedIn;
        this.setMutexBits(3);
    }
    public boolean shouldExecute() {
    	List<EntityItem> list = this.gem.world.<EntityItem>getEntitiesWithinAABB(EntityItem.class, this.gem.getEntityBoundingBox().grow(8.0D, 8.0D, 8.0D));
        double maxDistance = Double.MAX_VALUE;
        for (EntityItem item : list) {
            double newDistance = this.gem.getDistanceSqToEntity(item);
            if (newDistance <= maxDistance && this.gem.canPickUpItem(item.getItem().getItem()) && this.gem.canEntityBeSeen(item) && !item.isDead) {
                maxDistance = newDistance;
                this.item = item;
            }
        }
        return this.item != null && !this.item.isDead;
    }
    public boolean continueExecuting() {
        return this.item != null && !this.item.isDead && this.gem.canEntityBeSeen(this.item) && !this.gem.getNavigator().noPath();
    }
    public void startExecuting() {
        this.gem.getLookHelper().setLookPositionWithEntity(this.item, 30.0F, 30.0F);
    }
    public void resetTask() {
        this.gem.getNavigator().clearPathEntity();
        this.item = null;
    }
    public void updateTask() {
        if (this.gem.getDistanceSqToEntity(this.item) > 1) {
	       	this.gem.getNavigator().tryMoveToEntityLiving(this.item, this.movementSpeed);
        }
    }
}