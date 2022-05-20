package net.devtech.jerraria.render.api.instanced;

import net.devtech.jerraria.render.api.Shader;

/**
 * @param <S> The shader used for instancing
 * @param <T> The instance data type
 */
public interface InstanceDataUploader<S extends Shader<?> & InstancingShader, T> {
	/**
	 * @param shader The shader object to upload uniform data to <b>DO NOT</b> modify vertex data here!
	 * @param instanceId Say instance data is stored in an array indexed by gl_InstanceID, this instanceId represents
	 * 	the position data should be stored in said array
	 * @param data the instance data to upload
	 */
	void uploadTo(S shader, int instanceId, T data);
}
