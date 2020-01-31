package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.tesselation.GeometryMasks;
import dev.fiki.forgehax.main.util.tesselation.GeometryTessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * Created on 9/4/2016 by fr1kin
 */
@RegisterMod
public class StorageESPMod extends ToggleMod {
  
  public StorageESPMod() {
    super(Category.RENDER, "StorageESP", false, "Shows storage");
  }
  
  private int getTileEntityColor(TileEntity tileEntity) {
    if (tileEntity instanceof ChestTileEntity
        || tileEntity instanceof DispenserTileEntity
        || tileEntity instanceof ShulkerBoxTileEntity) {
      return Colors.ORANGE.toBuffer();
    } else if (tileEntity instanceof EnderChestTileEntity) {
      return Colors.PURPLE.toBuffer();
    } else if (tileEntity instanceof FurnaceTileEntity) {
      return Colors.GRAY.toBuffer();
    } else if (tileEntity instanceof HopperTileEntity) {
      return Colors.DARK_RED.toBuffer();
    } else {
      return -1;
    }
  }
  
  private int getEntityColor(Entity entity) {
    if (entity instanceof ChestMinecartEntity) {
      return Colors.ORANGE.toBuffer();
    } /*else if (entity instanceof ItemFrameEntity
        && ((ItemFrameEntity) entity).getDisplayedItem().getItem() instanceof ShulkerBox) {
      return Colors.YELLOW.toBuffer();
      // TODO: 1.15 detect if item is a shulkerbox
    }*/ else {
      return -1;
    }
  }
  
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    
    for (TileEntity tileEntity : Globals.getWorld().loadedTileEntityList) {
      BlockPos pos = tileEntity.getPos();
      
      int color = getTileEntityColor(tileEntity);
      if (color != -1) {
        GeometryTessellator.drawCuboid(event.getBuffer(), pos, GeometryMasks.Line.ALL, color);
      }
    }
    
    for (Entity entity : Globals.getWorld().getAllEntities()) {
      BlockPos pos = entity.getPosition();
      int color = getEntityColor(entity);
      if (color != -1) {
        GeometryTessellator.drawCuboid(
            event.getBuffer(),
            entity instanceof ItemFrameEntity ? pos.add(0, -1, 0) : pos,
            GeometryMasks.Line.ALL,
            color);
      }
    }
    
    event.getTessellator().draw();
  }
}