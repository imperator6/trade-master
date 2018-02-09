package com.rwe.cpd.kafka.model;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;

public class KafkaStatus {
	/* {System=EEX, MsgTime=2017-10-17T12:30:12Z, Action=0, Info=Heartbeat timed out since Tue Oct 17 14:27:59 CEST 2017, SeqId=5377, ParId=1} */

	public String System; /* System=EEX */

	public Date MsgTime; /* MsgTime=2017-10-17T12:30:12Z */

	public int Action; /* Action=0 */

	public String Info; /* Info=Heartbeat timed out since Tue Oct 17 14:27:59 CEST 2017 */

	public long SeqId; /* SeqId=5377 */

	public long ParId; /* ParId=1 */

	public HashMap toMap() {
		HashMap<String, String> dealMap = new HashMap<>();
		for (Field field : KafkaStatus.class.getDeclaredFields()) {
			// Skip this if you intend to access to public fields only
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			try {
				Object value = field.get(this);
				String sValue = value != null ? value.toString() : null;
				dealMap.put(field.getName(), sValue);
			} catch(Exception e) {}
		}
		return dealMap;
	}
}
