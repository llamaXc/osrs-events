package com.osrsevents.pojos;

import lombok.Getter;
import lombok.Setter;

public class BankItem {

    public BankItem(int pos, int quantity, int id ){
        setPosition(pos);
        setQuantity(quantity);
        setId(id);
    }

    @Getter
    @Setter
    private int position;

    @Getter
    @Setter
    private int quantity;

    @Getter
    @Setter
    private  int id;

}
