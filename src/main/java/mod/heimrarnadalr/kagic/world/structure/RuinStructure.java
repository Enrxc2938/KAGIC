package mod.heimrarnadalr.kagic.world.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import mod.akrivus.kagic.init.KAGIC;
import mod.heimrarnadalr.kagic.worlddata.ChunkLocation;
import mod.heimrarnadalr.kagic.worlddata.WorldDataRuins;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.WorldGenerator;

public class RuinStructure extends WorldGenerator {
	private final String type;
	private final int foundationDepth;
	private final boolean keepTerrain;
	private final boolean randomRotation;
	
	protected List<String> structures = new ArrayList<String>();
	protected int width;  //x
	protected int height; //y
	protected int length; //z
	protected final IBlockState foundationBlock;
	protected Set<Biome> allowedBiomes = new HashSet<Biome>();
	protected Set<IBlockState> allowedBlocks = new HashSet<IBlockState>();
	protected Map<BlockPos, ResourceLocation> chestTables = new HashMap<BlockPos, ResourceLocation>();
	
	public RuinStructure(String type, int foundationDepth, IBlockState foundation, boolean keepTerrain, boolean randomRotation) {
		this.type = type;
		this.foundationDepth = foundationDepth;
		this.foundationBlock = foundation;
		this.keepTerrain = keepTerrain;
		this.randomRotation = randomRotation;
	}
	
	public String getRandomVariant(Random rand) {
		if (structures.size() == 0) {
			throw new IllegalStateException("No structures defined for " + this.type + "!");
		}
		return structures.get(rand.nextInt(structures.size()));
	}
	
	Set<ChunkLocation> getAffectedChunks(World world, BlockPos pos, byte rotation) {
		Set<ChunkLocation> chunks = new HashSet<ChunkLocation>();
		ChunkLocation nearCorner = new ChunkLocation(pos.getX() >> 4, pos.getZ() >> 4);
		BlockPos far = rotation % 2 == 0 ? pos.add(this.width, 0, this.length) : pos.add(this.length, 0, this.width);
		ChunkLocation farCorner = new ChunkLocation(far.getX() >> 4, far.getZ() >> 4);
		
		for (int x = nearCorner.getX(); x <= farCorner.getX(); ++x) {
			for (int z = nearCorner.getZ(); z <= farCorner.getZ(); ++z) {
				chunks.add(new ChunkLocation(x, z));
			}
		}
		
		return chunks;
	}
	
	protected boolean checkBiome(World world, BlockPos pos) {
		//If we haven't defined any biomes, we can generate anywhere
		if (allowedBiomes.isEmpty()) {
			return true;
		}
		if (allowedBiomes.contains(world.getBiome(pos))) {
			return true;
		}
		return false;
	}
	
	protected boolean checkCorners(World world, BlockPos pos, byte rotation) {
		int xFar = pos.getX() + ((rotation % 2 == 0) ? this.width : this.length) - 1;
		int zFar = pos.getZ() + ((rotation % 2 == 0) ? this.length : this.width) - 1;
		BlockPos corner1 = world.getTopSolidOrLiquidBlock(pos).down();
		BlockPos corner2 = world.getTopSolidOrLiquidBlock(new BlockPos(xFar, 255, pos.getZ())).down();
		BlockPos corner3 = world.getTopSolidOrLiquidBlock(new BlockPos(pos.getX(), 255, zFar)).down();
		BlockPos corner4 = world.getTopSolidOrLiquidBlock(new BlockPos(xFar, 255, zFar)).down();
		
		if (!allowedBlocks.contains(world.getBlockState(corner1))) {
			return false;
		}
		if (!allowedBlocks.contains(world.getBlockState(corner2)) || (Math.abs(corner2.getY() - corner1.getY())) > this.foundationDepth) {
			return false;
		}
		if (!allowedBlocks.contains(world.getBlockState(corner3)) || (Math.abs(corner3.getY() - corner1.getY())) > this.foundationDepth) {
			return false;
		}
		if (!allowedBlocks.contains(world.getBlockState(corner4)) || (Math.abs(corner4.getY() - corner1.getY())) > this.foundationDepth) {
			return false;
		}
		return true;
	}
	
	protected boolean checkHeight(World world, BlockPos pos) {
		return pos.getY() + this.height < world.getActualHeight();
	}
	
	protected boolean checkChunks(World world, Set<ChunkLocation> chunks) {
		WorldDataRuins existingRuins = WorldDataRuins.get(world);
		for (ChunkLocation chunk : chunks) {
			if (existingRuins.chunkHasRuin(chunk)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		StructureData ruin = Schematic.loadSchematic(this.getRandomVariant(rand));
		this.width = ruin.getWidth();
		this.height = ruin.getHeight();
		this.length = ruin.getLength();
		
		byte rotation = (byte) (this.randomRotation ? rand.nextInt(4) : 0);

		if (!checkBiome(world, pos)) {
			//KAGIC.instance.chatInfoMessage("Biome check failed");
			return false;
		}
		if (!checkCorners(world, pos, rotation)) {
			//KAGIC.instance.chatInfoMessage("Corner check failed");
			return false;
		}

		if (!checkHeight(world, pos)) {
			return false;
		}
		
		Set<ChunkLocation> affectedChunks = this.getAffectedChunks(world, pos, rotation);
		if (!checkChunks(world, affectedChunks)) {
			//KAGIC.instance.chatInfoMessage("Existing ruin check failed");
			return false;
		}
		
		KAGIC.instance.chatInfoMessage("Generating " + this.type);
		this.markChunks(world, affectedChunks);
		this.generateFoundation(world, pos, rotation);
		
		Schematic.GenerateStructureAtPoint(ruin, world, pos, this.keepTerrain, rotation);
		
		if (!ruin.chests.isEmpty()) {
			for (Map.Entry<BlockPos, ResourceLocation> entry : this.chestTables.entrySet()) {
				BlockPos chestPos = Schematic.getRotatedPos(entry.getKey(), this.width, this.length, rotation);
				TileEntityChest chest = (TileEntityChest) world.getTileEntity(pos.add(chestPos));
				if (chest != null) {
					chest.setLootTable(entry.getValue(), rand.nextLong());
				} else {
					KAGIC.instance.chatInfoMessage("ERROR: could not find chest at position " + chestPos);
				}
			}
		}
		return true;
	}
	
	protected void generateFoundation(World world, BlockPos pos, byte rotation) {
		if (this.foundationDepth == 0) {
			return;
		}
		
		int width = rotation % 2 == 0 ? this.width : this.length;
		int length = rotation % 2 == 0 ? this.length : this.width;
		
		for (int x = 0; x < width; ++x) {
			for (int z = 0; z < length; ++z) {
				for (int y = 1; y <= this.foundationDepth; ++y) {
					world.setBlockState(pos.add(x, -y, z), this.foundationBlock);
				}
			}
		}
	}
	
	protected void markChunks(World world, Set<ChunkLocation> chunks) {
		WorldDataRuins existingRuins = WorldDataRuins.get(world);
		for (ChunkLocation chunk : chunks) {
			existingRuins.setChunk(chunk, this.type);
		}
	}
}