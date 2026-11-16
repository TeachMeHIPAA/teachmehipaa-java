package com.teachmehipaa.core;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** The library's own mapper, so a caller's global Jackson settings never change parsing. */
final class Json {
  static final ObjectMapper MAPPER =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          // Parse errors must not quote the body: it can carry Learner names and emails.
          .disable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
          // Otherwise a JSON key named after a getter, such as "extraFields", would be written
          // through that getter instead of landing in the extra fields.
          .disable(MapperFeature.USE_GETTERS_AS_SETTERS)
          .build();

  private Json() {}
}
