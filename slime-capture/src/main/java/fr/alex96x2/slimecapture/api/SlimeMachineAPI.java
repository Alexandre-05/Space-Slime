package fr.alex96x2.slimecapture.api;

import fr.alex96x2.slimecapture.item.SlimeCaptureItems;
import org.bukkit.inventory.ItemStack;

/**
 * API publique pour les machines qui consomment ou reçoivent des slimes capturés.
 */
public final class SlimeMachineAPI {

    private SlimeMachineAPI() {
    }

    public static boolean isSlimeCanister(ItemStack item) {
        return SlimeCaptureItems.isSlimeCanister(item);
    }

    public static int getCanisterAmount(ItemStack item) {
        return SlimeCaptureItems.getCanisterAmount(item);
    }

    /**
     * Retire des slimes d'une capsule. Retourne la capsule mise à jour, ou null si entièrement vidée.
     */
    public static ItemStack consumeFromCanister(ItemStack canister, int amount) {
        return SlimeCaptureItems.consumeFromCanister(canister, amount);
    }

    public static boolean isSlimeGel(ItemStack item) {
        return SlimeCaptureItems.isSlimeGel(item);
    }
}
