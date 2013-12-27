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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.jevaengine.world;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.script.ScriptException;

import org.mozilla.javascript.Scriptable;

import io.github.jevaengine.Core;
import io.github.jevaengine.CoreScriptException;
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.Script;
import io.github.jevaengine.Script.ScriptHiddenMember;
import io.github.jevaengine.audio.Audio;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import org.mozilla.javascript.Function;

public abstract class Entity implements IWorldAssociation
{
	private static AtomicInteger m_unnamedCount = new AtomicInteger(0);

	private ArrayList<ITask> m_pendingTasks = new ArrayList<ITask>();

	private ArrayList<ITask> m_runningTasks = new ArrayList<ITask>();

	private String m_name;
	
	private Vector2F m_location = new Vector2F();
	private WorldDirection m_direction = WorldDirection.Zero;
	
	private boolean m_isPaused = false;

	private EntityBridge<?> m_bridge;

	private IVariable m_config;

	private World m_parentWorld;

	private Observers m_observers = new Observers();

	private Script m_script;

	public Entity()
	{
		m_name = "__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement();
	}

	public Entity(@Nullable String name)
	{
		m_name = (name == null ? "__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement() : name);
	}

	protected <T extends EntityBridge<?>> Entity(@Nullable String name, IVariable config, T entityContext)
	{
		this(name == null ? "__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement() : name);
		
		init(config, entityContext);
	}

	protected <T extends EntityBridge<?>> Entity(@Nullable String name, IVariable config, T entityContext, WorldDirection direction)
	{
		this(name == null ? "__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement() : name);
		
		init(config, entityContext);
	}

	private <T extends EntityBridge<?>> void init(IVariable config, T entityContext)
	{
		m_config = config;

		entityContext.setMe(this);

		m_bridge = entityContext;

		EntityDeclaration entityConfig = new EntityDeclaration();
		entityConfig.deserialize(config);
		
		if (entityConfig.script != null && entityConfig.script.length() > 0)
		{
			m_script = Core.getService(IResourceLibrary.class).openScript(entityConfig.script, m_bridge);
			m_observers.add(new EntityScriptObserver());
		}else
			m_script = new Script(entityContext);
	}

	public void addObserver(IEntityObserver observer)
	{
		m_observers.add(observer);
	}

	public void removeObserver(IEntityObserver observer)
	{
		m_observers.remove(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.IWorldAssociation#isAssociated()
	 */
	@Override
	public final boolean isAssociated()
	{
		return m_parentWorld != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.IWorldAssociation#associate(jeva.world.World)
	 */
	@Override
	public final void associate(World world)
	{
		if (m_parentWorld != null)
			throw new WorldAssociationException("Already associated with world");

		m_parentWorld = world;

		m_observers.enterWorld();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.IWorldAssociation#disassociate()
	 */
	@Override
	public final void disassociate()
	{
		if (m_parentWorld == null)
			throw new WorldAssociationException("Not associated with world");

		this.cancelTasks();

		m_observers.leaveWorld();

		m_parentWorld = null;
	}
	
	
	public final Vector2F getLocation()
	{
		return m_location;
	}

	public final void setLocation(Vector2F location)
	{
		Vector2F oldLocation = m_location;
		m_location = location;

		if (!oldLocation.equals(location))
			m_observers.replaced();
	}
	
	public final WorldDirection getDirection()
	{
		return m_direction;
	}

	public final void setDirection(WorldDirection direction)
	{
		m_direction = direction;
	}

	public final void move(Vector2F delta)
	{
		m_location = m_location.add(delta);
	}

	protected final IVariable getConfiguration()
	{
		return m_config;
	}

	public final EntityBridge<?> getScriptBridge()
	{
		return m_bridge;
	}

	public final void pause()
	{
		m_isPaused = true;
	}

	public final void resume()
	{
		m_isPaused = false;
	}

	public final boolean isPaused()
	{
		return m_isPaused;
	}

	protected final void addTask(ITask task)
	{
		m_pendingTasks.add(task);
	}

	protected final void cancelTasks()
	{
		for (ITask task : m_runningTasks)
			task.cancel();

		m_pendingTasks.clear();
	}

	protected void cancelTask(ITask task)
	{
		if (!m_runningTasks.contains(task))
			throw new NoSuchElementException();

		task.cancel();
	}

	protected final boolean isTaskActive(ITask task)
	{
		return m_runningTasks.contains(task);
	}

	private boolean isTaskBlocking()
	{
		for (ITask task : m_runningTasks)
		{
			if (!task.isParallel())
				return true;
		}

		return false;
	}

	public final String getInstanceName()
	{
		return m_name;
	}
	
	public final World getWorld()
	{
		return m_parentWorld;
	}

	public final Script getScript()
	{
		return m_script;
	}

	public final void update(int deltaTime)
	{
		if (!this.isAssociated())
			throw new WorldAssociationException("Entity is unassociated with a world and thus cannot process logic.");

		if (!isPaused())
			doLogic(deltaTime);

		ArrayList<ITask> garbageTasks = new ArrayList<ITask>();

		LinkedList<ITask> reassignList = new LinkedList<ITask>();

		for (ITask task : m_pendingTasks)
		{
			if ((task.ignoresPause() && isPaused()) || !isPaused() && ((task.isParallel() && isTaskBlocking()) || !isTaskBlocking()))
			{
				if (reassignList.peekLast() == null || reassignList.peekLast().isParallel())
				{
					task.begin(this);
					reassignList.add(task);
				}
			}
		}

		for (ITask task : reassignList)
		{
			m_runningTasks.add(task);
			m_pendingTasks.remove(task);
		}

		if (!m_runningTasks.isEmpty())
		{
			for (ITask task : m_runningTasks)
			{
				if (!isPaused() || (isPaused() && task.ignoresPause()))
				{
					if (task.doCycle(deltaTime))
						garbageTasks.add(task);

					if (!task.isParallel())
						break;
				}
			}
		}

		for (ITask task : garbageTasks)
		{
			task.end();
			m_runningTasks.remove(task);
		}
	}

	public abstract void blendEffectMap(EffectMap globalEffectMap);

	public abstract void doLogic(int deltaTime);

	public interface IEntityObserver
	{
		void enterWorld();
		void leaveWorld();
		void replaced();
	}

	private class Observers extends StaticSet<IEntityObserver>
	{

		public void enterWorld()
		{
			for (IEntityObserver observer : this)
				observer.enterWorld();
		}

		public void leaveWorld()
		{
			for (IEntityObserver observer : this)
				observer.leaveWorld();
		}
		
		void replaced()
		{
			for (IEntityObserver observer : this)
				observer.replaced();
		}
	}

	private class EntityScriptObserver implements IEntityObserver
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see io.github.jeremywildsmith.jevaengine.world.Entity.IEntityObserver#enterWorld()
		 */
		@Override
		public void enterWorld()
		{
			try
			{
				m_script.invokeScriptFunction("onEnter");
			} catch (NoSuchMethodException e)
			{
				// Startup is an optional implementation for entities.
			} catch (ScriptException e)
			{
				throw new CoreScriptException("Error executing NPC Script onEnter routine for " + m_name + ", " + e.getMessage());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see io.github.jeremywildsmith.jevaengine.world.Entity.IEntityObserver#leaveWorld()
		 */
		@Override
		public void leaveWorld()
		{
			try
			{
				m_script.invokeScriptFunction("onLeave");
			} catch (NoSuchMethodException e)
			{
				// Startup is an optional implementation for entities.
			} catch (ScriptException e)
			{
				throw new CoreScriptException("Error executing entity Script onLeave routine of " + m_name + " " + e.getMessage());
			}
		}

		@Override
		public void replaced() { }
	}

	public static class EntityDeclaration implements ISerializable
	{
		public String script;

		@Override
		public void serialize(IVariable target)
		{
			if(script != null && script.length() > 0)
				target.addChild("script").setValue(script);
		}

		@Override
		public void deserialize(IVariable source)
		{
			if(source.childExists("script"))
				script = source.getChild("script").getValue(String.class);
		}
		
	}
	
	public static class EntityBridge<T extends Entity>
	{
		private Entity m_me;

		protected final void setMe(Entity me)
		{
			m_me = me;
		}

		@SuppressWarnings("unchecked")
		@ScriptHiddenMember
		public T getEntity()
		{
			return (T) m_me;
		}

		public String getName()
		{
			return getEntity().getInstanceName();
		}
		
		public float distance(EntityBridge<?> entity)
		{
			return getEntity().getLocation().difference(entity.getLocation()).getLength();
		}

		public Vector2D getLocation()
		{
			return getEntity().getLocation().round();
		}

		public void setLocation(Vector2F location)
		{
			getEntity().setLocation(location);
		}

		public void setLocation(float x, float y)
		{
			getEntity().setLocation(new Vector2F(x, y));
		}

		public void cancelTasks()
		{
			getEntity().cancelTasks();
		}

		public void pause()
		{
			getEntity().pause();
		}

		public void resume()
		{
			getEntity().resume();
		}

		public void playAudio(String audioName)
		{
			getEntity().addTask(new PlayAudioTask(new Audio(audioName), false));
		}
		
		public void invoke(Function target, Object ... parameters)
		{
			getEntity().addTask(new InvokeScriptFunctionTask(target, parameters));
		}

		public void idle(int length)
		{
			getEntity().addTask(new IdleTask(length));
		}

		public void leave()
		{
			getEntity().addTask(new SynchronousOneShotTask()
			{
				@Override
				public void run(Entity entity)
				{
					getEntity().getWorld().removeEntity(getEntity());
				}
			});
		}

		public Scriptable getScript()
		{
			return getEntity().getScript().getScriptedInterface();
		}
	}
}
