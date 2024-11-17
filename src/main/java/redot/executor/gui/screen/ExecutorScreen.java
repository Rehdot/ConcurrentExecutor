package redot.executor.gui.screen;

import com.google.common.collect.Lists;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.text.Text;
import redot.executor.gui.element.ExecBoxWidget;
import redot.executor.model.ConcurrentExecutor;
import redot.executor.model.CustomClass;
import redot.executor.model.EntrypointClass;
import redot.executor.util.Extensions;
import redot.executor.util.Messages;

import java.util.List;

@ExtensionMethod(Extensions.class)
public class ExecutorScreen extends Screen {

    private int page = 0;
    private final String packageLine = "package redot.executor.context;\n\n";
    private PageTurnWidget pageLeft, pageRight;
    private ButtonWidget addButton, deleteButton, compileButton, runButton;
    private ExecBoxWidget entryImportBox, entryCodeBox, consoleBox, customsBox;
    private final EntrypointClass entrypoint = EntrypointClass.getInstance();
    private final List<CustomClass> customs = Lists.newCopyOnWriteArrayList().with(new CustomClass(this.packageLine));

    public ExecutorScreen(String title) {
        super(Text.literal(title));
    }

    @Override
    protected void init() {
        super.init();
        this.updateEntrypoint();
        this.updateCurrentCustom();
        this.initExecBoxes();
        this.initButtons();
        this.draw();
    }

    @Override
    public void close() {
        this.updateEntrypoint();
        this.updateCurrentCustom();
        super.close();
    }

    public void initExecBoxes() {
        String consoleText = this.consoleBox.isNull() ? "" : this.consoleBox.getText();
        String entryImportText = this.entrypoint.getImports();
        String entryCodeText = this.entrypoint.getCode();

        this.entryImportBox = this.newExecBox(10,10, this.width / 3, this.height / 4 + 20, Messages.ENTRYPOINT_IMPORT_PLACEHOLDER);
        this.entryCodeBox = this.newExecBox(10, this.height / 3 - 10, this.width / 3, this.height / 3, Messages.ENTRYPOINT_CODE_PLACEHOLDER);
        this.consoleBox = this.newExecBox(10, (this.height / 3) * 2 + 40, this.width / 3, ((this.height / 3) / 2) + 20, Messages.OUTPUT_PLACEHOLDER);
        this.customsBox = this.newExecBox((this.width / 2) + (this.width / 6) - 10, 10, this.width / 3, (this.height / 2) + (this.height / 4), Messages.CUSTOM_PLACEHOLDER);

        this.entryImportBox.setText(entryImportText);
        this.entryCodeBox.setText(entryCodeText);
        this.customsBox.setText(this.customs.get(this.page).getCode());
        this.consoleBox.setText(consoleText);
        this.consoleBox.setMutable(false);
        this.updatePageMutability();
    }

    public void initButtons() {
        final int bottomCustomY = this.customsBox.getBottom() + 10;
        final int bottomEntryY = this.entryCodeBox.getBottom() + 10;

        this.pageLeft = this.getPageLeftButton(bottomCustomY);
        this.pageRight = this.getPageRightButton(bottomCustomY);
        this.compileButton = this.getCompileButton(bottomCustomY);
        this.runButton = this.getRunButton(bottomEntryY);
        this.addButton = this.getAddButton(bottomCustomY);
        this.deleteButton = this.getDeleteButton(bottomCustomY);
    }

    public void draw() {
        final int customsLastIndex = this.customs.size() - 1;

        this.clearChildren();
        this.addDrawableChildren(this.entryImportBox, this.entryCodeBox, this.consoleBox, this.customsBox);

        if (this.page > customsLastIndex) {
            this.page = customsLastIndex;
        }
        if (!this.customs.get(this.page).isCompiled()) {
            this.addDrawableChildren(this.compileButton, this.deleteButton);
        }
        if (customsLastIndex > this.page) {
            this.addDrawableChild(this.pageRight);
        }
        if (this.page - 1 >= 0) {
            this.addDrawableChild(this.pageLeft);
        }

        this.addDrawableChildren(this.runButton, this.addButton);
    }

