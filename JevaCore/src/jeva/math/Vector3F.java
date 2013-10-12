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
package jeva.math;

/**
 * The Class Vector3F.
 */
public class Vector3F implements Comparable<Vector3F>
{

	/** The Constant TOLERANCE. */
	public static final float TOLERANCE = 0.0000001F;

	/** The x. */
	public float x;

	/** The y. */
	public float y;

	/** The z. */
	public float z;

	/**
	 * Instantiates a new vector3 f.
	 * 
	 * @param v
	 *            the v
	 * @param fZ
	 *            the f z
	 */
	public Vector3F(Vector2F v, float fZ)
	{
		x = v.x;
		y = v.y;
		z = fZ;
	}

	/**
	 * Instantiates a new vector3 f.
	 * 
	 * @param v
	 *            the v
	 * @param fZ
	 *            the f z
	 */
	public Vector3F(Vector2D v, float fZ)
	{
		x = v.x;
		y = v.y;
		z = fZ;
	}

	/**
	 * Instantiates a new vector3 f.
	 * 
	 * @param fX
	 *            the f x
	 * @param fY
	 *            the f y
	 * @param fZ
	 *            the f z
	 */
	public Vector3F(float fX, float fY, float fZ)
	{
		x = fX;
		y = fY;
		z = fZ;
	}

	/**
	 * Instantiates a new vector3 f.
	 * 
	 * @param v
	 *            the v
	 */
	public Vector3F(Vector2F v)
	{
		x = v.x;
		y = v.y;
		z = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Vector3F v)
	{
		if (Math.abs(v.z - z) > TOLERANCE)
			return (z < v.z ? -1 : 1);

		float fLengthDifference = x * x + y * y - (v.x * v.x + v.y * v.y);

		if (fLengthDifference > TOLERANCE)
			return 1;
		else if (fLengthDifference < -TOLERANCE)
			return -1;
		else if (Math.abs(v.z - z) > TOLERANCE)
			return (z < v.z ? -1 : 1);
		else if (Math.abs(v.x - x) > TOLERANCE)
			return (x < v.x ? -1 : 1);
		else if (Math.abs(v.y - y) > TOLERANCE)
			return (y < v.y ? -1 : 1);
		else
			return 0;
	}
}
