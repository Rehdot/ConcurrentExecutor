package redot.executor.gui.element;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import redot.executor.util.Extensions;

@Getter @Setter
@ExtensionMethod(Extensions.class)
public class ExecBoxWidget extends EditBoxWidget {

    private boolean mutable = true;

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
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean shouldSpecialKeyRegister(int keyCode) {
        if (this.isMutable()) return true;
        return keyCode != GLFW.GLFW_KEY_BACKSPACE
                && keyCode != GLFW.GLFW_KEY_ENTER
                && !Screen.isPaste(keyCode)
                && !Screen.isCut(keyCode);
    }

    @Override
    public String getText() {
        return super.getText().ifNull("");
    }

}
