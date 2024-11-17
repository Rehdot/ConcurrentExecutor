package redot.executor.model;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.MinecraftClient;
import net.openhft.compiler.CachedCompiler;
import net.openhft.compiler.CompilerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redot.executor.gui.screen.ExecutorScreen;
import redot.executor.util.Extensions;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ExtensionMethod(Extensions.class)
public class ConcurrentExecutor {

    @Getter
    private static final ConcurrentExecutor instance = new ConcurrentExecutor();
    private final EntrypointClass entrypoint = EntrypointClass.getInstance();
    private final CachedCompiler compiler = CompilerUtils.CACHED_COMPILER;
    private final Set<String> compiledClasses = Sets.newConcurrentHashSet();
    @Getter
    private final Logger logger = LoggerFactory.getLogger("concurrentexecutor");
    @Getter @Setter
    private ExecutorScreen screen = null;
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private ConcurrentExecutor() { }

    public void compileCustom(CustomClass custom) {
        if (custom.getCode().isEmpty()) {
            this.log("Cannot parse empty code.");
            return;
        }

        final String className = custom.getClassName();

        if (className.isNull()) {
            this.log("Error: Class name returned null.");
            return;
        }
        if (this.compiledClasses.contains(className)) {
            this.log("Error: '" + className + "' is already a compiled class!");
            return;
        }

        this.log("Attempting to compile " + className + "...");

        this.service.submit(() -> {
            try {
                this.compiler.loadFromJava("redot.executor.context." + className, custom.getCode());
            } catch (ClassNotFoundException exception) {
                exception.printStackTrace();
                this.log(exception.getClass().getSimpleName() + " caught while compiling '" + className + "':\n" + this.getStackTraceString(exception));
                return;
            }
            custom.setCompiled(true);
            this.compiledClasses.add(className);
            this.log("Compiled and cached '" + className + "' for this runtime.");
            this.screen.updatePageMutability();
            this.redrawScreen();
        });
    }

    public void runEntrypoint() {
        if (this.entrypoint.getCode().isEmpty()) {
            this.log("Cannot parse empty code.");
            return;
        }

        final String className = this.entrypoint.getClassName();
        final String fullClass = this.entrypoint.getFullClass();

        this.log("Attempting to compile & run entrypoint...");
        this.entrypoint.compilations++;

        this.service.submit(() -> {
            try {
                Class<?> entrypointClass = this.compiler.loadFromJava("redot.executor.context." + className, fullClass);
                Method executeMethod = entrypointClass.getMethod("execute");
                executeMethod.invoke(null);
            } catch (Exception exception) {
                exception.printStackTrace();
                this.log(exception.getClass().getSimpleName() + " caught while compiling or running entrypoint:\n" + this.getStackTraceString(exception));
                return;
            }
            this.compiledClasses.add(className);
            this.log("Compiled and ran entrypoint successfully.");
            this.redrawScreen();
        });

    }

    private void redrawScreen() {
        MinecraftClient.getInstance().submit(this.screen::draw);
    }

    public void log(String message) {
        MinecraftClient.getInstance().submit(() -> this.screen.sendConsoleMessage(message));
    }

    private String getStackTraceString(Throwable exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public void updateClasspath() {
        try { // add mod jar to classpath
            URL url = this.getURLFromObject(this);
            this.addClassPathReference(url);
        } catch (URISyntaxException exception) {
            this.logger.error(exception.getMessage());
        }

        try { // add minecraft jar to classpath
            URL url = this.getURLFromObject(MinecraftClient.getInstance());
            this.addClassPathReference(url);
        } catch (URISyntaxException exception) {
            this.logger.error(exception.getMessage());
        }
    }

    private void addClassPathReference(URL url) {
        File jar = new File(url.getPath());
        String path = jar.getAbsolutePath();
        CompilerUtils.addClassPath(path);
        this.logger.info("Added " + path + " to classpath.");
    }

    private <T> URL getURLFromObject(T object) throws URISyntaxException {
        return object.getClass().getProtectionDomain().getCodeSource().getLocation();
    }

}
