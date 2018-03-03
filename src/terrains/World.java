package terrains;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.Loader;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.Maths;

public class World {
	private int width = 3;
	private int height = 2;
	
	public Terrain [][] terrains = new Terrain[width][height];
	private Loader loader;
	private TerrainTexturePack texturePack;
	private TerrainTexture blendMap;
	private float[][] heights;
	
	public Terrain selectedTerrain;
	private float heightOfSelectedTerrain;
	
	public World(Loader loader, TerrainTexturePack texturePack, TerrainTexture blendMap) {
		
		this.loader = loader;
		this.texturePack = texturePack;
		this.blendMap = blendMap;
		this.heights = new float[(int) getSizeWidth()][(int) getSizeHeight()];
		generateHeights();
		
		generateTerrains();
	}

	private void generateHeights() {
		
		HeightsGenerator generator = new HeightsGenerator();
		for(int x = 0; x<heights.length; x++)
			for(int z = 0; z<heights[0].length; z++)
				heights[x][z]=generator.generateHeight(x, z);
		
	}

	private void generateTerrains() {
		for(int x = 0; x<width; x++)
			for(int y = 0; y<height; y++)
				terrains[x][y] = new Terrain(x, y, loader, texturePack, blendMap, heights);
		
	}
	
	public float getHeightOfTerrain(float worldX, float worldZ) {
		float terrainX = worldX;// - this.x;
		float terrainZ = worldZ;//- this.z;
		float gridSquareSize = Terrain.SIZE / ((float)heights.length - 1);
		int gridX = (int) worldX;//(int) Math.floor(terrainX / gridSquareSize);
		int gridZ = (int) worldZ;//(int) Math.floor(terrainZ / gridSquareSize);
		
		if (worldX >= heights.length+1  || worldZ >= heights[0].length+1 || worldX < 0 || worldZ < 0) {
			return 0;
		}
		
		float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
		float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
			
		// each grid square is divide by a triangle, the dividing line is x = 1 - z
		// the bottom/right half is x > 1 - z
		float answer;
		if (xCoord <= (1 - zCoord)){
			answer = Maths.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1,
							heights[gridX + 1][gridZ], 0), new Vector3f(0,
							heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));

		}else { // the bottom/right half triangle
			answer = Maths.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(1,
							heights[gridX + 1][gridZ + 1], 1), new Vector3f(0,
							heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
		}
		return answer;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Terrain getSelectedTerrain() {
		return selectedTerrain;
	}

	public Terrain selectTerrain(float x, float z) {
		
		if (x>=Terrain.SIZE*(width) || x<0 || z>=Terrain.SIZE*(height) || z<0) return null;
		
		int X =(int) (x/Terrain.SIZE);
		int Z = (int) (z/Terrain.SIZE);
		selectedTerrain = terrains[X][Z];
		
		float newX = x-X*Terrain.SIZE;
		float newZ = z-Z*Terrain.SIZE;
		System.out.println(x+"/" +width*Terrain.SIZE+" X:"+X+"   -    "+z+"/"+height*Terrain.SIZE+" Z:"+Z);
		heightOfSelectedTerrain = getHeightOfTerrain(newX, newZ);
		return selectedTerrain;
		
	}

	public float getHeightOfSelectedTerrain() {
		// TODO Auto-generated method stub
		return heightOfSelectedTerrain;
	}

	public float getHeightOfWorld(float x, float z) {
		selectTerrain(x,z);
		return heights[(int) x][(int) z];//heightOfSelectedTerrain;
	}

	public float getSizeWidth() {
		// TODO Auto-generated method stub
		return Terrain.SIZE*this.getWidth();
	}
	
	public float getSizeHeight() {
		// TODO Auto-generated method stub
		return Terrain.SIZE*this.getHeight();
	}
	
	
}
