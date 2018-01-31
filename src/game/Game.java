package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class Game {
	
	public Player player;
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
	
	
	public Terrain terrain;
	public List<Terrain> terrains = new ArrayList<Terrain>();
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
		
		this.player = new Player(playerModel, new Vector3f(100, 0, -50), 0, 180, 0, 0.6f);
		this.camera = new Camera(player);
		this.picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);
		entities.add(player);
	}
	
	public void input() {
		
	}
	
	public void update() {
		player.move(terrain);
		camera.move();			
		//mousePickerTutorial(picker, lampEntity, light);// update after camera has moved

		
		
		
	}
	
	public void end() {
		fbos.cleanUp();
		waterShader.cleanUp();
		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
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
			renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, 1, 0, -w.getHeight() + 1.0f)); // render everything above the water // the add + 1.0f to rid of lines on edges of water in soft edge tutorial
			camera.getPosition().y += distance;
			camera.invertPitch();
			
			// render refraction texture
			fbos.bindRefractionFrameBuffer();
			renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, -1, 0, w.getHeight() + 1.0f)); // render everything under the water // the add + 1.0f to rid of lines on edges of water in soft edge tutorial 
			
		}
		
		// render to screen
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		fbos.unbindCurrentFrameBuffer();
		renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, 0, 0, 0));
		waterRenderer.render(waters, camera, sun);
		//guiRenderer.render(guiTextures);
		DisplayManager.updateDisplay();
	}


	public void setupTerrainTextures() {
		backgroundTexture = new TerrainTexture(loader.loadTexture("grassy2"));
		rTexture = new TerrainTexture(loader.loadTexture("mud"));
		gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
		bTexture = new TerrainTexture(loader.loadTexture("path"));
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
		
		terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightmap"); 	// darker the spot the lower the spot
		terrains.add(terrain);
		
		
	}

	public void createEntities() {
		
		createEntitiesBatch(fernModel, 400);
		createEntitiesBatch(lowPolyTreeModel,100);
		createEntitiesBatch(treeModel,100);
		normalMapEntities.add(new Entity(barrelModel, new Vector3f(75, 10, -75), 0, 0, 0, 1.0f));
		
		createLampEntity(new Vector3f(185, -4.7f, -293),true);
		createLampEntity(new Vector3f(370, 15, -295),true);
		createLampEntity(new Vector3f(293, 7, -305),true);

		entities.add(new Entity(lampModel, new Vector3f(185, -4.7f, -293), 0, 0, 0, 1));
		entities.add(new Entity(lampModel, new Vector3f(370, 4.2f, -300), 0, 0, 0, 1));
		entities.add(new Entity(lampModel, new Vector3f(293, -6.8f, -305), 0, 0, 0, 1));
		
		
		
		
		
	}

	private void createLampEntity(Vector3f position, boolean isOnGround) {
		//Perhaps there should be some kind of bond between the lamp object and the light itself, in case the object is moved or deleted.
		Vector3f pos = new Vector3f(position);
		Vector3f lightPos = new Vector3f(pos);
		
		
		if(isOnGround)
			pos.y = terrain.getHeightOfTerrain(pos.x, pos.z);
		
		entities.add(new Entity(lampModel, pos, 0, 0, 0, 1));
		
		lightPos.y += 14.7;
		lights.add(new Light(lightPos, new Vector3f(2, 0, 0), new Vector3f(1, 0.01f, 0.002f)));
	}

	private void createEntitiesBatch(TexturedModel model, int amount) {
		for (int i = 0; i < amount; ++i) {
			
				float x = random.nextFloat() * 800 - 400;
				float z = random.nextFloat() * -600;
				float y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(model, random.nextInt(4), new Vector3f(x, y, z), 0, 
						random.nextFloat() * 360, 0, 0.9f));

			
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
		for(int i=0; i<scale;i++)
			for(int j=0;j<scale;j++) {
				WaterTile water = new WaterTile(60*(15-i), 60*(15-j), 0);
				waters.add(water);
			}
		
		
	}
}
