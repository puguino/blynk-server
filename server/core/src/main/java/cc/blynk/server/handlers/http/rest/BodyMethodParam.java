package cc.blynk.server.handlers.http.rest;

import cc.blynk.utils.JsonParser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.MediaType;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 09.12.15.
 */
public class BodyMethodParam extends MethodParam {

    private static final Logger log = LogManager.getLogger(BodyMethodParam.class);

    private String expectedContentType;

    public BodyMethodParam(String name, Class<?> type, String expectedContentType) {
        super(name, type);
        this.expectedContentType = expectedContentType;
    }

    @Override
    Object get(URIDecoder uriDecoder) {
        if (uriDecoder.contentType == null || !uriDecoder.contentType.contains(expectedContentType)) {
            throw new RuntimeException("Unexpected content type. Expecting " + expectedContentType + ".");
        }
        if (expectedContentType.equals(MediaType.APPLICATION_JSON)) {
            String data = "";
            try {
                data = uriDecoder.bodyData.toString(CharsetUtil.UTF_8);
                return JsonParser.mapper.readValue(data, type);
            } catch (JsonParseException | JsonMappingException jsonParseError) {
                log.error("Error parsing body param. {}", data);
                throw new RuntimeException("Error parsing body param. " + data);
            } catch (Exception e) {
                log.error("Unexpected error during parsing body param.", e);
                throw new RuntimeException("Unexpected error during parsing body param.", e);
            }
        } else {
            return uriDecoder.bodyData.toString(CharsetUtil.UTF_8);
        }
    }

}
