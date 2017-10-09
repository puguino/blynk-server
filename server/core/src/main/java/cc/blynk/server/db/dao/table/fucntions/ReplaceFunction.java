package cc.blynk.server.db.dao.table.fucntions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.function.Function;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 10.10.17.
 */
public class ReplaceFunction implements Function<String, String> {

    public final String replaceFrom;
    public final String replaceTo;

    @JsonCreator
    public ReplaceFunction(@JsonProperty("replaceFrom") String replaceFrom,
                           @JsonProperty("replaceTo") String replaceTo) {
        this.replaceFrom = replaceFrom;
        this.replaceTo = replaceTo;
    }

    public ReplaceFunction(String replaceFrom) {
        this(replaceFrom, "");
    }

    @Override
    public String apply(String s) {
        return s.replace(replaceFrom, replaceTo);
    }
}
