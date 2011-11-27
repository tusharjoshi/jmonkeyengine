package com.g3d.audio;

import com.g3d.export.G3DExporter;
import com.g3d.export.G3DImporter;
import com.g3d.export.InputCapsule;
import com.g3d.export.OutputCapsule;
import java.io.IOException;

public class LowPassFilter extends Filter {

    protected float volume, highFreqVolume;

    public LowPassFilter(float volume, float highFreqVolume) {
        setVolume(volume);
        setHighFreqVolume(highFreqVolume);
    }

    public float getHighFreqVolume() {
        return highFreqVolume;
    }

    public void setHighFreqVolume(float highFreqVolume) {
        if (highFreqVolume < 0 || highFreqVolume > 1)
            throw new IllegalArgumentException("High freq volume must be between 0 and 1");

        this.highFreqVolume = highFreqVolume;
        this.updateNeeded = true;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        if (volume < 0 || volume > 1)
            throw new IllegalArgumentException("Volume must be between 0 and 1");
        
        this.volume = volume;
        this.updateNeeded = true;
    }

    public void write(G3DExporter ex) throws IOException{
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(volume, "volume", 0);
        oc.write(highFreqVolume, "hf_volume", 0);
    }

    @Override
    public void read(G3DImporter im) throws IOException{
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        volume = ic.readFloat("volume", 0);
        highFreqVolume = ic.readFloat("hf_volume", 0);
    }

}