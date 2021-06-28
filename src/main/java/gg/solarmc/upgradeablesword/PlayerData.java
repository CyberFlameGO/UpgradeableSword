package gg.solarmc.upgradeablesword;

import java.util.UUID;

public record PlayerData(int hits, UUID lastDamagedPlayer) {

    public PlayerData withHits(int hits) {
        return new PlayerData(hits, lastDamagedPlayer);
    }

    public PlayerData withLastDamagedPlayer(UUID lastDamagedPlayer){
        return new PlayerData(hits, lastDamagedPlayer);
    }

}
