package mod.akrivus.kagic.init;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Predicate;

import mod.akrivus.kagic.entity.EntityGem;
import mod.akrivus.kagic.entity.gem.EntityAgate;
import mod.akrivus.kagic.entity.gem.EntityRuby;
import mod.akrivus.kagic.entity.gem.EntityRutile;
import mod.akrivus.kagic.init.ModMetrics.Update;
import mod.akrivus.kagic.server.SpaceStuff;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ModEvents {
	public static void register() {
		MinecraftForge.EVENT_BUS.register(new ModEvents());
	}
	@SubscribeEvent
	public void onEntitySpawn(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) e.getEntity();
			if (e.getEntity().world.isRemote) {
				if (KAGIC.DEVELOPER) {
					player.sendMessage(new TextComponentString("You are playing KAGIC " + KAGIC.VERSION + " in DEVELOPER mode."));
					player.sendMessage(new TextComponentString("Note that some features may be removed!"));
				}
				else if (ModConfigs.notifyOnUpdates) {
					Update result = ModMetrics.checkForUpdates();
					if (result != null && !KAGIC.VERSION.equals(result.getNewVersion())) {
						player.sendMessage(ITextComponent.Serializer.jsonToComponent("[{\"text\":\"�cKAGIC v" + result.getNewVersion() + " is out for Minecraft " + KAGIC.MCVERSION + "�f\"}]"));
						player.sendMessage(ITextComponent.Serializer.jsonToComponent("[{\"text\":\"�e�nDownload�r�f\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" +  result.getDownloadLink() + "\"}}, {\"text\":\" | \"}, {\"text\":\"�3�nDiscord�r�f\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" +  result.getDiscordLink() + "\"}}]"));
					}
				}
				//player.addStat(ModAchievements.INSTALLED_KAGIC);
			}
		}
		if (e.getEntity() instanceof EntityMob) {
			EntityMob mob = (EntityMob) e.getEntity();
			if (!(e.getEntity() instanceof EntityEnderman || e.getEntity() instanceof EntityGolem)) {
				mob.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityGem>(mob, EntityGem.class, 1, true, true, new Predicate<EntityGem>() {
		            public boolean apply(EntityGem input) {
		                return !(input instanceof EntityAgate || (input.isDefective() && input instanceof EntityRutile));
		            }
		        }));
			}
			mob.tasks.addTask(1, new EntityAIAvoidEntity<EntityAgate>(mob, EntityAgate.class, 6.0F, 1.0D, 1.2D));
		}
		else if (e.getEntity() instanceof EntityGolem) {
			EntityGolem golem = (EntityGolem) e.getEntity();
			golem.targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityGem>(golem, EntityGem.class, 1, true, true, new Predicate<EntityGem>() {
	            public boolean apply(EntityGem input) {
	                return input.getServitude() > EntityGem.SERVE_HUMAN;
	            }
	        }));
		}
	}
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		MinecraftServer server = event.world.getMinecraftServer();
		if (ModConfigs.spawnMeteorRubies && server.getCurrentPlayerCount() > 0) {
			EntityPlayer player = server.getPlayerList().getPlayers().get(event.world.rand.nextInt(server.getCurrentPlayerCount()));
			if (!player.world.isRemote) {
				if (player.dimension == 0 && !player.world.isDaytime()) {
					if (player.world.getTotalWorldTime() - SpaceStuff.get().getLastRubyImpactTime() >= 24000 * ModConfigs.meteorRubyRate && player.world.rand.nextInt(12000) == 0) {
						EntityRuby ruby = new EntityRuby(player.world);
						double xdev = (player.world.rand.nextInt(128) - 64);
						double newX = player.posX + Math.abs(xdev) < 16 ? 128 : xdev;
						double zdev = (player.world.rand.nextInt(128) - 64);
						double newZ = player.posZ + Math.abs(zdev) < 16 ? 128 : zdev;
						ruby.setPosition(newX, 256, newZ);
						ruby.isSpaceBorn = true;
						player.world.spawnEntity(ruby);
						ruby.onInitialSpawn(ruby.world.getDifficultyForLocation(new BlockPos(ruby)), (IEntityLivingData)null);
						player.world.playSound(player, player.getPosition(), ModSounds.RUBY_EXPLODE, SoundCategory.NEUTRAL, 10.0F, 1.0F);
						SpaceStuff.get().setLastRubyImpactTime(player.world.getTotalWorldTime());
					}
				}
			}
		}
	}/* Access to Homeworld REMOVED in 1.9p1r10 - will revisit at a later date
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.player.posY > 480 && !event.player.world.isRemote) {
			event.player.world.getMinecraftServer().getPlayerList().transferPlayerToDimension((EntityPlayerMP) event.player, 2, new TeleporterHomeworld(event.player.world.getMinecraftServer().worldServerForDimension(event.player.dimension)));
		}
	}*/
	@SubscribeEvent
	public void onLootTableLoad(LootTableLoadEvent e) {
		if (e.getName().equals(LootTableList.CHESTS_ABANDONED_MINESHAFT) || e.getName().equals(LootTableList.CHESTS_VILLAGE_BLACKSMITH)) {
			e.getTable().addPool(new LootPool(new LootEntry[] {
					new LootEntryItem(ModItems.CRACKED_BISMUTH_GEM, 1, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic"),
					new LootEntryItem(ModItems.CRACKED_PERIDOT_GEM, 3, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic"),
					new LootEntryItem(ModItems.CRACKED_PEARL_GEM, 3, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic"),
					new LootEntryItem(ModItems.INACTIVE_GEM_BASE, 3, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(2))
					}, new LootCondition[0], "kagic"),
					new LootEntryItem(Item.getItemFromBlock(ModBlocks.INJECTOR), 3, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic"),
					new LootEntryItem(Item.getItemFromBlock(ModBlocks.GEM_DRILL), 3, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic"),
					new LootEntryItem(Item.getItemFromBlock(ModBlocks.INCUBATOR), 3, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic")
			}, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(2), "kagic"));
		}
		else if (e.getName().equals(LootTableList.CHESTS_DESERT_PYRAMID) || e.getName().equals(LootTableList.CHESTS_END_CITY_TREASURE) || e.getName().equals(LootTableList.CHESTS_JUNGLE_TEMPLE) || e.getName().equals(LootTableList.CHESTS_NETHER_BRIDGE)) {
			e.getTable().addPool(new LootPool(new LootEntry[] {
					new LootEntryItem(ModItems.CRACKED_PERIDOT_GEM, 4, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic"),
					new LootEntryItem(ModItems.CRACKED_AMETHYST_GEM, 2, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic"),
					new LootEntryItem(ModItems.CRACKED_JASPER_GEM, 2, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic"),
					new LootEntryItem(ModItems.CRACKED_CARNELIAN_GEM, 2, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic"),
			}, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(2), "kagic"));
		}
		else if (e.getName().equals(LootTableList.CHESTS_SIMPLE_DUNGEON) || e.getName().equals(LootTableList.CHESTS_STRONGHOLD_CORRIDOR)) {
			e.getTable().addPool(new LootPool(new LootEntry[] {
					new LootEntryItem(ModItems.CRACKED_RUBY_GEM, 2, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic"),
					new LootEntryItem(ModItems.CRACKED_PEARL_GEM, 2, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(1))
					}, new LootCondition[0], "kagic"),
					new LootEntryItem(ModItems.INACTIVE_GEM_BASE, 5, 1, new LootFunction[] {
							new SetCount(new LootCondition[0], new RandomValueRange(4))
					}, new LootCondition[0], "kagic")
			}, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(2), "kagic"));
		}
	}
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
		ModConfigs.syncConfiguration();
	}
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load e) {
		if (!e.getWorld().isRemote) {
			try {
				KAGIC.spaceStuff = new SpaceStuff(e.getWorld());
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		ModMetrics.sendMetrics();
	}
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save e) {
		if (!e.getWorld().isRemote) {
			KAGIC.spaceStuff.save();
		}
	}
	@SubscribeEvent
	public void onServerChat(ServerChatEvent e) {
		List<EntityGem> list = e.getPlayer().world.<EntityGem>getEntitiesWithinAABB(EntityGem.class, e.getPlayer().getEntityBoundingBox().grow(48.0D, 16.0D, 48.0D));
		for (EntityGem gem : list) {
        	boolean obeyed = gem.onSpokenTo(e.getPlayer(), e.getMessage());
            if (obeyed) {
            	gem.playObeySound();
            	e.setCanceled(true);
            }
        }
	}
}
