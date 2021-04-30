package store;

import utils.jdbc.beans.Item;

import java.io.Serializable;
import java.util.List;

public class Catalog implements Serializable {
    private List<Item> list;

    public Catalog(List<Item> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "Catalog{" +
                "list=" + list +
                '}';
    }
}
