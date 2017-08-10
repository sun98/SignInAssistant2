package cn.nibius.signinassistant;


/**
 * Created by Nibius at 2017/7/10 20:34.
 * Save current device's config.
 */

public class AppConfig {
    private int tapEventIndex;
    private int pixelCountX;
    private int pixelCountY;

    public int getTapEventIndex() {
        return tapEventIndex;
    }

    public void setTapEventIndex(int tapEventIndex) {
        this.tapEventIndex = tapEventIndex;
    }

    public int getPixelCountX() {
        return pixelCountX;
    }

    public void setPixelCountX(int pixelCountX) {
        this.pixelCountX = pixelCountX;
    }

    public int getPixelCountY() {
        return pixelCountY;
    }

    public void setPixelCountY(int pixelCountY) {
        this.pixelCountY = pixelCountY;
    }
}
