package imgui;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.gui.api.ImGui;
import net.devtech.jerraria.gui.api.ImGuiRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.widgets.Button;
import net.devtech.jerraria.gui.api.widgets.Menu;
import net.devtech.jerraria.gui.impl.ImGuiController;
import net.devtech.jerraria.gui.impl.ImGuiRendererImpl;
import net.devtech.jerraria.render.api.GlStateStack;
import net.devtech.jerraria.text.TriangulatedText;
import net.devtech.jerraria.util.math.Mat2x3f;

public class GuiTest {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static final TriangulatedText TAB_1 = TriangulatedText.text("Tab 1");
	public static final TriangulatedText TAB_2 = TriangulatedText.text("Tab 2");
	public static final TriangulatedText TAB_3 = TriangulatedText.text("Tab 3").withBold();

	public static class MyGui extends ImGui {
		static final Button.Settings CONFIG2 = Button.settings(Icon.color(0xFFFFFFAA).centered(TAB_1.asIcon(), .9f));
		static final Button.Settings CONFIG3 = Button.settings(Icon.color(0xFFFFAAAA).centered(TAB_2.asIcon(), .7f));
		static final Button.Settings CONFIG4 = Button.settings(Icon.color(0xFFAAAAFF).centered(TAB_3.asIcon(),.5f));
		static final Menu.Builder MENU = Menu.horizontal(1).tab(CONFIG2, 3).tab(CONFIG3, 3).tab(CONFIG4, 3);

		// currently selected tab
		int tab;

		public MyGui() {
			super(100, 100);
		}

		@Override
		protected void render0(ImGuiRenderer gui) {
			int selected = Menu.tabList(gui, 100/9f, MENU, this.tab);
			gui.spacer(10);
			try(gui.horizontal().pop) {
				gui.spacer(10);
				switch(this.tab = selected) {
					case 0 -> {
						if(Button.button(gui, 80, 70, CONFIG2)) {
							System.out.println("Tab 1");
						}
					}
					case 1 -> {
						if(Button.button(gui, 80, 70, CONFIG3)) {
							System.out.println("Tab 2");
						}
					}
					case 2 -> {
						if(Button.button(gui, 80, 70, CONFIG4)) {
							System.out.println("Tab 3");
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			MyGui gui = new MyGui();
			RenderThread.addRenderStage(() -> {
				Mat2x3f cartToIndexMat = new Mat2x3f();
				cartToIndexMat.offset(-1, 1);
				cartToIndexMat.scale(2, -2);
				float x = JerrariaClient.windowHeight() / (JerrariaClient.windowWidth() * 1f);
				cartToIndexMat.scale(x, 1f);

				ImGuiRenderer renderer = new ImGuiRendererImpl(
					gui.createGuiMatrix(cartToIndexMat, 1/x, 1)
				);
				gui.render0(renderer);
				try(GlStateStack.builder().blend(true).blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA).depthTest(true).apply().self) {
					renderer.draw(shader -> {});
				}

				ImGuiController.CONTROLLER.startFrame();
			}, 10);
			return null;
		});
	}
}
