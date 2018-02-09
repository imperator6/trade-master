package com.rwe.cpd.kafka

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer

class MapDeserializer implements Deserializer<Map<String,String>> {


    protected final ObjectMapper objectMapper;


    private volatile ObjectReader reader;

    public MapDeserializer() {
        this.objectMapper = new ObjectMapper()
        this.objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    /**
     * Configure this class.
     * @param configs configs in key/value pairs
     * @param isKey whether is for key or value
     */
    void configure(Map<String, ?> configs, boolean isKey) {
        // noop
    }

    /**
     * Deserialize a record value from a byte array into a value or object.
     * @param topic topic associated with the data
     * @param data serialized bytes; may be null; implementations are recommended to handle null by returning a value or null rather than throwing an exception.
     * @return deserialized typed data; may be null
     */
    Map<String,String> deserialize(String topic, byte[] data) {
        if (this.reader == null) {
            this.reader = this.objectMapper.readerFor(new TypeReference<Map<String,String>>(){});
        }
        try {
            Map<String,String> result = null;
            if (data != null) {
                result = this.reader.readValue(data);
            }
            return result;
        }
        catch (IOException e) {
            throw new SerializationException("Can't deserialize data [" + Arrays.toString(data) +
                    "] from topic [" + topic + "]", e);
        }
    }

    @Override
    void close() {
        // noop
    }


}
