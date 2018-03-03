package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.FieldCamera;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import input.Input;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import terrains.World;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class Game {
	
	private boolean running=false;
	private boolean paused = false;
	
	//public Player player;
	public Random random;
	public Loader loader;
	public TerrainTexture backgroundTexture;
	public TerrainTexture rTexture;
	public TerrainTexture gTexture;
	public TerrainTexture bTexture;
	public TerrainTexturePack texturePack;
	public TerrainTexture blendMap;
	
	public Light sun;
	
	
	
	public TexturedModel playerModel;
	public ModelTexture playerTexture;
	
	public TexturedModel treeModel; 
	public TexturedModel lowPolyTreeModel;
	public TexturedModel barrelModel;
	public ModelTexture fernTextureAtlas;
	public TexturedModel fernModel;
	public TexturedModel boxModel;
	public TexturedModel lampModel;
	
	public World world;
	
	
	public List<Entity> entities = new ArrayList<Entity>();
	public List<Entity> normalMapEntities = new ArrayList<Entity>();
	public List<Light> lights = new ArrayList<Light>();
	public List<GuiTexture> guiTextures = new ArrayList<GuiTexture>();
	public List<WaterTile> waters = new ArrayList<WaterTile>();
	
	public Camera camera;
	
	public GuiRenderer guiRenderer;
	public MasterRenderer renderer;
	public MousePicker picker;
	
	public WaterFrameBuffers fbos;
	public WaterShader waterShader;
	public WaterRenderer waterRenderer;
	private int frameCount;
	private int fps;
	

	public Game() {
		
		
		
		this.loader = new Loader();
		this.random = new Random();
		this.guiRenderer = new GuiRenderer(loader);
		this.renderer = new MasterRenderer(loader);
		this.fbos = new WaterFrameBuffers();
		this.waterShader = new WaterShader();
		this.waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), fbos);
		
		init();
		
	}
	
	public void init() {
		setupTerrainTextures();
		setupModels();
		setupTerrain();
		createEntities();
		createLight();
		createGUIs();
		createWater();
		
		//this.player = new Player(playerModel, new Vector3f(100, 0, 100), 0, 180, 0, 0.000000006f);
		this.camera = new FieldCamera();
		this.picker = new MousePicker(camera, renderer.getProjectionMatrix(), world);
		
	}
	
	public void start() {
		running=true;
		loop();
	}
	
	public void input() {
		Input.update();
	}
	
	public void update() {
		//player.move(terrain);
		camera.move();			
		//System.out.println(camera.getPitch());
		//mousePickerTutorial(picker, lampEntity, light);// update after camera has moved

		
		
		
	}
	
	public void render() {
		
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		
		// Frame Buffer Object on a GUI
		//fbos.bindReflectionFrameBuffer();
		//renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0, -1, 0, 15));
		//fbos.unbindCurrentFrameBuffer();
		// render reflection texture
		
		for(WaterTile w:waters) {
			fbos.bindReflectionFrameBuffer();
			float distance = 2 * (camera.getPosition().y - w.getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			renderer.renderScene(entities, normalMapEntities, world.terrains, lights, camera, new Vector4f(0, 1, 0, -w.getHeight() + 1.0f)); // render everything above the water // the add + 1.0f to rid of lines on edges of water in soft edge tutorial
			camera.getPosition().y += distance;
			camera.invertPitch();
			
			// render refraction texture
			fbos.bindRefractionFrameBuffer();
			renderer.renderScene(entities, normalMapEntities, world.terrains, lights, camera, new Vector4f(0, -1, 0, w.getHeight() + 1.0f)); // render everything under the water // the add + 1.0f to rid of lines on edges of water in soft edge tutorial 
			
		}
		
		// render to screen
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		fbos.unbindCurrentFrameBuffer();
		renderer.renderScene(entities, normalMapEntities, world.terrains, lights, camera, new Vector4f(0, 0, 0, 0));
		waterRenderer.render(waters, camera, sun);
		//guiRenderer.render(guiTextures);
		DisplayManager.updateDisplay();
		frameCount++;
	}

	public void end() {
		fbos.cleanUp();
		waterShader.cleanUp();
		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		
	}
	
	


	public void setupTerrainTextures() {
		backgroundTexture = new TerrainTexture(loader.loadTexture("grassy2"));
		rTexture = new TerrainTexture(loader.loadTexture("grassy2"));
		gTexture = new TerrainTexture(loader.loadTexture("grassy2"));
		bTexture = new TerrainTexture(loader.loadTexture("grassy2"));
		texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		
		
		
	}


	public void setupModels() {
		/* Player */
		playerModel 	= new TexturedModel(OBJLoader.loadObjModel("person", loader), new ModelTexture(loader.loadTexture("playerTexture")));
		playerTexture 	= playerModel.getTexture();
		playerTexture.setShineDamper(100);
		playerTexture.setReflectivity(10);
		
		/* Trees */
		treeModel 			= new TexturedModel(OBJLoader.loadObjModel("tree", loader), new ModelTexture(loader.loadTexture("tree")));
		lowPolyTreeModel 	= new TexturedModel(OBJLoader.loadObjModel("lowPolyTree", loader), new ModelTexture(loader.loadTexture("lowPolyTree")));
		
		/*Normal object*/
		barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader), new ModelTexture(loader.loadTexture("barrel")));
		barrelModel.getTexture().setShineDamper(10);
		barrelModel.getTexture().setReflectivity(0.5f);
		barrelModel.getTexture().setNormalMap(loader.loadTexture("barrelNormal"));
		
		/*Atlas objects*/
		fernTextureAtlas = new ModelTexture(loader.loadTexture("fernTextureAtlases"));
		fernTextureAtlas.setNumberOfRows(2);
		fernModel = new TexturedModel(OBJLoader.loadObjModel("fern", loader), fernTextureAtlas);
		fernModel.getTexture().setHasTransparency(true);
		
		/* Box */
		boxModel = new TexturedModel(OBJLoader.loadObjModel("box", loader), new ModelTexture(loader.loadTexture("box")));

		/* Lamp */
		lampModel = new TexturedModel(OBJLoader.loadObjModel("lamp", loader), new ModelTexture(loader.loadTexture("lamp")));

		
		
	}

	public void setupTerrain() {
		
		world = new World(loader, texturePack, blendMap); 	
		
		
		
	}

	public void createEntities() {
		float scale = 0.2f;
		
		createEntitiesBatch(fernModel, 400,scale,world);
		createEntitiesBatch(lowPolyTreeModel,1000,scale,world);
		createEntitiesBatch(treeModel,100, scale,world);
		normalMapEntities.add(new Entity(barrelModel, new Vector3f(75, 10, 75), 0, 0, 0, 1.0f));
		
		createLampEntity(new Vector3f(185, 4.7f, 293),true);
		createLampEntity(new Vector3f(370, 15, 295),true);
		createLampEntity(new Vector3f(293, 7, 305),true);

		
		
		
		
		
		
	}

	private void createLampEntity(Vector3f position, boolean isOnGround) {
		//Perhaps there should be some kind of bond between the lamp object and the light itself, in case the object is moved or deleted.
		Vector3f pos = new Vector3f(position);
		Vector3f lightPos = new Vector3f(pos);
		
		
		if(isOnGround)
			pos.y = world.getHeightOfWorld(pos.x, pos.z);
		
		entities.add(new Entity(lampModel, pos, 0, 0, 0, 1));
		
		lightPos.y += 14.7;
		lights.add(new Light(lightPos, new Vector3f(2, 0, 0), new Vector3f(1, 0.01f, 0.002f)));
	}

	private void createEntitiesBatch(TexturedModel model, int amount, float scale, World world) {
		
		
		for (int i = 0; i < amount; ++i) {
			
			float x,z,y =0;
			do  {
				x = (random.nextFloat()) * world.getSizeWidth();
				z = (random.nextFloat()) * world.getSizeHeight();
				y = world.getHeightOfWorld(x,z);
			}while(y<1);	
				
				entities.add(new Entity(model, random.nextInt(4), new Vector3f(x, y, z), 0, 
						random.nextFloat() * 360, 0, scale));

			
		}	
	}

	public void createLight() {
		
		// night
		//Light sun = new Light(new Vector3f(0, 10000, -7000), new Vector3f(0.4f, 0.4f, 0.4f)); // light source // light color
		// day
		sun = new Light(new Vector3f(0, 10000, -7000), new Vector3f(1.6f, 1.6f, 1.6f));
		lights.add(sun);
		
		
		
	}

	public void createGUIs() {
		
		GuiTexture gui = new GuiTexture(loader.loadTexture("socuwan"), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
		guiTextures.add(gui);
		
		/* Small mini view window */
		//GuiTexture miniView = new GuiTexture(fbos.getReflectionTexture(), new Vector2f(-0.65f, 0.65f), new Vector2f(0.30f, 0.30f));
		//guiTextures.add(miniView);
		//GuiTexture refraction = new GuiTexture(fbos.getRefractionTexture(), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
		//GuiTexture reflection = new GuiTexture(fbos.getReflectionTexture(), new Vector2f(-0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
		//guiTextures.add(refraction);
		//guiTextures.add(reflection);
		
	}

	public void createWater() {

		int scale = 1;
		float size = WaterTile.TILE_SIZE;
		for(int i=0; i<scale;i++)
			for(int j=0;j<scale;j++) {
				WaterTile water = new WaterTile(scale*(-i/scale), scale*(-j/scale), 0f);
				waters.add(water);
			}
		
		
	}

	public void loop() {
	
	   {
	      //This value would probably be stored elsewhere.
	      final double GAME_HERTZ = 60.0;
	      //Calculate how many ns each frame should take for our target game hertz.
	      final double TIME_BETWEEN_UPDATES = 1000000000 / GAME_HERTZ;
	      //At the very most we will update the game this many times before a new render.
	      //If you're worried about visual hitches more than perfect timing, set this to 1.
	      final int MAX_UPDATES_BEFORE_RENDER = 5;
	      //We will need the last update time.
	      double lastUpdateTime = System.nanoTime();
	      //Store the last time we rendered.
	      double lastRenderTime = System.nanoTime();
	      
	      //If we are able to get as high as this FPS, don't render again.
	      final double TARGET_FPS = 60;
	      final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;
	      
	      //Simple way of finding FPS.
	      int lastSecondTime = (int) (lastUpdateTime / 1000000000);
	      
	      while (running)
	      {
	         double now = System.nanoTime();
	         int updateCount = 0;
	         
	         if (!paused)
	         {
	             //Do as many game updates as we need to, potentially playing catchup.
	            while( now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER )
	            {
	            	input();
	               update();
	               lastUpdateTime += TIME_BETWEEN_UPDATES;
	               updateCount++;
	            }
	   
	            //If for some reason an update takes forever, we don't want to do an insane number of catchups.
	            //If you were doing some sort of game that needed to keep EXACT time, you would get rid of this.
	            if ( now - lastUpdateTime > TIME_BETWEEN_UPDATES)
	            {
	               lastUpdateTime = now - TIME_BETWEEN_UPDATES;
	            }
	         
	            //Render. To do so, we need to calculate interpolation for a smooth render.
	            //float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES) );
	            render();
	            lastRenderTime = now;
	         
	            //Update the frames we got.
	            int thisSecond = (int) (lastUpdateTime / 1000000000);
	            if (thisSecond > lastSecondTime)
	            {
	               //System.out.println("NEW SECOND " + thisSecond + " " + frameCount);
	               fps = frameCount;
	               frameCount = 0;
	               lastSecondTime = thisSecond;
	            }
	         
	            //Yield until it has been at least the target time between renders. This saves the CPU from hogging.
	            while ( now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES)
	            {
	               Thread.yield();
	            
	               //This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it.
	               //You can remove this line and it will still work (better), your CPU just climbs on certain OSes.
	               //FYI on some OS's this can cause pretty bad stuttering. Scroll down and have a look at different peoples' solutions to this.
	               try {Thread.sleep(1);} catch(Exception e) {} 
	            
	               now = System.nanoTime();
	            }
	            
	            
	         }
	         if(Display.isCloseRequested() || Input.GetKey(Input.KEY_ESCAPE) || Input.GetKey(Input.KEY_Q)) running=false;
	         
	      }
	   }
	}
	
}
