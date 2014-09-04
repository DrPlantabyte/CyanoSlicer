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

package hall.collin.christopher.stlslice;

import hall.collin.christopher.stl4j.STLParser;
import hall.collin.christopher.stl4j.Triangle;
import hall.collin.christopher.stl4j.Vec3d;
import hall.collin.christopher.stlslice.math.LineSegment;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

// see http://geomalgorithms.com/a06-_intersect-2.html

/**
 *
 * @author CCHall
 */
public class TestApp {
	private static File askForFile(){
		JFileChooser jfc = new JFileChooser();
		int action = jfc.showOpenDialog(null);
		if(action != JFileChooser.APPROVE_OPTION){
			return null;
		}
		return jfc.getSelectedFile();
	}
	/**
	 * Entry point of program
	 * @param arg ignored
	 */
	public static void main(String[] arg){
		File f = askForFile();
		if(f == null){
			// canceled by user
			Logger.getLogger(TestApp.class.getName()).log(Level.WARNING, "Canceled by user");
			System.exit(0);
		}
		try {
			List<Triangle> mesh = STLParser.parseSTLFile(f.toPath());
			
			double maxX = Double.NEGATIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;
			double maxZ = Double.NEGATIVE_INFINITY;
			double minX = Double.POSITIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY;
			double minZ = Double.POSITIVE_INFINITY;
			for(Triangle t : mesh){
				for(Vec3d v : t.getVertices()){
					maxX = Math.max(maxX, v.x);
					maxY = Math.max(maxY, v.y);
					maxZ = Math.max(maxZ, v.z);
					minX = Math.min(minX, v.x);
					minY = Math.min(minY, v.y);
					minZ = Math.min(minZ, v.z);
				}
			}
			
			Vec3d center = new Vec3d(0.5*(minX+maxX),0.5*(minY+maxY),0.5*(minZ+maxZ));
			Vec3d planeNormal = new Vec3d(0,0,1);
			
			// make a slice
			final Map<Triangle,LineSegment> outline = new ConcurrentHashMap<>();
			mesh.parallelStream().forEach((Triangle t)->{
				LineSegment l = calculateIntersectionWithPlane(t,center,planeNormal);
				if(l != null){
					outline.put(t, l);
				}
			});
			
			visualizeSlice(outline, minX, minY,maxX,maxY,4);
			
		} catch (IOException ex) {
			Logger.getLogger(TestApp.class.getName()).log(Level.SEVERE, null, ex);
			System.exit(ex.hashCode());
		}
		
		System.exit(0);
	}

	private static LineSegment calculateIntersectionWithPlane(Triangle t, Vec3d planeRefPt, Vec3d planeNormal) {
		// Learned hot to do this from http://geomalgorithms.com/a06-_intersect-2.html
		// Note: when optimizing for horizontal slices, just check whether all points are on same side of plane's Z coordinate
		
		// plane formula is ax+by+cz+d=0 where vector(a,b,c) is normal to the plane
		planeNormal = planeNormal.normalize();
		double a = planeNormal.x;
		double b = planeNormal.y;
		double c = planeNormal.z;
		double d = -1 * (a * planeRefPt.x + b * planeRefPt.y + c * planeRefPt.z);
		
		// if all points are on same side of plane, no intersection (return null)
		boolean[] isPositive = new boolean[3];
		for(int i = 0; i < 3; i++){
			Vec3d v = t.getVertices()[i];
			double distFromPlane = a * v.x + b * v.y + c * v.z + d;
			isPositive[i] = (distFromPlane > 0);
		}
		boolean allTheSame = (isPositive[0] == isPositive[1]) && (isPositive[1] == isPositive[2]);
		if(allTheSame){
			// all points are on same side of plane, no intersection
			return null;
		}
		
		// intersection detected, get intersection points
		// then find the intersections of edges that intersect the plane
		Vec3d[] intersections = new Vec3d[2];
		int index = 0;
		for(int i = 0; i < 3; i++){
			if(isPositive[i] != isPositive[(i+1)%3]){
				Vec3d p0 = t.getVertices()[i];
				Vec3d p1 = t.getVertices()[(i+1)%3];
				double dist = planeNormal.dot(planeRefPt.sub(p0)) / planeNormal.dot(p1.sub(p0));
				Vec3d intersection = p0.add(p1.sub(p0).mul(dist));
				intersections[index++] = intersection;
			}
		}
		return new LineSegment(intersections[0],intersections[1]);
	}

	private static void visualizeSlice(Map<Triangle, LineSegment> outline, double minX, double minY, double maxX, double maxY, double pixelsPerUnit) {
		final int width = (int)(pixelsPerUnit * (maxX - minX));
		final int height = (int)(pixelsPerUnit * (maxY - minY));
		final double xOffset = minX;
		final double yOffset = minY;
		
		BufferedImage bimg = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bimg.createGraphics();
		g.setStroke(new java.awt.BasicStroke(2.0f));
		g.setColor(Color.red);
		for(LineSegment l : outline.values()){
			int x1 = (int)((l.getP1().x - xOffset)*pixelsPerUnit);
			int y1 = (int)(height - (l.getP1().y - yOffset)*pixelsPerUnit);
			int x2 = (int)((l.getP2().x - xOffset)*pixelsPerUnit);
			int y2 = (int)(height - (l.getP2().y - yOffset)*pixelsPerUnit);
			g.drawLine(x1, y1, x2, y2);
		}
		
		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(bimg)));
	}

}
