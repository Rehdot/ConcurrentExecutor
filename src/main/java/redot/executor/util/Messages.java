package redot.executor.util;

import lombok.Getter;

@Getter
public enum Messages {

    ENTRYPOINT_IMPORT_PLACEHOLDER("Entrypoint Imports...\n\nThis box will contain your imports for the entrypoint below.\n\nimport redot.executor.helper.Helper;\n"),
    ENTRYPOINT_CODE_PLACEHOLDER("Entrypoint Code...\n\nSystem.out.println(\"Run any code here.\");\nSystem.out.println(\"No need for a class or method.\");"),
    CUSTOM_PLACEHOLDER("Custom Class...\n\nDefine a class here, then hit compile.\nIf the class compiles, you can reference it from the entrypoint."),
    OUTPUT_PLACEHOLDER("Output Window...\n\nThis will contain text as soon as you compile/run something.");

    private final String message;

    Messages(String message) {
        this.message = message;
    }

}
