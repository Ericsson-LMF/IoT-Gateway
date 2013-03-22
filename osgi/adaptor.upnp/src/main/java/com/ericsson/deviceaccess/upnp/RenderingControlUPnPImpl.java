package com.ericsson.deviceaccess.upnp;

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.spi.service.media.RenderingControlBase;
import org.osgi.service.upnp.UPnPDevice;

public class RenderingControlUPnPImpl extends RenderingControlBase {
    private UPnPDevice dev;

    public RenderingControlUPnPImpl(UPnPDevice dev) {
        this.dev = dev;
    }

    /**
     * {@inheritDoc}
     */
    //@Override
    public void executePlay(String url, String title) throws GenericDeviceException {
        try {
            String type = UPnPUtil.playMedia(dev, url, title);
        } catch (Exception e) {
            throw new GenericDeviceException(e.getMessage() + " (" + dev + ", " + url + ", " + title + ")", e);
        }
    }


    //@Override
//		public void executePlay(GenericDeviceActionContext sac) throws GenericDeviceException {
//			try {
//				PlayArgs arg = PlayArgs.wrap(sac.getArguments());
//				if(sac.isAuthorized()){
//					String type = UPnPUtil.playMedia(dev, arg.getUrl(), arg.getTitle());
//					GenericDeviceProperties result = new GenericDevicePropertiesImpl();
//					result.setStringValue("type", type);
//					sac.getResult().setValue(result);
//				}
//			} catch (Exception e) {
//				throw new GenericDeviceException(e.getMessage(), e);
//			}
//		}

    //@Override
    public void executeStop() throws GenericDeviceException {

        try {
            UPnPUtil.stopMedia(dev);
        } catch (Exception e) {
            throw new GenericDeviceException(e.getMessage() + " (" + dev + ")", e);
        }
    }

    //@Override
    public void executePause() throws GenericDeviceException {
        try {
            UPnPUtil.pauseMedia(dev);
        } catch (Exception e) {
            throw new GenericDeviceException(e.getMessage() + " (" + dev + ")", e);
        }
    }

    //@Override
    public void executeResume() throws GenericDeviceException {
        try {
            UPnPUtil.resumeMedia(dev);
        } catch (Exception e) {
            throw new GenericDeviceException(e.getMessage() + " (" + dev + ")", e);
        }
    }

    //@Override
    public void executeSetVolume(int volume) throws GenericDeviceException {
        try {
            UPnPUtil.setVolume(dev, "" + volume);
        } catch (Exception e) {
            throw new GenericDeviceException(e.getMessage() + " (" + dev + ", " + volume + ")", e);
        }
    }

    protected void refreshProperties() {
        // NOP
    }
}

