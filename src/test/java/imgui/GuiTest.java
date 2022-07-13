package imgui;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.client.render.textures.GuiTex;
import net.devtech.jerraria.gui.JerrarianTextRenderer;
import net.devtech.jerraria.gui.api.ImGui;
import net.devtech.jerraria.gui.api.ImGuiRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.icons.borders.NinePatchBorder;
import net.devtech.jerraria.gui.api.icons.borders.Simple3DBorder;
import net.devtech.jerraria.gui.api.widgets.Button;
import net.devtech.jerraria.gui.api.widgets.Menu;
import net.devtech.jerraria.gui.impl.ImGuiController;
import net.devtech.jerraria.client.render.text.TriangulatedText;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat2x3f;
import org.jetbrains.annotations.Nullable;

public class GuiTest {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static final TriangulatedText TAB_1 = TriangulatedText
		                                             .text("\uD83D\uDD74")
		                                             .withColor(0xFF000000); // man in a suit levitating
	public static final TriangulatedText TAB_2 = TriangulatedText.text("\uD83D\uDE33"); // flushed
	public static final TriangulatedText TAB_3 = TriangulatedText.text("Tab 3").withBold();

	static final Button.Settings CONFIG2 = Button.settings(Icon
		                                                       .color(0xFFFFFFAA)
		                                                       .centered(TAB_1.asIcon(), .9f)
		                                                       .bordered(
			                                                       Simple3DBorder.FACTORY,
			                                                       Simple3DBorder.DEFAULT.with(.5f)
		                                                       ));
	static final Button.Settings CONFIG3 = Button.settings(Icon
		                                                       .color(0xFFFFAAAA)
		                                                       .centered(TAB_2.asIcon(), .7f)
		                                                       .bordered(0xFFFFBBCC));

	public static class MyGui extends ImGui {
		//static final Button.Settings CONFIG4 = Button.settings(Icon
		//	                                                       .color(0xFFAAAAFF)
		//	                                                       .centered(TAB_3.asIcon(), .5f)
		//	                                                       .bordered(
		//		                                                       NinePatchBorder.FACTORY,
		//		                                                       NinePatchBorder.patch(GuiTex.DISABLED).cornerSize(8).cornerUv(
		//			                                                       8/256f, 8/512f).build()
		//	                                                       ));
		static final Button.Settings CONFIG4 = Button.settings(new NinePatchBorder(
			GuiTex.PATCH_DISABLED,
			100,
			100
		));
		static final Menu.Builder MENU = Menu.horizontal(1).tab(CONFIG2, 3).tab(CONFIG3, 3).tab(CONFIG4, 3);

		// currently selected tab
		int tab;

		public MyGui() {
			super(100, 100);
		}

		@Override
		protected void render0(ImGuiRenderer gui, float width, float height) {
			int selected = Menu.tabList(gui, 100 / MENU.width(), MENU, this.tab);
			gui.spacer(10);
			try(gui.horizontal().self) {
				gui.spacer(10);
				switch(this.tab = selected) {
					case 0 -> {
						if(Button.button(gui, 80, 70, "CONFIG2")) {
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

		@Override
		public @Nullable AutoCentering centering(float width, float height) {
			return this.defaultCentering();
		}
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			MyGui gui = new MyGui();
			RenderThread.addRenderStage(() -> {
				Mat cartToIndexMat = Mat.create();
				cartToIndexMat.offset(-1, 1);
				cartToIndexMat.scale(2, -2);
				float x = JerrariaClient.windowHeight() / (JerrariaClient.windowWidth() * 1f);
				cartToIndexMat.scale(x, 1f);
				gui.render(JerrarianTextRenderer.TEXT_RENDERER, cartToIndexMat, 1 / x, 1);
				ImGuiController.CONTROLLER.startFrame();
			}, 10);
			return null;
		});
	}
}
