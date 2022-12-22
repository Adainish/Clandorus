package io.github.adainish.clandorus.obj.clan.data;

public class Bank {
    public int balance = 0;

    public Bank(){}

    public void takeFromAccount(int amount)
    {
        this.balance -= amount;
    }

    public void addToAccount(int amount)
    {
        this.balance += amount;
    }

    public void setBalance(int amount)
    {
        this.balance = amount;
    }

    public boolean canAfford(int amount)
    {
        return balance - amount >= 0;
    }
}
