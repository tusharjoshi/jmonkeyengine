/*
 * Copyright (c) 2003, jMonkeyEngine - Mojo Monkey Coding
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this 
 * list of conditions and the following disclaimer. 
 * 
 * Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution. 
 * 
 * Neither the name of the Mojo Monkey Coding, jME, jMonkey Engine, nor the 
 * names of its contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.jme.scene;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.system.JmeException;

/**
 * <code>BezierMesh</code>
 * @author Mark Powell
 * @version $Id: BezierMesh.java,v 1.3 2004-01-11 01:58:12 mojomonkey Exp $
 */
public class BezierMesh extends TriMesh {
    private BezierPatch patch;
    
    public BezierMesh() {
        
    }
    
    public BezierMesh(BezierPatch patch) {
        this.patch = patch;
    }

    public void setPatch(BezierPatch patch) {
        this.patch = patch;
    }

    public void tessellate(BezierPatch patch, int detailLevel) {
        int u = 0, v;
        float py, px, pyold;

        Vector3f[] temp = new Vector3f[4];
        Vector3f[] last = new Vector3f[detailLevel + 1];

        temp[0] = patch.getAnchor(0, 3);
        temp[1] = patch.getAnchor(1, 3);
        temp[2] = patch.getAnchor(2, 3);
        temp[3] = patch.getAnchor(3, 3);

        for (v = 0; v <= detailLevel; v++) {
            px = ((float) v) / ((float) detailLevel);
            last[v] = calcBerstein(px, temp);
        }

        u = 1;
        Vector3f[] vertex = new Vector3f[((detailLevel * 2) + 2) * detailLevel];
        Vector2f[] texture =
            new Vector2f[((detailLevel * 2) + 2) * detailLevel];
        Vector3f[] normal = new Vector3f[vertex.length];
        int[] indices = new int[detailLevel * detailLevel * 6];

        int count = 0;
        for (u = 1; u <= detailLevel; u++) {

            py = ((float) u) / ((float) detailLevel);
            pyold = (u - 1.0f) / (detailLevel);
            temp[0] = calcBerstein(py, patch.getAnchors()[0]);
            temp[1] = calcBerstein(py, patch.getAnchors()[1]);
            temp[2] = calcBerstein(py, patch.getAnchors()[2]);
            temp[3] = calcBerstein(py, patch.getAnchors()[3]);

            for (v = 0; v <= detailLevel; v++) {
                px = ((float) v) / ((float) detailLevel);
                texture[count] = new Vector2f(pyold, px);
                vertex[count] = new Vector3f(last[v].x, last[v].y, last[v].z);
                count++;
                last[v] = calcBerstein(px, temp);
                texture[count] = new Vector2f(py, px);
                vertex[count] = new Vector3f(last[v].x, last[v].y, last[v].z);
                count++;
            }

        }
        
        int index = -1;
        for (int i = 0; i < detailLevel * detailLevel * 6; i = i + 6) {

            index++;
            if (i > 0 && i % (detailLevel * 6) == 0) {
                index += 1;
            }

            indices[i] = 2 * index;
            indices[(i + 1)] = (2 * index) + 1;
            indices[(i + 2)] = (2 * index) + 2;

            indices[(i + 3)] = (2 * index) + 3;
            indices[(i + 4)] = (2 * index) + 2;
            indices[(i + 5)] = (2 * index) + 1;
        }

        setVertices(vertex);
        setTextures(texture);
        setIndices(indices);
        //setNormals(normal);
    }

    private Vector3f calcBerstein(float u, Vector3f[] p) {
        if (p.length != 4) {
            throw new JmeException("Berstein point parameter must be length 4.");
        }
        Vector3f a = p[0].mult((float) Math.pow(u, 3));
        Vector3f b = p[1].mult(3 * (float) Math.pow(u, 2) * (1 - u));
        Vector3f c = p[2].mult(3 * u * (float) Math.pow((1 - u), 2));
        Vector3f d = p[3].mult((float) Math.pow((1 - u), 3));

        return (a.add(b)).add((c.add(d)));
    }

}
