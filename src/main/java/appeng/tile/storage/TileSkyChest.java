/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.tile.storage;


import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;

import appeng.tile.AEBaseInvTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;


public class TileSkyChest extends AEBaseInvTile
{

	private final int[] sides = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35 };
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 9 * 4 );
	// server
	private int numPlayersUsing;
	// client..
	private long lastEvent;
	private float lidAngle;
	private float prevLidAngle;

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileSkyChest( final ByteBuf data )
	{
		data.writeBoolean( this.getPlayerOpen() > 0 );
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileSkyChest( final ByteBuf data )
	{
		final int wasOpen = this.getPlayerOpen();
		this.setPlayerOpen( data.readBoolean() ? 1 : 0 );

		if( wasOpen != this.getPlayerOpen() )
		{
			this.setLastEvent( System.currentTimeMillis() );
		}

		return false; // TESR yo!
	}

	@Override
	public boolean requiresTESR()
	{
		return true;
	}

	@Override
	public boolean canRenderBreaking()
	{
		return true;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public void openInventory( final EntityPlayer player )
	{
		if( !player.isSpectator() )
		{
			this.setPlayerOpen( this.getPlayerOpen() + 1 );
			this.worldObj.addBlockEvent( this.pos, this.getBlockType(), 1, this.numPlayersUsing );
			this.worldObj.notifyNeighborsOfStateChange( this.pos, this.getBlockType() );
			this.worldObj.notifyNeighborsOfStateChange( this.pos.down(), this.getBlockType() );

			if( this.getPlayerOpen() == 1 )
			{
				this.getWorld().playSound( player, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.getWorld().rand.nextFloat() * 0.1F + 0.9F );
				this.markForUpdate();
			}
		}
	}

	@Override
	public void closeInventory( final EntityPlayer player )
	{
		if( !player.isSpectator() )
		{
			this.setPlayerOpen( this.getPlayerOpen() - 1 );
			this.worldObj.addBlockEvent( this.pos, this.getBlockType(), 1, this.numPlayersUsing );
			this.worldObj.notifyNeighborsOfStateChange( this.pos, this.getBlockType() );
			this.worldObj.notifyNeighborsOfStateChange( this.pos.down(), this.getBlockType() );

			if( this.getPlayerOpen() < 0 )
			{
				this.setPlayerOpen( 0 );
			}

			if( this.getPlayerOpen() == 0 )
			{
				this.getWorld().playSound( player, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.getWorld().rand.nextFloat() * 0.1F + 0.9F );
				this.markForUpdate();
			}
		}
	}

	@TileEvent( TileEventType.TICK )
	public void tick()
	{
		int i = this.pos.getX();
		int j = this.pos.getY();
		int k = this.pos.getZ();

		this.prevLidAngle = this.lidAngle;
		float f1 = 0.1F;

		if( this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F )
		{
			float f2 = this.lidAngle;

			if( this.numPlayersUsing > 0 )
			{
				this.lidAngle += 0.1F;
			}
			else
			{
				this.lidAngle -= 0.1F;
			}

			if( this.lidAngle > 1.0F )
			{
				this.lidAngle = 1.0F;
			}

			float f3 = 0.5F;

			if( this.lidAngle < 0.0F )
			{
				this.lidAngle = 0.0F;
			}
		}
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{

	}

	@Override
	public int[] getAccessibleSlotsBySide( final EnumFacing side )
	{
		return this.sides;
	}

	public float getLidAngle()
	{
		// System.out.println( lidAngle );
		return this.lidAngle;
	}

	public void setLidAngle( final float lidAngle )
	{
		this.lidAngle = lidAngle;
	}

	public float getPrevLidAngle()
	{
		return prevLidAngle;
	}

	public void setPrevLidAngle( float prevLidAngle )
	{
		this.prevLidAngle = prevLidAngle;
	}

	public int getPlayerOpen()
	{
		return this.numPlayersUsing;
	}

	private void setPlayerOpen( final int playerOpen )
	{
		this.numPlayersUsing = playerOpen;
	}

	public long getLastEvent()
	{
		return this.lastEvent;
	}

	private void setLastEvent( final long lastEvent )
	{
		this.lastEvent = lastEvent;
	}
}
