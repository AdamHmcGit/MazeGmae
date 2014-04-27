import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;


public class DisplayExample {

	Queue<LocationNode> currentRoute;
	
	
	List<Entity> entityList = new ArrayList<Entity>();
	Queue<Monster> monsterQueue = new LinkedList<Monster>();
	
	long lastSpawnTime;
	public static LocationNode [][] mapNodes; 
	
	float deltaX=0;
	float deltaY=0;
	
	/** position of quad */
	float x = 400, y = 300;
	/** angle of quad rotation */
	float rotation = 0;
 
	/** time at last frame */
	long lastFrame;
	
	static float defaultCellWidth;
	
	static float defaultCellHeight;
	
	/** frames per second */
	int fps;
	/** last fps time */
	long lastFPS;
	
	static int mapWidth;
	
	static int mapHeight;
	
	/** is VSync Enabled */
	boolean vsync;
	
	boolean isReadyToSpawn;

	Set<LocationNode> routeNodes;
	
	static LocationNode startNode;
	static LocationNode destinationNode;
	
	PathFinder pf;
	
	public static void main(String [] args) throws LWJGLException, IOException{
		DisplayExample ds = new DisplayExample();
		
		mapWidth=800;
		mapHeight=600;
		ds.initMap();
		DisplayExample.startNode = DisplayExample.mapNodes[50][70];
		DisplayExample.destinationNode = DisplayExample.mapNodes[1][1];
		ds.currentRoute = new ArrayDeque<LocationNode>(ds.pf.getShortestPath(DisplayExample.startNode, DisplayExample.destinationNode));
		ds.routeNodes = new HashSet<LocationNode>(ds.currentRoute);
		ds.start();
	}

	 
	public void start() {
		try {
			Display.setDisplayMode(new DisplayMode(mapWidth, mapHeight));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		spawn();
		initGL(); // init OpenGL
		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = getTime(); // call before loop to initialise fps timer
		renderGL();

		
		while (!Display.isCloseRequested()) {
			int delta = getDelta();
 
			update(delta);
			renderGL();
 
			Display.update();
			Display.sync(60); // cap fps to 60fps
		}
 
		Display.destroy();
	}
 
	public void update(int delta) {

		if (Mouse.isButtonDown(0)) {
			int targetX = Mouse.getX();
			int targetY = Mouse.getY();
			LocationNode targetNode = mapNodes[(int) (targetY/defaultCellHeight)][(int) (targetX/defaultCellWidth)];
			targetNode.isWalkable=false;
			if(routeNodes.contains(targetNode)){
				currentRoute.clear();
				currentRoute.addAll(pf.getShortestPath(DisplayExample.startNode, DisplayExample.destinationNode));
				routeNodes.clear();
				routeNodes.addAll(currentRoute);
			}
			for(Entity e:entityList){
				if(e instanceof Monster){
					((Monster) e).updateRoute(targetNode);
				}
			}
		}
		if(lastSpawnTime==0 || (getTime() - lastSpawnTime > 1000)){
			lastSpawnTime=getTime();
			Entity e = monsterQueue.poll();
			if(e!=null){
				e.setRoute(currentRoute);
				entityList.add(e);
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) x -= 0.35f * delta;
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) x += 0.35f * delta;
 
		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) y -= 0.35f * delta;
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) y += 0.35f * delta;
 
		while (Keyboard.next()) {
		    if (Keyboard.getEventKeyState()) {
		        if (Keyboard.getEventKey() == Keyboard.KEY_F) {
		        	setDisplayMode(800, 600, !Display.isFullscreen());
		        }
		        else if (Keyboard.getEventKey() == Keyboard.KEY_V) {
		        	vsync = !vsync;
		        	Display.setVSyncEnabled(vsync);
		        }
		    }
		}
		
		// keep quad on the screen
		if (x < 0) x = 0;
		if (x > 800) x = 800;
		if (y < 0) y = 0;
		if (y > 600) y = 600;
 