    private PageTurnWidget getPageLeftButton(int y) {
        return new PageTurnWidget(this.customsBox.getX(), y, false, button -> {
            this.updateCurrentCustom();
            this.page--;
            this.updatePageText();
            this.updatePageMutability();
            this.draw();
        }, false);
    }

    private PageTurnWidget getPageRightButton(int y) {
        return new PageTurnWidget(this.customsBox.getX() + 30, y, true, button -> {
            this.updateCurrentCustom();
            this.page++;
            this.updatePageText();
            this.updatePageMutability();
            this.draw();
        }, false);
    }

    private ButtonWidget getRunButton(int y) {
        return ButtonWidget.builder(Text.literal("Run Entrypoint"), button -> {
            this.updateEntrypoint();
            ConcurrentExecutor.getInstance().runEntrypoint();
        }).position(10, y)
                .size(100, 20)
                .build();
    }

    private ButtonWidget getCompileButton(int y) {
        return ButtonWidget.builder(Text.literal("Compile Class"), button -> {
            CustomClass custom = this.customs.get(this.page);
            this.updateCurrentCustom();
            if (custom.isCompiled()) return;
            ConcurrentExecutor.getInstance().compileCustom(custom);
        }).position(this.customsBox.getX() + this.customsBox.getWidth() - 100, y)
                .size(100, 20)
                .build();
    }

    private ButtonWidget getAddButton(int y) {
        return ButtonWidget.builder(Text.literal("Add Class"), button -> {
            this.updateCurrentCustom();
            this.customs.add(new CustomClass(this.packageLine));
            this.updatePageMutability();
            this.draw();
        }).position(this.customsBox.getX() + this.customsBox.getWidth() - 100, y + 20)
                .size(100, 20)
                .build();
    }

    private ButtonWidget getDeleteButton(int y) {
        return ButtonWidget.builder(Text.literal("Delete Class"), button -> {
            CustomClass currentClass = this.customs.get(this.page);
            if (currentClass.isCompiled() || this.customs.size() == 1) return;
            this.customs.remove(this.page);
            this.page = Math.min(this.page, this.customs.size() - 1);
            this.updatePageText();
            this.updatePageMutability();
            this.draw();
        }).position(this.customsBox.getX() + this.customsBox.getWidth() - 100, y + 40)
                .size(100, 20)
                .build();
    }

    @SafeVarargs
    protected final <T extends Element & Drawable & Selectable> void addDrawableChildren(T... children) {
        for (T child : children) this.addDrawableChild(child);
    }

    private void updateCurrentCustom() {
        if (this.customsBox.isNull()) return;
        CustomClass custom = this.customs.get(page);
        custom.setCode(this.customsBox.getText());
    }

    private void updateEntrypoint() {
        if (this.entryImportBox.isNull()) return;
        this.entrypoint.setImports(this.entryImportBox.getText());
        this.entrypoint.setCode(this.entryCodeBox.getText());
    }

    private void updatePageText() {
        if (this.customsBox.isNull()) return;
        CustomClass custom = this.customs.get(page);
        this.customsBox.setText(custom.getCode());
    }

    public void updatePageMutability() {
        CustomClass custom = this.customs.get(page);
        if (custom.isNull()) return;
        this.customsBox.setMutable(!custom.isCompiled());
    }

    private ExecBoxWidget newExecBox(int x, int y, int width, int height, Messages placeholder) {
        return new ExecBoxWidget(this.textRenderer, x, y, width, height, Text.literal(placeholder.getMessage()), "");
    }

    public void sendConsoleMessage(String message) {
        String currentText = this.consoleBox.getText();

        if (currentText.length() > 5000) {
            currentText = currentText.substring(currentText.length() / 2);
        }

        this.consoleBox.setText(currentText + "\n" + message);
    }

}