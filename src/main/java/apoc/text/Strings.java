package apoc.text;

import apoc.Description;
import apoc.result.LongResult;
import apoc.result.StringResult;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.Soundex;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.apache.commons.codec.language.Soundex.US_ENGLISH;

public class Strings {

    @Procedure
    @Description("apoc.text.phonetic(value) yield value - Compute the US_ENGLISH phonetic soundex encoding of all words of the text value which can be a single string or a list of strings")
    public Stream<StringResult> phonetic(final @Name("value") Object value) {
        Stream<Object> stream = value instanceof Iterable ? StreamSupport.stream(((Iterable) value).spliterator(), false) : Stream.of(value);

        return stream.map(str -> str == null ? StringResult.EMPTY :
                new StringResult(Stream.of(str.toString().split("\\W+"))
                .map(US_ENGLISH::soundex).reduce("", (a, s)->a+s)));
    }

    @Procedure
    @Description("apoc.text.phoneticDelta(text1, text2) yield phonetic1, phonetic2, delta - Compute the US_ENGLISH soundex character difference between two given strings")
    public Stream<PhoneticResult> phoneticDelta(final @Name("text1") String text1, final @Name("text2") String text2) {
        try {
            return Stream.of(new PhoneticResult(US_ENGLISH.soundex(text1),US_ENGLISH.soundex(text2),US_ENGLISH.difference(text1,text2)));
        } catch (EncoderException e) {
            throw new RuntimeException("Error encoding text "+text1+" or "+text2+" for delta measure",e);
        }
    }

    public static class PhoneticResult {
        public final String phonetic1, phonetic2;
        public final long delta;

        public PhoneticResult(String phonetic1, String phonetic2, Number delta) {
            this.phonetic1 = phonetic1;
            this.phonetic2 = phonetic2;
            this.delta = delta.longValue();
        }
    }
}