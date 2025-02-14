package mod.akrivus.kagic.entity.gem;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Predicate;

import mod.akrivus.kagic.entity.EntityGem;
import mod.akrivus.kagic.entity.EntitySlag;
import mod.akrivus.kagic.entity.ai.EntityAIAlignGems;
import mod.akrivus.kagic.entity.ai.EntityAIFollowDiamond;
import mod.akrivus.kagic.entity.ai.EntityAIHarvestFarmland;
import mod.akrivus.kagic.entity.ai.EntityAIPickUpItems;
import mod.akrivus.kagic.entity.ai.EntityAIStandGuard;
import mod.akrivus.kagic.entity.ai.EntityAIStay;
import mod.akrivus.kagic.init.ModItems;
import mod.akrivus.kagic.init.ModSounds;
import mod.akrivus.kagic.util.injector.InjectorResult;
import mod.heimrarnadalr.kagic.util.Colors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.InvWrapper;

public class EntityPeridot extends EntityGem implements IInventoryChangedListener {
	public static final HashMap<IBlockState, Double> PERIDOT_YIELDS = new HashMap<IBlockState, Double>();
	public InventoryBasic gemStorage;
	public InvWrapper gemStorageHandler;
	public InventoryBasic harvest;
	public InvWrapper harvestHandler;
	private int harvestTimer = 0;
	private int dropTimer = 100;
	private BlockPos lastCheckPos;
	private long lastCheckTime;
	private InjectorResult lastResult;
	
	private static final int SKIN_COLOR_BEGIN = 0x98FF72;
	private static final int SKIN_COLOR_END = 0x9CEC4D;
	private static final int NUM_HAIRSTYLES = 2;
	private static final int HAIR_COLOR_BEGIN = 0xEFF6B4;
	private static final int HAIR_COLOR_END = 0xFFFF9B;
	
