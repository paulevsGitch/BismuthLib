package ru.paulevs.colorfulfabric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.opengl.GL20;

import net.minecraft.client.texture.NativeImage;

public class ShaderUtil {
	private static Texture2D lightmap;
	private static int program;
	
	private static int makeProgram(int... shaders) {
		int program = GL20.glCreateProgram();
		for (int id: shaders) {
			GL20.glAttachShader(program, id);
		}
		GL20.glLinkProgram(program);
		return program;
	}
	
	private static int makeShader(int type, String source) {
		int shader = GL20.glCreateShader(type);
		GL20.glShaderSource(shader, source);
		GL20.glCompileShader(shader);
		
		int status = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS);
        if (status != GL20.GL_TRUE) {
            throw new RuntimeException(GL20.glGetShaderInfoLog(shader));
        }
		
		return shader;
	}
	
	public static void useProgram() {
		GL20.glUseProgram(program);
	}
	
	public static void unuseProgram() {
		GL20.glUseProgram(0);
	}
	
	public static void bindLightmap() {
		lightmap.bind7();
	}
	
	private static String loadText(String path) {
		String line;
		StringBuilder result = new StringBuilder();
		try {
			InputStream stream = ColorfulFabricClient.class.getResourceAsStream(path);
			if (stream != null) {
				InputStreamReader streamReader = new InputStreamReader(stream);
				BufferedReader bufferedReader = new BufferedReader(streamReader);
				while ((line = bufferedReader.readLine()) != null) {
					result.append(line);
					result.append("\n");
				}
				bufferedReader.close();
				streamReader.close();
				stream.close();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	
	public static void init() {
		String vertexShader = loadText("/assets/colorfulfabric/shaders/vertex.txt");
		String fragmentShader = loadText("/assets/colorfulfabric/shaders/fragment.txt");
		int vertex = makeShader(GL20.GL_VERTEX_SHADER, vertexShader);
		int fragment = makeShader(GL20.GL_FRAGMENT_SHADER, fragmentShader);
		program = makeProgram(vertex, fragment);
		unuseProgram();
		InputStream stream = ShaderUtil.class.getResourceAsStream("/assets/colorfulfabric/textures/lightmap/colors.png");
		NativeImage map = null;
		try {
			map = NativeImage.read(stream);
			stream.close();
			lightmap = new Texture2D(map);
			map.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			lightmap = new Texture2D();
		}
	}
}