		updateFPS(); // update FPS Counter
	}
 
	/**
	 * Set the display mode to be used 
	 * 
	 * @param width The width of the display required
	 * @param height The height of the display required
	 * @param fullscreen True if we want fullscreen mode
	 */
	public void setDisplayMode(int width, int height, boolean fullscreen) {

		// return if requested DisplayMode is already set
                if ((Display.getDisplayMode().getWidth() == width) && 
			(Display.getDisplayMode().getHeight() == height) && 
			(Display.isFullscreen() == fullscreen)) {
			return;
		}
		
		try {
			DisplayMode targetDisplayMode = null;
			
			if (fullscreen) {
				DisplayMode[] modes = Display.getAvailableDisplayModes();
				int freq = 0;
				
				for (int i=0;i<modes.length;i++) {
					DisplayMode current = modes[i];
					
					if ((current.getWidth() == width) && (current.getHeight() == height)) {
						if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
							if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
								targetDisplayMode = current;
								freq = targetDisplayMode.getFrequency();
							}
						}

						// if we've found a match for bpp and frequence against the 
						// original display mode then it's probably best to go for this one
						// since it's most likely compatible with the monitor
						if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel()) &&
						    (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
							targetDisplayMode = current;
							break;
						}
					}
				}
			} else {
				targetDisplayMode = new DisplayMode(width,height);
			}
			
			if (targetDisplayMode == null) {
				System.out.println("Failed to find value mode: "+width+"x"+height+" fs="+fullscreen);
				return;
			}

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);
			
		} catch (LWJGLException e) {
			System.out.println("Unable to setup mode "+width+"x"+height+" fullscreen="+fullscreen + e);
		}
	}
	
	/** 
	 * Calculate how many milliseconds have passed 
	 * since last frame.
	 * 
	 * @return milliseconds passed since last frame 
	 */
	public int getDelta() {
	    long time = getTime();
	    int delta = (int) (time - lastFrame);
	    lastFrame = time;
 
	    return delta;
	}
 
	/**
	 * Get the accurate system time
	 * 
	 * @return The system time in milliseconds
	 */
	public long getTime() {
	    return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
 
	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			Display.setTitle("FPS: " + fps);
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}
 
	public void initGL() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 800, 0, 600, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
 
	public void renderGL() {
		// Clear The Screen And The Depth Buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 
		// R,G,B,A Set The Color To Blue One Time Only
		GL11.glColor4f(24f, 0.5f, 1.0f, 100f);

		Iterator<Entity> iterator = entityList.iterator();
		while(iterator.hasNext()){
			Entity e = iterator.next();
			if(e.shouldRender)
				e.render();
			else
				iterator.remove();
		}
//		for(Entity e:entityList){
//			if(e.shouldRender)
//				e.render();
//		}
		
		for(int i=0;i<mapNodes.length;i++){
			for(int j=0;j<mapNodes[i].length;j++){
				LocationNode tempNode = mapNodes[i][j];
				if(!tempNode.isWalkable){
						GL11.glColor4f(24f, 0.5f, 1.0f, 100f);
						GL11.glBegin(GL11.GL_QUADS);
						GL11.glVertex2f(tempNode.centerX-defaultCellWidth/2, tempNode.centerY-defaultCellWidth/2);
						GL11.glVertex2f(tempNode.centerX+defaultCellWidth/2 ,tempNode.centerY-defaultCellWidth/2);
						GL11.glVertex2f(tempNode.centerX+defaultCellWidth/2, tempNode.centerY+defaultCellWidth/2);
						GL11.glVertex2f(tempNode.centerX-defaultCellWidth/2 , tempNode.centerY+defaultCellWidth/2);
						GL11.glEnd();
				}
				
			}
		}
		GL11.glColor4f(0.1f, 0.9f, 0.2f, 100f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(startNode.centerX-defaultCellWidth/2, startNode.centerY-defaultCellWidth/2);
		GL11.glVertex2f(startNode.centerX+defaultCellWidth/2 ,startNode.centerY-defaultCellWidth/2);
		GL11.glVertex2f(startNode.centerX+defaultCellWidth/2, startNode.centerY+defaultCellWidth/2);
		GL11.glVertex2f(startNode.centerX-defaultCellWidth/2 , startNode.centerY+defaultCellWidth/2);
		GL11.glEnd();

		GL11.glColor4f(0.5f, 0.5f, 0.8f, 100f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(destinationNode.centerX-defaultCellWidth/2, destinationNode.centerY-defaultCellWidth/2);
		GL11.glVertex2f(destinationNode.centerX+defaultCellWidth/2 ,destinationNode.centerY-defaultCellWidth/2);
		GL11.glVertex2f(destinationNode.centerX+defaultCellWidth/2, destinationNode.centerY+defaultCellWidth/2);
		GL11.glVertex2f(destinationNode.centerX-defaultCellWidth/2 , destinationNode.centerY+defaultCellWidth/2);
		GL11.glEnd();

	}
	
	public void initMap() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("mapData/mapTest.txt"));
		List<LocationNode []> arrayList = new ArrayList<LocationNode []>();
	    int arrayLength=0;
	    int lineCount=0;
		try {
	        String line = br.readLine();
	        while (line != null) {
	        	char[] cArray = line.toCharArray();
	        	arrayLength = cArray.length;
	        	LocationNode [] newNodeArray = new LocationNode[arrayLength];
	        	for(int i=0;i<cArray.length;i++){
	        		LocationNode newNode = new LocationNode();
	        		newNode.setIndex(lineCount, i);
	        		if(cArray[i]=='1')
	        			newNode.isWalkable=false;
	        		newNodeArray[i]=newNode;
	        	}
	        	arrayList.add(newNodeArray);
	            line = br.readLine();
	            lineCount++;
	        }
	    } finally {
	        br.close();
	    }
		defaultCellWidth = ((float)mapWidth)/arrayLength;
	    defaultCellHeight = ((float)mapHeight) / arrayList.size();
	    
	    mapNodes = new LocationNode [arrayList.size()][arrayLength];
	    for(int i=0;i<arrayList.size();i++){
	    	LocationNode [] tempArray = arrayList.get(i);
	    	for(int j=0;j<tempArray.length;j++){
	    		LocationNode tempNode = tempArray[j];
	    		tempNode.centerX=(j+1)*defaultCellWidth-defaultCellWidth/2;
	    		tempNode.centerY=(i+1)*defaultCellWidth-defaultCellWidth/2;
	    		mapNodes[i][j]= tempNode;
	    	}
	    }
	    
	    pf = new PathFinder(mapNodes);
	}
	
	public void spawn(){
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));		
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));		
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));		
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
		monsterQueue.add(new Monster(startNode.centerX,startNode.centerY,currentRoute));
	}
}
