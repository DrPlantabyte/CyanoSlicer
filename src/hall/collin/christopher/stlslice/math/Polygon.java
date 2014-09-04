/*
 * The MIT License (MIT)
 * 	
 * Copyright (c) 2014 CCHall (aka Cyanobacterium aka cyanobacteruim)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package hall.collin.christopher.stlslice.math;

import hall.collin.christopher.stl4j.Vec3d;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author CCHall
 */
public class Polygon {
	/** One point per corner */
	private final List<Vec3d> points;
	/** For each corner, there's a ray pointing out to indicate direction of 
	 * movement for scaling operations.
	 */
	private final List<Vec3d> cornerRays;
	
	public static Polygon traceLineSegments(java.util.Collection<LineSegment> outline){
		// assume that adjacent segments are NOT in adjacent indices
		// and that there are tiny gaps due to rounding errors
		outline
		// TODO
	}
}
