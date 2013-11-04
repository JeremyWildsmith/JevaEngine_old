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
package io.github.jevaengine.rpgbase.client;

import io.github.jevaengine.Core;
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.config.Variable;
import io.github.jevaengine.config.VariableStore;
import io.github.jevaengine.game.FollowCamera;
import io.github.jevaengine.game.Game;
import io.github.jevaengine.graphics.ui.Button;
import io.github.jevaengine.graphics.ui.IWindowManager;
import io.github.jevaengine.graphics.ui.MenuStrip;
import io.github.jevaengine.graphics.ui.UIStyle;
import io.github.jevaengine.graphics.ui.Window;
import io.github.jevaengine.graphics.ui.WorldView;
import io.github.jevaengine.graphics.ui.MenuStrip.IMenuStripListener;
import io.github.jevaengine.graphics.ui.WorldView.IWorldViewListener;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.client.ClientCommunicator.IClientCommunicatorObserver;
import io.github.jevaengine.rpgbase.client.ClientUser.IClientUserObserver;
import io.github.jevaengine.rpgbase.client.ui.ChatMenu;
import io.github.jevaengine.rpgbase.ui.CharacterMenu;
import io.github.jevaengine.rpgbase.ui.InventoryMenu;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.IInteractable;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.World.IWorldObserver;

public class PlayingState implements IGameState
{
	private ClientGame m_context;

	private EventHandler m_handler = new EventHandler();

	private World m_world;

	private ClientUser m_user;

	@Nullable private RpgCharacter m_playerCharacter;

	@Nullable private String m_playerEntityName;

	private FollowCamera m_playerCamera = new FollowCamera();

	private Window m_worldViewWindow;
	private Window m_hud;
	private ChatMenu m_chatMenu;
	private InventoryMenu m_inventoryMenu = new InventoryMenu();
	private CharacterMenu m_characterMenu = new CharacterMenu();

	private MenuStrip m_contextStrip = new MenuStrip();

