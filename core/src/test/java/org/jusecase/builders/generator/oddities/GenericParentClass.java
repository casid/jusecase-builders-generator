package org.jusecase.builders.generator.oddities;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class GenericParentClass<Key extends Comparable<Key>, Item> {
    public void setKey(Key key) {
    }

    public void setItem(Item item) {
    }

    public void setItemList(List<Item> itemList) {
    }

    public void setItemMap(Map<? extends Key, ? super Item> itemMap) {
    }

    public void setItemArray(Item[] itemArray) {
    }
}
