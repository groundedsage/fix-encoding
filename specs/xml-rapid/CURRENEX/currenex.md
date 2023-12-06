# CURRENEX SPECIFIC FIX DICTIONARY

The following changes have been applied relative to “FX” dictionary:
    1. Currenex sends on Quote Subscription 7560=N
    2. Currenex sends on ExecutionReport 7585=1 or 2
    3. New Order Time in Force Seconds



    1. Currenex sends on Quote Subscription 7560=N
  	This Field is never sent in other API Implementations. Therefore the FIX Dictionaries
 	that define the validity (and interpretation) of messages received  must be amended
	to accept this field 7560. In the interpretation later on, this field is ignored; but
 	this does not mean that the validity-check of the FIX Dictionary can be skipped.
FIELDS.XML (added in the end)
<Fields>
<Tag>7560</Tag>
<FieldName>CurrenexQuoteSubscription</FieldName>
<Type>String</Type>
<Desc>CurrenexQuoteSubscription</Desc>
<LenRefers>0</LenRefers>
<AbbrName>CurrenexQuoteSubscription</AbbrName>
<OverrideXMLName></OverrideXMLName>
<NotReqXML>0</NotReqXML>
</Fields>
MsgContents.xml (added in the middle above the end of section MSG=29 (=quote subscription)
<MsgContents>
<Indent>0</Indent>
<Position>10</Position>
<TagText>7560</TagText>
<Reqd>0</Reqd> 		Not required. Universal does not send this field.
<Description>CurrenexQuoteSubscription
</Description>
<MsgID>29</MsgID>
</MsgContents>

    2. Currenex sends on ExecutionReport 7585=1 or 2
  	This Field is never sent in other API Implementations. Therefore the FIX Dictionaries
 	that define the validity (and interpretation) of messages received  must be amended
	to accept this field 7585. In the interpretation later on, this field is ignored; but
 	this does not mean that the validity-check of the FIX Dictionary can be skipped.

 	MatchingType. 
 	1 = aggressor
 	2 = aggressed. 

FIELDS.XML (added in the end)
<Fields>
<Tag>7585</Tag>
<FieldName>CurrenexMatchingType</FieldName>
<Type>String</Type>
<Desc> CurrenexMatchingType </Desc>
<LenRefers>0</LenRefers>
<AbbrName> CurrenexMatchingType </AbbrName>
<OverrideXMLName></OverrideXMLName>
<NotReqXML>0</NotReqXML>
</Fields>
MsgContents.xml (added in the middle above the end of section MsgID=9 (=execution report)
 	(Be careful ExecutionReport=”8” but in the xml it has the ID 9)
<MsgContents>
<Indent>0</Indent>
<Position>100</Position>
<TagText>7585</TagText>
<Reqd>0</Reqd>
<Description>
</Description>
<MsgID>8</MsgID>
</MsgContents>

Patch 3:  New Order Time In Force Seconds
Enum 59=TimeInForce Allow X=x Seconds in Force (Important for FixExecutionServer)
Added in ENUMS.xml:
<Enums>
<Tag>59</Tag>
<Enum>X</Enum>
<Description>Good For X Seconds</Description>
</Enums>
TimeInForceSeconds(7558) is sent with a new oder message
Added in FIELDS.xml
<Fields>
<Tag>7558</Tag>
<FieldName>CurrenexTimeInForceSeconds</FieldName>
<Type>String</Type>
<Desc> CurrenexTimeInForceSeconds </Desc>
<LenRefers>0</LenRefers>
<AbbrName> CurrenexTimeInForceSeconds </AbbrName>
<OverrideXMLName></OverrideXMLName>
<NotReqXML>0</NotReqXML>
</Fields>

Added/ Changed in MsgContents.xml 	Message Type D (ID: 14) 
<MsgContents>
<Indent>0</Indent>
<Position>76</Position>
<TagText>7558</TagText>
<Reqd>0</Reqd>
<Description>CurrenexTimeInForceSeconds 
</Description>
<MsgID>14</MsgID>
</MsgContents>
<MsgContents>
<Indent>0</Indent>
<Position>77</Position> 		position has changed
<TagText>StandardTrailer</TagText>
<Reqd>1</Reqd>
<Description>
</Description>
<MsgID>14</MsgID>
</MsgContents>