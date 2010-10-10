/*
 * Copyright (c) 2009-2010 jMonkeyEngine
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
package jme3test.bullet;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.nodes.PhysicsCharacterNode;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.terrain.jbullet.TerrainPhysicsShapeFactory;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.util.ArrayList;
import java.util.List;
import jme3tools.converters.ImageToAwt;

/**
 *
 * @author normenhansen
 */
public class TestWalkingChar extends SimpleBulletApplication implements ActionListener {
    PhysicsCharacterNode character;
    Node model;
    TerrainQuad terrain;
    Node terrainPhysicsNode;
    Material matRock;
    Material matWire;
    AnimChannel animationChannel;
    AnimControl animationControl;
    boolean left = false, right = false, up = false, down = false;
    ChaseCamera chaseCam;
    Vector3f walkDirection = new Vector3f();
    Quaternion rotation = new Quaternion();

    public static void main(String[] args) {
        TestWalkingChar app = new TestWalkingChar();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setupKeys();
        createLight();
        createSky();
        createTerrain();
        createCharacter();
        setupChaseCamera();
        setupAnimationController();
    }

    private void setupKeys() {
        flyCam.setMoveSpeed(50);
        inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(this, "wireframe");
        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("CharUp", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharDown", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("CharSpace", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("CharReset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, "CharLeft");
        inputManager.addListener(this, "CharRight");
        inputManager.addListener(this, "CharUp");
        inputManager.addListener(this, "CharDown");
        inputManager.addListener(this, "CharSpace");
        inputManager.addListener(this, "CharReset");
    }

    private void createLight() {
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -0.7f, -1).normalizeLocal());
        dl.setColor(new ColorRGBA(1f, 1f, 1f, 1.0f));
        rootNode.addLight(dl);
    }

    private void createSky(){
        Sphere sphereMesh = new Sphere(32, 32, 10, false, true);
        Geometry sphere = new Geometry("Sky", sphereMesh);
        sphere.setQueueBucket(Bucket.Sky);
        Material sky = new Material(assetManager, "Common/MatDefs/Misc/Sky.j3md");
        TextureKey key = new TextureKey("Textures/Sky/Bright/BrightSky.dds", true);
        key.setGenerateMips(true);
        key.setAsCube(true);
        Texture tex = assetManager.loadTexture(key);
        sky.setTexture("m_Texture", tex);
        sky.setVector3("m_NormalScale", Vector3f.UNIT_XYZ);
        sphere.setMaterial(sky);
        sphere.setCullHint(CullHint.Never);
        rootNode.attachChild(sphere);
    }

    private void createTerrain() {
        matRock = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        matRock.setTexture("m_Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matRock.setTexture("m_Tex1", grass);
        matRock.setFloat("m_Tex1Scale", 64f);
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        matRock.setTexture("m_Tex2", dirt);
        matRock.setFloat("m_Tex2Scale", 32f);
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        matRock.setTexture("m_Tex3", rock);
        matRock.setFloat("m_Tex3Scale", 128f);
        matWire = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        matWire.setColor("m_Color", ColorRGBA.Green);

        AbstractHeightMap heightmap = null;
        try {
            heightmap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0), 0.25f);
            heightmap.load();

        } catch (Exception e) {
            e.printStackTrace();
        }

        terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        List<Camera> cameras = new ArrayList<Camera>();
        cameras.add(getCamera());
        TerrainLodControl control = new TerrainLodControl(terrain, cameras);
        terrain.addControl(control);
        terrain.setMaterial(matRock);
        terrain.setModelBound(new BoundingBox());
        terrain.updateModelBound();
        terrain.setLocalScale(new Vector3f(2, 2, 2));
        rootNode.attachChild(terrain);

        TerrainPhysicsShapeFactory factory = new TerrainPhysicsShapeFactory();
        terrainPhysicsNode = factory.createPhysicsMesh(terrain);
        rootNode.attachChild(terrainPhysicsNode);
        getPhysicsSpace().add(terrainPhysicsNode);
    }

    private void createCharacter() {
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(1.5f, 2f);
        character = new PhysicsCharacterNode(capsule, 0.01f);
        model = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        model.setLocalScale(0.5f);
        character.attachChild(model);
        character.setLocalTranslation(new Vector3f(0, 60, 0));
        rootNode.attachChild(character);
        getPhysicsSpace().add(character);
    }

    private void setupChaseCamera() {
        flyCam.setEnabled(false);
        chaseCam = new ChaseCamera(cam, character, inputManager);
    }

    private void setupAnimationController() {
        animationControl = model.getControl(AnimControl.class);
        animationChannel = animationControl.createChannel();
        animationChannel.setLoopMode(LoopMode.Cycle);
    }

    @Override
    public void simpleUpdate(float tpf) {
        rootNode.updateGeometricState();
        //Die walkDirection des Charakters
        Vector3f camDir = cam.getDirection().clone().multLocal(0.1f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.05f);
        camDir.y=0;
        camLeft.y=0;
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        if (walkDirection.length() == 0) {
            animationChannel.setAnim("stand");
        } else {
            rotation.lookAt(walkDirection, Vector3f.UNIT_Y);
            if (!animationChannel.getAnimationName().equals("Walk")) {
                animationChannel.setAnim("Walk");
            }
        }
        model.setLocalRotation(rotation);
        character.setWalkDirection(walkDirection);
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("CharLeft")) {
            if (value) {
                left = true;
            } else {
                left = false;
            }
        } else if (binding.equals("CharRight")) {
            if (value) {
                right = true;
            } else {
                right = false;
            }
        } else if (binding.equals("CharUp")) {
            if (value) {
                up = true;
            } else {
                up = false;
            }
        } else if (binding.equals("CharDown")) {
            if (value) {
                down = true;
            } else {
                down = false;
            }
        } else if (binding.equals("CharSpace")) {
            character.jump();
        }
    }
}
