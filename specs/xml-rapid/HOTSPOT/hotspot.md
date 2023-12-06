# HOTSPOT: Fix to the XML FILES.

HOTSPOT SPECIFIC FIX DICTIONARY:
The following changes have been applied relative to “FX” dictionary:
    1. OrderStatus(150) new tag “F” as trade added.
    2. UTC TIME IN ORDER QUOTE UPDATE MESSAGES
    3. 

    1. FIX to Field 150
Enum 150 (OrderStatus) can also contain the tag “F” = TRADE.
Added to ENUMS.xml:

<Enums>
<Tag>150</Tag>
<Enum>F</Enum>
<Description>TRADE(Hotspot)</Description>
</Enums>
