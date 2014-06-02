/*
 * Copyright Ericsson AB 2011-2014. All Rights Reserved.
 *
 * The contents of this file are subject to the Lesser GNU Public License,
 *  (the "License"), either version 2.1 of the License, or
 * (at your option) any later version.; you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * License along with this software. If not, it can be
 * retrieved online at https://www.gnu.org/licenses/lgpl.html. Moreover
 * it could also be requested from Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
 * WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
 * EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR
 * OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY OF ANY KIND,

 * EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
 * LIBRARY IS WITH YOU. SHOULD THE LIBRARY PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
 * REDISTRIBUTE THE LIBRARY AS PERMITTED ABOVE, BE LIABLE TO YOU FOR
 * DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE LIBRARY
 * (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED
 * INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE
 * OF THE LIBRARY TO OPERATE WITH ANY OTHER SOFTWARE), EVEN IF SUCH
 * HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package com.ericsson.deviceaccess.tutorial;

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.spi.schema.ActionSchema;
import com.ericsson.deviceaccess.spi.service.homeautomation.lighting.DimmingBase;

/**
 * Adaptor specific implementation of the <i>Dimming</i> service, which has been
 * augmented with the "MyAugmentAction" action.
 */
public class AugmentedDimming extends DimmingBase {

    private static ActionSchema MY_ACTION = new ActionSchema.Builder("MyAugmentAction")
            .addArgument(p -> {
                p.setName("arg1");
                p.setType(String.class);
            })
            .addResult(p -> {
                p.setName("res1");
                p.setType(Integer.class);
            })
            .build();
    private int currentLoadLevel;

    /**
     * Create the instance.
     */
    public AugmentedDimming() {
        // Define the custom action which augments this service
        defineCustomAction(MY_ACTION, context -> {
            String arg1 = context.getArguments().getStringValue("arg1");
            context.getResult().getValue().setIntValue("res1", arg1.length());
        });
    }

    @Override
    protected void refreshProperties() {
        updateCurrentLoadLevel(Math.min((int) (currentLoadLevel + ((Math.random() * 10) - 5)), 100));
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This is the adaptor specific implementation of the
     * <i>SetLoadLevelTarget</i> action.
     * <p/>
     * It will be called by the base class when a client invokes the action.
     */
    @Override
    public void executeSetLoadLevelTarget(int loadLevelTarget) throws GenericDeviceException {
        currentLoadLevel = loadLevelTarget;
        System.out.println("Set load level target: " + loadLevelTarget);
    }

    @Override
    public void executeSetLoadLevelTargetWithRate(int loadLevelTarget, float rate) throws GenericDeviceException {
        currentLoadLevel = loadLevelTarget;
        System.out.println("Set load level target: " + loadLevelTarget + " with rate: " + rate);
    }

    @Override
    public void executeOn() throws GenericDeviceException {
        System.out.println("Execute ON");
    }

    @Override
    public void executeOff() throws GenericDeviceException {
        System.out.println("Execute OFF");
    }
}
