/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package jevarpg;

import java.util.HashMap;

import proguard.annotation.KeepClassMemberNames;
import jeva.config.VariableStore;
import jeva.game.Game;
import jeva.game.IGameScriptProvider;
import jeva.graphics.AnimationState;
import jeva.graphics.Sprite;
import jeva.graphics.ui.Window;
import jeva.graphics.ui.MenuStrip;
import jeva.graphics.ui.MenuStrip.IMenuStripListener;
import jeva.graphics.ui.UIStyle;
import jeva.graphics.ui.IWindowManager;
import jeva.joystick.InputManager.InputMouseEvent;
import jeva.joystick.InputManager.InputMouseEvent.MouseButton;
import jeva.math.Vector2D;
import jeva.world.IInteractable;
import jeva.world.World.WorldScriptContext;
import jeva.Core;
import jeva.IResourceLibrary;
import jevarpg.library.RpgEntityLibrary;
import jevarpg.quest.QuestState;

public abstract class RpgGame extends Game
{
    private Window m_contextStripContainer;
    private MenuStrip m_contextStrip;
    
    private UIStyle m_gameStyle;

    private Sprite m_cursor;

    @Override
    protected void startup()
    {
        IResourceLibrary fileSystem = Core.getService(IResourceLibrary.class);

        UIStyle styleLarge = UIStyle.create(VariableStore.create(fileSystem.openResourceStream("ui/tech/large.juis")));
        m_gameStyle = UIStyle.create(VariableStore.create(fileSystem.openResourceStream("ui/tech/small.juis")));

        m_cursor = Sprite.create(VariableStore.create(fileSystem.openResourceStream("ui/tech/cursor.jsf")));
        m_cursor.setAnimation("idle", AnimationState.Play);

        m_contextStrip = new MenuStrip();
        m_contextStripContainer = new Window(styleLarge, 100, 220);
        m_contextStripContainer.addControl(m_contextStrip, new Vector2D());
        m_contextStripContainer.setRenderBackground(false);
        m_contextStripContainer.setVisible(false);

        Core.getService(IWindowManager.class).addWindow(m_contextStripContainer);
    }

    public RpgEntityLibrary getEntityLibrary()
    {
        return new RpgEntityLibrary();
    }

    @Override
    public void update(int deltaTime)
    {
        m_cursor.update(deltaTime);

        super.update(deltaTime);
    }

    @Override
    protected void worldSelection(InputMouseEvent e, Vector2D location)
    {
        final IInteractable[] interactables = getWorld().getTileEffects(location).interactables.toArray(new IInteractable[0]);

        if (e.mouseButton == MouseButton.Left)
        {
            if (getPlayer() != null)
                getPlayer().moveTo(location);

            m_contextStrip.setVisible(false);
        } else if (e.mouseButton == MouseButton.Right)
        {
            if (interactables.length > 0 && interactables[0].getCommands().length > 0)
            {
                m_contextStrip.setContext(interactables[0].getCommands(), new IMenuStripListener()
                {
                    @Override
                    public void onCommand(String command)
                    {
                        interactables[0].doCommand(command);
                    }
                });
                m_contextStripContainer.setVisible(true);
                m_contextStripContainer.setLocation(e.location);
                m_contextStripContainer.setWidth(m_contextStrip.getBounds().width);
                m_contextStripContainer.setHeight(m_contextStrip.getBounds().height);
            }
        }
    }

    @Override
    public UIStyle getGameStyle()
    {
        return m_gameStyle;
    }

    @Override
    protected void onLoadedWorld()
    {
        m_contextStrip.setVisible(false);
    }

    @Override
    protected Sprite getCursor()
    {
        return m_cursor;
    }

    public abstract RpgCharacter getPlayer();

    @Override
    public IGameScriptProvider getScriptBridge()
    {
        return new RpgGameScriptProvider();
    }

    public class RpgGameScriptProvider implements IGameScriptProvider
    {
        @Override
        public Object getGameBridge()
        {
            return new GameBridge();
        }

        @Override
        public HashMap<String, Object> getGlobals()
        {
            HashMap<String, Object> vars = new HashMap<String, Object>();

            vars.put("quest_notStarted", QuestState.NotStarted);
            vars.put("quest_failed", QuestState.Failed);
            vars.put("quest_completed", QuestState.Completed);
            vars.put("quest_inProgress", QuestState.InProgress);

            return vars;
        }

        @KeepClassMemberNames
        public class GameBridge
        {
            public WorldScriptContext getWorld()
            {
                return RpgGame.this.getWorld().getScriptBridge();
            }

            public RpgCharacter.EntityBridge<?> getPlayer()
            {
                if (RpgGame.this.getPlayer() == null)
                    return null;

                return RpgGame.this.getPlayer().getScriptBridge();
            }
        }
    }
}
