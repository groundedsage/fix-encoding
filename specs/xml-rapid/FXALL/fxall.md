# fxall changes

Component 2029 (MKtDataIncMD)
Added 537. And changed the o4rder to move 537 after field 271 
Added 64 (SettlDate) as last field in MktDataIncMD
Field 290
Changed to String. This is not a position within the orderbook, but a long representation of a datetime. Correct would have been as “Long”; but this seems not to be supported by RapidAddition.
