package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import renderEngine.DisplayManager;
import terrains.Terrain;

public class Player extends Entity {
	
	private static final float RUN_SPEED = 1000;
	private static final float TURN_SPEED = 180;
	private static final float GRAVITY = -100;
	private static final float JUMP_POWER = 30;
	private static final double PRECISION=0.00000000001;
	
	//private static final float TERRAIN_HEIGHT = 0;
	
	private float currentSpeed = 0;
	private float currentTurnSpeed = 0;
	private float upwardsSpeed = 0;
	
	private boolean isInAir = false;

	public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
	}

	public void move(Terrain terrain) {
		checkInputs();
		super.increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0); // turn per second (the number of seconds that have passed)

		float distance = currentSpeed * DisplayManager.getFrameTimeSeconds(); // speed per second (the number of seconds that have passed)
		float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
		upwardsSpeed += GRAVITY * DisplayManager.getFrameTimeSeconds(); // gravity per second (the number of seconds that have passed)
		
		float terrainHeight = terrain.getHeightOfTerrain(super.getPosition().x, super.getPosition().z);
		if (super.getPosition().y - terrainHeight < PRECISION) {
			upwardsSpeed = 0;
			isInAir = false;
			super.getPosition().y = terrainHeight;
		}
		
		super.increasePosition(dx, 0*upwardsSpeed * DisplayManager.getFrameTimeSeconds(), dz); // upwards speed per second  (the number of seconds that have passed)
		
	}
	
	
	private void jump() {
		if (!isInAir) {
			this.getPosition().y+=PRECISION*2;
		this.upwardsSpeed = JUMP_POWER;
		isInAir = true;
		}
	}
	
	
	private void checkInputs() {
		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			this.currentSpeed = RUN_SPEED;
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			this.currentSpeed = -RUN_SPEED;
		}else {
			this.currentSpeed = 0;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			this.currentTurnSpeed = -TURN_SPEED;
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			this.currentTurnSpeed = TURN_SPEED;
		}else {
			this.currentTurnSpeed = 0;
		}
		
		
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			jump();
		}
	}

}
