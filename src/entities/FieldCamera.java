package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class FieldCamera extends Camera{

	private final int RIGHT_MOUSE_BUTTON = 1;
	private final int LEFT_MOUSE_BUTTON = 0;
	
	
	
	private Vector3f position = new Vector3f(0.0f,0.0f,0.0f);
	private float pitch = 23;
	private float minHeight = 50f;
	private float maxHeight = 300f;
	
	private float deltaX = 0f;
	private float deltaZ = 0f;
	private float deltaY = 0f;
	private float acceleration = 1f;
	private float deltaTime = 0f;
	private float friction = 0.95f;
	private float threshold = 0.001f;
	
	
	
	
	public FieldCamera() {
		
	}
	
	public void move() {
		
		getInput();
		calculateCameraPosition();
		
	}
	
	/* TODO: delete this method */
	public void getInput() {
		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			deltaZ -= acceleration;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			deltaZ += acceleration;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			deltaX += acceleration;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			deltaX -= acceleration;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_NEXT)) {
			deltaY += acceleration;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_PRIOR)) {
			deltaY -= acceleration;
		}
		
		
		

	}
	
	public void invertPitch() {
		pitch = -pitch;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	
	
	private void calculateCameraPosition() {
		
		//take friction into account
		deltaX *= friction;
		deltaZ *= friction;
		deltaY *= friction;
		
		if(Math.abs(deltaX) < threshold)  deltaX =0;
		if(Math.abs(deltaZ) < threshold ) deltaZ =0;
		if(Math.abs(deltaY) < threshold ) deltaY =0;
		
		
		
		position.x = this.getPosition().x + deltaX;
		position.y = this.getPosition().y + deltaY;
		position.z = this.getPosition().z + deltaZ;
		
		if		(position.y < minHeight	) position.y = minHeight;
		else if	(position.y	> maxHeight	) position.y = maxHeight;
		
		
	}
	
	
	
	
	
	
	
}
