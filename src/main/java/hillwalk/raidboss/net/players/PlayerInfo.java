package hillwalk.raidboss.net.players;


import java.util.UUID;


public class PlayerInfo {
    private UUID playerUUID;
    private double damageDealt;


    public PlayerInfo(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.damageDealt = 0;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public double getDamageDealt() {
        return damageDealt;
    }

    public void addDamageDealt(double damage) {
        this.damageDealt += damage;
    }
}
