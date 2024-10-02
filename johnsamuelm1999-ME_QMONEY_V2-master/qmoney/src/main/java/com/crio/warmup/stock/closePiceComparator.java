package com.crio.warmup.stock;

import java.util.Comparator;
import com.crio.warmup.stock.dto.TotalReturnsDto;

public class closePiceComparator implements Comparator<TotalReturnsDto>{

    @Override
    public int compare(TotalReturnsDto arg0, TotalReturnsDto arg1) {
        // TODO Auto-generated method stub
        if(arg0.getClosingPrice()>arg1.getClosingPrice()){
            return 1;
        }else if (arg0.getClosingPrice()<arg1.getClosingPrice()){
            return -1;
        }
        return 0;
    }
    
}
