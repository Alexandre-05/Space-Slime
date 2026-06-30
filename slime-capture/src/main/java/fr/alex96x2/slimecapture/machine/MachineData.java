package fr.alex96x2.slimecapture.machine;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public final class MachineData {

    private final Location location;
    private ItemStack slimeSlot;
    private ItemStack fuelSlot;
    private ItemStack foodSlot;
    private int virtualFuel;
    private int virtualFood;
    private int storedGel;
    private int progress;

    public MachineData(Location location) {
        this.location = location;
    }

    public Location location() {
        return location;
    }

    public ItemStack slimeSlot() {
        return slimeSlot;
    }

    public void slimeSlot(ItemStack slimeSlot) {
        this.slimeSlot = slimeSlot;
    }

    public ItemStack fuelSlot() {
        return fuelSlot;
    }

    public void fuelSlot(ItemStack fuelSlot) {
        this.fuelSlot = fuelSlot;
    }

    public ItemStack foodSlot() {
        return foodSlot;
    }

    public void foodSlot(ItemStack foodSlot) {
        this.foodSlot = foodSlot;
    }

    public int virtualFuel() {
        return virtualFuel;
    }

    public void virtualFuel(int virtualFuel) {
        this.virtualFuel = Math.max(0, virtualFuel);
    }

    public int virtualFood() {
        return virtualFood;
    }

    public void virtualFood(int virtualFood) {
        this.virtualFood = Math.max(0, virtualFood);
    }

    public int storedGel() {
        return storedGel;
    }

    public void storedGel(int storedGel) {
        this.storedGel = Math.max(0, storedGel);
    }

    public int progress() {
        return progress;
    }

    public void progress(int progress) {
        this.progress = Math.max(0, progress);
    }
}
