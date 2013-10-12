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
package jeva.graphics;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import jeva.math.Vector2D;

/**
 * The Class Text.
 * 
 * @author Jeremy. A. W
 */
public final class Text implements IRenderable
{
	/** The m_anchor. */
	private Vector2D m_anchor;

	/** The m_font. */
	private Font m_font;

	/** The m_text. */
	private String m_text;

	/** The m_f scale. */
	private float m_fScale;

	/**
	 * Instantiates a new text.
	 * 
	 * @param text
	 *            the text
	 * @param anchor
	 *            the anchor
	 * @param font
	 *            the font
	 * @param fScale
	 *            the f scale
	 */
	public Text(String text, Vector2D anchor, Font font, float fScale)
	{
		m_text = text;
		m_anchor = anchor;
		m_font = font;
		m_fScale = fScale;
	}

	/**
	 * Sets the text.
	 * 
	 * @param text
	 *            the new text
	 */
	public void setText(String text)
	{
		m_text = text;
	}

	/**
	 * Gets the text.
	 * 
	 * @return the text
	 */
	public String getText()
	{
		return m_text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.IRenderable#render(java.awt.Graphics2D, int, int,
	 * float)
	 */
	@Override
	public void render(Graphics2D g, int x, int y, float fScale)
	{
		Rectangle[] str = m_font.getString(m_text);

		int xOffset = x;

		for (int i = 0; i < str.length; i++)
		{
			g.drawImage(m_font.getSource(), -m_anchor.x + xOffset, -m_anchor.y + y, (int) (xOffset + str[i].width * fScale * m_fScale), (int) (y + str[i].height * fScale * m_fScale), str[i].x, str[i].y, str[i].x + str[i].width, str[i].y + str[i].height, null);
			xOffset += str[i].width * fScale * m_fScale;
		}
	}
}