	public PlayingState(String playerEntityName, ClientUser user, World world)
	{
		final UIStyle styleLarge = UIStyle.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream("ui/tech/large.juis")));
		final UIStyle styleSmall = UIStyle.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream("ui/tech/small.juis")));

		m_playerEntityName = playerEntityName;
		m_user = user;
		m_world = world;

		m_hud = new Window(styleLarge, 177, 80);
		m_hud.addControl(new Button("Inventory")
		{

			@Override
			public void onButtonPress()
			{
				m_inventoryMenu.accessInventory(m_context.getPlayer().getInventory(), m_context.getPlayer());
			}
		}, new Vector2D(5, 10));

		m_hud.addControl(new Button("Character")
		{
			@Override
			public void onButtonPress()
			{
				m_characterMenu.showCharacter(m_context.getPlayer());
			}
		}, new Vector2D(5, 40));

		m_chatMenu = new ChatMenu(styleSmall)
		{

			@Override
			public void onSend(String message)
			{
				m_user.sendChat(message);
			}
		};

		m_chatMenu.setLocation(new Vector2D(560, 570));
		m_chatMenu.setMovable(false);

		m_hud.setLocation(new Vector2D(20, 670));
		m_hud.setMovable(false);
		m_hud.setVisible(false);

		Vector2D resolution = Core.getService(Game.class).getResolution();

		m_playerCamera = new FollowCamera();

		WorldView worldViewport = new WorldView(resolution.x, resolution.y);
		worldViewport.setRenderBackground(false);
		worldViewport.setCamera(m_playerCamera);
		worldViewport.addListener(new WorldViewListener());

		m_worldViewWindow = new Window(styleSmall, resolution.x, resolution.y);
		m_worldViewWindow.setRenderBackground(false);
		m_worldViewWindow.setMovable(false);
		m_worldViewWindow.setFocusable(false);

		m_worldViewWindow.addControl(worldViewport);
		m_worldViewWindow.addControl(m_contextStrip);
	}

	@Override
	public void enter(ClientGame context)
	{

		m_context = context;

		final IWindowManager windowManager = Core.getService(IWindowManager.class);

		windowManager.addWindow(m_hud);
		windowManager.addWindow(m_chatMenu);
		windowManager.addWindow(m_inventoryMenu);
		windowManager.addWindow(m_characterMenu);
		windowManager.addWindow(m_worldViewWindow);

		m_user.addObserver(m_handler);
		context.getCommunicator().addObserver(m_handler);

		if (m_playerEntityName != null && m_world.variableExists(m_playerEntityName))
		{
			Variable characterVar = m_world.getVariable(m_playerEntityName);

			if (!(characterVar instanceof RpgCharacter))
				m_context.getCommunicator().disconnect("Server did not assign proper character entity");

			playerAdded((RpgCharacter) characterVar);
		}

		m_world.addObserver(m_handler);
	}

	@Override
	public void leave()
	{
		final IWindowManager windowManager = Core.getService(IWindowManager.class);

		windowManager.removeWindow(m_hud);
		windowManager.removeWindow(m_chatMenu);
		windowManager.removeWindow(m_inventoryMenu);
		windowManager.removeWindow(m_characterMenu);
		windowManager.removeWindow(m_worldViewWindow);

		m_user.removeObserver(m_handler);
		m_world.removeObserver(m_handler);
		m_context.getCommunicator().removeObserver(m_handler);

		m_context.setPlayer(null);

		m_context = null;
	}

	@Override
	public void update(int deltaTime)
	{
		m_world.update(deltaTime);
	}

	private void playerAdded(RpgCharacter player)
	{
		m_playerCharacter = player;
		m_playerCamera.attach(m_world);
		m_playerCamera.setTarget(player.getName());
		m_context.setPlayer(player);
		m_hud.setVisible(true);
	}

	private void playerRemoved()
	{
		m_playerCharacter = null;
		m_playerCamera.dettach();
		m_context.setPlayer(null);
		m_hud.setVisible(false);
	}

	private class WorldViewListener implements IWorldViewListener
	{

		@Override
		public void worldSelection(Vector2D screenLocation, Vector2D worldLocation, MouseButton button)
		{
			final IInteractable[] interactables = m_world.getTileEffects(worldLocation).interactables.toArray(new IInteractable[0]);

			if (button == MouseButton.Left)
			{
				if (m_playerCharacter != null)
					m_playerCharacter.moveTo(worldLocation);

				m_contextStrip.setVisible(false);
			} else if (button == MouseButton.Right)
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

					m_contextStrip.setLocation(screenLocation.difference(m_contextStrip.getParent().getAbsoluteLocation()));
				}
			}
		}

	}

	private class EventHandler implements IClientUserObserver, IClientCommunicatorObserver, IWorldObserver
	{
		@Override
		public void timeout()
		{
			m_context.getCommunicator().disconnect("Timeout");
		}

		@Override
		public void recieveChatMessage(String user, String message)
		{
			m_chatMenu.recieveChatMessage(user, message);
		}

		@Override
		public void assignedPlayer(String entityName)
		{
			m_playerEntityName = entityName;
		}

		@Override
		public void unassignedPlayer()
		{
			m_context.setPlayer(null);
			m_playerEntityName = null;
		}

		@Override
		public void unservedWorld()
		{
			m_context.setState(new LoadingState(m_user, m_playerEntityName));
		}

		@Override
		public void disconnected(String cause)
		{
			m_context.setState(new LoginState(cause));
		}

		// By the time we've reached this state, the player is fully
		// authenticated.
		@Override
		public void authenticated()
		{
		}

		@Override
		public void authenticationFailed()
		{
		}

		@Override
		public void servedUser(ClientUser user)
		{
		}

		@Override
		public void unservedUser()
		{
		}

		@Override
		public void servedWorld(World world)
		{
		}

		@Override
		public void addedEntity(Entity e)
		{
			if (m_playerEntityName != null && e instanceof RpgCharacter && e.getName().equals(m_playerEntityName))
				playerAdded((RpgCharacter) e);
		}

		@Override
		public void removedEntity(Entity e)
		{
			if (m_playerEntityName != null && e.getName().equals(m_playerEntityName))
				playerRemoved();
		}
	}
}