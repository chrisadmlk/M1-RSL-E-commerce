package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Catalog implements Serializable {
    private List<ItemStore> items;

    public Catalog() {
        this.items = new ArrayList<>();
    }

    public List<ItemStore> getItems() {
        return items;
    }

    public void setItems(List<ItemStore> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return " --- \nCatalog : \n" + items + " --- \n";
    }
}
