package terrains;

import java.util.Random;

public class HeightsGenerator {

	private static final float AMPLITUDE = 20f;
	
	private Random random = new Random();
	
	private int seed;
	
	
	public HeightsGenerator() {
		this.seed = random.nextInt(1000000000);
		
		
	}
	
	public float generateHeight(int x, int z) {
		float total = 0;
		total += getInterpolatedCurve(x,z,8f,1f);
		total += getInterpolatedCurve(x,z,4f,2f);
		total += getInterpolatedCurve(x,z,2f,4f);
		//total += getInterpolatedCurve(x,z,4f,4f);
		
		return total;
	}
	
	private float getInterpolatedCurve(float x, float z, float frequency, float amplitudeFactor) {
		float interpolatedHeight = getInterpolatedNoise(x/frequency, z/frequency) * AMPLITUDE/amplitudeFactor;
		return applyHeightLogic(interpolatedHeight);
		
	}
	
	private float applyHeightLogic(float f) {
				
		float height = f/AMPLITUDE;
		
		if(height < 0) {
			height /= 1;
		}else if(height < 0.01) {
			height /= 5;
		}else if(height < 0.2) {
			height /= 4;
		}else if(height< 0.4) {
			height /= 2;
		}
		
		return height*AMPLITUDE;
	}

	private float getNoise(int x, int z) {
		random.setSeed(x*34536+z*123245+seed);
		
		return random.nextFloat()*2f-1f;
	}
	
	private float getSmoothNoise(int x, int z) {
		
		float corners[] = new float[4];
		float sides[] = new float [4];
		
		
		corners[0] = getNoise(x+1, z+1);
		corners[1] = getNoise(x+1, z-1);
		corners[2] = getNoise(x-1, z+1);
		corners[3] = getNoise(x-1,z-1);
		
		sides[0] = getNoise(x, z+1);
		sides[1] = getNoise(x+1, z);
		sides[2] = getNoise(x-1, z);
		sides[3] = getNoise(x, z-1);
		
		float xz = getNoise(x,z);
		
		
		float cornersF=0;
		float sidesF =0;
		float centerF =0;
		
		float landmassFactor = 0;
		int count = 0;
		for(int i=0;i<corners.length;i++) {
			if(corners[i]> landmassFactor) count++;
			if(sides[i]> landmassFactor) count++;
			cornersF +=corners[i];
			sidesF += sides[i];
		}
			cornersF /=16;
			sidesF /= 8;
			centerF =  xz/4;
		
		
		
		float result = (cornersF+sidesF+centerF);
		
		/*if(result < 0) {
			Random random = new Random();
			boolean probability = random.nextFloat() > (float)(count/10) ? false : true;
			result = probability? 1: result;
		}*/
		
		
		return result;
	}
	
	private float getInterpolatedNoise(float x, float z ) {
		int intX = (int) x;
		int intZ = (int) z;
		float fracX = x - intX;
		float fracZ = z - intZ;
		
		
		float v1 = getSmoothNoise(intX, intZ);
		float v2 = getSmoothNoise(intX+1, intZ);
		float v3 = getSmoothNoise(intX, intZ+1);
		float v4 = getSmoothNoise(intX+1, intZ+1);
		
		float i1 = interpolate(v1,v2,fracX);
		float i2 = interpolate(v3,v4,fracX);
		return interpolate(i1,i2,fracZ);
	}
	
	private float interpolate(float a, float b, float blend) {
		double theta = blend * Math.PI;
		float f = (float)(1f - Math.cos(theta))*0.5f;
		return a*(1-f)+b*f;
	}
}
