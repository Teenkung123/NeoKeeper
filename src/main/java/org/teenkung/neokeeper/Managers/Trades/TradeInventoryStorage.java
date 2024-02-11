package org.teenkung.neokeeper.Managers.Trades;

public class TradeInventoryStorage {
    private String id;
    private Integer offset;
    private Integer selecting;

    public TradeInventoryStorage(String id, Integer offset) {
        this.id = id;
        this.offset = offset;
        this.selecting = 0;
    }

    // Assuming you have getters here

    public void selecting(Integer index) { this.selecting = index; }
    public Integer selecting() { return this.selecting; }
    public void offset(Integer offset) { this.offset = offset; }
    public Integer offset() { return offset; }
    public String id() { return id; };

}