	public EntityPeridot(World worldIn) {
		super(worldIn);
		this.setSize(0.7F, 1.9F);
		this.initGemStorage();
		this.seePastDoors();

		//Define valid gem cuts and placements
		//No back of head because it gets covered up by squaridot hair
		//this.setCutPlacement(GemCuts.PERIDOT, GemPlacements.BACK_OF_HEAD);
		this.setCutPlacement(GemCuts.PERIDOT, GemPlacements.FOREHEAD);
		this.setCutPlacement(GemCuts.PERIDOT, GemPlacements.LEFT_EYE);
		this.setCutPlacement(GemCuts.PERIDOT, GemPlacements.RIGHT_EYE);
		this.setCutPlacement(GemCuts.TRIANGULAR, GemPlacements.BACK);
		this.setCutPlacement(GemCuts.TRIANGULAR, GemPlacements.CHEST);
		this.setCutPlacement(GemCuts.TRIANGULAR, GemPlacements.BELLY);

		// Apply entity AI.
		this.stayAI = new EntityAIStay(this);
		this.tasks.addTask(1, new EntityAIAvoidEntity<EntityCreeper>(this, EntityCreeper.class, new Predicate<EntityCreeper>() {
			public boolean apply(EntityCreeper input) {
				return ((EntityCreeper) input).getCreeperState() == 1;
			}
        }, 6.0F, 1.0D, 1.2D));
		this.tasks.addTask(1, new EntityAIFollowDiamond(this, 1.0D));
        this.tasks.addTask(3, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(4, new EntityAIAlignGems(this, 0.9D));
        this.tasks.addTask(4, new EntityAIHarvestFarmland(this, 0.6D));
        this.tasks.addTask(4, new EntityAIPickUpItems(this, 0.9D));
        this.tasks.addTask(5, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsTarget(this, 0.414D, 32.0F));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityMob.class, 16.0F));
        this.tasks.addTask(7, new EntityAIStandGuard(this, 0.6D));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        
        // Apply target AI.
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntitySlag>(this, EntitySlag.class, true, true));
        
        // Apply entity attributes.
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(400.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
        this.droppedGemItem = ModItems.PERIDOT_GEM;
		this.droppedCrackedGemItem = ModItems.CRACKED_PERIDOT_GEM;
	}
	
	public float[] getGemColor() {
    	return new float[] { 47F / 255F, 248F / 255F, 42F / 255F };
    }
	
	public void convertGems(int placement) {
    	switch (placement) {
    	case 0:
    		this.setGemPlacement(GemPlacements.FOREHEAD.id);
    		this.setGemCut(GemCuts.PERIDOT.id);
    		break;
    	case 1:
    		this.setGemPlacement(GemPlacements.CHEST.id);
    		this.setGemCut(GemCuts.TRIANGULAR.id);
    		break;
    	}
    }
	
	/*********************************************************
	 * Methods related to loading.                           *
	 *********************************************************/
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        return super.onInitialSpawn(difficulty, livingdata);
    }

    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.gemStorage.getSizeInventory(); ++i) {
            ItemStack itemstack = this.gemStorage.getStackInSlot(i);
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("slot", (byte) i);
            itemstack.writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }
        compound.setTag("items", nbttaglist);
        nbttaglist = new NBTTagList();
        for (int i = 0; i < this.harvest.getSizeInventory(); ++i) {
            ItemStack itemstack = this.harvest.getStackInSlot(i);
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("slot", (byte) i);
            itemstack.writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }
        compound.setTag("harvestItems", nbttaglist);
        compound.setInteger("harvestTimer", this.harvestTimer);
	}
    
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList("items", 10);
        this.initGemStorage();
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("slot") & 255;
            if (j >= 0 && j < this.gemStorage.getSizeInventory()) {
                this.gemStorage.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
        nbttaglist = compound.getTagList("harvestItems", 10);
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("slot") & 255;
            if (j >= 0 && j < this.harvest.getSizeInventory()) {
                this.harvest.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
        this.harvestTimer = compound.getInteger("harvestTimer");
    }
	
	/*********************************************************
	 * Methods related to interaction.                       *
	 *********************************************************/
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (!this.world.isRemote) {
			if (hand == EnumHand.MAIN_HAND) {
				ItemStack stack = player.getHeldItemMainhand();
				if (this.isTamed()) {
		        	if (this.isOwner(player)) {
		        		if (this.isCoreItem(stack)) {
		        			return super.processInteract(player, hand);
		        		}
		        		else if (stack.getItem() instanceof ItemHoe) {
		        			boolean toolChanged = true;
							if (!this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isItemEqualIgnoreDurability(stack)) {
								this.entityDropItem(this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), 0.0F);
							}
							else {
								toolChanged = false;
							}
		        			if (toolChanged) {
								ItemStack heldItem = stack.copy();
								this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, heldItem);
								this.playObeySound();
								if (!player.capabilities.isCreativeMode) {
									stack.shrink(1);
								}
		        			}
							return true;
		        		}
		        		else if (this.isFarmer()) {
	        				for (int i = 0; i < this.gemStorage.getSizeInventory(); ++i) {
	        	                ItemStack itemstack = this.gemStorage.getStackInSlot(i);
        	                	this.setCanPickUpLoot(false);
        	                	this.dropTimer = 0;
        	                	if (itemstack.getItem() == Items.WHEAT || itemstack.getItem() == Items.BEETROOT) {
        	                		this.gemStorage.setInventorySlotContents(i, ItemStack.EMPTY);
        	                		this.harvest.addItem(itemstack);
        	                	}
        	                	else if (itemstack.getCount() > 1) {
        	                		ItemStack newstack = itemstack.splitStack(itemstack.getCount() - 8);
        	                		this.gemStorage.setInventorySlotContents(i, itemstack);
        	                		this.harvest.addItem(newstack);
	        	                }
	        	            }
	        				if (this.harvest.isEmpty()) {
	        					player.sendMessage(new TextComponentString("<" + this.getName() + "> " + new TextComponentTranslation("command.kagic.peridot_no_harvest").getUnformattedComponentText()));
	        				}
	        				else {
	        					player.sendMessage(new TextComponentString("<" + this.getName() + "> " + new TextComponentTranslation("command.kagic.peridot_harvest").getUnformattedComponentText()));
	        				}
	        				//player.addStat(ModAchievements.HELLO_CORN);
	        				this.openGUI(player);
	        				this.playObeySound();
	        				return true;
		        		}
		        		else {
		        			this.checkSurroundings(this.world, this.getPosition());
		        			//player.addStat(ModAchievements.IM_REPORTING_THIS);
		        			this.playObeySound();
		        			return true;
		        		}
		        	}
		        	else {
		        		player.sendMessage(new TextComponentTranslation("command.kagic.does_not_serve_you", this.getName()));
		        		return true;
		        	}
				}
			}
		}
        return super.processInteract(player, hand);
    }
	public boolean onSpokenTo(EntityPlayer player, String message) {
    	boolean spokenTo = super.onSpokenTo(player, message);
    	message = message.toLowerCase();
    	if (this.isBeingCalledBy(player, message)) {
			this.getLookHelper().setLookPositionWithEntity(player, 30.0F, 30.0F);
			if (this.isOwner(player)) {
				if (this.isMatching("regex.kagic.harvest", message)) {
					for (int i = 0; i < this.harvest.getSizeInventory(); ++i) {
						if (player.inventory.getFirstEmptyStack() > -1) {
							player.inventory.addItemStackToInventory(this.harvest.getStackInSlot(i));
						}
					}
				}
				this.playObeySound();
			}
    	}
    	return spokenTo;
    }
	public void checkSurroundings(World worldIn, BlockPos pos) {
		if (!worldIn.isRemote) {
			InjectorResult result = ((this.lastCheckPos != null && this.getDistanceSq(this.lastCheckPos) > 32.0F) || this.lastCheckPos == null || this.world.getTotalWorldTime() - this.lastCheckTime > 2400) ? InjectorResult.create(worldIn, pos, false) : this.lastResult;
			String defectivity = Math.round(result.getDefectivity() * 100) + "%";
			if (result.getGem() != null) {
				this.getOwner().sendMessage(new TextComponentString("<" + this.getName() + "> " + new TextComponentTranslation("command.kagic.peridot_find_gem", result.getName(), defectivity).getUnformattedComponentText()));
			}
			else {
				this.getOwner().sendMessage(new TextComponentString("<" + this.getName() + "> " + new TextComponentTranslation("command.kagic.peridot_cant_find_gem").getUnformattedComponentText()));
			}
			this.lastCheckTime = this.world.getTotalWorldTime();
			this.lastCheckPos = this.getPosition();
			this.lastResult = result;
		}
	}
	
	/*********************************************************
	 * Methods related to living.                            *
	 *********************************************************/
	public void onLivingUpdate() {
		if ((!this.canPickUpLoot() && this.dropTimer > 40) && this.isFarmer()) {
			this.setCanPickUpLoot(this.isFarmer());
		}
		if (this.isFarmer()) {
			++this.harvestTimer;
		}
		if (this.dropTimer < 100) {
			++this.dropTimer;
		}
		super.onLivingUpdate();
	}
	
	/*********************************************************
     * Methods related to death.                             *
     *********************************************************/
    public void onDeath(DamageSource cause) {
    	this.setCanPickUpLoot(false);
    	super.onDeath(cause);
    }
	
	/*********************************************************
	 * Methods related to storage.                           *
	 *********************************************************/
    public void onInventoryChanged(IInventory inventory) {
		this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, this.gemStorage.getStackInSlot(0));
	}
    public void openGUI(EntityPlayer playerEntity) {
        if (!this.world.isRemote && this.isTamed()) {
            this.harvest.setCustomName(new TextComponentTranslation("command.kagic.peridot_inventory", this.getName()).getUnformattedComponentText());
            playerEntity.displayGUIChest(this.harvest);
        }
    }
    private void initGemStorage() {
        InventoryBasic gemStorage = this.gemStorage;
        this.gemStorage = new InventoryBasic("gemStorage", false, 36);
        if (gemStorage != null) {
            for (int i = 0; i < this.gemStorage.getSizeInventory(); ++i) {
                ItemStack itemstack = gemStorage.getStackInSlot(i);
                this.gemStorage.setInventorySlotContents(i, itemstack.copy());
            }
        }
        this.gemStorageHandler = new InvWrapper(this.gemStorage);
        InventoryBasic harvest = this.harvest;
        this.harvest = new InventoryBasic("harvest", true, 36);
        if (harvest != null) {
        	harvest.removeInventoryChangeListener(this);
            for (int i = 0; i < this.harvest.getSizeInventory(); ++i) {
                ItemStack itemstack = harvest.getStackInSlot(i);
                this.harvest.setInventorySlotContents(i, itemstack.copy());
            }
        }
        this.harvest.addInventoryChangeListener(this);
        this.harvestHandler = new InvWrapper(this.harvest);
        this.setCanPickUpLoot(this.isTamed());
    }
	protected void updateEquipmentIfNeeded(EntityItem itementity) {
        ItemStack itemstack = itementity.getItem();
        if (this.canPickupItem(itemstack.getItem()) && this.isFarmer()) {
	        ItemStack itemstack1 = this.gemStorage.addItem(itemstack);
	        if (itemstack1.isEmpty()) {
	            itementity.setDead();
	        }
	        else {
	            itemstack.setCount(itemstack1.getCount());
	        }
        }
    }
	public boolean canPickupItem(Item itemIn) {
        return this.isFarmer() && (itemIn == Items.POTATO || itemIn == Items.CARROT || itemIn == Items.WHEAT || itemIn == Items.WHEAT_SEEDS || itemIn == Items.BEETROOT || itemIn == Items.BEETROOT_SEEDS || itemIn == Items.NETHER_WART);
    }
	public boolean isFarmer() {
		return this.isTamed() && this.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemHoe;
	}
	
	/*********************************************************
	 * Methods related to combat.                            *
	 *********************************************************/
	public void onStruckByLightning(EntityLightningBolt lightningBolt) {
		if (!this.world.isRemote && !this.isDead) {
			this.dropItem(ModItems.RECORD_LITTLE_PERIDOT, 1);
		}
	}

	/*********************************************************
	 * Methods related to sounds.                            *
	 *********************************************************/
	protected SoundEvent getAmbientSound() {
		return ModSounds.PERIDOT_LIVING;
	}
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.PERIDOT_HURT;
	}
	protected SoundEvent getObeySound() {
		return ModSounds.PERIDOT_OBEY;
	}
	protected SoundEvent getDeathSound() {
		return ModSounds.PERIDOT_DEATH;
	}

	/*********************************************************
	 * Methods related to rendering.                         *
	 *********************************************************/
	@Override
	protected int generateSkinColor() {
		ArrayList<Integer> skinColors = new ArrayList<Integer>();
		skinColors.add(EntityPeridot.SKIN_COLOR_BEGIN);
		skinColors.add(EntityPeridot.SKIN_COLOR_END);
		return Colors.arbiLerp(skinColors);
	}
	
	@Override
	protected int generateHairStyle() {
		return this.rand.nextInt(EntityPeridot.NUM_HAIRSTYLES);
	}
	
	@Override
	protected int generateHairColor() {
		ArrayList<Integer> hairColors = new ArrayList<Integer>();
		hairColors.add(EntityPeridot.HAIR_COLOR_BEGIN);
		hairColors.add(EntityPeridot.HAIR_COLOR_END);
		return Colors.arbiLerp(hairColors);
	}
}