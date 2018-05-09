package red.man10.mappstore;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.util.Vector;


public class BoundingBox {


    //min and max points of hit box
    public Vector max;
    public Vector min;

    BoundingBox(Vector min, Vector max) {
        this.max = max;
        this.min = min;
    }

    //gets min and max point of block
    //  ** 1.8 and earlier **
///    BoundingBox(Block block) {
 //       IBlockData blockData = ((CraftWorld) block.getWorld()).getHandle().getType(new BlockPosition(block.getX(), block.getY(), block.getZ()));
 //       net.minecraft.server.v1_8_R3.Block blockNative = blockData.getBlock();
//        blockNative.updateShape(((CraftWorld) block.getWorld()).getHandle(), new BlockPosition(block.getX(), block.getY(), block.getZ()));
//        min = new Vector((double) block.getX() + blockNative.B(), (double) block.getY() + blockNative.D(), (double) block.getZ() + blockNative.F());
//        max = new Vector((double) block.getX() + blockNative.C(), (double) block.getY() + blockNative.E(), (double) block.getZ() + blockNative.G());
//    }

    //gets min and max point of block
    //  ** 1.10 **
    BoundingBox(Block block) {

        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        AxisAlignedBB box = world.getType(pos).d(world, pos);

        min = new Vector(pos.getX() + box.a, pos.getY() + box.b, pos.getZ() + box.c);
        max = new Vector(pos.getX() + box.d, pos.getY() + box.e, pos.getZ() + box.f);

    }

    //gets min and max point of entity
    // only certain nms versions ****
       BoundingBox(Entity entity){
      //  AxisAlignedBB bb = ((CraftEntity) entity).getHandle().getBoundingBox();
      //  min = new Vector(bb.a,bb.b,bb.c);
      //  max = new Vector(bb.d,bb.e,bb.f);
    }


    BoundingBox (AxisAlignedBB bb){
        min = new Vector(bb.a,bb.b,bb.c);
        max = new Vector(bb.d,bb.e,bb.f);
    }

    public Vector midPoint(){
        return max.clone().add(min).multiply(0.5);
    }

}

