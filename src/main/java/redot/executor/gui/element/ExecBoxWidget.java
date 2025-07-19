package redot.executor.gui.element;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import redot.executor.util.Extensions;

import java.lang.reflect.Field;

@Getter @Setter
@ExtensionMethod(Extensions.class)
public class ExecBoxWidget extends EditBoxWidget {

    private boolean mutable = true;
    private EditBox editBox = null;

    public ExecBoxWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder, String message) {
        super(textRenderer, x, y, width, height, placeholder, Text.literal(message));
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.isMutable()) {
            return super.charTyped(chr, modifiers);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.shouldSpecialKeyRegister(keyCode)) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            if (!this.isMutable()) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            this.handleTab();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public String getText() {
        return super.getText().ifNull("");
    }

    private boolean shouldSpecialKeyRegister(int keyCode) {
        if (this.isMutable()) return true;
        return keyCode != GLFW.GLFW_KEY_BACKSPACE
                && keyCode != GLFW.GLFW_KEY_ENTER
                && !Screen.isPaste(keyCode)
                && !Screen.isCut(keyCode);
    }

    private void handleTab() {
        if (this.editBox == null) {
            if (!this.setEditBox()) return;
        }
        this.editBox.replaceSelection("    ");
    }

    /**
     * @apiNote reflectively gets ahold of the EditBox instance this EditBoxWidget instance has.
     *          avoids the pitfalls of fabric's remapping via no usage of "Class.forName()" etc.
     *          should only run once per ExecBoxWidget instance, in the case that tab was pressed.
     * @return true if the field was successfully set
     */
    private boolean setEditBox() {
        try {
            for (Field field : EditBoxWidget.class.getDeclaredFields()) {
                if (field.getType() != EditBox.class) {
                    continue;
                }

                field.setAccessible(true);
                EditBox box = (EditBox) field.get(this);

                if (box != null) {
                    this.editBox = box;
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
