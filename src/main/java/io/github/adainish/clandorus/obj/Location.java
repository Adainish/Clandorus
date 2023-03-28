package io.github.adainish.clandorus.obj;

import io.github.adainish.clandorus.util.WorldUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Location {
    private String worldID = "world";
    private double posX = 0.0;
    private double posY = 100.0;
    private double posZ = 0.0;

    public Location()
    {

    }

    public BlockPos returnBlockPos()
    {
        return new BlockPos(getPosX(), getPosY(), getPosZ());
    }

    public World getWorld()
    {
        if (WorldUtil.getWorld(getWorldID()).isPresent())
            return WorldUtil.getWorld(getWorldID()).get();
        return WorldUtil.getBasicWorld();
    }

    public String getWorldID() {
        return worldID;
    }

    public void setWorldID(String worldID) {
        this.worldID = worldID;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }
}
