package org.jusecase.builders.generator.entities;

@SuppressWarnings("unused")
public abstract class Card {
    private long id;
    private String sinceVersion;
    private String name;
    private int rarity;
    private boolean blackMarketOffer;
    private boolean forgeable;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSinceVersion(String sinceVersion) {
        this.sinceVersion = sinceVersion;
    }

    public String getSinceVersion() {
        return sinceVersion;
    }

    public abstract int getType();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public boolean isBlackMarketOffer() {
        return blackMarketOffer;
    }

    public void setBlackMarketOffer(boolean blackMarketOffer) {
        this.blackMarketOffer = blackMarketOffer;
    }

    public void setForgeable(boolean forgeable) {
        this.forgeable = forgeable;
    }

    public boolean isForgeable() {
        return forgeable;
    }

    public int getTradeValue() {
        switch (rarity) {
            case 1:
                return 2;
            case 2:
                return 4;
            case 3:
                return 8;
            case 4:
                return 16;
            case 5:
                return 40;
        }
        return 0;
    }

    public int getPrice() {
        return 100;
    }
}
