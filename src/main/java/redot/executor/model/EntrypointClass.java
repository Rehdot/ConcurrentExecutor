package redot.executor.model;

import lombok.Getter;
import lombok.Setter;

public class EntrypointClass {

    @Getter
    private static final EntrypointClass instance = new EntrypointClass();
    @Getter @Setter
    private String imports, code;
    public int compilations;

    private EntrypointClass() {
        this.imports = "";
        this.code = "";
        this.compilations = 0;
    }

    public String getFullClass() {
        return "package redot.executor.context;\n" +
                this.imports + "\n" +
                "public class " + this.getClassName() + " {\n" +
                    "public static void execute() {\n" +
                        this.code + "\n" +
                    "}\n" +
                "}";
    }

    public String getClassName() {
        return "Entrypoint_" + this.compilations;
    }

}
