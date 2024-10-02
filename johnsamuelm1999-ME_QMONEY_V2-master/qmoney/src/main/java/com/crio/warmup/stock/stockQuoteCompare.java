package com.crio.warmup.stock;

import java.util.Comparator;
import com.crio.warmup.stock.dto.Candle;

public class stockQuoteCompare implements Comparator<Candle> {

    @Override
    public int compare(Candle candle1, Candle candle2) {
        // TODO Auto-generated method stub
        if(candle1.getDate().isBefore(candle2.getDate())){
            return -1;
        }else if(candle1.getDate().isAfter(candle2.getDate())){
            return 1;
        }
        return 0;
    }
    
}
