/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.util.inventory.IDropInventory;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class DropInventoryLootEntry extends LootPoolSingletonContainer
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "drop_inv");

	protected DropInventoryLootEntry(int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn)
	{
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	@Override
	protected void createItemStack(@Nonnull Consumer<ItemStack> output, LootContext context)
	{
		if(context.hasParam(LootContextParams.BLOCK_ENTITY))
		{
			BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
			if(te instanceof IGeneralMultiblock dummyBE)
			{
				BlockEntity masterTE = (BlockEntity)dummyBE.master();
				boolean switchToMaster = true;
				if(masterTE instanceof MultiblockPartBlockEntity<?> multiblockPart&&masterTE.getLevel()!=null)
					switchToMaster = multiblockPart.onlyLocalDissassembly!=masterTE.getLevel().getGameTime();
				if(switchToMaster)
					te = masterTE;
			}
			if(te instanceof IDropInventory ieInvBE&&ieInvBE.getDroppedItems()!=null)
				ieInvBE.getDroppedItems().forEach(output);
			else if(te!=null)
			{
				IItemHandler itemHandler = CapabilityUtils.getCapability(te, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
				if(itemHandler instanceof IEInventoryHandler ieHandler)
				{
					for(int i = 0; i < ieHandler.getSlots(); i++)
						if(!ieHandler.getStackInSlot(i).isEmpty())
						{
							output.accept(ieHandler.getStackInSlot(i));
							ieHandler.setStackInSlot(i, ItemStack.EMPTY);
						}
				}
			}
		}
	}

	public static LootPoolSingletonContainer.Builder<?> builder()
	{
		return simpleBuilder(DropInventoryLootEntry::new);
	}

	@Nonnull
	@Override
	public LootPoolEntryType getType()
	{
		return IELootFunctions.dropInventory;
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<DropInventoryLootEntry>
	{
		@Nonnull
		@Override
		protected DropInventoryLootEntry deserialize(
				@Nonnull JsonObject json,
				@Nonnull JsonDeserializationContext context,
				int weight,
				int quality,
				@Nonnull LootItemCondition[] conditions,
				@Nonnull LootItemFunction[] functions
		)
		{
			return new DropInventoryLootEntry(weight, quality, conditions, functions);
		}
	}

}
