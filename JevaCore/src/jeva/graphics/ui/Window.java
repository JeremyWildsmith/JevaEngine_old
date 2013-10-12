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
package jeva.graphics.ui;

/**
 * The Class Window.
 */
public class Window extends Panel
{

	/** The m_is movable. */
	private boolean m_isMovable;

	/** The m_is focusable. */
	private boolean m_isFocusable;

	/**
	 * Instantiates a new window.
	 * 
	 * @param style
	 *            the style
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public Window(UIStyle style, int width, int height)
	{
		super(width, height);
		m_isFocusable = true;
		m_isMovable = true;

		this.setStyle(style);
	}

	/**
	 * Checks if is movable.
	 * 
	 * @return true, if is movable
	 */
	public boolean isMovable()
	{
		return m_isMovable;
	}

	/**
	 * Checks if is focusable.
	 * 
	 * @return true, if is focusable
	 */
	public boolean isFocusable()
	{
		return m_isFocusable;
	}

	/**
	 * Sets the movable.
	 * 
	 * @param isMovable
	 *            the new movable
	 */
	public void setMovable(boolean isMovable)
	{
		m_isMovable = isMovable;
	}

	/**
	 * Sets the focusable.
	 * 
	 * @param isFocusable
	 *            the new focusable
	 */
	public void setFocusable(boolean isFocusable)
	{
		m_isFocusable = isFocusable;
	}
}
