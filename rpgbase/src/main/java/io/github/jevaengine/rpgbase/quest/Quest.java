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
package io.github.jevaengine.rpgbase.quest;

import java.util.ArrayList;

import io.github.jevaengine.config.Variable;

public class Quest
{
	private String m_name;
	private String m_description;

	private QuestTask[] m_tasks;

	public Quest(String name, String description, QuestTask[] tasks)
	{
		m_name = name;
		m_description = description;
		m_tasks = tasks;
	}

	public static Quest create(Variable root)
	{
		String name = root.getVariable("name").getValue().getString();
		String description = root.getVariable("description").getValue().getString();

		ArrayList<QuestTask> tasks = new ArrayList<QuestTask>();

		for (Variable vTask : root.getVariable("task").getVariableArray())
		{
			tasks.add(new QuestTask(vTask.getVariable("id").getValue().getString(), vTask.getVariable("name").getValue().getString(), vTask.getVariable("description").getValue().getString()));
		}

		return new Quest(name, description, tasks.toArray(new QuestTask[tasks.size()]));
	}

	public QuestTask getTask(String id)
	{
		for (QuestTask t : m_tasks)
		{
			if (t.getId().compareTo(id) == 0)
				return t;
		}

		return null;
	}

	public QuestState getState()
	{
		QuestState state = QuestState.Failed;

		for (QuestTask t : m_tasks)
		{
			if (t.getState().compareTo(state) < 0)
				state = t.getState();
		}

		return state;
	}

	public String getName()
	{
		return m_name;
	}

	public String getDescription()
	{
		return m_description;
	}

	public QuestTask[] getTasks()
	{
		return m_tasks;
	}
}