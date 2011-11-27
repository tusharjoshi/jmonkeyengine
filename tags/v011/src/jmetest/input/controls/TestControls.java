/*
 * Copyright (c) 2003-2006 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jmetest.input.controls;

import com.jme.image.*;
import com.jme.input.*;
import com.jme.input.controls.*;
import com.jme.input.controls.binding.*;
import com.jme.input.controls.controller.*;
import com.jme.math.*;
import com.jme.scene.shape.*;
import com.jme.scene.state.*;
import com.jme.util.*;
import com.jmex.game.*;
import com.jmex.game.state.*;

/**
 * @author Matthew D. Hicks
 */
public class TestControls {
	public static void main(String[] args) throws Exception {
		// Create StandardGame
		final StandardGame game = new StandardGame("Test Controls");
		game.start();
		
		// Create our GameState
		DebugGameState state = new DebugGameState(false);
		GameStateManager.getInstance().attachChild(state);
		state.setActive(true);
		
		// Create Box
		Box box = new Box("Test Node", new Vector3f(), 5.0f, 5.0f, 5.0f);
		state.getRootNode().attachChild(box);
		TextureState ts = game.getDisplay().getRenderer().createTextureState();
	    Texture t = TextureManager.loadTexture(TestSwingControlEditor.class.getClassLoader().getResource("jmetest/data/images/Monkey.jpg"), Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
	    t.setWrap(Texture.WM_WRAP_S_WRAP_T);
	    ts.setTexture(t);
	    box.setRenderState(ts); 
	    box.updateRenderState();
	    state.getRootNode().attachChild(box);
	    
	    // Create our Controls
	    GameControlManager manager = new GameControlManager();
	    GameControl forward = manager.addControl("Forward");
	    forward.addBinding(new KeyboardBinding(KeyInput.KEY_W));
	    GameControl backward = manager.addControl("Backward");
	    backward.addBinding(new KeyboardBinding(KeyInput.KEY_S));
	    GameControl rotateLeft = manager.addControl("Rotate Left");
	    rotateLeft.addBinding(new KeyboardBinding(KeyInput.KEY_A));
	    GameControl rotateRight = manager.addControl("Rotate Right");
	    rotateRight.addBinding(new KeyboardBinding(KeyInput.KEY_D));
	    GameControl exit = manager.addControl("Exit");
	    exit.addBinding(new KeyboardBinding(KeyInput.KEY_ESCAPE));
	    
	    // Configure controls to "make it go"
	    ThrottleController throttle = new ThrottleController(box, forward, 1.0f, backward, -1.0f, 0.05f, 0.5f, 1.0f, false, Axis.Z);
		state.getRootNode().addController(throttle);
	    RotationController rotation = new RotationController(box, rotateLeft, rotateRight, 0.2f, Axis.Y);
	    state.getRootNode().addController(rotation);
	    ActionChangeController quit = new ActionChangeController(exit, new ControlChangeListener() {
			public void changed(GameControl control, float oldValue, float newValue, float time) {
				if (newValue == 1.0f) {
					game.shutdown();
				}
			}
	    });
	    state.getRootNode().addController(quit);
	}
}