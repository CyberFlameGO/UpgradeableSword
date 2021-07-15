package gg.solarmc.upgradeablesword;

import java.util.UUID;

public record PlayerData(int hits, UUID lastDamagedPlayer, int lastDamagedPlayerHits) {

    public PlayerData withHits(int hits) {
        return new PlayerData(hits, lastDamagedPlayer, lastDamagedPlayerHits);
    }

    public PlayerData withLastDamagedPlayer(UUID lastDamagedPlayer) {
        return new PlayerData(hits, lastDamagedPlayer, lastDamagedPlayerHits);
    }

    public PlayerData withLastDamagedPlayerHits(int playerHits) {
        return new PlayerData(hits, lastDamagedPlayer, playerHits);
    }

}
