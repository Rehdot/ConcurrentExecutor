package redot.executor.model;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter @Setter
public class CustomClass {

    private String code;
    private boolean compiled = false;

    public CustomClass(String code) {
        this.code = code;
    }

    @Nullable
    public String getClassName() {
        Pattern pattern = Pattern.compile("\\b(class|enum|interface|record)\\s+(\\w+)");
        Matcher matcher = pattern.matcher(this.code);

        if (matcher.find()) {
            return matcher.group(2);
        }

        return null;
    }

}
