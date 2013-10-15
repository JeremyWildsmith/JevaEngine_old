package jeva.graphics.ui;

import jeva.graphics.IRenderable;
import jeva.joystick.InputManager;

public interface IWindowManager extends IRenderable
{
	void addWindow(Window window);
	void removeWindow(Window window);
	
	void onMouseEvent(InputManager.InputMouseEvent mouseEvent);
	boolean onKeyEvent(InputManager.InputKeyEvent keyEvent);
	
	void update(int deltaTime);
}
