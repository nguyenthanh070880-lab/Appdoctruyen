package com.novelapp.model;

public class CoinPackage {
    private int packageId;
    private String packageName;
    private int coinAmount;
    private long priceVnd;
    private int bonusCoin;
    private boolean isActive;

    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public int getCoinAmount() { return coinAmount; }
    public void setCoinAmount(int coinAmount) { this.coinAmount = coinAmount; }

    public long getPriceVnd() { return priceVnd; }
    public void setPriceVnd(long priceVnd) { this.priceVnd = priceVnd; }

    public int getBonusCoin() { return bonusCoin; }
    public void setBonusCoin(int bonusCoin) { this.bonusCoin = bonusCoin; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getTotalCoin() {
        return coinAmount + bonusCoin;
    }
}